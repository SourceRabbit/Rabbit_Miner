package rabbitminer.ClusterNode.Miner.Hashers.Scrypt;

import com.sun.jna.Library;

/**
 *
 * @author Nikos Siatras
 */
public interface ISCryptHasher_CPP extends Library
{

    double Add(double a, double b);

    void xorSalsa8(int[] X, int xi, int di);
}
