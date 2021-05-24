package rabbitminer.ClusterNode.Miner;

import java.security.GeneralSecurityException;
import rabbitminer.ClusterNode.ClusterNode;
import rabbitminer.ClusterNode.Miner.POW.POW;
import rabbitminer.ClusterNode.Miner.POW.*;
import rabbitminer.Core.Computer;
import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;
import rabbitminer.Stratum.StratumJob;

/**
 *
 * @author Nikos Siatras
 */
public class Miner
{

    private final ClusterNode fMyClusterNode;

    // Miner Threads
    private final int fActiveMinersCount;
    private final int fMaxMinersCount;
    protected final MinerThread[] fMinerThreads;

    public POW fMyPOW;

    // Locks
    private final Object fSetPowLock = new Object(); // Αυτό το Lock είναι για το SetPow

    public Miner(ClusterNode myClusterNode)
    {
        fMyClusterNode = myClusterNode;

        fActiveMinersCount = Computer.getComputerCPUCoresCount();
        fMaxMinersCount = fActiveMinersCount;

        try
        {
            fMyPOW = new POW(ECryptoAlgorithm.SCrypt);
            fMyPOW.AssignJob(null);
        }
        catch (Exception ex)
        {

        }

        // Ξεκινάμε Threads 
        fMinerThreads = new MinerThread[fMaxMinersCount];
        boolean min = true;
        for (int i = 0; i < fMaxMinersCount; i++)
        {
            fMinerThreads[i] = new MinerThread(i, this);
            fMinerThreads[i].setName("Thread #" + i);
            fMinerThreads[i].setPriority(Thread.MIN_PRIORITY);
            fMinerThreads[i].start();
            min = !min;
        }
    }

    public void setPOW(final POW pow)
    {
        synchronized (fSetPowLock)
        {
            fMyPOW = pow;

            // Κάνουμε Set το POW σε κάθε MineThread!
            for (MinerThread th : fMinerThreads)
            {
                th.SetMyPOW(fMyPOW);
            }
        }
    }

    public void SetJob(final StratumJob job) throws GeneralSecurityException
    {
        synchronized (fSetPowLock)
        {
            // Έλεγξε αν πρέπει να αλλαχθεί το POW
            boolean changePow = fMyPOW == null || fMyPOW.fMyJob == null || (fMyPOW.fMyJob.getCryptoAlgorithm() != job.getCryptoAlgorithm());

            // Αν το Job είναι Null κάνει σετ το
            if (job == null)
            {
                fMyPOW = new POW(ECryptoAlgorithm.SCrypt);
            }

            // Ανάλογα με το CryptoAlgorithm σέταρε στον Miner το
            // σωστό POW
            switch (job.getCryptoAlgorithm())
            {
                case SCrypt:
                    if (changePow)
                    {
                        setPOW(new POW_SCrypt());
                    }
                    fMyPOW.AssignJob(job);
                    break;

                case RandomX:
                    if (changePow)
                    {
                        setPOW(new POW_RandomX());
                    }
                    fMyPOW.AssignJob(job);
                    break;
            }

            final int nOnceFrom = job.getNOnceRangeFrom();
            final int nOnceTo = job.getNOnceRangeTo();
            final int totalNOnce = nOnceTo - nOnceFrom;

            // Το κάθε thread θα ψάξει σε δικό του Range
            final int nOnceRangeStep = totalNOnce / fActiveMinersCount;

            for (int i = 0; i < fActiveMinersCount; i++)
            {
                final int start = nOnceFrom + (i * nOnceRangeStep);
                int end = (start + nOnceRangeStep) - 1;

                // Για το τελευταίο Thread στη σειρά
                if (i == (fActiveMinersCount - 1) && end < nOnceTo)
                {
                    end = nOnceTo;
                }

                // Set the NOnce range that the Miner Thread has to work on
                fMinerThreads[i].SetRange(start, end);
            }

            fMyClusterNode.setStatus("Mining...");
        }

    }

    public ClusterNode getMyClusterNode()
    {
        return fMyClusterNode;
    }

    public void TellThreadsToKillLoops()
    {
        for (MinerThread th : fMinerThreads)
        {
            th.KillLoop();
        }
    }

    public void WaitForAllMinerThreadsToFinishWork()
    {
        ClusterNode.ACTIVE_INSTANCE.setStatus("Waiting for miner threads to finish their work");
        for (MinerThread th : fMinerThreads)
        {
            th.WaitToFinishScanning();
        }
        //System.out.println("All Miner Threads finished their work!");
    }

    public double getHashesPerSecond()
    {
        double result = 0;
        try
        {
            for (MinerThread th : fMinerThreads)
            {
                result += th.getHashesPerSecond();
            }
        }
        catch (Exception ex)
        {

        }
        return result;
    }

}
