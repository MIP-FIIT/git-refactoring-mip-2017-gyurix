package gyurix.test;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.spigotlib.Config;

import java.io.File;

/**
 * Created by GyuriX on 2016. 07. 31..
 */
public class Test {
    public static void main(String[] args) throws Throwable {
        System.out.println(new ConfigData(new ConfigFile(new File("D:\\DEV\\Servers\\Test\\plugins\\SpigotLib\\config.yml")).data.deserialize(Config.class)));
        //System.out.println(ChatTag.fromColoredText("null"));
    }
}
