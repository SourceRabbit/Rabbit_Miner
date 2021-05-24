package rabbitminer.Cluster.StratumClient;

import java.net.InetAddress;
import rabbitminer.Crypto.CryptoAlgorithms.ECryptoAlgorithm;

/**
 *
 * @author Nikos Siatras
 */
public class StratumPoolSettings implements java.io.Serializable
{

    private InetAddress fIPAddress;
    private int fPort;
    private String fUsername, fPassword;
    private ECryptoAlgorithm fCryptoAlgorithm;

    public StratumPoolSettings(InetAddress poolIP, int poolPort, String username, String password, ECryptoAlgorithm algorithm)
    {
        fIPAddress = poolIP;
        fPort = poolPort;
        fUsername = username;
        fPassword = password;
        fCryptoAlgorithm = algorithm;
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

    public String getUsername()
    {
        return fUsername;
    }

    public void setUsername(String username)
    {
        fUsername = username;
    }

    public String getPassword()
    {
        return fPassword;
    }

    public void setPassword(String password)
    {
        fPassword = password;
    }

    public ECryptoAlgorithm getCryptoAlgorithm()
    {
        return fCryptoAlgorithm;
    }

    public void setCryptoAlgorithm(ECryptoAlgorithm algorithm)
    {
        fCryptoAlgorithm = algorithm;
    }

}
