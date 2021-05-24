package rabbitminer.ClusterNode.Miner.Hashers;

import java.security.MessageDigest;

/**
 *
 * @author Nikos Siatras
 */
public class Sha256
{

    public static byte[] sha256(byte[] value)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(value);
            return md.digest();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public static String sha256(String value)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(BinAscii.unhexlify(value));
            return BinAscii.hexlify(md.digest());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
}
