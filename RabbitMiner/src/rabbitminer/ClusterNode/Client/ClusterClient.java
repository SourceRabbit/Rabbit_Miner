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
package rabbitminer.ClusterNode.Client;

import Extasys.DataFrame;
import Extasys.DataConvertion.Base64Converter;
import Extasys.ManualResetEvent;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import java.net.InetAddress;
import rabbitminer.Cluster.ClusterCommunicationCommons;
import rabbitminer.ClusterNode.ClusterNode;
import rabbitminer.Core.Computer;
import rabbitminer.Stratum.StratumJob;
import rabbitminer.Stratum.StratumJob_RandomX;
import rabbitminer.Stratum.StratumJob_SCrypt;

/**
 *
 * @author Nikos Siatras
 */
public class ClusterClient extends Extasys.Network.TCP.Client.ExtasysTCPClient
{

    public static ClusterClient ACTIVE_INSTANCE;
    private final ClusterNode fMyClusterNode;

    // Auto Reconnect Thread
    private Thread fAutoReconnectThread = null;
    private boolean fKeepAutoReconnect = true;

    public ClusterClient(ClusterNode myClusterNode)
    {
        super("", "", 2, 2);

        fMyClusterNode = myClusterNode;
        ClusterClient.ACTIVE_INSTANCE = this;
    }

    @Override
    public void Start() throws Exception
    {
        StartAutoReconnect();
    }

    @Override
    public void Stop()
    {
        fKeepAutoReconnect = false;
        super.Stop();
    }

    @Override
    public void OnDataReceive(TCPConnector connector, DataFrame data)
    {
        try
        {
            final String str = new String(data.getBytes(), "UTF-8");
            final String[] parts = str.split(ClusterCommunicationCommons.fMessageSplitter);

            //System.out.println(str);
            switch (parts[0])
            {
                case "AUTHORIZED":
                    // Πες στο Node να αρχίσει να ζητάει Jobs!
                    fMyClusterNode.setStatus("Connection established! Waiting for a job...");
                    fMyClusterNode.StartAskingForJobs();
                    break;

                case "NOT_AUTHORIZED":
                    fMyClusterNode.setStatus("Miner is not authorized. Check connection settings and password!");
                    break;

                case "WRONG_PASSWORD":
                    fMyClusterNode.setStatus("Wrong password!");
                    break;

                case "JOB":
                    // Ό Cluster Server έστειλε Job !
                    StratumJob job = new StratumJob(parts[1]);

                    // Κάνε Cast το Job ανάλογα με τον Αλγόριθμο
                    switch (job.getCryptoAlgorithm())
                    {
                        case SCrypt:
                            job = new StratumJob_SCrypt(parts[1]);
                            break;

                        case RandomX:
                            job = new StratumJob_RandomX(parts[1]);
                            break;

                        default:
                            System.err.println("ClusterClient.OnDataReceive: Δεν υπάρχει σωστό type για το Job!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            break;
                    }

                    // Πές στο Node οτι ο Cluster Server έστειλε νέο Job
                    fMyClusterNode.setStatus("Working");
                    fMyClusterNode.ClusterSentNewJobToThisNode(job);
                    break;

                case "NO_JOB":
                    // Server has no new job at the moment
                    fMyClusterNode.setStatus("Server has no job at the moment");
                    fMyClusterNode.ClusterSentNewJobToThisNode(null);
                    break;

                case "CLEAN_JOBS":
                    // Server asks to clean jobs!
                    // Kill the current job and wait for new one
                    fMyClusterNode.CleanJob();
                    break;

                case "PING":
                    // Server sents PING.
                    // Answer with PONG.
                    connector.SendData("PONG" + ClusterCommunicationCommons.fMessageSplitter);
                    break;

            }
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public void OnConnect(TCPConnector connector)
    {
        fMyClusterNode.setStatus("Requesting access to Cluster...");
        String loginStr = "LOGIN" + ClusterCommunicationCommons.fMessageSplitter;
        loginStr += fMyClusterNode.getClusterPassword() + ClusterCommunicationCommons.fMessageSplitter;
        loginStr += String.valueOf(Computer.getComputerCPUCoresCount());
        try
        {
            SendData(loginStr);
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public void OnDisconnect(TCPConnector connector)
    {
        fMyClusterNode.setStatus("Disconnected from Cluster Server");
    }

    private void StartAutoReconnect()
    {
        fAutoReconnectThread = new Thread(() ->
        {
            while (fKeepAutoReconnect)
            {

                boolean isConnected = false;
                if (!ClusterClient.ACTIVE_INSTANCE.getConnectors().isEmpty())
                {
                    if (ClusterClient.ACTIVE_INSTANCE.getConnectors().get(0).isConnected())
                    {
                        isConnected = true;
                    }
                }

                // Check if connector is disconnected
                if (!isConnected)
                {
                    // Get old connector properties
                    InetAddress serverIP = ACTIVE_INSTANCE.fMyClusterNode.getClusterIP();
                    int serverPort = ACTIVE_INSTANCE.fMyClusterNode.getClusterPort();
                    int readBufferSize = 10240;

                    // Remove the old TCPConnector
                    ClusterClient.ACTIVE_INSTANCE.RemoveConnector("");

                    // Add new connector
                    TCPConnector conn = ClusterClient.ACTIVE_INSTANCE.AddConnector("", serverIP, serverPort, readBufferSize, ClusterCommunicationCommons.fETX);
                    conn.setAutoApplyMessageSplitterState(true);
                    conn.setConnectionDataConverter(new Base64Converter());

                    fMyClusterNode.setStatus("Trying to connect to server...");
                    try
                    {
                        ((TCPConnector) ClusterClient.ACTIVE_INSTANCE.getConnectors().get(0)).Start();
                    }
                    catch (Exception ex)
                    {
                        fMyClusterNode.setStatus(ex.getMessage());
                    }
                }

                // Wait 2 sec
                ManualResetEvent evt = new ManualResetEvent(false);
                try
                {
                    evt.WaitOne(2000);
                }
                catch (Exception ex)
                {

                }
            }
        });

        fAutoReconnectThread.start();

    }

}
