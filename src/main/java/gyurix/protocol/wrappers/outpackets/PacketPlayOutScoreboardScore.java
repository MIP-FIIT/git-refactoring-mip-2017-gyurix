package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.utils.WrappedData;
import gyurix.protocol.wrappers.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayOutScoreboardScore
        extends WrappedPacket {
    public ScoreAction action;
    public String board;
    public String player;
    public int score;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.player, this.board, this.score, this.action.toNMS());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketOutType.ScoreboardScore.getPacketData(packet);
        this.player = (String) data[0];
        this.board = (String) data[1];
        this.score = (Integer) data[2];
        this.action = ScoreAction.valueOf(data[3].toString());
    }

    public enum ScoreAction implements WrappedData {
        CHANGE,
        REMOVE;

        private static final Method valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction"), "valueOf", String.class);

        ScoreAction() {
        }

        public Object toNMS() {
            try {
                return valueOf.invoke(null, this.name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

