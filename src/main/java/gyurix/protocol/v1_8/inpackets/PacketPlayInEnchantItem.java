package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

public class PacketPlayInEnchantItem extends WrappedPacket{
    public int window,enchantment;
    @Override
    public Object getVanillaPacket() {
        return PacketInType.EnchantItem.newPacket(window,enchantment);
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.EnchantItem.getPacketData(packet);
        window=(Integer)data[0];
        enchantment=(Integer)data[1];
    }
}