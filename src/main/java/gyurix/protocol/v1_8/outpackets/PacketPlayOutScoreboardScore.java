package gyurix.protocol.v1_8.outpackets;

import gyurix.protocol.PacketOutType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

import java.lang.reflect.Method;

/**
 * Created by GyuriX on 2015.10.17..
 */
public class PacketPlayOutScoreboardScore extends WrappedPacket{
    public String player;
    public String board;
    public int score;
    public ScoreAction action;

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.ScoreboardScore.newPacket(player,board,score,action.toVanillaScoreAction());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketOutType.ScoreboardScore.getPacketData(packet);
        player= (String) data[0];
        board= (String) data[1];
        score= (int) data[2];
        action=ScoreAction.valueOf(data[3].toString());
    }

    public enum ScoreAction{
        CHANGE,
        REMOVE;
        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayOutScoreboardScore$EnumScoreboardAction"), "valueOf",String.class);
        }

        public Object toVanillaScoreAction() {
            try {
                return valueOf.invoke(null, name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
