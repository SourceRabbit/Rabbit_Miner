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
