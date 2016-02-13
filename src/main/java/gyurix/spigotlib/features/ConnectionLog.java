package gyurix.spigotlib.features;

import com.google.common.collect.Lists;
import gyurix.protocol.PacketInEvent;
import gyurix.protocol.Protocol;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.SU;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by gyurix on 24/11/2015.
 */
public class ConnectionLog implements Protocol.PacketInListener {
    public static PrintWriter logFile;

    @Override
    public void onPacketIN(PacketInEvent e) {
        if (!Config.ConnectionLog.enabled)
            return;
        String ip = ((InetSocketAddress) e.getChannel().remoteAddress()).getAddress().toString().substring(1);
        if (Config.ConnectionLog.blacklist.contains(ip))
            return;
        boolean login = e.getPacketData()[3].toString().equals("LOGIN");
        String time = new SimpleDateFormat(Config.ConnectionLog.time).format(System.currentTimeMillis()) + " ";
        ArrayList<String> uuids = Lists.newArrayList(SU.pf.getString(ip).split(" "));
        if (uuids == null || uuids.isEmpty()) {
            SU.cs.sendMessage(time + (login ? Config.ConnectionLog.loginunknown : Config.ConnectionLog.pingunknown).replace("<ip>", ip));
            return;
        }
        ArrayList<String> list = new ArrayList<>();
        String n = "?";
        for (String id : uuids) {
            n = "?";
            try {
                n = Bukkit.getOfflinePlayer(UUID.fromString(id)).getName();
            } catch (Throwable err) {
            }
            list.add(n + "(" + id + ")");
        }
        if (list.size() == 1) {
            logFile.println(time + (login ? Config.ConnectionLog.login : Config.ConnectionLog.ping).replace("<ip>", ip).replace("<uuid>", "" + uuids.iterator().next()).replace("<name>", n));
            return;
        }
        logFile.println(time + (login ? Config.ConnectionLog.loginmore : Config.ConnectionLog.pingmore).replace("<ip>", ip) + StringUtils.join(list, ", "));
    }
}
