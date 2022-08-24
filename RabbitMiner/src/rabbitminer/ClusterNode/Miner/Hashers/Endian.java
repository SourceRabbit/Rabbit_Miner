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
