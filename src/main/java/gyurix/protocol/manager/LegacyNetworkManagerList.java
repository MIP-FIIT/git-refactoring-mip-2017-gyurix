package gyurix.protocol.manager;

import gyurix.protocol.manager.ProtocolLegacyImpl.NewChannelHandler;
import gyurix.spigotlib.SU;
import net.minecraft.util.io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LegacyNetworkManagerList extends ArrayList {
    public static ConcurrentHashMap<String, Channel> lastChannel = new ConcurrentHashMap<>();

    public LegacyNetworkManagerList(List backup) {
        super(backup);
    }

    @Override
    public boolean add(Object o) {
        try {
            lastChannel.remove(Thread.currentThread().getName()).pipeline().addLast("SpigotLib", new NewChannelHandler());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        synchronized (this) {
            return super.add(o);
        }
    }
}
