package gyurix.mysql;

import com.mysql.jdbc.Connection;
import gyurix.configfile.ConfigSerialization;
import gyurix.spigotlib.Config;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MySQLDatabase {
    @ConfigSerialization.ConfigOptions(serialize = false)
    private static ExecutorService executeThread = Executors.newSingleThreadExecutor(), prepareThread = Executors.newSingleThreadExecutor(), runnableThread = Executors.newSingleThreadExecutor();
    public String table;
    private String host;
    private String username;
    private String password;
    private String database;
    private int timeout = 10000;
    @ConfigSerialization.ConfigOptions(serialize = false)
    private Connection con;

    public MySQLDatabase() {

    }
    public MySQLDatabase(String host, String database, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.database = database;
        this.openConnection();
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

    public boolean openConnection() {
        try {
            this.con = (Connection) DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.database + "?autoReconnect=true", this.username, this.password);
            this.con.setAutoReconnect(true);
            this.con.setConnectTimeout(timeout);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Connection getConnection() {
        try {
            if (this.con == null || !this.con.isValid(timeout)) {
                this.openConnection();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this.con;
    }

    public ResultSet querry(String cmd) {
        ResultSet rs;
        PreparedStatement st;
        try {
            st = this.getConnection().prepareStatement(cmd);
            rs = st.executeQuery();
            return rs;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public int update(String cmd) {
        PreparedStatement st;
        try {
            st = this.getConnection().prepareStatement(cmd);
            int out = st.executeUpdate();
            return out;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return -1;
        }
    }

    public boolean command(String cmd) {
        PreparedStatement st;
        try {
            st = this.getConnection().prepareStatement(cmd);
            return st.execute();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void batch(Iterable<String> commands, Runnable r) {
        prepareThread.submit(new MySQLPrepare(commands, r));
    }

    public class MySQLPrepare implements Runnable {
        private final Iterable<String> ps;
        private final Runnable r;

        public MySQLPrepare(Iterable<String> cmds, Runnable r) {
            this.ps = cmds;
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
                            System.err.println("MySQL ERROR: error on executing command " + s + ":");
                            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    public class MySQLExecute implements Runnable {
        private final Statement st;
        private final Runnable r;

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
                e.printStackTrace();
            }
        }
    }
}

