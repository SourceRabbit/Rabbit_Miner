package tk.netindev.drill.hasher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Nikos Siatras
 */
public class Hasher
{

    public static native void slowHash(byte[] input, byte[] output);

    static
    {
        String library = "";
        final String system = System.getProperty("os.name").toLowerCase();
        if (system.contains("win"))
        {
            //System.loadLibrary("cryptonight");
            library = "/win64/cryptonight.dll";
        }
        else if (system.contains("nix") || system.contains("nux") || system.contains("aix"))
        {
            library = "/unix/libcryptonight.so";
        }

        try
        {
            loadLibrary(library);
        }
        catch (Exception ex)
        {
            System.err.println("Cannot Load Cryptonight!!!!");
            System.err.println(ex.getMessage());
        }
    }

    public static void Initialize()
    {
        // Do nothing...
    }

    private static void loadLibrary(String name) throws IOException
    {
        final File temp;
        try (InputStream inputStream = Hasher.class.getResourceAsStream(name))
        {
            final byte[] buffer = new byte[1024];
            int read;
            temp = File.createTempFile(name, "");
            final FileOutputStream outputStream = new FileOutputStream(temp);
            while ((read = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, read);
            }
            outputStream.close();
        }

        System.load(temp.getAbsolutePath());
    }

}
