 package gyurix.protocol.v1_8.inpackets;

 import gyurix.protocol.PacketInType;
import gyurix.protocol.WrappedPacket;

 public class PacketPlayInChat extends WrappedPacket{
     public String message;
     @Override
     public Object getVanillaPacket() {
         return PacketInType.Chat.newPacket(message);
     }

     @Override
     public void loadVanillaPacket(Object packet) {
         message= (String) PacketInType.Chat.getPacketData(packet)[0];
     }
 }