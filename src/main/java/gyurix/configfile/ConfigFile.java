package gyurix.configfile;

import gyurix.mysql.MySQLDatabase;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static gyurix.spigotlib.SU.*;

public class ConfigFile {
    public static final ExecutorService fileIO = Executors.newFixedThreadPool(1);
    public static final ExecutorService sqlIO = Executors.newFixedThreadPool(1);
    public final String addressSplit = "\\.";
    public String backup = "";
    public ConfigData data = new ConfigData();
    public MySQLDatabase db;
    public String dbTable, dbKey, dbValue, dbArgs;
    public File file;

    public ConfigFile() {
    }

    public ConfigFile(InputStream stream) {
        load(stream);
    }

    public boolean load(InputStream stream) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
                int id = stream.read();
                if (id < 0)
                    break;
                bos.write(id);
            }
            stream.close();
            return load(new String(bos.toByteArray(), utf8));
        } catch (Throwable e) {
            error(cs, e, "SpigotLib", "gyurix");
        }
        return false;
    }

    public boolean load(String in) {
        ArrayList<ConfigReader> readers = new ArrayList<>();
        readers.add(new ConfigReader(-1, data));
        for (String s : in.split("\r?\n")) {
            int blockLvl = 0;
            while (s.length() > blockLvl && s.charAt(blockLvl) == ' ')
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

    public String getString(String address) {
        ConfigData cd = getData(address);
        return String.valueOf(cd.objectData == null ? cd.stringData : cd.objectData);
    }

    public ConfigData getData(String address) {
        String[] parts = address.split(addressSplit);
        ConfigData d = data;
        for (String p : parts) {
            if (p.matches("#\\d+")) {
                int num = Integer.valueOf(p.substring(1));
                if (d.listData == null || d.listData.size() <= num)
                    return new ConfigData("");
                d = d.listData.get(num);
            } else {
                ConfigData key = new ConfigData(p);
                if (d.mapData == null)
                    return new ConfigData("");
                if (d.mapData.containsKey(key)) {
                    d = d.mapData.get(key);
                } else {
                    return new ConfigData("");
                }
            }
        }
        return d;
    }

    public ConfigFile(File file) {
        load(file);
    }

    public boolean load(File f) {
        try {
            file = f;
            f.createNewFile();
            byte[] b = Files.readAllBytes(f.toPath());
            load(new String(b, utf8));
            return true;
        } catch (Throwable e) {
            error(cs, e, "SpigotLib", "gyurix");
        }
        return false;
    }

    public ConfigFile(MySQLDatabase mysql) {
        db = mysql;
        dbTable = mysql.table;
        dbKey = "key";
        dbValue = "value";
    }

    public ConfigFile(MySQLDatabase mysql, String table, String key, String value) {
        db = mysql;
        dbTable = table;
        dbKey = key;
        dbValue = value;
    }

    public ConfigFile(File file, Class cl, Type... types) {
        load(file);
        data.deserialize(cl, types);
    }

    public ConfigFile(String in) {
        load(in);
    }

    public ConfigFile(ConfigData d) {
        data = d;
    }

    public boolean containsKey(ConfigData key) {
        return data.mapData != null && data.mapData.containsKey(key);
    }

    public boolean containsKey(String key) {
        return data.mapData != null && data.mapData.containsKey(new ConfigData(key));
    }

    public <T> T get(String adress, Class<T> cl) {
        return getData(adress).deserialize(cl);
    }

    public <T> T get(String adress, Class<T> cl, Type... types) {
        return getData(adress).deserialize(cl, types);
    }

    public boolean getBoolean(String address) {
        return getData(address).deserialize(Boolean.class);
    }

    public byte getByte(String address) {
        return getData(address).deserialize(Byte.class);
    }

    public double getDouble(String address) {
        return getData(address).deserialize(Double.class);
    }

    public float getFloat(String address) {
        return getData(address).deserialize(Float.class);
    }

    public int getInt(String address) {
        return getData(address).deserialize(Integer.class);
    }

    public long getLong(String address) {
        return getData(address).deserialize(Long.class);
    }

    public short getShort(String address) {
        return getData(address).deserialize(Short.class);
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

    public void mysqlLoad(String key, String args) {
        String q = "SELECT `" + dbKey + "`, `" + dbValue + "` FROM " + dbTable + " WHERE " + args;
        try {
            ResultSet rs = db.querry(q);
            while (rs.next()) {
                String k = rs.getString(dbKey);
                String v = rs.getString(dbValue);
                setData(key + "." + k, new ConfigFile(v).data);
            }
        } catch (Throwable e) {
            cs.sendMessage("§cFailed to load data from MySQL database.\n" +
                    "The used querry command:\n");
            error(cs, e, "SpigotLib", "gyurix");
            e.printStackTrace();
        }
    }

    public void setData(String address, ConfigData cd) {
        String[] parts = address.split(addressSplit);
        ConfigData last = data;
        ConfigData lastKey = data;
        ConfigData d = data;
        for (String p : parts) {
            ConfigData key = new ConfigData(p);
            if (d.mapData == null)
                d.mapData = new LinkedHashMap<>();
            last = d;
            lastKey = key;
            if (d.mapData.containsKey(key)) {
                d = d.mapData.get(key);
            } else {
                d.mapData.put(key, d = new ConfigData(""));
            }
        }
        last.mapData.put(lastKey, cd);
    }

    public void mysqlLoad() {
        String q = "SELECT `" + dbKey + "`, `" + dbValue + "` FROM " + dbTable;
        try {
            ResultSet rs = db.querry(q);
            while (rs.next()) {
                String k = rs.getString(dbKey);
                String v = rs.getString(dbValue);
                setData(k, new ConfigFile(v).data);
            }
        } catch (Throwable e) {
            cs.sendMessage("§cFailed to load data from MySQL database.\n" +
                    "The used querry command:\n");
            error(cs, e, "SpigotLib", "gyurix");
            e.printStackTrace();
        }
    }

    public boolean reload() {
        if (file == null) {
            System.err.println("Error on reloading ConfigFile, missing file data.");
            return false;
        }
        data = new ConfigData();
        return load(file);
    }

    public boolean removeData(String address) {
        String[] allParts = address.split(addressSplit);
        int len = allParts.length - 1;
        String[] parts = (String[]) ArrayUtils.subarray(allParts, 0, len);
        ConfigData d = data;
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
            int id = Integer.valueOf(allParts[len].substring(1));
            return d.listData.remove(id) != null;
        }
        return d.mapData == null || d.mapData.remove(new ConfigData(allParts[len])) != null;
    }

    public boolean save() {
        if (db != null && dbArgs != null) {
            ArrayList<String> sl = new ArrayList<>();
            mysqlUpdate(sl, dbArgs);
            db.batch(sl, null);
            return true;
        } else if (file != null) {
            final String data = toString();
            fileIO.submit(new Runnable() {
                @Override
                public void run() {
                    saveDataToFile(data);
                }
            });
            return true;
        }
        System.err.println("Failed to save ConfigFile: Missing file / valid MySQL data.");
        return false;
    }

    public void mysqlUpdate(ArrayList<String> l, String args) {
        if (dbTable == null)
            return;
        l.add("DELETE FROM " + dbTable + (dbArgs == null ? "" : (" WHERE " + dbArgs)));
        if (args == null)
            args = dbArgs == null ? "'<key>','<value>'" : dbArgs.substring(dbArgs.indexOf('=') + 1) + ",'<key>','<value>'";
        data.saveToMySQL(l, dbTable, args, "");
    }

    public String toString() {
        try {
            String s = data.toString().replace("\n  ", "\n").replaceAll("\n +#", "\n#");
            return s.startsWith("\n") ? s.substring(1) : s;
        } catch (Throwable e) {
            return "";
        }
    }

    private void saveDataToFile(String data) {
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
                    error(cs, e, "SpigotLib", "gyurix");
                }
            }
            File tempf = new File(file + ".tmp");
            tempf.createNewFile();
            Writer w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempf), utf8));
            w.write(data);
            w.close();
            file.delete();
            tempf.renameTo(file);
        } catch (Throwable e) {
            error(cs, e, "SpigotLib", "gyurix");
        }
    }

    public boolean save(OutputStream out) {
        try {
            byte[] data = toString().getBytes(utf8);
            out.write(data);
            out.flush();
            out.close();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveNoAsync() {
        if (db != null) {
            ArrayList<String> sl = new ArrayList<>();
            mysqlUpdate(sl, dbArgs);
            db.batchNoAsync(sl);
            return true;
        } else if (file != null) {
            saveDataToFile(toString());
            return true;
        }
        System.err.println("Failed to save ConfigFile: Missing file / valid MySQL data.");
        return false;
    }

    /**
     * Serializes the ConfigData for being able to throw all the object references
     */
    public void serialize() {
        ConfigData newCd = new ConfigFile(data.toString()).data;
        data.objectData = null;
        data.listData = newCd.listData;
        data.mapData = newCd.mapData;
        data.stringData = newCd.stringData;
        data.types = newCd.types;
    }

    public void setObject(String address, Object obj) {
        getData(address, true).objectData = obj;
    }

    public ConfigData getData(String address, boolean autoCreate) {
        if (!autoCreate)
            return getData(address);
        String[] parts = address.split(addressSplit);
        ConfigData d = data;
        for (String p : parts) {
            if (p.matches("#\\d+")) {
                int num = Integer.valueOf(p.substring(1));
                if (d.listData == null) {
                    d.listData = new ArrayList<>();
                }
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

    public void setString(String adress, String value) {
        getData(adress, true).stringData = value;
    }

    public ConfigFile subConfig(String address) {
        return new ConfigFile(getData(address, true));
    }

    public ConfigFile subConfig(String address, String dbArgs) {
        ConfigFile kf = new ConfigFile(getData(address, true));
        kf.db = db;
        kf.dbTable = dbTable;
        kf.dbKey = dbKey;
        kf.dbValue = dbValue;
        kf.dbArgs = dbArgs;
        return kf;
    }

    public ConfigFile subConfig(ConfigData key) {
        return new ConfigFile(getData(key));
    }

    public ConfigData getData(ConfigData key) {
        if (data.mapData == null)
            data.mapData = new LinkedHashMap<>();
        ConfigData out = data.mapData.get(key);
        if (out == null)
            data.mapData.put(key, out = new ConfigData(""));
        return out;
    }

    public ConfigFile subConfig(int id) {
        try {
            return new ConfigFile(data.listData.get(id));
        } catch (Throwable e) {
            error(cs, e, "SpigotLib", "gyurix");
        }
        return null;
    }
}