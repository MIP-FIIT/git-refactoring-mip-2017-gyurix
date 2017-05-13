package gyurix.pluginloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RemoteClassLoader extends ClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final HashMap<String, byte[]> cache = new HashMap<>();

    public RemoteClassLoader(byte[] jarBytes) {
        try {
            ZipInputStream jis = new ZipInputStream(new ByteArrayInputStream(jarBytes));
            for (ZipEntry e = jis.getNextEntry(); e != null; e = jis.getNextEntry()) {
                if (!e.isDirectory()) {
                    String n = e.getName();
                    byte[] d = read(jis);
                    if (n.endsWith(".class"))
                        defineClass(n.substring(0, n.length() - 6).replace("/", "."), d, 0, d.length);
                    else
                        cache.put(e.getName(), d);
                }
                jis.closeEntry();
            }
            jis.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static byte[] read(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (true) {
            int i = in.read();
            if (i == -1)
                return bos.toByteArray();
            bos.write(i);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] d = cache.get(name);
        return d == null ? super.getResourceAsStream(name) : new ByteArrayInputStream(d);
    }
}