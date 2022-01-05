package com.github.reneranzinger.pad.tsubaki.download;

import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import com.github.reneranzinger.pad.tsubaki.download.util.DBInterfaceSqlite;
import com.github.reneranzinger.pad.tsubaki.download.util.Downloader;

/**
 *
 */
public class App
{
    /**
     * Name of the database file in the local file system
     */
    private static final String DEFAULT_FILENAME = "dadguide.sqlite";

    /**
     * Keys used in the properties file
     */
    private static final String PROPERTY_KEY_PATH = "download_folder";
    private static final String PROPERTY_KEY_URL = "download_url";

    public static void main(String[] a_args) throws ClassNotFoundException, SQLException
    {
        String t_tsubakiDatabaseURL = null;
        String t_localPath = null;
        // initialize from properties file
        try
        {
            Properties t_properties = App.loadTsubakiProperties();
            t_localPath = t_properties.getProperty(App.PROPERTY_KEY_PATH);
            t_tsubakiDatabaseURL = t_properties.getProperty(App.PROPERTY_KEY_URL);
        }
        catch (Exception e)
        {
            // nothing to do
        }
        // overwrite from arguments
        if (a_args.length == 2)
        {
            t_localPath = a_args[0];
            t_tsubakiDatabaseURL = a_args[1];
        }
        else
        {
            System.out.println("Unable to perform download. Expect two commandline arguments.");
            App.printComandParameter();
            return;
        }
        // check if information is there
        if (t_tsubakiDatabaseURL == null)
        {
            System.out.println("Unable to perform download. Missing URL.");
            App.printComandParameter();
            return;
        }
        if (t_localPath == null)
        {
            System.out.println("Unable to perform download. Missing local path.");
            App.printComandParameter();
            return;
        }
        else
        {
            File t_file = new File(t_localPath);
            if (!t_file.exists() || !t_file.isDirectory())
            {
                System.out.println(
                        "Unable to perform download. Local path is not an existing directory.");
                App.printComandParameter();
                return;
            }
        }
        // create folders and download files
        System.out.print("Download SQLite database ... ");
        // create the folder with a timestamp
        Date t_date = Calendar.getInstance().getTime();
        DateFormat t_dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String t_folderName = t_dateFormat.format(t_date);
        String t_folderSqlite = t_localPath + File.separator + t_folderName + File.separator;
        File t_folder = new File(t_folderSqlite);
        t_folder.mkdirs();
        // download files into the folder
        try
        {
            Downloader t_downloader = new Downloader();
            // database file
            t_downloader.downloadFile(t_tsubakiDatabaseURL, t_folderSqlite + App.DEFAULT_FILENAME);
        }
        catch (Exception e)
        {
            System.out.println("failed: " + e.getMessage());
            e.printStackTrace(System.err);
            return;
        }
        System.out.println("finished");
        // check if the file is really a database
        System.out.print("Testing database ... ");
        DBInterfaceSqlite t_db = new DBInterfaceSqlite(t_folderSqlite + App.DEFAULT_FILENAME);
        if (!t_db.validDatabase())
        {
            System.out.println("failed");

        }
        else
        {
            System.out.println("success");
        }
    }

    private static void printComandParameter()
    {
        System.out.println("Usage: java -jar pad.tsubaki.download.jar <localPath> <URL>");
        System.out.println();
        System.out.println(
                "Where <localPath> is a directory in the local file system that the Tsubaki");
        System.out.println(
                "database will be downloaded to. <URL> is the web adress of the database to");
        System.out.println("be downloaded.");
    }

    private static Properties loadTsubakiProperties() throws Exception
    {
        // open the file
        FileReader t_reader = new FileReader("tsubaki.properties");
        // read properties
        Properties t_properties = new Properties();
        t_properties.load(t_reader);
        // close file
        t_reader.close();
        return t_properties;
    }
}
