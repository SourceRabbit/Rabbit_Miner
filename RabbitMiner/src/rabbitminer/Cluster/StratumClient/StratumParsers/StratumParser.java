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

import Extasys.ManualResetEvent;
import Extasys.Network.TCP.Client.Exceptions.ConnectorCannotSendPacketException;
import Extasys.Network.TCP.Client.Exceptions.ConnectorDisconnectedException;
import rabbitminer.Cluster.StratumClient.StratumClient;
import rabbitminer.Cluster.StratumClient.StratumToken;

/**
 *
 * @author Nikos Siatras
 */
public class StratumParser
{

    protected long fStratumID = 0;
    protected final StratumClient fMyClient;

    // Stratum Connection procedure
    protected boolean fMinerIsSubscribed = false, fMinerIsAuthorized = false;
    protected final Object fConnectOrDisconnectLock = new Object();
    protected ManualResetEvent fMinerConnectionWaitEvent = new ManualResetEvent(false);

    // Stratum data
    protected String fExtranonce1;
    protected long fExtranonce2Size;
    protected double fDifficulty = 0;

    public StratumParser(StratumClient myClient)
    {
        fMyClient = myClient;
    }

    public void Parse(StratumToken token) throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {

    }

    public void ClientEstablishedTCPConnectionWithThePool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {

    }

    public void ClientDisconnectedFromPool() throws ConnectorDisconnectedException, ConnectorCannotSendPacketException
    {

    }

    public double getDifficulty()
    {
        return fDifficulty;
    }

    public String getExtranonce1()
    {
        return fExtranonce1;
    }

    public long getExtranonce2Size()
    {
        return fExtranonce2Size;
    }

    public long getStratumID()
    {
        fStratumID += 1;
        return fStratumID;
    }

    public boolean isMinerSubscribed()
    {
        return fMinerIsSubscribed;
    }

    public boolean isMinerAuthorized()
    {
        return fMinerIsAuthorized;
    }

}
