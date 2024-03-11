package rabbitminer.ClusterNode.Miner.POW;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import rabbitminer.Cluster.ClusterCommunicationCommons;
import rabbitminer.Cluster.StratumClient.StratumToken;
import rabbitminer.ClusterNode.ClusterNode;
import rabbitminer.ClusterNode.Miner.Hashers.*;
import rabbitminer.ClusterNode.Miner.Hashers.Scrypt.*;
import rabbitminer.Core.Computer;
import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;
import rabbitminer.Stratum.StratumJob;
import rabbitminer.Stratum.StratumJob_SCrypt;

/**
 *
 * @author Nikos Siatras
 */
public class POW_SCrypt extends POW
{

    private String fPreviusSolvedJobID = "";

    private byte[] fBlockBytesWithoutNonce;

    // ExtraNonce2
    private String fExtraNonce2 = "00000000";
    private final SCryptHasher[] fSCryptHashers;

    public POW_SCrypt() throws GeneralSecurityException
    {
        super(ECryptoAlgorithm.SCrypt);

        // Δημιούργησε ενα SCryptHasher για κάθε CPU Core
        final int CPUCoresCount = Computer.getComputerCPUCoresCount();
        fSCryptHashers = new SCryptHasher[CPUCoresCount];
        for (int i = 0; i < CPUCoresCount; i++)
        {
            fSCryptHashers[i] = new SCryptHasher();
        }
    }

    @Override
    public void AssignJob(StratumJob job)
    {
        super.AssignJob(job);

        // Κάνουμε Cast το StratumJob που έχει έρθει σε StratumJob_SCrypt
        StratumJob_SCrypt scryptJob = (StratumJob_SCrypt) job;

        Double difficulty = scryptJob.getDifficulty();
        String extranonce1 = scryptJob.getExtranonce1();

        // Calculate Target and get Target Bytes
        fTarget = GetTargetFromDifficulty(difficulty);
        fTargetBytes = BinAscii.unhexlify(fTarget);

        final StratumToken token = job.getToken();
        //final String jobID = (String) token.getParams().get(0);
        final String prevHash = (String) token.getParams().get(1);
        final String coinb1 = (String) token.getParams().get(2);
        final String coinb2 = (String) token.getParams().get(3);
        final ArrayList<String> merkleBranch = (ArrayList<String>) token.getParams().get(4);
        final String version = (String) token.getParams().get(5);
        final String nBits = (String) token.getParams().get(6);
        final String nTime = (String) token.getParams().get(7);
        //boolean cleanJobs = (boolean) token.getParams().get(8);

        // GENERATE A RANDOM EXTRANONCE_2  
        fExtraNonce2 = "00000000";

        // Build block header
        final String coinBase = BuildCoinBase(coinb1, extranonce1, fExtraNonce2, coinb2);
        final String blockHeader = BuildBlockHeader(version, prevHash, merkleBranch, nTime, nBits, coinBase);
        fBlockBytesWithoutNonce = BinAscii.unhexlify(blockHeader);

        // Κάνε Assign σε κάθε ScryptHasher to fBlockBytesWithoutNonce
        for (SCryptHasher s : fSCryptHashers)
        {
            s.AssignBlockBytesWithoutNonce(fBlockBytesWithoutNonce);
        }

        fNOnceFrom = job.getNOnceRangeFrom();
        fNOnceTo = job.getNOnceRangeTo();
    }

    @Override
    public void DoWork(final int nOnce, final int threadNo)
    {
        int i;

        try
        {
            // Πάρε τον ScryptHasher του Thread απο το fSCryptHashers
            // κάνε Hash με το nOnce που έχει έρθει σαν παράμετρος και
            // μετά κάνε το Compare με το target
            final byte[] hash = fSCryptHashers[threadNo].hash(nOnce);

            for (i = hash.length - 1; i >= 0; i--)
            {
                if ((hash[i] & 0xff) > (fTargetBytes[i] & 0xff))
                {
                    return;
                }

                if ((hash[i] & 0xff) < (fTargetBytes[i] & 0xff))
                {
                    // Meets the target !!!!
                    // INFORM SERVER THAT JOB IS SOLVED
                    JobSolved(nOnce, fExtraNonce2);
                    System.err.println("Scrypt Job Solved Nonce:" + nOnce);
                    return;
                }
            }
        }
        catch (Exception ex)
        {
            System.err.println("POW_SCrypt.DoWork Error:" + ex.getMessage());
        }
    }

