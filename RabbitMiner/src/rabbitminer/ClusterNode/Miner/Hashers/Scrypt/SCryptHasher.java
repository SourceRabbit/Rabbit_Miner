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
package rabbitminer.ClusterNode.Miner.Hashers.Scrypt;

import java.io.IOException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import static java.lang.System.arraycopy;
import static java.lang.Integer.rotateLeft;

public class SCryptHasher
{

    private final Mac fMac;
    private final byte[] fBlockBytesWithoutNonce = new byte[76];
    private final byte[] H = new byte[32];
    private final byte[] B = new byte[128 + 4];
    private final int[] X = new int[32];
    private final int[] V = new int[32 * 1024];

    private int i, j, k;

    public SCryptHasher() throws GeneralSecurityException
    {
        fMac = Mac.getInstance("HmacSHA256");
    }

    public final void AssignBlockBytesWithoutNonce(final byte[] blockBytesWithoutNonce)
    {
        // Κάνε copy το blockBytesWithoutNonce στο fBlockBytesWithoutNonce
        arraycopy(blockBytesWithoutNonce, 0, fBlockBytesWithoutNonce, 0, 76);
    }

    public byte[] hash(int nonce) throws GeneralSecurityException, IOException
    {
        arraycopy(fBlockBytesWithoutNonce, 0, B, 0, 76);

        //B[76] = (byte) (nonce >> 0);
        B[76] = (byte) (nonce);
        B[77] = (byte) (nonce >> 8);
        B[78] = (byte) (nonce >> 16);
        B[79] = (byte) (nonce >> 24);

        // Το Key για το HmacSHA256 ειναι τα πρώτα 80bytes του πίνακα Β
        fMac.init(new SecretKeySpec(B, 0, 80, "HmacSHA256"));

        B[80] = 0;
        B[81] = 0;
        B[82] = 0;

        for (i = 0; i < 4; i++)
        {
            B[83] = (byte) (i + 1);
            fMac.update(B, 0, 84);
            fMac.doFinal(H, 0);

            for (j = 0; j < 8; j++)
            {
                X[i * 8 + j] = (H[j * 4 + 0] & 0xff) | (H[j * 4 + 1] & 0xff) << 8 | (H[j * 4 + 2] & 0xff) << 16 | (H[j * 4 + 3] & 0xff) << 24;
            }
        }

        for (i = 0; i < 1024; i++)
        {
            arraycopy(X, 0, V, i * 32, 32);
            xorSalsa8(0, 16);
            xorSalsa8(16, 0);
        }

        for (i = 0; i < 1024; i++)
        {
            k = (X[16] & 1023) * 32;
            for (j = 0; j < 32; j++)
            {
                X[j] ^= V[k + j];
            }
            xorSalsa8(0, 16);
            xorSalsa8(16, 0);
        }

        for (i = 0; i < 32; i++)
        {
            //B[i * 4 + 0] = (byte) (X[i] >> 0);
            B[i * 4] = (byte) (X[i]);
            B[i * 4 + 1] = (byte) (X[i] >> 8);
            B[i * 4 + 2] = (byte) (X[i] >> 16);
            B[i * 4 + 3] = (byte) (X[i] >> 24);
        }

        B[128 + 3] = 1;
        fMac.update(B, 0, 128 + 4);
        fMac.doFinal(H, 0);

        return H;
    }

    protected final void xorSalsa8(final int di, final int xi)
    {
        int x00 = (X[di] ^= X[xi + 0]);
        int x01 = (X[di + 1] ^= X[xi + 1]);
        int x02 = (X[di + 2] ^= X[xi + 2]);
        int x03 = (X[di + 3] ^= X[xi + 3]);
        int x04 = (X[di + 4] ^= X[xi + 4]);
        int x05 = (X[di + 5] ^= X[xi + 5]);
        int x06 = (X[di + 6] ^= X[xi + 6]);
        int x07 = (X[di + 7] ^= X[xi + 7]);
        int x08 = (X[di + 8] ^= X[xi + 8]);
        int x09 = (X[di + 9] ^= X[xi + 9]);
        int x10 = (X[di + 10] ^= X[xi + 10]);
        int x11 = (X[di + 11] ^= X[xi + 11]);
        int x12 = (X[di + 12] ^= X[xi + 12]);
        int x13 = (X[di + 13] ^= X[xi + 13]);
        int x14 = (X[di + 14] ^= X[xi + 14]);
        int x15 = (X[di + 15] ^= X[xi + 15]);
        for (int i = 0; i < 8; i += 2)
        {
            x04 ^= rotateLeft(x00 + x12, 7);
            x08 ^= rotateLeft(x04 + x00, 9);
            x12 ^= rotateLeft(x08 + x04, 13);
            x00 ^= rotateLeft(x12 + x08, 18);
            x09 ^= rotateLeft(x05 + x01, 7);
            x13 ^= rotateLeft(x09 + x05, 9);
            x01 ^= rotateLeft(x13 + x09, 13);
            x05 ^= rotateLeft(x01 + x13, 18);
            x14 ^= rotateLeft(x10 + x06, 7);
            x02 ^= rotateLeft(x14 + x10, 9);
            x06 ^= rotateLeft(x02 + x14, 13);
            x10 ^= rotateLeft(x06 + x02, 18);
            x03 ^= rotateLeft(x15 + x11, 7);
            x07 ^= rotateLeft(x03 + x15, 9);
            x11 ^= rotateLeft(x07 + x03, 13);
            x15 ^= rotateLeft(x11 + x07, 18);
            x01 ^= rotateLeft(x00 + x03, 7);
            x02 ^= rotateLeft(x01 + x00, 9);
            x03 ^= rotateLeft(x02 + x01, 13);
            x00 ^= rotateLeft(x03 + x02, 18);
            x06 ^= rotateLeft(x05 + x04, 7);
            x07 ^= rotateLeft(x06 + x05, 9);
            x04 ^= rotateLeft(x07 + x06, 13);
            x05 ^= rotateLeft(x04 + x07, 18);
            x11 ^= rotateLeft(x10 + x09, 7);
            x08 ^= rotateLeft(x11 + x10, 9);
            x09 ^= rotateLeft(x08 + x11, 13);
            x10 ^= rotateLeft(x09 + x08, 18);
            x12 ^= rotateLeft(x15 + x14, 7);
            x13 ^= rotateLeft(x12 + x15, 9);
            x14 ^= rotateLeft(x13 + x12, 13);
            x15 ^= rotateLeft(x14 + x13, 18);
        }
        X[di] += x00;
        X[di + 1] += x01;
        X[di + 2] += x02;
        X[di + 3] += x03;
        X[di + 4] += x04;
        X[di + 5] += x05;
        X[di + 6] += x06;
        X[di + 7] += x07;
        X[di + 8] += x08;
        X[di + 9] += x09;
        X[di + 10] += x10;
        X[di + 11] += x11;
        X[di + 12] += x12;
        X[di + 13] += x13;
        X[di + 14] += x14;
        X[di + 15] += x15;

    }

}
