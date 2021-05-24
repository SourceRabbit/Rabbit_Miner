package rabbitminer.Stratum;

import java.text.DecimalFormat;
import java.util.HashMap;
import rabbitminer.Cluster.StratumClient.StratumToken;
import rabbitminer.Crypto.CryptoAlgorithms.CryptoAlgorithmsManager;
import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;
import rabbitminer.JSON.JSONSerializer;

/**
 *
 * @author Nikos Siatras
 */
public class StratumJob
{

    protected final ECryptoAlgorithm fCryptoAlgorithm;
    protected final StratumToken fStratumToken;
    protected int fNOnceRangeFrom = 0, fNOnceRangeTo = 0;
    private final DecimalFormat fNonceFormater = new DecimalFormat("###,###.###");

    public StratumJob(ECryptoAlgorithm algorithm, StratumToken stratumToken)
    {
        fCryptoAlgorithm = algorithm;
        fStratumToken = stratumToken;
    }

    public StratumJob(String fromJson)
    {
        final HashMap map = JSONSerializer.DeserializeObjectToHash(fromJson);

        // Πάρε τον αλγοριθμο απο το Json
        fCryptoAlgorithm = CryptoAlgorithmsManager.getCryptoAlgorithmEnumFromName((String) map.get("Algorithm"));

        // Πάρε το NOnce Range που πρέπει ο Miner να δουλεψει
        final String tmpRange = (String) map.get("NOnceRange");
        final String[] tmp = tmpRange.split("-");
        fNOnceRangeFrom = Integer.parseInt(tmp[0]);
        fNOnceRangeTo = Integer.parseInt(tmp[1]);

        // Πάρε το Stratum Token
        fStratumToken = new StratumToken((String) map.get("StratumToken"));

    }

    public boolean isRandomXJob()
    {
        return fCryptoAlgorithm == ECryptoAlgorithm.RandomX;
    }

    public boolean isSCryptJob()
    {
        return fCryptoAlgorithm == ECryptoAlgorithm.SCrypt;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Κοινοί Παράμετροι
    //////////////////////////////////////////////////////////////////////////////////////////////
    public ECryptoAlgorithm getCryptoAlgorithm()
    {
        return fCryptoAlgorithm;
    }

    public StratumToken getToken()
    {
        return fStratumToken;
    }

    /**
     * Set the NOnceRange for this Job
     *
     * @param from
     * @param to
     */
    public void setNOnceRange(int from, int to)
    {
        fNOnceRangeFrom = from;
        fNOnceRangeTo = to;
    }

    /**
     * Return the NOnceRange to scan for this job
     *
     * @return
     */
    public String getNOnceRange()
    {
        return fNonceFormater.format(fNOnceRangeFrom) + " - " + fNonceFormater.format(fNOnceRangeTo);
    }

    public int getNOnceRangeFrom()
    {
        return fNOnceRangeFrom;
    }

    public int getNOnceRangeTo()
    {
        return fNOnceRangeTo;
    }

    /**
     * Serialize this Job to JSON
     *
     * @return
     */
    public String toJSON()
    {
        HashMap map = new HashMap();
        map.put("Algorithm", fCryptoAlgorithm.toString());
        map.put("NOnceRange", String.valueOf(fNOnceRangeFrom) + "-" + String.valueOf(fNOnceRangeTo));
        map.put("StratumToken", fStratumToken.getRawData());
        return JSONSerializer.SerializeObject(map);
    }
}
