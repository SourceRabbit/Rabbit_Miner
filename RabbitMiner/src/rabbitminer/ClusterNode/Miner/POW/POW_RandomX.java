package rabbitminer.ClusterNode.Miner.POW;

import java.util.HashMap;
import rabbitminer.Cluster.ClusterCommunicationCommons;
import rabbitminer.ClusterNode.ClusterNode;
import rabbitminer.ClusterNode.Miner.Hashers.BinAscii;
import rabbitminer.Core.Computer;
import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;
import rabbitminer.Stratum.StratumJob;
import rabbitminer.Stratum.StratumJob_RandomX;
import tk.netindev.drill.hasher.Hasher;

/**
 *
 * @author Nikos Siatras
 */
public class POW_RandomX extends POW
{

    private int fCurrentJobTarget;
    private byte[] fCurrentJobBlob;

    private final HashMap<Integer, byte[]> fJobBlobsPerThread = new HashMap<>();

    public POW_RandomX()
    {
        super(ECryptoAlgorithm.RandomX);

        // Δημιουργούμε ένα Blob Array για κάθε Computer Core
        // !!! Τα threads του Miner μπορεί να είναι λιγότερα απο τα fJobBlobsPerThread !!!
        int computerCores = Computer.getComputerCPUCoresCount();
        for (int i = 0; i < computerCores; i++)
        {
            fJobBlobsPerThread.put(i, null);
        }
    }

    @Override
    public void AssignJob(StratumJob job)
    {
        super.AssignJob(job);

        // Κάνουμε Cast το StratumJob που έχει έρθει σε StratumJob_RandomX
        StratumJob_RandomX randomXJob = (StratumJob_RandomX) job;

        fMyJob = job;

        fCurrentJobBlob = BinAscii.unhexlify(randomXJob.getBlob());
        fCurrentJobTarget = randomXJob.getTarget();

        // Βάζουμε στο fJobBlobsPerThread την τιμή του
        // νεου Blob για να "παίζει" το καθε Thread με τη δικιά του
        int computerCores = Computer.getComputerCPUCoresCount();
        for (int i = 0; i < computerCores; i++)
        {
            final byte[] blob = new byte[fCurrentJobBlob.length];
            System.arraycopy(fCurrentJobBlob, 0, blob, 0, fCurrentJobBlob.length);
            fJobBlobsPerThread.put(i, blob);
        }
        fNOnceFrom = job.getNOnceRangeFrom();
        fNOnceTo = job.getNOnceRangeTo();
    }

    @Override
    public void DoWork(final int nOnce, final int threadNo)
    {
        if (MeetsTarget(nOnce, threadNo))
        {
            // INFORM SERVER THAT JOB IS SOLVED
            JobSolved();
        }
    }

    public boolean MeetsTarget(final int nonce, final int threadNo)
    {
        final byte[] hash = new byte[32];
        final byte[] blob = fJobBlobsPerThread.get(threadNo);

        blob[39] = (byte) nonce;
        blob[40] = (byte) (nonce >> 8);
        blob[41] = (byte) (nonce >> 16);
        blob[42] = (byte) (nonce >> 24);

        try
        {
            Hasher.slowHash(blob, hash);
        }
        catch (Exception ex)
        {

        }

        final int difficulty = (((hash[31] << 24) | ((hash[30] & 255) << 16)) | ((hash[29] & 255) << 8)) | (hash[28] & 255);

        if (difficulty >= 0 && difficulty <= fCurrentJobTarget)
        {
            final byte[] array = new byte[4];
            array[0] = (byte) nonce;
            array[1] = (byte) (nonce >> 8);
            array[2] = (byte) (nonce >> 16);
            array[3] = (byte) (nonce >> 24);

            // Πές στον Server οτι το λύσαμε
            StratumJob_RandomX randomXJob = (StratumJob_RandomX) fMyJob;

            randomXJob.setSolution_NonceHexlifyByteArray(BinAscii.hexlify(array).toLowerCase());
            randomXJob.setSolution_HashHexlifyByteArray(BinAscii.hexlify(hash).toLowerCase());

            String reply = "JOB_SOLVED_RANDOMX" + ClusterCommunicationCommons.fMessageSplitter;
            reply += randomXJob.toJSON() + ClusterCommunicationCommons.fMessageSplitter;
            reply += ClusterCommunicationCommons.fETX;

            try
            {
                ClusterNode.ACTIVE_INSTANCE.getClusterClient().SendData(reply);
            }
            catch (Exception ex)
            {

            }

            System.err.println("---------------------------------- JOB SOLVED ----------------------------------------");

            return true;
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void JobSolved()
    {
        // Set Job to Null!
        ClusterNode.ACTIVE_INSTANCE.ClusterSentNewJobToThisNode(null);
    }
}
