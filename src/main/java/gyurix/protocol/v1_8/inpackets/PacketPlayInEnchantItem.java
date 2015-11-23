package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInEnchantItem
        extends WrappedPacket {
    public int window;
    public int enchantment;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.EnchantItem.newPacket(this.window, this.enchantment);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data = PacketInType.EnchantItem.getPacketData(packet);
        this.window = (Integer) data[0];
        this.enchantment = (Integer) data[1];
    }
}

