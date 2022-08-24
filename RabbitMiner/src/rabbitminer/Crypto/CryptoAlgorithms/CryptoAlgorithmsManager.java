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
