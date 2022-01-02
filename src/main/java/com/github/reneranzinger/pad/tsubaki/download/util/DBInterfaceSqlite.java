package com.github.reneranzinger.pad.tsubaki.download.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class that serves as persistent layer for the SQLite database files.
 *
 */
public class DBInterfaceSqlite
{
    // connection object that can be used to generate statements
    private Connection m_connection = null;

    /**
     * Constructor of the interface. Needs the filename/path of the database
     * file.
     *
     * @param a_databaseFilePath
     *            Filename and path of the database file to be opened
     * @throws ClassNotFoundException
     *             thrown if the database class can not be found
     * @throws SQLException
     *             thrown if the database connection can not be created
     */
    public DBInterfaceSqlite(String a_databaseFilePath) throws ClassNotFoundException, SQLException
    {
        // load driver into memory so it can be found
        Class.forName("org.sqlite.JDBC");
        // database URL
        String t_url = "jdbc:sqlite:" + a_databaseFilePath;
        // create a connection to the database
        this.m_connection = DriverManager.getConnection(t_url);
    }

    /**
     * Tests if the connected file is a valid database by querying on the
     * monsters table.
     *
     * @throws SQLException
     */
    public boolean validDatabase()
    {
        try
        {
            // prepare a statement -> this will already fail if its not a
            // database
            PreparedStatement t_statement = this.m_connection
                    .prepareStatement("SELECT monster_id FROM monsters");
            // perform the selected -> will fail if the table does not exist
            ResultSet t_set = t_statement.executeQuery();
            t_set.close();
            t_statement.close();
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    /**
     * Close the database connection.
     *
     * @throws SQLException
     *             thrown if the database connection can not be closed
     */
    public void close() throws SQLException
    {
        // check if a connection object exists
        if (this.m_connection != null)
        {
            // close the connection
            this.m_connection.close();
        }
    }
}
