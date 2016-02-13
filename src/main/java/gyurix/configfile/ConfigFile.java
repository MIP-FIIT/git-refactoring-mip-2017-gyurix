package gyurix.configfile;

import gyurix.mysql.MySQLDatabase;
import gyurix.utils.ArrayUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigFile {
    public static final ExecutorService fileIO = Executors.newFixedThreadPool(1);
    public static final ExecutorService sqlIO = Executors.newFixedThreadPool(1);
    public final Charset charset = Charset.forName("UTF-8");
    public final String addressSplit = "\\.";
    public String backup = "";
    public File file;
    public MySQLDatabase db;
    public String dbTable, dbKey, dbValue, dbArgs = null;
    public ConfigData data = new ConfigData();

    public ConfigFile() {
    }

    public ConfigFile(InputStream stream) {
        load(stream);
    }

    public ConfigFile(File file) {
        load(file);
    }

    public ConfigFile(MySQLDatabase mysql, String table, String key, String value) {
        db = mysql;
        dbTable = table;
        dbKey = key;
        dbValue = value;
    }

    public ConfigFile(String in) {
        load(in);
    }

    public ConfigFile(ConfigData d) {
        this.data = d;
    }

    public boolean reload() {
        if (file == null) {
            System.err.println("Error on reloading ConfigFile, missing file data.");
            return false;
        }
        this.data = new ConfigData();
        return load(this.file);
    }

    public boolean save() {
        if (db != null && dbArgs != null) {
            ArrayList<String> sl = new ArrayList<>();
            mysqlUpdate(sl, dbArgs);
            db.batch(sl, null);
            return true;
        } else if (file != null) {
            fileIO.submit(new SaveRunnable(toString()));
            return true;
        }
        System.err.println("Failed to save ConfigFile: Missing file / valid MySQL data.");
        return false;
    }

    public boolean save(OutputStream out) {
        try {
            byte[] data = toString().getBytes(charset);
            out.write(data);
            out.flush();
            out.close();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean load(File f) {
        try {
            this.file = f;
            f.createNewFile();
            byte[] b = Files.readAllBytes(f.toPath());
            load(new String(b, this.charset));
            return true;
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return false;
    }

    public boolean load(InputStream stream) {
        try {
            byte[] b = new byte[stream.available()];
            stream.read(b);
            return load(new String(b, this.charset));
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return false;
    }

    public boolean load(String in) {
        ArrayList<ConfigReader> readers = new ArrayList<ConfigReader>();

        readers.add(new ConfigReader(-1, this.data));

        for (String s : in.split("\r?\n")) {
            int blockLvl = 0;
            while ((s.length() > blockLvl) && (s.charAt(blockLvl) == ' '))
                blockLvl++;
            s = s.substring(blockLvl);
            int id = readers.size() - 1;
            if (!s.isEmpty()) {
                if (s.startsWith("#")) {
                    readers.get(id).addComment(s.substring(1));
                } else {
                    while (readers.get(id).blockLvl > blockLvl) {
                        readers.remove(id);
                        id--;
                    }
                    readers.get(id).handleInput(readers, s, blockLvl);
                }
            }
        }
        backup = getString("backup");
        return true;
    }

    public ConfigData getData(ConfigData key) {
        if (data.mapData == null)
            data.mapData = new LinkedHashMap<>();
        ConfigData out = data.mapData.get(key);
        if (out == null)
            data.mapData.put(key, out = new ConfigData(""));
        return out;
    }

    public ConfigData getData(String address) {
        String[] parts = address.split(this.addressSplit);
        ConfigData d = data;
        for (String p : parts) {
            if (p.matches("#\\d+")) {
                int num = Integer.valueOf(p.substring(1));
                if (d.listData == null)
                    d.listData = new ArrayList<>();
                while (d.listData.size() <= num) {
                    d.listData.add(new ConfigData(""));
                }
                d = d.listData.get(num);
            } else {
                ConfigData key = new ConfigData(p);
                if (d.mapData == null)
                    d.mapData = new LinkedHashMap<>();
                if (d.mapData.containsKey(key)) {
                    d = d.mapData.get(key);
                } else {
                    d.mapData.put(key, d = new ConfigData(""));
                }
            }
        }
        return d;
    }

    public boolean removeData(String address) {
        String[] allParts = address.split(this.addressSplit);
        int len = allParts.length - 1;
        String[] parts = (String[]) ArrayUtils.subarray(allParts, 0, len);
        ConfigData d = this.data;
        for (String p : parts) {
            if (p.matches("#\\d+")) {
                if (d.listData == null)
                    return false;
                int num = Integer.valueOf(p.substring(1));
                if (d.listData.size() >= num) {
                    return false;
                }
                d = d.listData.get(num);
            } else {
                ConfigData key = new ConfigData(p);
                if (d.mapData == null || !d.mapData.containsKey(key)) {
                    return false;
                } else {
                    d = d.mapData.get(key);
                }
            }
        }
        if (allParts[len].matches("#\\d+")) {
            return d.listData.remove(Integer.valueOf(allParts[len].substring(1)));
        }
        return d.mapData == null || d.mapData.remove(new ConfigData(allParts[len])) != null;
    }

    public Object get(String adress, Class cl) {
        return getData(adress).deserialize(cl);
    }

    public Object get(String adress, Class cl, Type... types) {
        return getData(adress).deserialize(cl, types);
    }

    public double getDouble(String address) {
        return (double) (Double) getData(address).deserialize(Double.class);
    }

    public float getFloat(String address) {
        return (float) (Float) getData(address).deserialize(Float.class);
    }

    public long getLong(String address) {
        return (long) (Long) getData(address).deserialize(Long.class);
    }

    public int getInt(String address) {
        return (int) (Integer) getData(address).deserialize(Integer.class);
    }

    public short getShort(String address) {
        return (short) (Short) getData(address).deserialize(Short.class);
    }

    public byte getByte(String address) {
        return (byte) (Byte) getData(address).deserialize(Byte.class);
    }

    public boolean getBoolean(String address) {
        return (boolean) (Boolean) getData(address).deserialize(Boolean.class);
    }

    public ConfigFile subConfig(String address) {
        return new ConfigFile(getData(address));
    }

    public ConfigFile subConfig(String address, String dbArgs) {
        ConfigFile kf = new ConfigFile(getData(address));
        kf.db = db;
        kf.dbTable = dbTable;
        kf.dbKey = dbKey;
        kf.dbValue = dbValue;
        kf.dbArgs = dbArgs;
        return kf;
    }

    public void mysqlLoad(String key, String args) {
        String q = "SELECT `" + dbKey + "`, `" + dbValue + "` FROM " + dbTable + " WHERE " + args;
        try {
            ResultSet rs = db.querry(q);
            while (rs.next()) {
                String k = rs.getString(dbKey);
                String v = rs.getString(dbValue);
                getData(key + "." + k).stringData = v;
            }
        } catch (Throwable e) {
            System.err.println("Failed to load data from MySQL database.\n" +
                    "The used querry command: " + q);
            e.printStackTrace();
        }
    }

    public void mysqlUpdate(ArrayList<String> l, String args) {
        if (dbArgs == null || dbTable == null)
            return;
        l.add("DELETE FROM " + dbTable + " WHERE " + dbArgs);
        if (args == null)
            args = dbArgs.substring(dbArgs.indexOf("=") + 1) + ",'<key>','<value>'";
        data.saveToMySQL(l, dbTable, args, "");
    }

    public ConfigFile subConfig(ConfigData key) {
        return new ConfigFile(getData(key));
    }

    public ConfigFile subConfig(int id) {
        try {
            return new ConfigFile(this.data.listData.get(id));
        } catch (Throwable e) {
            ConfigSerialization.errorLog(e);
        }
        return null;
    }


    public String getString(String address) {
        String out = getData(address).stringData;
        return out == null ? "" : out;
    }


    public void setString(String adress, String value) {
        getData(adress).stringData = value;
    }

    public void setObject(String address, Object obj) {
        getData(address).objectData = obj;
    }

    public String toString() {
        try {
            String s = data.toString().replace("\n  ", "\n").replaceAll("\n +#", "\n#");
            return s.startsWith("\n") ? s.substring(1) : s;
        } catch (Throwable e) {
            return "";
        }
    }

    public ArrayList<String> getStringKeyList() {
        ArrayList<String> out = new ArrayList<>();
        try {
            for (ConfigData cd : data.mapData.keySet()) {
                out.add(cd.stringData);
            }
        } catch (Throwable e) {

        }
        return out;
    }

    public ArrayList<String> getStringKeyList(String key) {
        ArrayList<String> out = new ArrayList<>();
        try {
            for (ConfigData cd : getData(key).mapData.keySet()) {
                out.add(cd.stringData);
            }
        } catch (Throwable e) {

        }
        return out;
    }

    private class SaveRunnable implements Runnable {
        String out;

        public SaveRunnable(String data) {
            out = data;
        }

        @Override
        public void run() {
            try {
                if (file.exists() && !backup.isEmpty()) {
                    File f = null;
                    try {
                        String time = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss SSSS").format(new Date());
                        f = new File(backup.replace("<time>", time));
                        f.getParentFile().mkdirs();
                        Files.copy(file.toPath(), f.toPath());
                    } catch (FileAlreadyExistsException e) {
                        System.out.println("Failed to save backup file, the backup file \"" + f + "\" already exists");
                    } catch (Throwable e) {
                        e.printStackTrace();
                        ConfigSerialization.errorLog(e);
                    }
                }
                File tempf = new File(file + ".tmp");
                tempf.createNewFile();
                Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempf), charset));
                w.write(out);
                w.close();
                file.delete();
                tempf.renameTo(file);
            } catch (Throwable e) {
                ConfigSerialization.errorLog(e);
            }
        }
    }
}