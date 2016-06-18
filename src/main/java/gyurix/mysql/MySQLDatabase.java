package gyurix.mysql;

import com.mysql.jdbc.Connection;
import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class representing a MySQL database connection and containing the required methods and utils
 * for executing MySQL querries
 */
public class MySQLDatabase {
    @ConfigOptions(serialize = false)
    private static ExecutorService executeThread = Executors.newSingleThreadExecutor(), prepareThread = Executors.newSingleThreadExecutor(), runnableThread = Executors.newSingleThreadExecutor();
    public String table;
    @ConfigOptions(serialize = false)
    private Connection con;
    private String database;
    private String host;
    private String password;
    private int timeout = 10000;
    private String username;

    public MySQLDatabase() {

    }

    public MySQLDatabase(String host, String database, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
        openConnection();
    }

    public static String escape(String in) {
        StringBuilder out = new StringBuilder();
        for (char c : in.toCharArray()) {
            switch (c) {
                case '\u0000':
                    out.append("\\0");
                    break;
                case '\u001a':
                    out.append("\\Z");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\'':
                    out.append("\\'");
                    break;
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
        }
        return out.toString();
    }

    public void batch(Iterable<String> commands) {
        prepareThread.submit(new MySQLPrepare(commands, null));
    }

    public void batch(Iterable<String> commands, Runnable r) {
        prepareThread.submit(new MySQLPrepare(commands, r));
    }

    public void batchNoAsync(ArrayList<String> list) {
        try {
            Statement st = getConnection().createStatement();
            for (String s : list)
                st.addBatch(s);
            st.executeBatch();
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    public boolean command(String cmd) {
        PreparedStatement st;
        try {
            st = getConnection().prepareStatement(cmd);
            return st.execute();
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return false;
    }

    private Connection getConnection() {
        try {
            if (con == null || !con.isValid(timeout)) {
                openConnection();
            }
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return con;
    }

    public boolean openConnection() {
        try {
            con = (Connection) DriverManager.getConnection("jdbc:mysql://" + host + "/" + database + "?autoReconnect=true", username, password);
            con.setAutoReconnect(true);
            con.setConnectTimeout(timeout);
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return false;
        }
        return true;
    }

    public ResultSet querry(String cmd) {
        ResultSet rs;
        PreparedStatement st;
        try {
            st = getConnection().prepareStatement(cmd);
            rs = st.executeQuery();
            return rs;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    public int update(String cmd) {
        PreparedStatement st;
        try {
            st = getConnection().prepareStatement(cmd);
            int out = st.executeUpdate();
            return out;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return -1;
        }
    }

    public class MySQLExecute implements Runnable {
        private final Runnable r;
        private final Statement st;

        public MySQLExecute(Statement st, Runnable r) {
            this.st = st;
            this.r = r;
        }

        @Override
        public void run() {
            try {
                st.executeBatch();
                if (r != null)
                    runnableThread.submit(r);
            } catch (SQLException e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }
    }

    public class MySQLPrepare implements Runnable {
        private final Iterable<String> ps;
        private final Runnable r;

        public MySQLPrepare(Iterable<String> cmds, Runnable r) {
            ps = cmds;
            this.r = r;
        }

        @Override
        public void run() {
            try {
                if (Config.debug) {
                    for (String s : ps) {
                        try {
                            PreparedStatement ps = getConnection().prepareStatement(s);
                            ps.execute();
                        } catch (Throwable e) {
                            SU.cs.sendMessage("§cMySQL ERROR:§e Error on executing command §b" + s + "§c:");
                            SU.error(SU.cs, e, "SpigotLib", "gyurix");
                        }
                    }
                } else {
                    Statement st = getConnection().createStatement();
                    for (String s : ps) {
                        st.addBatch(s);
                    }
                    executeThread.submit(new MySQLExecute(st, r));
                }

            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
            }
        }
    }
}

