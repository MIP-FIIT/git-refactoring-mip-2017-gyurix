 package gyurix.protocol.v1_8.inpackets;

 import gyurix.protocol.PacketInType;
 import gyurix.protocol.WrappedPacket;
 import gyurix.protocol.utils.BlockLocation;
 import gyurix.protocol.utils.Direction;
 import gyurix.protocol.utils.ItemStackWrapper;

 public class PacketPlayInBlockPlace extends WrappedPacket
 {
   public BlockLocation location;
   public Direction face;
   public ItemStackWrapper itemStack;
   public float cursorX;
   public float cursorY;
   public float cursorZ;
   public long timestamp;
   @Override
   public Object getVanillaPacket() {
       return PacketInType.BlockPlace.newPacket(location.toVanillaBlockPosition(),face==null?255:face.ordinal(),
               itemStack==null?null:itemStack.toVanillaStack(),cursorX,cursorY,cursorZ,timestamp);
   }

   @Override
   public void loadVanillaPacket(Object packet) {
       Object[] data= PacketInType.BlockPlace.getPacketData(packet);
       location=new BlockLocation(data[0]);
       face=Direction.get((Integer) data[1]);
       itemStack=data[2]==null?null:new ItemStackWrapper(data[2]);
       cursorX=(Float)data[3];
       cursorY=(Float)data[4];
       cursorZ=(Float)data[5];
       timestamp=(Long)data[6];
   }
 }


