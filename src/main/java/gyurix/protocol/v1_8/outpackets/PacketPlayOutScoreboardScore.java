package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayOutScoreboardScore
        extends WrappedPacket {
    public String player;
    public String board;
    public int score;
    public ScoreAction action;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.ScoreboardScore.newPacket(this.player, this.board, this.score, this.action.toVanillaScoreAction());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketOutType.ScoreboardScore.getPacketData(packet);
        this.player = (String) data[0];
        this.board = (String) data[1];
        this.score = (Integer) data[2];
        this.action = ScoreAction.valueOf(data[3].toString());
    }

    public enum ScoreAction {
        CHANGE,
        REMOVE;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction"), "valueOf", String.class);
        }

        ScoreAction() {
        }

        public Object toVanillaScoreAction() {
            try {
                return valueOf.invoke(null, this.name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

