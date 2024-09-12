/**
 * MIT License
 *
 * Copyright (c) 2022 Nikolaos Siatras
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rabbitminer.Cluster.Server;

import Extasys.DataFrame;
import Extasys.DataConvertion.Base64Converter;
import Extasys.ManualResetEvent;
import Extasys.Network.TCP.Server.Listener.Exceptions.ClientIsDisconnectedException;
import Extasys.Network.TCP.Server.Listener.Exceptions.OutgoingPacketFailedException;
import Extasys.Network.TCP.Server.Listener.TCPClientConnection;
import Extasys.Network.TCP.Server.Listener.TCPListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import rabbitminer.Cluster.ClusterCommunicationCommons;
import rabbitminer.Cluster.RabbitCluster;
import rabbitminer.Cluster.StratumClient.StratumParsers.Parser_RandomX;
import rabbitminer.Core.Computer;
import rabbitminer.JSON.JSONSerializer;
import rabbitminer.Stratum.StratumJob;
import rabbitminer.Stratum.StratumJob_RandomX;
import rabbitminer.UI.frmClusterControl;

/**
 *
 * @author Nikos Siatras
 */
public class ClusterServer extends Extasys.Network.TCP.Server.ExtasysTCPServer
{

    private final RabbitCluster fMyCluster;
    private final Object fClientsConnectOrDisconnectLock = new Object();
    private final HashMap<String, TCPClientConnection> fConnectedClients;
    private final Thread fPingConnectedClientsThread;

    public ClusterServer(RabbitCluster myCluster, ClusterServerSettings clusterServerSettings)
    {
        super("", "", Computer.getComputerCPUCoresCount(), Computer.getComputerCPUCoresCount() * 2);
        TCPListener listener = super.AddListener("", clusterServerSettings.getIPAddress(), clusterServerSettings.getPort(), 5000, 10240, 30000, 150, ClusterCommunicationCommons.fETX);

        listener.setAutoApplyMessageSplitterState(true);
        listener.setConnectionDataConverter(new Base64Converter());

        fMyCluster = myCluster;
        fConnectedClients = new HashMap<>();

        // Κάνουμε initialize ενα Thread για να κάνει Ping τους Clients
        // κάθε 15 δευτερόλεπτα.
        fPingConnectedClientsThread = new Thread(() ->
        {
            final ManualResetEvent evt = new ManualResetEvent(false);
            while (true)
            {
                PingClients();
                try
                {
                    evt.WaitOne(15000);
                }
                catch (Exception ex)
                {

                }
                evt.Reset();
            }
        });
        fPingConnectedClientsThread.start();
    }

