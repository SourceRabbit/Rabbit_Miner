/**
 * MIT License
 *
 * Copyright (c) 2022 Nikolaos Siatras
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rabbitminer.Stratum;

import java.util.HashMap;
import rabbitminer.Cluster.StratumClient.StratumToken;

import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;
import rabbitminer.JSON.JSONSerializer;

/**
 *
 * @author nsiat
 */
public class StratumJob_RandomX extends StratumJob
{

    private final String fJobID;
    private final String fBlob;
    private final int fTarget;
    private final String fSeedHash;
    private final long fHeight;

    private String fSolution_Nonce_HexlifyString = "";
    private String fSolution_Hash_HexlifyString = "";

    public StratumJob_RandomX(String jobID, String blob, int target, String seedHash, long height, StratumToken stratumToken)
    {
        super(ECryptoAlgorithm.RandomX, stratumToken);
        fJobID = jobID;
        fBlob = blob;
        fTarget = target;
        fSeedHash = seedHash;
        fHeight = height;
    }

    public StratumJob_RandomX(String fromJson)
    {
        super(fromJson);

        HashMap map = JSONSerializer.DeserializeObjectToHash(fromJson);

        fJobID = (String) map.get("JobID");
        fBlob = (String) map.get("Blob");
        fTarget = Integer.parseInt(map.get("Target").toString());
        fSeedHash = (String) map.get("SeedHash");
        fHeight = (Long) map.get("Height");

        // Αυτά γεμίζουν μονο όταν λυθεί το Job!
        fSolution_Nonce_HexlifyString = (String) map.get("Solution_NonceByteArray");
        fSolution_Hash_HexlifyString = (String) map.get("Solution_HashByteArray");

    }

    public String getJobID()
    {
        return fJobID;
    }

    public String getBlob()
    {
        return fBlob;
    }

    public int getTarget()
    {
        return fTarget;
    }

    public String getSeedHash()
    {
        return fSeedHash;
    }

    public Long getHeight()
    {
        return fHeight;
    }

    public double getDifficulty()
    {
        // Το Difficulty υπολογίζεται απο την παρακάτω διαίρεση
        return (Integer.MAX_VALUE / fTarget) * 2;
    }

    public void setSolution_NonceHexlifyByteArray(String hexlifyArray)
    {
        fSolution_Nonce_HexlifyString = hexlifyArray;
    }

    public String getSolution_NonceHexlifyByteArray()
    {
        return fSolution_Nonce_HexlifyString;
    }

    public void setSolution_HashHexlifyByteArray(String hexlifyByteArray)
    {
        fSolution_Hash_HexlifyString = hexlifyByteArray;
    }

    public String getSolution_HashHexlifyByteArray()
    {
        return fSolution_Hash_HexlifyString;
    }

    @Override
    public String toJSON()
    {
        HashMap map = new HashMap();

        // Πάντα στο Serialize βάζουμε αυτά
        map.put("Algorithm", fCryptoAlgorithm.toString());
        map.put("NOnceRange", String.valueOf(fNOnceRangeFrom) + "-" + String.valueOf(fNOnceRangeTo));
        map.put("StratumToken", fStratumToken.getRawData());

        // Τα απο εδώ και κάτω είναι οι παράμετροι
        // που χρειάζονται για το RandomX Job
        map.put("JobID", fJobID);
        map.put("Blob", fBlob);
        map.put("Target", fTarget);
        map.put("SeedHash", fSeedHash);
        map.put("Height", fHeight);

        map.put("Solution_NonceByteArray", fSolution_Nonce_HexlifyString);
        map.put("Solution_HashByteArray", fSolution_Hash_HexlifyString);

        return JSONSerializer.SerializeObject(map);
    }

}
