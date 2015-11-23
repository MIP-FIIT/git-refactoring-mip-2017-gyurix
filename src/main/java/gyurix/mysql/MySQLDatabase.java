package gyurix.mysql;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;

import java.sql.DriverManager;
import java.sql.ResultSet;

public class MySQLDatabase {
    private final String HOST;
    private final String USERNAME;
    private final String PASSWORD;
    private final String DATABASE;
    private final int TIMEOUT = 10000;
    private Connection con;

    public MySQLDatabase(String host, String database, String username, String password) {
        this.HOST = host;
        this.USERNAME = username;
        this.PASSWORD = password;
        this.DATABASE = database;
        this.openConnection();
    }

    public boolean openConnection() {
        try {
            this.con = (Connection) DriverManager.getConnection("jdbc:mysql://" + this.HOST + "/" + this.DATABASE + "?autoReconnect=true", this.USERNAME, this.PASSWORD);
            this.con.setAutoReconnect(true);
            this.con.setConnectTimeout(10000);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Connection getConnection() {
        try {
            if (this.con == null || !this.con.isValid(10000)) {
                this.openConnection();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this.con;
    }

    public ResultSet querry(String cmd) {
        ResultSet rs = null;
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) this.getConnection().prepareStatement(cmd);
            rs = st.executeQuery();
            return rs;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public int update(String cmd) {
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) this.getConnection().prepareStatement(cmd);
            int out = st.executeUpdate();
            return out;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public boolean command(String cmd) {
        PreparedStatement st = null;
        try {
            st = (PreparedStatement) this.getConnection().prepareStatement(cmd);
            boolean out = st.execute();
            return out;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return false;
        }
    }
}

