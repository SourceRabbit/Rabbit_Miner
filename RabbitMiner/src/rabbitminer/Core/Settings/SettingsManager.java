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

    private static final String fAppVersion = "1.0.5";
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
