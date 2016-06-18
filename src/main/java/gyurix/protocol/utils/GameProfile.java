package gyurix.protocol.utils;

import com.google.common.collect.Multimap;
import gyurix.configfile.ConfigSerialization.ConfigOptions;
import gyurix.json.JsonAPI;
import gyurix.json.JsonSettings;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.12.25..
 */
public class GameProfile implements WrappedData {
    @JsonSettings(serialize = false)
    @ConfigOptions(serialize = false)
    private static final Class cl = Reflection.getUtilClass("com.mojang.authlib.GameProfile");
    @JsonSettings(serialize = false)
    @ConfigOptions(serialize = false)
    private static final Constructor con = Reflection.getConstructor(cl, UUID.class, String.class);
    @JsonSettings(serialize = false)
    @ConfigOptions(serialize = false)
    private static final Field fid = Reflection.getField(cl, "id"),
            fname = Reflection.getField(cl, "name"),
            fproperties = Reflection.getField(cl, "properties"),
            flegacy = Reflection.getField(cl, "legacy");
    public boolean demo;
    public UUID id;
    public boolean legacy;
    public String name;
    public ArrayList<Property> properties = new ArrayList<>();

    public GameProfile() {

    }

    public GameProfile(String n) {
        name = n;
        id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + n).getBytes());
    }
    public GameProfile(String n, UUID uid) {
        name = n;
        id = uid;
    }

    public GameProfile(Object nmsProfile) {
        try {
            id = (UUID) fid.get(nmsProfile);
            name = (String) fname.get(nmsProfile);
            legacy = flegacy.getBoolean(nmsProfile);
            try {
                for (Object obj : ((Multimap) fproperties.get(nmsProfile)).values()) {
                    properties.add(new Property(obj));
                }
            } catch (Throwable e) {
            }

        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public Object toNMS() {
        try {
            Object o = con.newInstance(id, name);
            flegacy.set(o, legacy);
            try {
                Multimap m = (Multimap) fproperties.get(o);
                for (Property p : properties)
                    m.put(p.name, p.toNMS());
            } catch (Throwable e) {
            }
            return o;
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
            return null;
        }
    }

    @Override
    public String toString() {
        return JsonAPI.serialize(this);
    }


    public static class Property implements WrappedData {
        @JsonSettings(serialize = false)
        @ConfigOptions(serialize = false)
        private static final Class cl = Reflection.getClass("com.mojang.authlib.properties.Property");
        @JsonSettings(serialize = false)
        @ConfigOptions(serialize = false)
        private static final Constructor con = Reflection.getConstructor(cl, String.class, String.class, String.class);
        @JsonSettings(serialize = false)
        @ConfigOptions(serialize = false)
        private static final Field fname = Reflection.getField(cl, "name"),
                fvalue = Reflection.getField(cl, "value"),
                fsignature = Reflection.getField(cl, "signature");
        public String name;
        public String signature;
        public String value;


        public Property(Object o) {
            try {
                name = (String) fname.get(o);
                value = (String) fvalue.get(o);
                signature = (String) fsignature.get(o);
            } catch (Throwable e) {
            }
        }

        public Property(String name, String value, String signature) {
            this.name = name;
            this.value = value;
            this.signature = signature;
        }

        public Object toNMS() {
            try {
                return con.newInstance(name, value, signature);
            } catch (Throwable e) {
                SU.error(SU.cs, e, "SpigotLib", "gyurix");
                return null;
            }
        }
    }
}