package gyurix.spigotlib.features;

import gyurix.configfile.ConfigData;
import gyurix.configfile.ConfigFile;
import gyurix.protocol.PacketInEvent;
import gyurix.protocol.Protocol;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Created by gyurix on 24/11/2015.
 */
public class LongerChat implements Protocol.PacketInListener {
    @Override
    public void onPacketIN(PacketInEvent e) {
        if (!Config.Chat.enabled)
            return;
        Player plr = e.getPlayer();
        boolean puffered;
        e.setCancelled(true);
        ConfigFile pf = SU.getPlayerConfig(plr);
        String msg = (String) e.getPacketData()[0];
        boolean bl = puffered = msg.length() >= Config.Chat.Long.pufferAfter;
        if (!("" + Config.Chat.newLineCharacter).isEmpty() && plr.hasPermission("chat.multiline")) {
            msg = msg.replace(Config.Chat.newLineCharacter, "\n");
            if (Config.Chat.allowEmptyLines && plr.hasPermission("chat.emptylines")) {
                msg = msg.replace("\n\n", "\n \n");
                msg = msg.replace("\n\n", "\n \n");
            }
            for (char c : Config.Chat.colors.toCharArray()) {
                if (!plr.hasPermission("chat.color." + c)) continue;
                msg = msg.replace(Config.Chat.colorPrefix + c, "\u00a7" + c);
            }
        }
        if (Config.Chat.Long.enabled && plr.hasPermission("chat.longer")) {
            int limit = 100;
            for (Map.Entry<String, Integer> group : Config.Chat.Long.lengthLimit.entrySet()) {
                if (!plr.hasPermission("chat.longerlimit." + group.getKey()) || group.getValue() <= limit)
                    continue;
                limit = group.getValue();
            }
            ConfigData puffer = pf.getData("chat.puffer");
            if (puffered && puffer.stringData.length() + msg.length() < limit) {
                puffer.stringData = puffer.stringData + msg;
            } else {
                msg = puffer.stringData + msg;
                msg = msg.length() > limit ? msg.substring(0, limit) : msg;
                int id = msg.indexOf(" ");
                if (id == -1) {
                    id = msg.length();
                }
                if (msg.startsWith("/")) {
                    SU.sch.scheduleSyncDelayedTask(Main.pl, new AsyncCommandExecutionAvoid(msg, plr));
                } else {
                    plr.chat(msg);
                }
                puffer.stringData = "";
            }
        } else {
            int id = msg.indexOf(" ");
            if (id == -1) {
                id = msg.length();
            }
            if (msg.startsWith("/")) {
                SU.sch.scheduleSyncDelayedTask(Main.pl, new AsyncCommandExecutionAvoid(msg, plr));
            } else {
                plr.chat(msg);
            }
        }
    }

    public static class AsyncCommandExecutionAvoid
            implements Runnable {
        private final String msg;
        private final Player plr;

        public AsyncCommandExecutionAvoid(String msg, Player plr) {
            this.msg = msg;
            this.plr = plr;
        }

        @Override
        public void run() {
            this.plr.chat(this.msg);
        }
    }
}
