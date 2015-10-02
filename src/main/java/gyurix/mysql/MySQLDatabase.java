package gyurix.mysql;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 * A simple API for communicating with a MySQL database. Make sure to call every method of this
 * API asynchronously, because handling these method calls could took very long (up to 10 seconds),
 * it depends on the network speed and quality of both your servers network and the databases network,
 * and on the success of the connection
 */
public class MySQLDatabase {
    private final String HOST;
    private final String USERNAME;
    private final String PASSWORD;
    private final String DATABASE;
    private final int TIMEOUT=10000;

    private Connection con;

    /**
     * Create and connect to a MySQL connection.
     *
     * @param host Hostname with port number of the database
     * @param database Name of the database on the MySQL server
     * @param username Username for the MySQL server
     * @param password Password for the MySQL server
     */
    public MySQLDatabase(String host, String database, String username, String password) {
        HOST = host;
        USERNAME = username;
        PASSWORD = password;
        DATABASE = database;
        openConnection();
    }

    /**
     * Connects to the database. Used on the initialization.
     * @return The success rate of the connection.
     */
    public boolean openConnection(){
        try {
            con = (Connection) DriverManager.getConnection("jdbc:mysql://" + HOST + "/" + DATABASE+"?autoReconnect=true", USERNAME, PASSWORD);
            con.setAutoReconnect(true);
            con.setConnectTimeout(TIMEOUT);
        }
        catch (Throwable e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private Connection getConnection() {
        try {
            if(con == null || !con.isValid(TIMEOUT)) {
                openConnection();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Sends the given querry command to the database.
     *
     * @param cmd Querry MySQL command
     * @return the ResultSet respond of the database, or null if there where an error on
     * the handling of the request
     */
    public ResultSet querry(String cmd){
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) getConnection().prepareStatement(cmd);
            rs = st.executeQuery();
            return rs;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Sends the given update command to the database.
     *
     * @param cmd Update MySQL command
     * @return The amount of the changed rows or fields
     */
    public int update(String cmd){
        PreparedStatement st = null;
        int out;
        try {
            st = (PreparedStatement) getConnection().prepareStatement(cmd);
            out = st.executeUpdate();
            return out;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Send the given MySQL command to the database
     *
     * @param cmd MySQL command to be sent
     * @return The success rate of the command execution.
     */
    public boolean command(String cmd){
        PreparedStatement st = null;
        boolean out;
        try {
            st = (PreparedStatement) getConnection().prepareStatement(cmd);
            out = st.execute();
            return out;
        } catch(Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }
}