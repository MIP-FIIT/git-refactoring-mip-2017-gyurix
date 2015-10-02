package gyurix.protocol;

import com.google.common.collect.Lists;
import gyurix.spigotlib.Main;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;


public enum PacketOutType {
    Abilities, Animation, AttachEntity, Bed, BlockAction, BlockBreakAnimation, BlockChange, Camera, Chat, CloseWindow, Collect, CombatEvent, CustomPayload, Entity,
    EntityDestroy, EntityEffect, EntityEquipment, EntityHeadRotation, EntityLook, EntityMetadata, EntityStatus, EntityTeleport, EntityVelocity,
    Experience, Explosion, GameStateChange, HeldItemSlot, KeepAlive, KickDisconnect, Login, LoginOutDisconnect, LoginOutEncryptionBegin,
    LoginOutSetCompression, LoginOutSuccess, Map, MapChunk, MapChunkBulk, MultiBlockChange,
    NamedEntitySpawn, NamedSoundEffect, OpenSignEditor, OpenWindow, PlayerInfo, PlayerListHeaderFooter, Position, RelEntityMove,
    RelEntityMoveLook, RemoveEntityEffect, ResourcePackSend, Respawn, ScoreboardDisplayObjective, ScoreboardObjective, ScoreboardScore,
    ScoreboardTeam, ServerDifficulty, SetCompression, SetSlot, SpawnEntity, SpawnEntityExperienceOrb, SpawnEntityLiving, SpawnEntityPainting,
    SpawnEntityWeather, SpawnPosition, Statistic, TabComplete, TileEntityData, Title, Transaction, UpdateAttributes, UpdateEntityNBT,
    UpdateHealth, UpdateSign, UpdateTime, WindowData, WindowItems, WorldBorder, WorldEvent, WorldParticles, StatusOutPong, StatusOutServerInfo;
    private static final HashMap<Class, PacketOutType> packets = new HashMap();
    ArrayList<Field> fs;
    Constructor emptyConst;

    PacketOutType() {
    }

    public static void init() {
        for (PacketOutType t : values()) {
            String name = t.name();
            try {
                String cln = "Packet" + ((name.startsWith("LoginOut")) || (name.startsWith("Status")) ? name : "PlayOut" + name);
                Class cl = Reflection.getNMSClass(cln);
                packets.put(cl, t);
                t.emptyConst=cl.getConstructor();
                t.fs=new ArrayList<Field>();
                for (Field f : cl.getDeclaredFields()) {
                    if ((f.getModifiers()& Modifier.STATIC)==0){
                        f.setAccessible(true);
                        t.fs.add(f);
                    }

                }
            } catch (Throwable e) {
                Main.log.severe("Out packet type " + name + " hasn't found!");
            }
        }
    }

    public static PacketOutType getType(Object packet) {
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
                    e.printStackTrace();
                }
            }
        }
    }
}



/* Location:           D:\pluginok\ServerLib\out\artifacts\SpigotLib.jar

 * Qualified Name:     gyurix.protocol.PacketOutType

 * JD-Core Version:    0.7.0.1

 */