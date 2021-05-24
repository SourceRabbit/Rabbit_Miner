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
