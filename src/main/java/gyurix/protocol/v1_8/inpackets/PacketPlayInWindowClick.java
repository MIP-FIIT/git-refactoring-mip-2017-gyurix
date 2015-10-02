package gyurix.protocol.v1_8.inpackets;

public class PacketPlayInWindowClick{

}
/*    *//*
 package net.minecraft.server.v1_8_R2;
*/
/*    *//*

*/
/*    *//*
 import java.io.IOException;
*/
/*    *//*

*/
/*    *//*
 public class PacketPlayInWindowClick
*/
/*    *//*
   implements Packet<PacketListenerPlayIn>
*/
/*    *//*
 {
*/
/*    *//*
   private int a;
*/
/*    *//*
   private int slot;
*/
/*    *//*
   private int button;
*/
/*    *//*
   private short d;
*/
/*    *//*
   private ItemStack item;
*/
/*    *//*
   private int shift;
*/
/*    *//*

*/
/*    *//*
   public void a(PacketListenerPlayIn ☃)
*/
/*    *//*
   {
*/
/* 31 *//*
     ☃.a(this);
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public void a(PacketDataSerializer ☃) throws IOException
*/
/*    *//*
   {
*/
/* 36 *//*
     this.a = ☃.readByte();
*/
/* 37 *//*
     this.slot = ☃.readShort();
*/
/* 38 *//*
     this.button = ☃.readByte();
*/
/* 39 *//*
     this.d = ☃.readShort();
*/
/* 40 *//*
     this.shift = ☃.readByte();
*/
/*    *//*

*/
/* 42 *//*
     this.item = ☃.i();
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public void b(PacketDataSerializer ☃) throws IOException
*/
/*    *//*
   {
*/
/* 47 *//*
     ☃.writeByte(this.a);
*/
/* 48 *//*
     ☃.writeShort(this.slot);
*/
/* 49 *//*
     ☃.writeByte(this.button);
*/
/* 50 *//*
     ☃.writeShort(this.d);
*/
/* 51 *//*
     ☃.writeByte(this.shift);
*/
/*    *//*

*/
/* 53 *//*
     ☃.a(this.item);
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public int a() {
*/
/* 57 *//*
     return this.a;
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public int b() {
*/
/* 61 *//*
     return this.slot;
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public int c() {
*/
/* 65 *//*
     return this.button;
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public short d() {
*/
/* 69 *//*
     return this.d;
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public ItemStack e() {
*/
/* 73 *//*
     return this.item;
*/
/*    *//*
   }
*/
/*    *//*

*/
/*    *//*
   public int f() {
*/
/* 77 *//*
     return this.shift;
*/
/*    *//*
   }
*/
/*    *//*
 }

*/
