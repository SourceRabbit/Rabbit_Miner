package rabbitminer.Cluster.StratumClient;

import Extasys.DataFrame;
import Extasys.ManualResetEvent;
import Extasys.Network.TCP.Client.Connectors.TCPConnector;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import rabbitminer.Cluster.RabbitCluster;
import rabbitminer.Cluster.StratumClient.StratumParsers.*;

/**
 *
 * @author Nikos Siatras
 */
public class StratumClient extends Extasys.Network.TCP.Client.ExtasysTCPClient
{

    private final RabbitCluster fMyCluster;
    private final StratumPoolSettings fStratumPoolSettings;
    private StratumParser fMyStratumParser;
    private String fStatus = "";
    private boolean fTCPConnectionIsEstablished = false;

    private Thread fKeepClientConnectedToPoolThread = null;
    private ManualResetEvent fWaitToEstablishTCPConnection = new ManualResetEvent(false);

    public StratumClient(RabbitCluster myCluster, StratumPoolSettings poolSettings)
    {
        super("", "", 4, 6);
        super.AddConnector("PoolConnector", poolSettings.getIPAddress(), poolSettings.getPort(), 10240, "\n");

        fStratumPoolSettings = poolSettings;
        fMyCluster = myCluster;

        switch (poolSettings.getCryptoAlgorithm())
        {
            case SCrypt:
                fMyStratumParser = new Parser_SCrypt(this);
                break;

            case RandomX:
                fMyStratumParser = new Parser_RandomX(this);
                break;
        }
    }

    @Override
    public void OnDataReceive(TCPConnector connector, DataFrame data)
    {
        try
        {
            System.out.println("Stratum Client incoming data:");
            System.out.println(new String(data.getBytes()));
            System.out.println("\n");
            StratumToken token = new StratumToken(new String(data.getBytes()));
            try
            {
                fMyStratumParser.Parse(token);
            }
            catch (ConnectorDisconnectedException | ConnectorCannotSendPacketException ex)
            {
                this.Stop();
            }
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public void OnConnect(TCPConnector connector)
    {
        // Με το που το mining pool δεχθεί το Connection
        // ζήτα, ΣΕ ΝΕΟ THREAD, απο τον fMyStratumParser
        // να τρέξει το method ClientConnectedToPool
        Thread th = new Thread(() ->
        {
            try
            {
                fMyStratumParser.ClientEstablishedTCPConnectionWithThePool();
                fStatus = "Connected";
                fTCPConnectionIsEstablished = true;
                fWaitToEstablishTCPConnection.Set();
            }
            catch (ConnectorDisconnectedException | ConnectorCannotSendPacketException ex)
            {
                if (fMyStratumParser == null)
                {
                    System.err.println("Stratum Parser is Null!!!");
                    fStatus = "Disconnected";
                    fTCPConnectionIsEstablished = false;

                    fWaitToEstablishTCPConnection.Set();
                }
            }
        });
        th.start();

    }

    @Override
    public void OnDisconnect(TCPConnector connector)
    {
        fTCPConnectionIsEstablished = false;
        fStatus = "Disconnected";

        fMyCluster.setCurrentStratumJob(null, true);

        try
        {
            fMyStratumParser.ClientDisconnectedFromPool();
        }
        catch (Exception ex)
        {

        }
    }

    @Override
    public void Start() throws Exception
    {
        if (fKeepClientConnectedToPoolThread == null)
        {
            // Το παρακάτω Thread ελέγχει αν ο Stratum Client 
            // είναι συνδεδεμένος με το Mining Pool.
            // Στην περίπτωση που δέν ειναι συνδεδεμένος κάνει σύνδεση
            fKeepClientConnectedToPoolThread = new Thread(() ->
            {
                while (true)
                {
                    if (!isClientConnectedAndAuthorizedByThePool())
                    {
                        fStatus = "Connecting to pool...";

                        // Κάνε Stop τον Client
                        try
                        {
                            ForceStop();
                        }
                        catch (Exception ex)
                        {
                            System.err.println(ex.getMessage());
                        }

                        // Κάνε remove τον παλιό TCPConnector και δημιούργησε έναν νέο
                        RemoveConnector("PoolConnector");
                        AddConnector("PoolConnector", fStratumPoolSettings.getIPAddress(), fStratumPoolSettings.getPort(), 10240, "\n");

                        // Κάνε Start τον client
                        try
                        {
                            fWaitToEstablishTCPConnection.Reset();
                            StratumClient.super.Start();
                            fWaitToEstablishTCPConnection.WaitOne(6000);
                        }
                        catch (Exception ex)
                        {
                            fTCPConnectionIsEstablished = false;
                            fStatus = "Error while trying to connect to pool";
                        }
                    }

                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception ex)
                    {

                    }

                }
            });
            fKeepClientConnectedToPoolThread.start();
        }
    }

    /**
     * Για να είναι ένας Stratum Client σωστά συνδεδεμένος με το Pool θα πρέπει
     * να έχει ενεργό TCP Connection και ο Miner να είναι Subscribed και
     * Authorized
     *
     * @return
     */
    public boolean isClientConnectedAndAuthorizedByThePool()
    {
        return fTCPConnectionIsEstablished && fMyStratumParser.isMinerSubscribed() && fMyStratumParser.isMinerAuthorized();
    }

    public RabbitCluster getMyCluster()
    {
        return fMyCluster;
    }

    public StratumParser getMyParser()
    {
        return fMyStratumParser;
    }

    public String getStatus()
    {
        return fStatus;
    }

    public void setStatus(String status)
    {
        fStatus = status;
    }

}
