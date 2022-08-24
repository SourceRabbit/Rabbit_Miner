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
public class StratumJob_SCrypt extends StratumJob
{

    private final String fJobID;
    private final double fDifficulty;
    private final String fExtranonce1;
    private final long fExtranonce2Size;

    public StratumJob_SCrypt(String jobID, double difficulty, String extranonce1, long extranonce2Size, StratumToken stratumToken)
    {
        super(ECryptoAlgorithm.SCrypt, stratumToken);
        fJobID = jobID;
        fDifficulty = difficulty;
        fExtranonce1 = extranonce1;
        fExtranonce2Size = extranonce2Size;
    }

    public StratumJob_SCrypt(String fromJson)
    {
        super(fromJson);

        HashMap map = JSONSerializer.DeserializeObjectToHash(fromJson);

        fJobID = (String) map.get("JobID");
        fDifficulty = (double) map.get("Difficulty");
        fExtranonce1 = (String) map.get("Extranonce1");
        fExtranonce2Size = (long) map.get("Extranonce2Size");
    }

    public String getJobID()
    {
        return fJobID;
    }

    public double getDifficulty()
    {
        return fDifficulty;
    }

    public String getExtranonce1()
    {
        return fExtranonce1;
    }

    public long getExtraNonce2Size()
    {
        return fExtranonce2Size;
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
        // που χρειάζονται για το SCrypt Job
        map.put("JobID", fJobID);
        map.put("Difficulty", fDifficulty);
        map.put("Extranonce1", fExtranonce1);
        map.put("Extranonce2Size", fExtranonce2Size);

        return JSONSerializer.SerializeObject(map);
    }

}
