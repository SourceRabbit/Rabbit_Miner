package rabbitminer.Cluster.StratumClient.StratumParsers;

import Extasys.ManualResetEvent;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import rabbitminer.Cluster.StratumClient.StratumClient;
import rabbitminer.Cluster.StratumClient.StratumToken;

/**
 *
 * @author Nikos Siatras
 */
public class StratumParser
{

    protected long fStratumID = 0;
    protected final StratumClient fMyClient;

    // Stratum Connection procedure
    protected boolean fMinerIsSubscribed = false, fMinerIsAuthorized = false;
    protected final Object fConnectOrDisconnectLock = new Object();
    protected ManualResetEvent fMinerConnectionWaitEvent = new ManualResetEvent(false);

    // Stratum data
    protected String fExtranonce1;
    protected long fExtranonce2Size;
    protected double fDifficulty = 0;

    public StratumParser(StratumClient myClient)
    {
        fMyClient = myClient;
    }

    public void Parse(StratumToken token) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {

    }

    public void ClientEstablishedTCPConnectionWithThePool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {

    }

    public void ClientDisconnectedFromPool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {

    }

    public double getDifficulty()
    {
        return fDifficulty;
    }

    public String getExtranonce1()
    {
        return fExtranonce1;
    }

    public long getExtranonce2Size()
    {
        return fExtranonce2Size;
    }

    public long getStratumID()
    {
        fStratumID += 1;
        return fStratumID;
    }

    public boolean isMinerSubscribed()
    {
        return fMinerIsSubscribed;
    }

    public boolean isMinerAuthorized()
    {
        return fMinerIsAuthorized;
    }

}
