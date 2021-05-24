/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rabbitminer.ClusterNode.Miner.Hashers;

/**
 *
 * @author nsiatras
 */
public class BinAscii
{

    private static final char charGlyph_[] =
    {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String hexlify(byte[] bytes)
    {
        StringBuilder hexAscii = new StringBuilder(bytes.length * 2);

        for (int i = 0; i < bytes.length; ++i)
        {
            byte b = bytes[i];
            hexAscii.append(charGlyph_[(int) (b & 0xf0) >> 4]);
            hexAscii.append(charGlyph_[(int) (b & 0x0f)]);
        }
        return hexAscii.toString();
        
        
    }

    public static byte[] unhexlify(String asciiHex)
    {
        if (asciiHex.length() % 2 != 0)
        {
            throw new RuntimeException("Input to unhexlify must have even-length");
        }

        int len = asciiHex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(asciiHex.charAt(i), 16) << 4) + Character.digit(asciiHex.charAt(i + 1), 16));
        }
        return data;
    }
}
