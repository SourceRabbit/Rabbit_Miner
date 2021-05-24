package rabbitminer.ClusterNode.Miner.Hashers;

/**
 *
 * @author Nikos Siatras
 */
public class Endian
{

    public static String EndianReverse(String str)
    {
        String result = "";
        String reversed = ReverseString(str);

        for (int i = 0; i < reversed.length(); i += 2)
        {
            result += ReverseString(reversed.substring(i, i + 2));
        }

        return result;
    }

    public static String reverse8(String str)
    {
        String result = "";

        for (int i = 0; i < str.length(); i += 8)
        {
            String part = str.substring(i, i + 8);
            result += EndianReverse(part);
        }

        return result;
    }

    private static String ReverseString(String str)
    {
        return new StringBuffer(str).reverse().toString();
    }
}