    @Override
    public void OnDataReceive(TCPClientConnection sender, DataFrame data)
    {
        try
        {
            String incomingStr = new String(data.getBytes(), "UTF-8");
            String[] parts = incomingStr.split(ClusterCommunicationCommons.fMessageSplitter);

            switch (parts[0])
            {
                case "LOGIN":
                    // O client ζητάει να κάνει login
                    // Το μήνυμα ειναι έτσι: LOGIN + fMessageSplitter + Password + fMessageSplitter + Αριθμός Thread
                    if (parts[1].equals(fMyCluster.getClusterServerSettings().getPassword()))
                    {
                        int threadsCount = Integer.parseInt(parts[2]);

                        // Αν το Password είναι σωστό τότε δημιουργούμε NodeTCPConnectionVariables
                        // για τον client-node που ζητάει να συνδεθεί
                        NodeTCPConnectionVariables var = new NodeTCPConnectionVariables();
                        var.ClientLoggedInSucessfully();
                        var.setThreadsCount(threadsCount);
                        sender.setTag(var);

                        // Ειδοποιούμε το Node οτι συνδέθηκε
                        sender.SendData("AUTHORIZED" + ClusterCommunicationCommons.fMessageSplitter);
                    }
                    else
                    {
                        sender.SendData("WRONG_PASSWORD" + ClusterCommunicationCommons.fMessageSplitter);
                    }
                    break;

                case "GET_JOB":
                    if (CheckIfClientIsAuthorized(sender))
                    {
                        // Ζήτα απο το Cluster να φτιάξει ένα
                        // job για να το δώσουμε στο Node
                        StratumJob job = fMyCluster.GiveNodeAJobToDo(sender);

                        if (job != null)
                        {
                            sender.SendData("JOB" + ClusterCommunicationCommons.fMessageSplitter + job.toJSON());
                        }
                        else
                        {
                            // Δεν υπάρχει Job....
                            sender.SendData("NO_JOB" + ClusterCommunicationCommons.fMessageSplitter);
                        }
                    }
                    break;

                case "JOB_SOLVED":
                    if (CheckIfClientIsAuthorized(sender))
                    {
                        final String jobID = parts[1];
                        final String extranonce2 = parts[2];
                        final String nTime = parts[3];
                        final String nonce = parts[4];

                        String submitJobStr = "{\"params\": [\"#WORKER_NAME#\", \"#JOB_ID#\", \"#EXTRANONCE_2#\", \"#NTIME#\", \"#NONCE#\"], \"id\": #STRATUM_MESSAGE_ID#, \"method\": \"mining.submit\"}";
                        submitJobStr = submitJobStr.replace("#WORKER_NAME#", fMyCluster.getStratumPoolSettings().getUsername());
                        submitJobStr = submitJobStr.replace("#JOB_ID#", jobID);
                        submitJobStr = submitJobStr.replace("#EXTRANONCE_2#", extranonce2);
                        submitJobStr = submitJobStr.replace("#NTIME#", nTime);
                        submitJobStr = submitJobStr.replace("#NONCE#", nonce);
                        submitJobStr = submitJobStr.replace("#STRATUM_MESSAGE_ID#", String.valueOf(fMyCluster.getStratumClient().getMyParser().getStratumID()));

                        // Καποιο Node ολοκλήρωσε ενα job με επιτυχία!
                        // Στειλε το αποτέλεσμα στον Stratum Server
                        fMyCluster.setCurrentStratumJob(null, false);

                        // SEND DATA
                        fMyCluster.getStratumClient().SendData(submitJobStr + "\n");
                        fMyCluster.fJobsSubmitted += 1;
                    }
                    break;

                case "JOB_SOLVED_RANDOMX":
                    if (CheckIfClientIsAuthorized(sender))
                    {
                        StratumJob_RandomX randomXJobSolved = new StratumJob_RandomX(parts[1]);

                        LinkedHashMap solvedJobParams = new LinkedHashMap();
                        solvedJobParams.put("id", Parser_RandomX.fPoolLoginID);
                        solvedJobParams.put("job_id", randomXJobSolved.getJobID());
                        solvedJobParams.put("nonce", randomXJobSolved.getSolution_NonceHexlifyByteArray());
                        solvedJobParams.put("result", randomXJobSolved.getSolution_HashHexlifyByteArray());

                        LinkedHashMap messageToPool = new LinkedHashMap();
                        messageToPool.put("id", 1);
                        messageToPool.put("jsonrpc", "2.0");
                        messageToPool.put("method", "submit");
                        messageToPool.put("params", solvedJobParams);

                        String dataToSend = JSONSerializer.SerializeObject(messageToPool);

                        System.err.println(dataToSend);

                        fMyCluster.getStratumClient().SendData(dataToSend + "\n");
                        System.out.println("SOLVED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                        // Καποιο Node ολοκλήρωσε ενα job με επιτυχία!
                        // Στειλε το αποτέλεσμα στον Stratum Server
                        fMyCluster.setCurrentStratumJob(null, false);
                    }

                    break;

                case "PONG":
                    if (CheckIfClientIsAuthorized(sender))
                    {

                    }
                    break;
            }
        }
        catch (Exception ex)
        {

        }
    }

    /**
     * Ελέγχει αν ο client έχει κάνει connect σωστά. Με σωστό password κτλ...
     *
     * @param client
     * @return
     */
    private boolean CheckIfClientIsAuthorized(TCPClientConnection client)
    {
        if (client.getTag() != null)
        {
            NodeTCPConnectionVariables var = (NodeTCPConnectionVariables) client.getTag();
            return var.isClientAuthorized();
        }

        return false;
    }

    @Override
    public void OnClientConnect(TCPClientConnection client)
    {
        synchronized (fClientsConnectOrDisconnectLock)
        {
            fConnectedClients.put(client.getIPAddress(), client);

            // Πές στη φόρμα frmClusterControl οτι ένα Node συνδέθηκε
            frmClusterControl.ACTIVE_INSTANCE.NodeConnected(client);
        }
    }

    @Override
    public void OnClientDisconnect(TCPClientConnection client)
    {
        synchronized (fClientsConnectOrDisconnectLock)
        {
            if (fConnectedClients.containsKey(client.getIPAddress()))
            {
                fConnectedClients.remove(client.getIPAddress());

                // Πές στη φόρμα frmClusterControl οτι ένα Node συνδέθηκε
                frmClusterControl.ACTIVE_INSTANCE.NodeDisconnected(client);
            }
        }
    }

    public void InformClientsToCleanJobs()
    {
        synchronized (fClientsConnectOrDisconnectLock)
        {
            fConnectedClients.values().forEach(client ->
            {
                try
                {
                    client.SendData("CLEAN_JOBS" + ClusterCommunicationCommons.fMessageSplitter);
                }
                catch (ClientIsDisconnectedException | OutgoingPacketFailedException ex)
                {
                }
            });
        }

    }

    /**
     * Στείλε Ping σε όλους τους Clients
     */
    private void PingClients()
    {
        synchronized (fClientsConnectOrDisconnectLock)
        {
            fConnectedClients.values().forEach(con ->
            {
                try
                {
                    con.SendData("PING" + ClusterCommunicationCommons.fMessageSplitter);
                }
                catch (ClientIsDisconnectedException | OutgoingPacketFailedException ex)
                {

                }
            });
        }
    }

    public HashMap<String, TCPClientConnection> getConnectedClients()
    {
        return fConnectedClients;
    }

    public void ClearRangesFromClients()
    {
        synchronized (fClientsConnectOrDisconnectLock)
        {
            fConnectedClients.values().forEach(con ->
            {
                try
                {
                    ((NodeTCPConnectionVariables) con.getTag()).setWorkRange(0, 0);
                }
                catch (Exception ex)
                {

                }
            });
        }
    }

}