    /**
     * JOB SOLVED!!!!
     *
     * @param nonce
     * @param extranonce2
     */
    public void JobSolved(final int nonce, final String extranonce2)
    {
        // Πές στον Miner να ειδοποιείσει τα threads να σταματήσουν!
        ClusterNode.ACTIVE_INSTANCE.getMyMiner().TellThreadsToKillLoops();

        final String jobID = (String) fMyJob.getToken().getParams().get(0);
        final String nTime = (String) fMyJob.getToken().getParams().get(7);

        // Το if (!fPreviusSolvedJobID.equals(jobID)) το έχω
        // βάλει για ασφάλεια. Για να μην γίνεται Submit το Job 2 ή και περισσότερες φορές
        if (!fPreviusSolvedJobID.equals(jobID))
        {
            fPreviusSolvedJobID = jobID;

            String reply = "JOB_SOLVED" + ClusterCommunicationCommons.fMessageSplitter;
            reply += jobID + ClusterCommunicationCommons.fMessageSplitter;
            reply += extranonce2 + ClusterCommunicationCommons.fMessageSplitter;
            reply += nTime + ClusterCommunicationCommons.fMessageSplitter;
            reply += DecToHex.decToHex(nonce);


            try
            {
                ClusterNode.ACTIVE_INSTANCE.getClusterClient().SendData(reply);
            }
            catch (Exception ex)
            {

            }

            // Set Job to Null!
            ClusterNode.ACTIVE_INSTANCE.setStatus("Job Solved!");
            ClusterNode.ACTIVE_INSTANCE.ClusterSentNewJobToThisNode(null);
        }
    }

    /**
     * Build the coin base !
     *
     * @param coinb1
     * @param extranonce1
     * @param extranonce2
     * @param coinb2
     * @return
     */
    private String BuildCoinBase(String coinb1, String extranonce1, String extranonce2, String coinb2)
    {
        final String coinbaseString = coinb1 + extranonce1 + extranonce2 + coinb2;
        return Sha256.sha256(Sha256.sha256(coinbaseString));
    }

    /**
     * Build the block header !
     *
     * @param version
     * @param prevHash
     * @param merkleBranch
     * @param nTime
     * @param nBits
     * @param coinBase
     * @return
     */
    private String BuildBlockHeader(String version, String prevHash, ArrayList<String> merkleBranch, String nTime, String nBits, String coinBase)
    {
        // Build Merkle Root
        String merkleRoot = coinBase;
        for (String s : merkleBranch)
        {
            merkleRoot = Sha256.sha256(Sha256.sha256(merkleRoot + s));
        }

        String result = "";
        result += Endian.EndianReverse(version);
        result += Endian.reverse8(prevHash);
        result += merkleRoot;
        result += Endian.EndianReverse(nTime);
        result += Endian.EndianReverse(nBits);
        return result;
    }

    /**
     * Επιστρέφει το Target απο το Difficulty
     *
     * @param diff
     * @return
     */
    private String GetTargetFromDifficulty(double diff)
    {
        diff = diff / 65536.0;

        long m;
        int k;
        byte[] target = new byte[8 * 4];
        for (k = 6; k > 0 && diff > 1.0; k--)
        {
            diff /= 4294967296.0;
        }
        m = (long) (4294901760.0 / diff);
        if (m == 0 && k == 6)
        {
            Arrays.fill(target, (byte) 0xff);
        }
        else
        {
            Arrays.fill(target, (byte) 0);
            for (int i = 0; i < 8; i++)
            {
                target[k * 4 + i] = (byte) ((m >> (i * 8)) & 0xff);
            }
        }
        StringBuilder sb = new StringBuilder(80);
        for (int i = 0; i < target.length; i++)
        {
            sb.append(Integer.toString((target[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}
