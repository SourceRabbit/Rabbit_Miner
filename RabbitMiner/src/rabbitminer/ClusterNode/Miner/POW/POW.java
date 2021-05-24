package rabbitminer.ClusterNode.Miner.POW;

import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;
import rabbitminer.Stratum.StratumJob;

/**
 *
 * @author Nikos Siatras
 */
public class POW
{

    public StratumJob fMyJob;

    // NOnce Range to Scan
    protected int fNOnceFrom = 0, fNOnceTo = 0;

    // Target
    protected String fTarget;
    protected byte[] fTargetBytes;
    public boolean fHasJob = false;

    private final ECryptoAlgorithm fCryptoAlgorith;

    public POW(ECryptoAlgorithm algorith)
    {
        fCryptoAlgorith = algorith;
    }

    /**
     *
     * @param job
     */
    public void AssignJob(final StratumJob job)
    {
        fHasJob = (job != null);
        fMyJob = job;
    }

    public void DoWork(final int nOnce, final int threadNo)
    {

    }

    public ECryptoAlgorithm getMyCryptoAlgorith()
    {
        return fCryptoAlgorith;
    }

    public boolean HasJob()
    {
        return fHasJob;
    }

}
