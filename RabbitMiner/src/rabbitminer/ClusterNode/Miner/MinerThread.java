package rabbitminer.ClusterNode.Miner;

import Extasys.ManualResetEvent;
import rabbitminer.ClusterNode.ClusterNode;
import rabbitminer.ClusterNode.Miner.POW.POW;

/**
 *
 * @author Nikos Siatras
 */
public class MinerThread extends Thread
{

    private final Miner fMyMiner;
    private POW fMyPOW;
    private final int fThreadNumber;
    private double fHashesPerSecond = 0.0;
    private int fNOnce = 0;
    private int fNOnceFrom = 0, fNOnceTo = 0;
    private boolean fKeepRunning = true;
    private boolean fIsWorking = false;
    private boolean fKillLoop = false;

    private final ManualResetEvent fWaitToSetRangesEvt = new ManualResetEvent(false);
    private final ManualResetEvent fWaitToFinishScanning = new ManualResetEvent(false);

    public MinerThread(int threadNo, Miner myMiner)
    {
        fThreadNumber = threadNo;
        fMyMiner = myMiner;
    }

    public void SetMyPOW(POW myPow)
    {
        fMyPOW = myPow;
    }

    @Override
    public void run()
    {
        fWaitToSetRangesEvt.Reset();
        fWaitToFinishScanning.Reset();
        fKillLoop = false;

        while (fKeepRunning)
        {
            try
            {
                // Step 1 - Περίμενε να έρθει Range
                // System.out.println("Thread " + getName() + " is waiting for range.");
                fKillLoop = false;
                fWaitToSetRangesEvt.WaitOne();
                fWaitToFinishScanning.Reset();
                //System.out.println("Thread " + getName() + " is working");

                // Step 2 - Σκάναρε όλο το Range σου
                fIsWorking = true;
                long timeStart = System.currentTimeMillis();
                while (fNOnce < fNOnceTo && !fKillLoop)
                {
                    //if (fMyPOW.fHasJob);
                    //{
                    fMyPOW.DoWork(fNOnce, fThreadNumber);
                    //}

                    fNOnce += 1;
                }
                long timeFinish = System.currentTimeMillis();
                fHashesPerSecond = ((double) (fNOnce - fNOnceFrom) / (double) (timeFinish - timeStart)) * 1000;

                fIsWorking = false;
                fWaitToFinishScanning.Set();

                // Step 3 - Το Thread έψαξε όλο το Range του !!!!!!
                // Αν το fNOnce ξεπερνάει το NOnceRangeTo του Job 
                // τότε πές στον Miner οτι η δουλειά τελείωσε
                if (fNOnce > fMyMiner.fMyPOW.fMyJob.getNOnceRangeTo())
                {
                    ClusterNode.ACTIVE_INSTANCE.JobFinished(fMyMiner.fMyPOW.fMyJob);
                    // System.out.println("Job finished! NOnce: " + fNOnce + " Job NOnce To : " + fMyMiner.fMyPOW.fMyJob.getNOnceRangeTo());
                }

                fWaitToSetRangesEvt.Reset();
            }
            catch (Exception ex)
            {

            }
        }
    }

    /**
     * Νέο Range δόθηκε στο Thread !
     *
     * @param nOnceFrom αρχή
     * @param nOnceTo τέλος
     */
    public void SetRange(final int nOnceFrom, final int nOnceTo)
    {
        // Προσθέτουμε στο fNOnceTo + 1
        // γιατί μέσα στο run του thread ψάχνουμε για fNOnce < fNOnceTo
        fNOnceFrom = nOnceFrom;
        fNOnceTo = nOnceTo + 1;
        fNOnce = nOnceFrom;
        fWaitToSetRangesEvt.Set();
    }

    public void WaitToFinishScanning()
    {
        while (fIsWorking)
        {
            fMyMiner.getMyClusterNode().setStatus("Waiting for threads to finish...");
            fWaitToFinishScanning.WaitOne();
        }
    }

    /**
     * Αυτό το Method καλείτε όταν ένα Job λυθεί με επιτυχία για να σταματήσει
     * την While του σκανάρισμα
     */
    public void KillLoop()
    {
        fKillLoop = true;
    }

    public void Stop()
    {
        fKeepRunning = false;
    }

    public double getHashesPerSecond()
    {
        return fHashesPerSecond;
    }

}
