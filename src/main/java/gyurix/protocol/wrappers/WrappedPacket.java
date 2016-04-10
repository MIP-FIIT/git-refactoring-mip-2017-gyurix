package gyurix.protocol.wrappers;

/**
 * Represents a wrapped (user friendly) form of a Vanilla/NMS packet.
 */
public abstract class WrappedPacket {
    /**
     * Converts this wrapped packet to a Vanilla/NMS packet
     *
     * @return The conversion result, NMS packet
     */
    public abstract Object getVanillaPacket();

    /**
     * Loads a Vanilla/NMS packet to this wrapper
     *
     * @param packet - The loadable packet
     */
    public abstract void loadVanillaPacket(Object packet);
}

