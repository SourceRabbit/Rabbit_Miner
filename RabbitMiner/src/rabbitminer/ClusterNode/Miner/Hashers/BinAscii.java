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
