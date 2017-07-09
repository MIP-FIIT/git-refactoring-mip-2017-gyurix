package gyurix.protocol.manager;

import gyurix.protocol.manager.ProtocolImpl.NewChannelHandler;
import gyurix.spigotlib.SU;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by GyuriX, on 2017. 05. 16..
 */
public class NetworkManagerList extends ArrayList {
    public static ConcurrentLinkedQueue<Channel> queue = new ConcurrentLinkedQueue<Channel>();

    public NetworkManagerList(List backup) {
        super(backup);
    }

    @Override
    public boolean add(Object o) {
        try {
            queue.poll().pipeline().addLast("SpigotLib", new NewChannelHandler());
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
        synchronized (this) {
            return super.add(o);
        }
    }
}
