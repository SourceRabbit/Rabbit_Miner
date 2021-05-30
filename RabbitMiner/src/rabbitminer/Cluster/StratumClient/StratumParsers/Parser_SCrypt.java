package rabbitminer.Cluster.StratumClient.StratumParsers;

import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import rabbitminer.Cluster.StratumClient.StratumClient;
import rabbitminer.Cluster.StratumClient.StratumToken;
import rabbitminer.Core.Settings.SettingsManager;
import rabbitminer.JSON.JSONSerializer;
import rabbitminer.Stratum.StratumJob_SCrypt;

/**
 *
 * @author Nikos Siatras
 */
public class Parser_SCrypt extends StratumParser
{

   
    
    
    public Parser_SCrypt(StratumClient myClient)
    {
        super(myClient);
    }

    /**
     *
     * @param token
     * @throws ConnectorDisconnectedException
     * @throws ConnectorCannotSendPacketException
     */
    @Override
    public void Parse(StratumToken token) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        try
        {
            // Αν  έρθει Token και ο Miner δέν έχει κανει Subscribe
            // ή ο Miner δέν έχει κάνει Authorize τότε
            // ανάλογα με το Token ID κάνε subscribe ή Authorize τον Miner
            if (token.getID() != null && (!fMinerIsSubscribed || !fMinerIsAuthorized) && token.getID() < 3)
            {
                switch (token.getID().intValue())
                {
                    case 1:
                        // Miner Subscribed
                        if (token.getResult() != null && token.getResult() instanceof ArrayList)
                        {
                            ArrayList result = (ArrayList) token.getResult();

                            fExtranonce1 = (String) result.get(1);
                            fExtranonce2Size = (long) result.get(2);

                            fMinerIsSubscribed = true;
                            fMinerConnectionWaitEvent.Set();
                            fMyClient.setStatus("Miner subscribed to pool");
                        }
                        break;

                    case 2:
                        // Miner Authorized
                        if (token.getResult() == Boolean.TRUE)
                        {
                            fMinerIsAuthorized = true;
                            fMinerConnectionWaitEvent.Set();
                        }
                        fMyClient.setStatus("Miner Authorized by the pool!");
                        Step3_ExtranonceSubscribe();
                        break;
                }

                return;
            }

            // FIX STRATUM ID
            try
            {
                if (token.getID() != null && token.getID() > fStratumID)
                {
                    fStratumID = token.getID() + 1;
                }
            }
            catch (Exception ex)
            {

            }

            if (token.getMethod() != null)
            {
                switch (token.getMethod())
                {
                    case "mining.set_difficulty":
                        // Stratum server changed difficulty
                        Double newDiff = Double.parseDouble(token.getParams().get(0).toString());
                        if (newDiff != fDifficulty)
                        {
                            fDifficulty = Double.parseDouble(token.getParams().get(0).toString());
                            //fMyClient.getMyCluster().setCurrentStratumJob(null, true);
                        }
                        break;

                    case "mining.set_extranonce":
                        fExtranonce1 = (String) token.getParams().get(1);
                        fExtranonce2Size = (long) token.getParams().get(2);
                        break;

                    case "mining.notify":
                        // Νέο Job ήρθε απο το Pool !
                        if (token.getParams() != null && token.getParams().size() > 0)
                        {
                            // Ελέγουμε αν μαζί με το Job, απο το Pool,
                            // εχει έρθει και εντολή για Clean Jobs
                            boolean cleanJobs = (Boolean) token.getParams().get(8);

                            // Δημιουργούμε ένα νέο StratumJob_SCrypt
                            // Και το κάνουμε set σαν το τρέχων Job
                            String jobID = (String) token.getParams().get(0);
                            StratumJob_SCrypt job = new StratumJob_SCrypt(jobID, fDifficulty, fExtranonce1, fExtranonce2Size, token);
                            fMyClient.getMyCluster().setCurrentStratumJob(job, cleanJobs);
                        }
                        break;

                    default:
                        System.err.println("Unknown Incoming data:");
                        System.err.println(token.getRawData());
                        break;
                }
            }
            else
            {
                System.err.println("No token method");
            }
        }
        catch (Exception ex)
        {
            System.err.println("Parser_Scrypt Error: " + ex.getMessage());
            System.err.println(token.getRawData());
        }
    }

    @Override
    public void ClientEstablishedTCPConnectionWithThePool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fConnectOrDisconnectLock)
        {
            fStratumID = 0;
            fMinerIsSubscribed = false;
            fMinerIsAuthorized = false;

            Step1_AskPoolForMinerSubscription();

            if (!fMinerIsSubscribed)
            {
                fMyClient.setStatus("Unable to subscribe miner...");
                try
                {
                    fMyClient.ForceStop();
                }
                catch (Exception ex)
                {

                }

                return;
            }

            Step2_AskPoolToAuthorizeMiner();

            if (!fMinerIsAuthorized)
            {
                fMyClient.setStatus("Client failed to authorize with the mining pool. Check username and password!");
                fMyClient.Stop();
            }
        }
    }

    @Override
    public void ClientDisconnectedFromPool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fConnectOrDisconnectLock)
        {
            fStratumID = 0;
            fMinerIsSubscribed = false;
            fMinerIsAuthorized = false;
        }
    }

    /**
     * Ask pool to subscribe our miner!
     *
     * @throws ConnectorDisconnectedException
     * @throws ConnectorCannotSendPacketException
     */
    private void Step1_AskPoolForMinerSubscription() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        fMinerConnectionWaitEvent.Reset();
        System.out.println("Step1 AskPoolForMinerSubscription...");

        LinkedHashMap map = new LinkedHashMap();
        map.put("id", getStratumID());
        map.put("method", "mining.subscribe");

        String[] params;
        switch (fMyClient.getMyCluster().getStratumPoolSettings().getCryptoAlgorithm())
        {
            case SCrypt:
                params = new String[2];
                params[0] = "RabbitMiner/" + SettingsManager.getAppVersion();
                params[1] = "Stratum/1.0.0";
                break;

            default:
                params = new String[2];
                params[0] = "RabbitMiner/" + SettingsManager.getAppVersion();
                params[1] = "Stratum/1.0.0";
                break;
        }

        map.put("params", params);

        fMyClient.SendData(JSONSerializer.SerializeObject(map) + "\n");

        fMinerConnectionWaitEvent.WaitOne(3000);
    }

    private void Step2_AskPoolToAuthorizeMiner() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        fMinerConnectionWaitEvent.Reset();
        System.out.println("Step2 AskPoolToAuthorizeMiner...");

        LinkedHashMap map = new LinkedHashMap();
        map.put("id", getStratumID());
        map.put("method", "mining.authorize");
        String[] params =
        {
            fMyClient.getMyCluster().getStratumPoolSettings().getUsername(), fMyClient.getMyCluster().getStratumPoolSettings().getPassword()
        };
        map.put("params", params);
        fMyClient.SendData(JSONSerializer.SerializeObject(map) + "\n");

        fMinerConnectionWaitEvent.WaitOne(3000);
    }

    private void Step3_ExtranonceSubscribe() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        System.out.println("Step3 ExtranonceSubscribe...");

        LinkedHashMap map = new LinkedHashMap();
        map.put("id", getStratumID());
        map.put("method", "mining.extranonce.subscribe");
        String[] params =
        {

        };
        map.put("params", params);
        fMyClient.SendData(JSONSerializer.SerializeObject(map) + "\n");
    }

}
