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
package rabbitminer.Cluster.Server;

/**
 *
 * @author Nikos Siatras
 */
public class NodeTCPConnectionVariables
{

    private boolean fClientIsAuthorized = false;
    private int fThreadsCount;

    private int fWorkRangeFrom = 0, fWorkRangeTo = 0;

    private double fLastTimeNodeTookAJob = 0;
    private double fLastTotalRangeToWork = 0;

    private double fAverageHashesPerSecond = 0;
    private int fLastHashesPerSecondIndex = 0;
    private final double[] fLastHashesPerSecond = new double[10];

    public NodeTCPConnectionVariables()
    {
        for (int i = 0; i < fLastHashesPerSecond.length; i++)
        {
            fLastHashesPerSecond[i] = 0;
        }
    }

    public boolean isClientAuthorized()
    {
        return fClientIsAuthorized;
    }

    /**
     * Αυτό το method καλείτε απο τον Cluster Server αφου ο client κάνει connect
     * με το σωστό Password
     */
    public void ClientLoggedInSucessfully()
    {
        fClientIsAuthorized = true;
    }

    public int getThreadsCount()
    {
        return fThreadsCount;
    }

    public void setThreadsCount(int count)
    {
        fThreadsCount = count;
    }

    /**
     * Κάνει set το nonce range που δουλευει το Node Ειναι γιά χρήση με το UI
     *
     * @param from
     * @param to
     */
    public void setWorkRange(final int from, final int to)
    {
        if (fLastTimeNodeTookAJob > 0)
        {
            // Στη μεταβλητή lastHashesPerSecond Υπολογίζουμε πόσα Hashes/second έκανε στην τελευταία δουλειά
            double lastHashesPerSecond = fLastTotalRangeToWork / (((double) System.currentTimeMillis()) - ((double) fLastTimeNodeTookAJob)) * 1000;

            // Καλούμε το method CalculateAverageHashesPerSecond για να υπολογίσει το Average Hashes/sec
            CalculateAverageHashesPerSecond(lastHashesPerSecond);
        }

        fWorkRangeFrom = from;
        fWorkRangeTo = to;

        fLastTimeNodeTookAJob = System.currentTimeMillis();
        fLastTotalRangeToWork = to - from;
    }

    private void CalculateAverageHashesPerSecond(final double hashesPerSecond)
    {
        fLastHashesPerSecond[fLastHashesPerSecondIndex] = hashesPerSecond;
        fLastHashesPerSecondIndex += 1;

        double averageHashesPerSecond = 0;
        for (double d : fLastHashesPerSecond)
        {
            averageHashesPerSecond += d;
        }
        fAverageHashesPerSecond = averageHashesPerSecond / fLastHashesPerSecond.length;

        if (fLastHashesPerSecondIndex >= fLastHashesPerSecond.length)
        {
            fLastHashesPerSecondIndex = 0;
        }
    }

    public String getWorkRange()
    {
        return String.valueOf(fWorkRangeFrom) + " - " + String.valueOf(fWorkRangeTo);
    }

    public double getHashesPerSecond()
    {
        return fAverageHashesPerSecond;
    }

}
