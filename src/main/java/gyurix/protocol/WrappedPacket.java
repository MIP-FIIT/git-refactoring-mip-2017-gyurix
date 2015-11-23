package gyurix.protocol;

public abstract class WrappedPacket {
    public abstract Object getVanillaPacket();

    public abstract void loadVanillaPacket(Object var1);
}

