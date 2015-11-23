package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.Reflection;
import gyurix.protocol.WrappedPacket;

import java.lang.reflect.Method;

public class PacketPlayInClientCommand
        extends WrappedPacket {
    public ClientCommand command;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.ClientCommand.newPacket(this.command.toVanillaClientCommand());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        this.command = ClientCommand.valueOf(PacketInType.ClientCommand.getPacketData(packet)[0].toString());
    }

    public enum ClientCommand {
        PERFORM_RESPAWN,
        REQUEST_STATS,
        OPEN_INVENTORY_ACHIEVEMENT;

        private static final Method valueOf;

        static {
            valueOf = Reflection.getMethod(Reflection.getNMSClass("PacketPlayInClientCommand$EnumClientCommand"), "valueOf", String.class);
        }

        ClientCommand() {
        }

        public Object toVanillaClientCommand() {
            try {
                return valueOf.invoke(null, this.name());
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}

