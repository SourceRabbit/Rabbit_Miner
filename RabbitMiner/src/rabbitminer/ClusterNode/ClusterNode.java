package rabbitminer.ClusterNode;

import Extasys.ManualResetEvent;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.net.InetAddress;
import rabbitminer.Cluster.ClusterCommunicationCommons;
import rabbitminer.ClusterNode.Client.ClusterClient;
import rabbitminer.ClusterNode.Miner.Miner;
import rabbitminer.Stratum.StratumJob;

/**
 *
 * @author Nikos Siatras
 */
public class ClusterNode
{
    // Περίληψη:
    // Το CLusterNode έχει έναν ClusterClient που μιλάμει με τον Cluster Server,
    // έναν Miner που κάνει Mine
    // και το Job που έχει στείλει ο Cluster Server

    //////////////////////////////////////////////////////////////////////////////////
    public static ClusterNode ACTIVE_INSTANCE;

    private final ClusterClient fClusterClient;
    private final Miner fMyMiner;
    private StratumJob fCurrentJob;
    private final Object fGetOrSetCurrentJobLock = new Object();

    private long fJobReceivedTime;
    public double fHashesPerMillisecond = 0;

    ///////////////////////////////////////////////////
    private InetAddress fClusterIP;
    private int fClusterPort;
    private String fClusterPassword;
    private String fStatus = "";

    /////////////////////////////////////////
    private boolean fKeepAskingForJobs = false;
    private Thread fKeepAskingForJobsThread;
    private final ManualResetEvent fAskServerForJobAndWaitForReplyEvent = new ManualResetEvent(false);

    public ClusterNode(InetAddress clusterIP, int port, String password)
    {
        fClusterIP = clusterIP;
        fClusterPort = port;
        fClusterPassword = password;

        fClusterClient = new ClusterClient(this);
        fMyMiner = new Miner(this);

        ACTIVE_INSTANCE = this;
    }

    public void StartNode() throws Exception
    {
        fClusterClient.Start();
    }

    public void StopNode()
    {
        fClusterClient.Stop();
    }

    /**
     * Ο cluster server μας έστειλε ένα νέο Job. Προσοχή το Job μπορεί να είναι
     * NULL!
     *
     * @param job
     */
    public void ClusterSentNewJobToThisNode(StratumJob job)
    {
        synchronized (fGetOrSetCurrentJobLock)
        {
            try
            {
                fCurrentJob = job;
                fJobReceivedTime = System.currentTimeMillis();

                fAskServerForJobAndWaitForReplyEvent.Set();

                if (job == null)
                {
                    fStatus = "No job...";
                }
                else
                {
                    fStatus = "Job received!";
                    // Κανε σετ στον Miner για το Job
                    fMyMiner.SetJob(job);
                }
            }
            catch (Exception ex)
            {
                fStatus = "ClusterNode.ClusterSentNewJobToThisNode Error:" + ex.getMessage();
            }
        }
    }

    /**
     * Ξεκίνα να ζητάς Jobs από τον Cluster Server
     */
    public void StartAskingForJobs()
    {
        fKeepAskingForJobs = true;

        if (fKeepAskingForJobsThread == null)
        {
            fKeepAskingForJobsThread = new Thread(() ->
            {
                while (fKeepAskingForJobs)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (Exception ex)
                    {

                    }

                    if (fCurrentJob == null)
                    {
                        AskServerForJobAndWaitForReply();
                    }
                }
            });
            fKeepAskingForJobsThread.start();
        }
    }

    private void AskServerForJobAndWaitForReply()
    {
        fStatus = "Asking cluster for a job...";
        fAskServerForJobAndWaitForReplyEvent.Reset();

        ManualResetEvent tmpWait = new ManualResetEvent(false);
        try
        {
            tmpWait.WaitOne(100);
        }
        catch (Exception ex)
        {

        }

        try
        {
            fAskServerForJobAndWaitForReplyEvent.Reset();

            fClusterClient.SendData("GET_JOB" + ClusterCommunicationCommons.fMessageSplitter + ClusterCommunicationCommons.fETX);
            try
            {
                fAskServerForJobAndWaitForReplyEvent.WaitOne(5000);
            }
            catch (Exception ex)
            {

            }
        }
        catch (ConnectorDisconnectedException | ConnectorCannotSendPacketException ex)
        {
            fAskServerForJobAndWaitForReplyEvent.Set();
        }

        // Για να το πιάνει Garbage Collector σύντομα
        tmpWait = null;
    }

    public void CleanJob()
    {
        synchronized (fGetOrSetCurrentJobLock)
        {
            fCurrentJob = null;
        }
    }

    public void JobFinished(StratumJob job)
    {
        synchronized (fGetOrSetCurrentJobLock)
        {
            // Κάποιο απο τα Thread του Miner σκάναρε όλο το ευρος
            // του Nonce που του δώθηκε.
            // Για να προχωρήσουμε πρέπει όμως να ολοκληρώσουν το σκανάρισμα όλα τα Threads
            fMyMiner.WaitForAllMinerThreadsToFinishWork();

            long msPassedSinceJobReceived = (System.currentTimeMillis() - fJobReceivedTime);
            fHashesPerMillisecond = (msPassedSinceJobReceived == 0) ? 0 : ((double) (job.getNOnceRangeTo() - job.getNOnceRangeFrom()) / (double) msPassedSinceJobReceived);
            ClusterSentNewJobToThisNode(null);
        }
    }

    /**
     * Επιστρέφει την τρέχουσα δουλειά που έχει στείλει ο Cluster Server
     *
     * @return
     */
    public StratumJob getCurrentJob()
    {
        synchronized (fGetOrSetCurrentJobLock)
        {
            return fCurrentJob;
        }
    }

    public Miner getMyMiner()
    {
        return fMyMiner;
    }

    public ClusterClient getClusterClient()
    {
        return fClusterClient;
    }

    public InetAddress getClusterIP()
    {
        return fClusterIP;
    }

    public void setClusterIP(InetAddress ip)
    {
        fClusterIP = ip;
    }

    public int getClusterPort()
    {
        return fClusterPort;
    }

    public void setClusterPort(int port)
    {
        fClusterPort = port;
    }

    public String getClusterPassword()
    {
        return fClusterPassword;
    }

    public void setClusterPassword(String pass)
    {
        fClusterPassword = pass;
    }

    public String getStatus()
    {
        return fStatus;
    }

    public void setStatus(String status)
    {
        fStatus = status;
    }

    public double getHashesPerSecond()
    {
        return fMyMiner.getHashesPerSecond();
    }

}
