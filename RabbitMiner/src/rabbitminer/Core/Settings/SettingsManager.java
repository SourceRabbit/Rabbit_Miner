package rabbitminer.Core.Settings;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

/**
 *
 * @author Nikos Siatras
 */
public class SettingsManager
{

    private static final String fAppVersion = "1.0.3";
    public static final String fClusterSettingsFileExtension = "rbmcs";
    private static final String fSettingsFileName = "RabbitMiner.conf";

    public static final Properties fAppSettings = new Properties();
    private static String fSettingsFilePath = "";

    static
    {
        fSettingsFilePath = getAppSettingsFilePath();
        LoadSettings();
    }

    /**
     * Load the App settings
     */
    private static void LoadSettings()
    {
        try
        {
            File configFile = new File(fSettingsFilePath);
            try (FileReader reader = new FileReader(configFile))
            {
                fAppSettings.load(reader);
            }
        }
        catch (Exception ex)
        {
            // file does not exist
        }
    }

    /**
     * Save the App settings
     */
    public static void SaveSettings()
    {
        try
        {
            File configFile = new File(fSettingsFilePath);
            try (FileWriter writer = new FileWriter(configFile))
            {
                fAppSettings.store(writer, "RabbitMinerSettings");
            }
        }
        catch (Exception ex)
        {
            // file does not exist
        }
    }

    public static String getLastFolderPathUsedToSaveSettings()
    {
        String value = fAppSettings.getProperty("LastPathUsedToSaveSettings");
        if (value == null || value.equals(""))
        {
            return System.getProperty("user.home") + File.separator;
        }
        else
        {
            return value;
        }
    }

    public static void setLastFolderPathUsedToSaveSettings(String dir)
    {
        fAppSettings.setProperty("LastPathUsedToSaveSettings", dir);
        SaveSettings();
    }

    public static String getAppSettingsFilePath()
    {
        return System.getProperty("user.home") + File.separator + fSettingsFileName;
    }

    /**
     * Επιστρέφει την έκδοση του Software
     *
     * @return
     */
    public static String getAppVersion()
    {
        return fAppVersion;
    }

}
