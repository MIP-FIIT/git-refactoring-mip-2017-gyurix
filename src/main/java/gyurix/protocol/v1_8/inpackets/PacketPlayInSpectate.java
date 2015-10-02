 package gyurix.protocol.v1_8.inpackets;

 import gyurix.protocol.PacketInType;
 import gyurix.protocol.WrappedPacket;

 import java.util.UUID;

 public class PacketPlayInSpectate extends WrappedPacket
 {
   private UUID entityUUID;

   @Override
   public Object getVanillaPacket() {
     return PacketInType.Spectate.newPacket(entityUUID);
   }

   @Override
   public void loadVanillaPacket(Object packet) {
     entityUUID= (UUID) PacketInType.Spectate.getPacketData(packet)[0];
   }
 }