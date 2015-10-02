package gyurix.protocol.v1_8.inpackets;

public class PacketPlayInUseEntity{

}
/*
package net.minecraft.server.v1_8_R2;

import java.io.IOException;

public class PacketPlayInUseEntity
  implements Packet<PacketListenerPlayIn>
{
  private int a;
  private EnumEntityUseAction action;
  private Vec3D c;

  public void a(PacketDataSerializer ☃)
    throws IOException
  {
    this.a = ☃.e();
    this.action = ((EnumEntityUseAction)☃.a(EnumEntityUseAction.class));
    if (this.action == EnumEntityUseAction.INTERACT_AT)
      this.c = new Vec3D(☃.readFloat(), ☃.readFloat(), ☃.readFloat());
  }

  public void b(PacketDataSerializer ☃)
    throws IOException
  {
    ☃.b(this.a);
    ☃.a(this.action);
    if (this.action == EnumEntityUseAction.INTERACT_AT) {
      ☃.writeFloat((float)this.c.a);
      ☃.writeFloat((float)this.c.b);
      ☃.writeFloat((float)this.c.c);
    }
  }

  public void a(PacketListenerPlayIn ☃)
  {
    ☃.a(this);
  }

  public Entity a(World ☃) {
    return ☃.a(this.a);
  }

  public EnumEntityUseAction a() {
    return this.action;
  }

  public Vec3D b() {
    return this.c;
  }

  public static enum EnumEntityUseAction
  {
  }
}*/
