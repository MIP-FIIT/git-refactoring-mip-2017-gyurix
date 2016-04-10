package gyurix.protocol.event;

import com.google.common.collect.Lists;
import gyurix.protocol.Reflection;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.Config;
import gyurix.spigotlib.Main;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.ServerVersion;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public enum PacketOutType {
    Abilities,
    Animation,
    AttachEntity,
    Bed,
    Boss(ServerVersion.v1_9),
    BlockAction,
    BlockBreakAnimation,
    BlockChange,
    Camera,
    Chat,
    CloseWindow,
    Collect,
    CombatEvent,
    CustomPayload,
    Entity,
    EntityDestroy,
    EntityEffect,
    EntityEquipment,
    EntityHeadRotation,
    EntityLook,
    EntityMetadata,
    EntityStatus,
    EntityTeleport,
    EntityVelocity,
    Experience,
    Explosion,
    GameStateChange,
    HeldItemSlot,
    KeepAlive,
    KickDisconnect,
    Login,
    LoginOutDisconnect,
    LoginOutEncryptionBegin,
    LoginOutSetCompression,
    LoginOutSuccess,
    Map,
    MapChunk,
    MapChunkBulk(ServerVersion.v1_8),
    Mount(ServerVersion.v1_9),
    MultiBlockChange,
    NamedEntitySpawn,
    NamedSoundEffect,
    OpenSignEditor,
    OpenWindow,
    PlayerInfo,
    PlayerListHeaderFooter,
    Position,
    RelEntityMove,
    RelEntityMoveLook,
    RemoveEntityEffect,
    ResourcePackSend,
    Respawn,
    ScoreboardDisplayObjective,
    ScoreboardObjective,
    ScoreboardScore,
    ScoreboardTeam,
    ServerDifficulty,
    SetCompression(ServerVersion.v1_8),
    SetCooldown(ServerVersion.v1_9),
    SetSlot,
    SpawnEntity,
    SpawnEntityExperienceOrb,
    SpawnEntityLiving,
    SpawnEntityPainting,
    SpawnEntityWeather,
    SpawnPosition,
    Statistic,
    TabComplete,
    TileEntityData,
    Title,
    Transaction,
    UnloadChunk(ServerVersion.v1_9),
    UpdateAttributes,
    UpdateEntityNBT(ServerVersion.v1_8),
    UpdateHealth,
    UpdateSign,
    UpdateTime,
    VehicleMove(ServerVersion.v1_9),
    WindowData,
    WindowItems,
    WorldBorder,
    WorldEvent,
    WorldParticles,
    StatusOutPong,
    StatusOutServerInfo;

    private static final HashMap<Class, PacketOutType> packets = new HashMap<>();
    public Class<? extends WrappedPacket> wrapper;
    ArrayList<Field> fs;
    private Constructor emptyConst;
    private ArrayList<ServerVersion> versions;

    PacketOutType() {

    }

    PacketOutType(ServerVersion... ver) {
        versions = Lists.newArrayList(ver);
    }

    /**
     * Get the type of an outgoing packet
     *
     * @param packet - The outgoing packet
     * @return The type of the given packet
     */
    public static PacketOutType getType(Object packet) {
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

    /**
     * Initializes the PacketOutType, DO NOT USE THIS METHOD
     */
    public static void init() {
        StringBuilder sb = new StringBuilder();
        sb.append("§e-------->  §4[§cPacketAPI - PacketOutType INIT§4]§e");
        ArrayList<PacketOutType> nowrapper = new ArrayList<>();
        ArrayList<PacketOutType> notfound = new ArrayList<>();
        for (PacketOutType t : PacketOutType.values()) {
            String name = t.name();
            String cln = "Packet" + (name.startsWith("LoginOut") || name.startsWith("Status") ? name : "PlayOut" + name);
            if (t.versions == null || t.versions.contains(Reflection.ver)) {
                try {
                    Class cl = Reflection.getNMSClass(cln);
                    packets.put(cl, t);
                    t.emptyConst = cl.getConstructor();
                    t.fs = new ArrayList();
                    for (Field f : cl.getDeclaredFields()) {
                        if ((f.getModifiers() & 8) != 0) continue;
                        f.setAccessible(true);
                        t.fs.add(f);
                    }
                } catch (Throwable e) {
                    notfound.add(t);
                }
                try {
                    t.wrapper = (Class<? extends WrappedPacket>) Class.forName("gyurix.protocol.wrappers.outpackets." + cln);
                } catch (Throwable e) {
                    nowrapper.add(t);
                }
            }
        }
        if (notfound.size() > 0)
            sb.append("\n§cNot found OUT packets (please report to the dev):§f " + StringUtils.join(notfound, ", "));
        else
            sb.append("\n§aFound every supported packets (no errors)");
        if (nowrapper.size() > 0)
            sb.append("\n§eMissing OUT packet wrappers (will be coded later):§f " + StringUtils.join(nowrapper, ", "));
        else
            sb.append("\n§aFound wrappers for all the IN packet types (that's awesome)");
        sb.append("\n§e-------------------------------------------------------------------------------");
        SU.cs.sendMessage(sb.toString());
    }

    /**
     * Fills the given packet with the given data
     *
     * @param packet - The fillable packet
     * @param data   - The filling data
     */
    public void fillPacket(Object packet, Object... data) {
        ArrayList<Field> fields = Lists.newArrayList(fs);
        for (Object d : data) {
            for (int f = 0; f < fields.size(); f++) {
                try {
                    Field ff = fields.get(f);
                    ff.set(packet, d);
                    fields.remove(f--);
                    break;
                } catch (Throwable e) {
                    if (Config.debug)
                        SU.error(SU.cs, e, "SpigotLib", "gyurix");
                }
            }
        }
    }

    /**
     * Returns the packet data of a packet
     *
     * @param packet - The packet
     * @return The contents of all the non static fields of the packet
     */
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

    /**
     * Tells if this packet is supported or not by the current server version.
     *
     * @return True if it is supported, false otherwise
     */
    public boolean isSupported() {
        return versions == null || versions.contains(Reflection.ver);
    }

    /**
     * Creates a new packet of this type and fills its fields with the given data
     *
     * @param data - Data to fill packet fields with
     * @return The crafted packet
     */
    public Object newPacket(Object... data) {
        try {
            Object out = this.emptyConst.newInstance();
            this.fillPacket(out, data);
            return out;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the wrapper of the given NMS packet.
     *
     * @param nmsPacket - The NMS packet
     * @return The wrapper of the given NMS packet
     */
    public WrappedPacket wrap(Object nmsPacket) {
        try {
            WrappedPacket wp = wrapper.newInstance();
            wp.loadVanillaPacket(nmsPacket);
            return wp;
        } catch (Throwable e) {
            SU.log(Main.pl, "§4[§cPacketAPI§4] §eError on wrapping §c" + name() + "§e out packet.");
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }
}

