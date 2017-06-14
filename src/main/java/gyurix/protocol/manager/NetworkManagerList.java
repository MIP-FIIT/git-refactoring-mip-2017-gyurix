package gyurix.protocol.manager;

import gyurix.protocol.manager.ProtocolImpl.NewChannelHandler;
import gyurix.spigotlib.SU;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by GyuriX, on 2017. 05. 16..
 */
public class NetworkManagerList extends ArrayList {
    public static ConcurrentHashMap<String, Channel> lastChannel = new ConcurrentHashMap<>();

    public NetworkManagerList(List backup) {
        super(backup);
    }

    @Override
    public boolean add(Object o) {
        try {
            lastChannel.remove(Thread.currentThread().getName()).pipeline().addLast("SpigotLib", new NewChannelHandler());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        return super.add(o);
    }
}
