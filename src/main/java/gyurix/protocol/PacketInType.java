package gyurix.protocol;

import com.google.common.collect.Lists;
import gyurix.spigotlib.Main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;


public enum PacketInType {
    HandshakingInSetProtocol, LoginInEncryptionBegin, LoginInStart, Abilities, ArmAnimation, BlockDig, BlockPlace, Chat, ClientCommand, CloseWindow, CustomPayload,
    EnchantItem, EntityAction, Flying, HeldItemSlot, KeepAlive, Look, Position, RelPositionLook, ResourcePackStatus, SetCreativeSlot, Settings,
    Spectate, SteerVehicle, TabComplete, Transaction, UpdateSign, UseEntity, WindowClick, StatusInPing, StatusInStart;

    private static final HashMap<Class, PacketInType> packets = new HashMap();
    ArrayList<Field> fs=new ArrayList<Field>();
    Constructor emptyConst;

    PacketInType() {
    }

    public static void init() {
        for (PacketInType t : values()) {
            String name = t.name();
            try {
                String cln = "Packet" + ((name.startsWith("Login")) || (name.startsWith("Status")) || (name.startsWith("Handshaking")) ? name : "PlayIn" + name);
                Class cl = Reflection.getNMSClass(cln);
                packets.put(cl, t);
                t.emptyConst = cl.getConstructor();
                t.fs=new ArrayList<Field>();
                for (Field f : cl.getDeclaredFields()) {
                    if ((f.getModifiers()& Modifier.STATIC)==0){
                        f.setAccessible(true);
                        t.fs.add(f);
                    }

                }
            } catch (Throwable e) {
                Main.log.severe("In packet type " + name + " hasn't found!");
                e.printStackTrace();
            }
        }
    }

    public static PacketInType getType(Object packet) {
        Class cl=packet.getClass();
        String cn=cl.getName();
        while (cn.contains("$")){
            try{
                cl=Class.forName(cn.substring(0,cn.indexOf("$")));
                cn=cl.getName();
            }
            catch (ClassNotFoundException e){
                e.printStackTrace();
            }
        }
        return packets.get(cl);
    }

    public Object newPacket(Object... fields) {
        try {
            Object out = this.emptyConst.newInstance();
            fillPacket(out, fields);
            return out;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object[] getPacketData(Object packet) {
        Object[] out = new Object[fs.size()];
        try {
            for (int i = 0; i < fs.size(); i++) {
                out[i] = fs.get(i).get(packet);
            }
            return out;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fillPacket(Object packet, Object... fields) {
        ArrayList flist = Lists.newArrayList(fields);
        for (Field f : this.fs) {
            for (int i = 0; i < flist.size(); i++) {
                try {
                    f.set(packet, flist.get(i));
                    flist.remove(i);
                    break;
                } catch (Throwable e) {

                }
            }
        }
    }
}