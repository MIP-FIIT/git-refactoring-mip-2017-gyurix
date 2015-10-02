 package gyurix.protocol.v1_8.inpackets;

 import gyurix.protocol.PacketInType;
 import gyurix.protocol.WrappedPacket;

 public class PacketPlayInSteerVehicle extends WrappedPacket
 {
   public float sideways;
   public float forward;
   public boolean jump;
   public boolean unmount;

     @Override
     public Object getVanillaPacket() {
         return PacketInType.SteerVehicle.newPacket(sideways,forward,jump,unmount);
     }

     @Override
     public void loadVanillaPacket(Object packet) {
         Object[] data=PacketInType.SteerVehicle.getPacketData(packet);
         sideways=(Float)data[0];
         forward=(Float)data[1];
         jump=(Boolean)data[2];
         unmount=(Boolean)data[3];
     }
 }