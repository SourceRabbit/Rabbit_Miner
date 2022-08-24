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
package rabbitminer.Cluster.StratumClient.StratumParsers;

import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import rabbitminer.Cluster.StratumClient.StratumClient;
import rabbitminer.Cluster.StratumClient.StratumToken;
import rabbitminer.ClusterNode.Miner.Hashers.BinAscii;
import rabbitminer.Core.Settings.SettingsManager;
import rabbitminer.JSON.JSONSerializer;
import rabbitminer.Stratum.StratumJob_RandomX;

/**
 *
 * @author nsiat
 */
public class Parser_RandomX extends StratumParser
{

    public static String fPoolLoginID = "";

    public Parser_RandomX(StratumClient myClient)
    {
        super(myClient);
    }

    /**
     *
     * @param token
     * @throws ConnectorDisconnectedException
     * @throws ConnectorCannotSendPacketException
     */
    @Override
    public void Parse(StratumToken token) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        try
        {
            HashMap tokenData = JSONSerializer.DeserializeObjectToHash(token.getRawData());

            // Την πρώτη φορά το Job Είναι μέσα στο Result !!!
            if (!fMinerIsAuthorized && token.getResult() != null)
            {
                HashMap resultHash = (HashMap) token.getResult();
                if (resultHash.containsKey("id"))
                {
                    // ΑΥΤΟ ΕΙΝΑΙ ΤΟ LOGIN ID ΣΤΟ POOL
                    fPoolLoginID = resultHash.get("id").toString();

                    fMinerIsAuthorized = true;
                    fMinerIsSubscribed = true;
                    fMyClient.setStatus("Miner authorized by the pool!");
                    fMinerConnectionWaitEvent.Set();
                }

                if (resultHash.containsKey("job"))
                {
                    HashMap jobParams = (HashMap) resultHash.get("job");
                    NewJobCameFromStratumPool(jobParams, token);
                }
            }

            if (!fMinerIsAuthorized)
            {
                fMyClient.setStatus("Miner is not authorized by the pool!");
                return;
            }

            // FIX STRATUM ID
            try
            {
                if (token.getID() != null && token.getID() > fStratumID)
                {
                    fStratumID = token.getID() + 1;
                }
            }
            catch (Exception ex)
            {

            }

            if (token.getMethod() != null)
            {
                switch (token.getMethod())
                {
                    case "job":
                        // Το Pool μας έστειλε νεο Job
                        HashMap params = (HashMap) tokenData.get("params");
                        NewJobCameFromStratumPool(params, token);
                        break;
                }
            }
            else
            {
                System.err.println("Parser_RandomX Error: Token has null method!");
            }

        }
        catch (Exception ex)
        {
            System.err.println("Parser_RandomX Error: " + ex.getMessage());
            System.err.println(token.getRawData());
        }
    }

    private void NewJobCameFromStratumPool(HashMap jobParams, StratumToken stratumToken)
    {
        String jobID = (String) jobParams.get("job_id");
        String blob = (String) jobParams.get("blob");

        final byte[] targetByteArray = BinAscii.unhexlify((String) jobParams.get("target"));
        int target = ((((targetByteArray[3] << 24) | ((targetByteArray[2] & 255) << 16)) | ((targetByteArray[1] & 255) << 8)) | (targetByteArray[0] & 255));

        String seedHash = (String) jobParams.get("seed_hash");
        Long height = (Long) jobParams.get("height");

        StratumJob_RandomX job = new StratumJob_RandomX(jobID, blob, target, seedHash, height, stratumToken);
        fMyClient.getMyCluster().setCurrentStratumJob(job, true);
    }

    @Override
    public void ClientEstablishedTCPConnectionWithThePool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fConnectOrDisconnectLock)
        {
            fStratumID = 0;
            fMinerIsAuthorized = false;
            fMinerIsSubscribed = false;

            Step1_AskMoneroPoolForMinerSubscription();
        }
    }

    @Override
    public void ClientDisconnectedFromPool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        synchronized (fConnectOrDisconnectLock)
        {
            fStratumID = 0;
            fMinerIsAuthorized = false;
            fMinerIsSubscribed = false;
        }
    }

    private void Step1_AskMoneroPoolForMinerSubscription() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {
        fMinerConnectionWaitEvent.Reset();

        LinkedHashMap map = new LinkedHashMap();
        map.put("id", getStratumID());
        map.put("method", "login");

        LinkedHashMap params = new LinkedHashMap();
        params.put("login", fMyClient.getMyCluster().getStratumPoolSettings().getUsername());
        params.put("pass", fMyClient.getMyCluster().getStratumPoolSettings().getPassword());
        params.put("agent", "RabbitMiner/" + SettingsManager.getAppVersion());
        map.put("params", params);

        fMyClient.SendData(JSONSerializer.SerializeObject(map) + "\n");
        fMinerConnectionWaitEvent.WaitOne(3000);
    }
}
