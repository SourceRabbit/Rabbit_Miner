package rabbitminer.Core;

/**
 *
 * @author Nikos Siatras
 */
public class Computer
{


    static
    {

    }

    public static int getComputerCPUCoresCount()
    {
        return Runtime.getRuntime().availableProcessors();
    }

}
