package rabbitminer.Crypto.CryptoAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Nikos Siatras
 */
public class CryptoAlgorithmsManager
{

    private static final HashMap<String, ECryptoAlgorithm> fCryptoAlgorithmsByName = new HashMap<>();
    private static final HashMap<ECryptoAlgorithm, String> fCryptoAlgorithmsByEnum = new HashMap<>();
    private static final ArrayList<String> fCryptoAlgorithmsNamesList;

    static
    {
        // Εδώ δημιουργούμε το HashMap με τα CryptoAlgorithms που μπορεί να διαχειριστεί 
        // η εφαρμογή
        fCryptoAlgorithmsByName.put("SCrypt", ECryptoAlgorithm.SCrypt);
        fCryptoAlgorithmsByName.put("RandomX", ECryptoAlgorithm.RandomX);

        // Το fCryptoAlgorithmsByEnum και το fCryptocurrenciesNamesList 
        // χτίζενται απο το fCryptoAlgorithmsByName
        fCryptoAlgorithmsNamesList = new ArrayList<String>();
        ///////
        for (String s : fCryptoAlgorithmsByName.keySet())
        {
            fCryptoAlgorithmsByEnum.put(fCryptoAlgorithmsByName.get(s), s);
            fCryptoAlgorithmsNamesList.add(s);
        }
    }

    /**
     * Επιστρέφει το όνομα του αλγόριθμου απο το Enum
     *
     * @param e
     * @return
     */
    public static String getCryptoAlgorithmNameFromEnum(ECryptoAlgorithm e)
    {
        return fCryptoAlgorithmsByEnum.get(e);
    }

    /**
     * Επιστρέφει το Enum του αλγόριθμου απο το όνομα του
     *
     * @param name
     * @return
     */
    public static ECryptoAlgorithm getCryptoAlgorithmEnumFromName(String name)
    {
        return fCryptoAlgorithmsByName.get(name);
    }

    /**
     * Επιστρέφει μία λίστα με τα ονόματα των Αλγόριθμων
     *
     * @return
     */
    public static ArrayList<String> getCryptoAlgorithmsNamesList()
    {
        return fCryptoAlgorithmsNamesList;
    }

}
