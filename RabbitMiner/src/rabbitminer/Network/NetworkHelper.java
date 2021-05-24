package rabbitminer.Network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 *
 * @author Nikos Siatras
 */
public class NetworkHelper
{

    /**
     * Returns a list with all IP addresses of the current machine
     *
     * @return
     */
    public static ArrayList<InetAddress> getAllMachineIPAddresses()
    {
        ArrayList<InetAddress> result = new ArrayList<>();
        try
        {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    result.add(i);
                }
            }
        }
        catch (Exception ex)
        {
            // Do nothing
        }
        return result;
    }
}
