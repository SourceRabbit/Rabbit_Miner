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
package rabbitminer.Cluster.Server;

import java.net.InetAddress;

/**
 *
 * @author Nikos Siatras
 */
public class ClusterServerSettings implements java.io.Serializable
{

    private InetAddress fIPAddress;
    private int fPort;
    private String fPassword;

    public ClusterServerSettings(InetAddress clusterIP, int clusterPort, String password)
    {
        fIPAddress = clusterIP;
        fPort = clusterPort;
        fPassword = password;
    }

    public InetAddress getIPAddress()
    {
        return fIPAddress;
    }

    public void setIPAddress(InetAddress ip)
    {
        fIPAddress = ip;
    }

    public int getPort()
    {
        return fPort;
    }

    public void setPort(int port)
    {
        fPort = port;
    }

    public String getPassword()
    {
        return fPassword;
    }

    public void setPassword(String password)
    {
        fPassword = password;
    }

}
