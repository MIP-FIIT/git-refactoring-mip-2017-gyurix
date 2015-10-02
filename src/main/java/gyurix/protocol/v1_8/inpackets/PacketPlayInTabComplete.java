package gyurix.protocol.v1_8.inpackets;

import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;
import gyurix.protocol.utils.BlockLocation;

public class PacketPlayInTabComplete extends WrappedPacket
{
  public String text;
  public BlockLocation block;

    @Override
    public Object getVanillaPacket() {
        return PacketInType.TabComplete.newPacket(text,block==null?null:block.toVanillaBlockPosition());
    }

    @Override
    public void loadVanillaPacket(Object packet) {
        Object[] data=PacketInType.TabComplete.getPacketData(packet);
        text=(String)data[0];
        block=data[1]==null?null:new BlockLocation(data[1]);
    }
}