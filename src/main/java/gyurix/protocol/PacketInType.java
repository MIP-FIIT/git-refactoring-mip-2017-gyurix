package gyurix.protocol;

import com.google.common.collect.Lists;
import gyurix.spigotlib.Main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public enum PacketInType {
    HandshakingInSetProtocol,
    LoginInEncryptionBegin,
    LoginInStart,
    Abilities,
    ArmAnimation,
    BlockDig,
    BlockPlace,
    Chat,
    ClientCommand,
    CloseWindow,
    CustomPayload,
    EnchantItem,
    EntityAction,
    Flying,
    HeldItemSlot,
    KeepAlive,
    Look,
    Position,
    RelPositionLook,
    ResourcePackStatus,
    SetCreativeSlot,
    Settings,
    Spectate,
    SteerVehicle,
    TabComplete,
    Transaction,
    UpdateSign,
    UseEntity,
    WindowClick,
    StatusInPing,
    StatusInStart;

    private static final HashMap<Class, PacketInType> packets;

    static {
        packets = new HashMap();
    }

    ArrayList<Field> fs = new ArrayList();
    Constructor emptyConst;

    PacketInType() {
    }

    public static void init() {
        for (PacketInType t : PacketInType.values()) {
            String name = t.name();
            try {
                String cln = "Packet" + (name.startsWith("Login") || name.startsWith("Status") || name.startsWith("Handshaking") ? name : "PlayIn" + name);
                Class cl = Reflection.getNMSClass(cln);
                packets.put(cl, t);
                t.emptyConst = cl.getConstructor();
                t.fs = new ArrayList();
                for (Field f : cl.getDeclaredFields()) {
                    if ((f.getModifiers() & 8) != 0) continue;
                    f.setAccessible(true);
                    t.fs.add(f);
                }
                continue;
            } catch (Throwable e) {
                Main.log.severe("In packet type " + name + " hasn't found!");
                e.printStackTrace();
            }
        }
    }

    public static PacketInType getType(Object packet) {
        Class cl = packet.getClass();
        String cn = cl.getName();
        while (cn.contains("$")) {
            try {
                cl = Class.forName(cn.substring(0, cn.indexOf("$")));
                cn = cl.getName();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return packets.get(cl);
    }

    public /* varargs */ Object newPacket(Object... fields) {
        try {
            Object out = this.emptyConst.newInstance();
            this.fillPacket(out, fields);
            return out;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object[] getPacketData(Object packet) {
        Object[] out = new Object[this.fs.size()];
        try {
            for (int i = 0; i < this.fs.size(); ++i) {
                out[i] = this.fs.get(i).get(packet);
            }
            return out;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public /* varargs */ void fillPacket(Object packet, Object... fields) {
        ArrayList flist = Lists.newArrayList((Object[]) fields);
        block2:
        for (Field f : this.fs) {
            for (int i = 0; i < flist.size(); ++i) {
                try {
                    f.set(packet, flist.get(i));
                    flist.remove(i);
                    continue block2;
                } catch (Throwable var7_7) {
                    continue;
                }
            }
        }
    }
}

