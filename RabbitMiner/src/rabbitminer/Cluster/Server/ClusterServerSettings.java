package rabbitminer.Cluster.Server;

import java.net.InetAddress;
import java.util.HashMap;

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
