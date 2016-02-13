package gyurix.protocol.utils;

import gyurix.json.JsonAPI;
import gyurix.json.JsonSettings;
import gyurix.protocol.Reflection;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * Created by GyuriX on 2015.12.25..
 */
public class GameProfile {
    @JsonSettings(serialize = false)
    public static final Constructor vanillaConst;

    static {
        vanillaConst = Reflection.getConstructor(Reflection.getClass("com.mojang.authlib.GameProfile"), UUID.class, String.class);
    }

    public UUID id;
    public String name;
    public ArrayList<Property> properties = new ArrayList<>();
    public boolean legacy;
    public boolean demo;

    public GameProfile(Object o) {
        try {
            id = (UUID) o.getClass().getField("id").get(o);
            name = (String) o.getClass().getField("name").get(o);
            legacy = o.getClass().getField("legacy").getBoolean(o);

        } catch (Throwable e) {
        }
    }

    @Override
    public String toString() {
        return JsonAPI.serialize(this);
    }

    public Object toVanillaGameProfile() {
        try {
            Object o = vanillaConst.newInstance(id, name);
            o.getClass().getField("legacy").set(o, legacy);
            Map m = (Map) o.getClass().getField("properties").get(o);
            for (Property p : properties) {
                m.put(p.name, p.toVanillaProperty());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class Property {
        @JsonSettings(serialize = false)
        private static final Constructor vanillaConst;

        static {
            vanillaConst = Reflection.getConstructor(Reflection.getClass("com.mojang.authlib.properties.Property"), String.class, String.class);
        }

        public String name;
        public String value;
        public String signature;

        public Property(Object o) {
            try {
                name = (String) o.getClass().getField("name").get(o);
                value = (String) o.getClass().getField("value").get(o);
                signature = (String) o.getClass().getField("signature").get(o);
            } catch (Throwable e) {
            }
        }

        public Property(String name, String value, String signature) {
            this.name = name;
            this.value = value;
            this.signature = signature;
        }

        public Object toVanillaProperty() {
            try {
                return vanillaConst.newInstance(name, value, signature);
            } catch (Throwable e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}