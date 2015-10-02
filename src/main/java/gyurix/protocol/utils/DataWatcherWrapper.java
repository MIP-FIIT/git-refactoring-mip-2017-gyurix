package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import org.bukkit.DyeColor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author GyuriX
 *
 * The wrapper class of the NMS DataWatcher class.
 */
public class DataWatcherWrapper {
    public HashMap<Integer,Object> data=new HashMap<Integer, Object>();
    private static Field watchableDataObject,dataWatcherEntity,watchableObjectMap;
    private static Constructor watchableObjectConstructor;
    static {
        Class dw= Reflection.getNMSClass("DataWatcher");
        Class wo= Reflection.getNMSClass("DataWatcher$WatchableObject");
        watchableObjectMap=Reflection.getFirstFieldOfType(dw,Map.class,"java.lang.Integer",wo.getName());
        dataWatcherEntity=Reflection.getFirstFieldOfType(dw,Reflection.getNMSClass("Entity"));
        watchableDataObject=Reflection.getFirstFieldOfType(wo,Object.class);
        watchableObjectConstructor=wo.getConstructors()[0];
    }
    //Hook
    public DataWatcherWrapper(Object vanillaWrapper){

    }
    //Basic getters
    public byte getByte(int id){
        return (Byte)data.get(id);
    }
    public short getShort(int id){
        return (Short)data.get(id);
    }
    public int getInt(int id){
        return (Integer)data.get(id);
    }
    public float getFloat(int id){
        return (Byte)data.get(id);
    }
    public String getString(int id){
        return (String)data.get(id);
    }
    public ItemStackWrapper getItemStack(int id){
        return (ItemStackWrapper)data.get(id);
    }
    public BlockLocation getBlockLocation(int id){
        return (BlockLocation)data.get(id);
    }
    public Rotation getRotation(int id){
        return (Rotation)data.get(id);
    }

    //Entity getters
    public byte getStatus(){
        return getByte(0);
    }
    public boolean isOnFire(){
        byte b=getByte(0);
        return (b&1)==1;
    }
    public boolean isCrouched(){
        byte b=getByte(0);
        return (b&2)==2;
    }
    public boolean isSprinting(){
        byte b=getByte(0);
        return (b&8)==8;
    }
    public boolean isRightClicking(){
        byte b=getByte(0);
        return (b&16)==16;
    }
    public boolean isInvisible(){
        byte b=getByte(0);
        return (b&32)==32;
    }
    public short getAir(){
        return getShort(1);
    }

    //Entity setters
    public void setStatus(byte status){
        data.put(0,status);
    }
    public void setOnFire(boolean onFire){
        byte b=getByte(0);
        data.put(0,(byte)(onFire?b|1:b-(b&1)));
    }
    public void setCrouched(boolean crouched){
        byte b=getByte(0);
        data.put(0,(byte)(crouched?b|2:b-(b&2)));
    }
    public void setSprinting(boolean sprinting){
        byte b=getByte(0);
        data.put(0,(byte)(sprinting?b|8:b-(b&8)));
    }
    public void setRightClicking(boolean rightClicking){
        byte b=getByte(0);
        data.put(0,(byte)(rightClicking?b|16:b-(b&16)));
    }
    public void setInvisible(boolean invisible){
        byte b=getByte(0);
        data.put(0,(byte)(invisible?b|32:b-(b&32)));
    }
    public void setAir(short air){
        data.put(1,air);
    }


    //Living Entity getters
    public String getCustomName(){
        return (String) data.get(2);
    }
    public boolean hasCustomName(){
        return ((Byte)data.get(3))!=0;
    }
    public float getHealth(){
        return (Float)data.get(6);
    }
    public int getPotionEffectColor(){
        return (Integer)data.get(7);
    }
    public boolean isPotionEffectAmbient(){
        return ((Byte)data.get(8))!=0;
    }
    public byte getArrowCount(){
        return (Byte)data.get(9);
    }
    public boolean hasNoAI(){
        return ((Byte)data.get(15))!=0;
    }

    //Living Entity setters
    public void setCustomName(String customName){
        data.put(2, customName);
    }
    public void setCustomNameVisible(boolean visible){
        data.put(3, (byte) (visible ? 1 : 0));
    }
    public void setHealth(float health){
        data.put(6,health);
    }
    public void setPotionEffectColor(int color){
        data.put(7,color);
    }
    public void setPotionEffectAmbient(boolean ambient){
        data.put(8,(byte)(ambient?1:0));
    }
    public void setArrowCount(byte arrowCount){
        data.put(9,arrowCount);
    }
    public void setNoAI(boolean noAI){
        data.put(15,(byte)(noAI?1:0));
    }

    //Ageable
    public byte getAge(){
        return (Byte)data.get(12);
    }
    public void setAge(byte age){
        data.put(12,age);
    }

    //ArmourStand getters
    public byte getArmourStandFlags(){
        return (Byte) data.get(10);
    }
    public boolean isArmourStandSmall(){
        return (getArmourStandFlags()&1)==1;
    }
    public boolean hasArmourStandGravity(){
        return (getArmourStandFlags()&2)==2;
    }
    public boolean hasArmourStandArms(){
        return (getArmourStandFlags()&4)==4;
    }
    public boolean hasArmourStandBasePlate(){
        return (getArmourStandFlags()&8)==0;
    }
    public boolean hasArmourStandBoundingBox(){
        return (getArmourStandFlags()&16)==0;
    }
    public Rotation getArmourStandHeadRotation(){
        return (Rotation)data.get(11);
    }
    public Rotation getArmourStandBodyRotation(){
        return (Rotation)data.get(12);
    }
    public Rotation getArmourStandLeftArmRotation(){
        return (Rotation)data.get(13);
    }
    public Rotation getArmourStandRightArmRotation(){
        return (Rotation)data.get(14);
    }
    public Rotation getArmourStandLeftLegRotation(){
        return (Rotation)data.get(15);
    }
    public Rotation getArmourStandRightLegRotation(){
        return (Rotation)data.get(16);
    }

    //ArmourStand setters
    public void setArmourStandFlags(byte flags){
        data.put(10, flags);
    }
    public void setArmourStandSmall(boolean small){
        byte b=(Byte)data.get(10);
        data.put(10,(byte)(small?b-(b&1):b|1));
    }
    public void setArmourStandGravity(boolean gravity){
        byte b=(Byte)data.get(10);
        data.put(10,(byte)(gravity?b-(b&1):b|1));
    }
    public void setArmourStandArms(boolean arms){
        byte b=(Byte)data.get(10);
        data.put(10,(byte)(arms?b-(b&1):b|1));
    }
    public void setArmourStandBasePlate(boolean basePlate){
        byte b=(Byte)data.get(10);
        data.put(10,(byte)(basePlate?b|1:b-(b&1)));
    }
    public void setArmourStandBoundingBox(boolean boundingBox){
        byte b=(Byte)data.get(10);
        data.put(10,(byte)(boundingBox?b|1:b-(b&1)));
    }
    public void setArmourStandHeadRotation(Rotation headRotation){
        data.put(11, headRotation);
    }
    public void setArmourStandBodyRotation(Rotation bodyRotation){
        data.put(12,bodyRotation);
    }
    public void setArmourStandLeftArmRotation(Rotation leftArmRotation){
        data.put(13,leftArmRotation);
    }
    public void setArmourStandRightArmRotation(Rotation rightArmRotation){
        data.put(14,rightArmRotation);
    }
    public void setArmourStandLeftLegRotation(Rotation leftLegRotation){
        data.put(15,leftLegRotation);
    }
    public void setArmourStandRightLegRotation(Rotation rightLegRotation){
        data.put(16,rightLegRotation);
    }

    //Human getters
    public byte getHumanSkinFlags(){
        return (Byte)data.get(10);
    }
    public byte getHumanHiddenSkinParts(){
        return (Byte)data.get(16);
    }
    public boolean isHumanCapeHidden(){
        return (getHumanHiddenSkinParts()&2)==2;
    }
    public float getHumanAbsorptionHearts(){
        return (Float)data.get(17);
    }
    public int getHumanScore(){
        return (Integer)data.get(18);
    }

    //Human setters
    public void setHumanSkinFlags(byte skinParts){
        data.put(10, skinParts);
    }
    public void setHumanHiddenSkinParts(byte hiddenSkinParts){
        data.put(16, hiddenSkinParts);
    }
    public void setHumanCapeHidden(boolean hideCape){
        byte b=getHumanHiddenSkinParts();
        data.put(16, hideCape ? b | 2 : b - (b & 2));
    }
    public void setHumanAbsorptionHearts(float absorptionHearts){
        data.put(17, absorptionHearts);
    }
    public void setHumanScore(int score){
        data.put(18, score);
    }

    //Firework getters
    public ItemStackWrapper getFireworkInfo() {
        return (ItemStackWrapper) data.get(8);
    }
    //Firework setters
    public void setFireworkInfo(ItemStackWrapper info){
        data.put(8,info);
    }

    //ItemFrame getters
    public ItemStackWrapper getItemFrameItem(){
        return (ItemStackWrapper)data.get(8);
    }
    public byte getItemFrameRotation(){
        return (Byte)data.get(9);
    }
    //ItemFrame setters
    public void setItemFrameItem(ItemStackWrapper item){
        data.put(8,item);
    }
    public void setItemFrameRotation(byte rotation){
        data.put(9, rotation);
    }

    //EnderCrystal getters
    public int getEnderCrystalHealth(){
        return (Integer)data.get(8);
    }
    //EnderCrystal setters
    public void setEnderCrystalHealth(int enderCrystalHealth){
        data.put(8,enderCrystalHealth);
    }

    //Bat getters
    public boolean isBatHanging(){
        return ((Byte)data.get(16))!=0;
    }
    //Bat setters
    public void setBatHanging(boolean hanging){
        data.put(16,(byte)(hanging?1:0));
    }

    //Tameable getters
    public byte getTameableFlags(){
        return (Byte)data.get(16);
    }
    public boolean isTameableSitting(){
        return (getTameableFlags()&1)==1;
    }
    public boolean isTameableTame(){
        return (getTameableFlags()&4)==4;
    }
    public String getTameableOwner(){
        return (String)data.get(17);
    }
    //Tameable setters
    public void setTameableFlags(byte flags){
        data.put(16,flags);
    }
    public void setTameableSitting(boolean sitting){
        byte b=getTameableFlags();
        data.put(16,(byte)(sitting?b|1:b-(b&1)));
    }
    public void setTameableTame(boolean tame){
        byte b=getTameableFlags();
        data.put(16,(byte)(tame?b|4:b-(b&4)));
    }
    public void setTameableOwner(String owner){
        data.put(17,owner);
    }

    //Ocelot getters
    public byte getOcelotType(){
        return (Byte)data.get(18);
    }
    //Ocelot setters
    public void setOcelotType(byte ocelotType){
        data.put(18,ocelotType);
    }

    //Wolf getters
    public byte getWolfFlags(){
        return (Byte)data.get(16);
    }
    public boolean isWolfAngry(){
        return (getWolfFlags()&2)==2;
    }
    public float getWolfHealth(){
        return (Float)data.get(18);
    }
    public boolean isWolfBegging(){
        return ((Byte)data.get(19))!=0;
    }
    public DyeColor getWolfCollarColor(){
        return DyeColor.values()[(Byte)data.get(20)];
    }
    //Wolf setters
    public void setWolfFlags(byte wolfFlags){
        data.put(16,wolfFlags);
    }
    public void setWolfAngry(boolean angry){
        byte b=(Byte)data.get(16);
        data.put(16,(byte)(angry?b|2:b-(b&2)));
    }
    public void setWolfHealth(float wolfHealth){
        data.put(18,wolfHealth);
    }
    public void setWolfBegging(boolean wolfBegging){
        data.put(19,(byte)(wolfBegging?1:0));
    }
    public void setWolfCollarColor(DyeColor wolfCollarColor){
        data.put(20,(byte)wolfCollarColor.ordinal());
    }

    //Pig getters
    public boolean hasPigSaddle(){
        return ((Byte)data.get(16))!=0;
    }
    //Pig setters
    public void setPigSaddle(boolean hasSaddle){
        data.put(16,(byte)(hasSaddle?1:0));
    }

    //Rabbit getters
    public byte getRabbitType(){
        return (Byte)data.get(18);
    }
    //Rabbit setters
    public void setRabbitType(byte rabbitType){
        data.put(18,rabbitType);
    }

    //Sheep getters
    public byte getSheepData(){
        return (Byte)data.get(16);
    }
    public DyeColor getSheepColor(){
        return DyeColor.values()[getSheepData()%16];
    }
    public boolean isSheepSheared(){
        return (getSheepData()&16)==16;
    }

    //Sheep setters
    public void setSheepData(byte sheepData){
        data.put(16,sheepData);
    }
    public void setSheepSheared(boolean sheared){
        byte b=getSheepData();
        data.put(16,(byte)(sheared?b|16:b-(b&16)));
    }
    public void setSheepColor(DyeColor dc){
        byte b=getSheepData();
        data.put(16,(byte)(b-(b%16)+dc.ordinal()));
    }

    //Villager getters
    public enum VillagerType{Farmer,Librarian,Priest,Blacksmith,Butcher}
    public VillagerType getVillagerType(){
        return VillagerType.values()[(Integer)data.get(16)];
    }

    //Villager setters
    public void setVillagerType(VillagerType type){
        data.put(16,type.ordinal());
    }

    //Enderman getters
    public short getEndermanCarriedBlock(){
        return (Short)data.get(16);
    }
    public byte getEndermanCarriedBlockData(){
        return (Byte)data.get(17);
    }
    public boolean isEndermanScreaming(){
        return ((Byte)data.get(18))!=0;
    }
    //Enderman setters
    public void setEndermanCarriedBlock(short carriedBlock){
        data.put(16,carriedBlock);
    }
    public void setEndermanCarriedBlockData(byte carriedBlockData){
        data.put(17,carriedBlockData);
    }
    public void setEndermanScreaming(boolean screaming){
        data.put(18,(byte)(screaming?1:0));
    }

    //Zombie getters
    public boolean isZombieChild(){
        return ((Byte)data.get(12))!=0;
    }
    public boolean isZombieVillager(){
        return ((Byte)data.get(13))!=0;
    }
    public boolean isZombieConvering(){
        return ((Byte)data.get(14))!=0;
    }

    //Zombie setters
    public void setZombieChild(boolean isChild){
        data.put(12,(byte)(isChild?1:0));
    }
    public void setZombieVillager(boolean isVillager){
        data.put(13,(byte)(isVillager?1:0));
    }
    public void setZombieConvering(boolean isConvering){
        data.put(14,(byte)(isConvering?1:0));
    }

    //Blaze getters
    public boolean isBlazeOnFire(){
        return ((Byte)data.get(16))!=0;
    }
    //Blaze setters
    public void setBlazeOnFire(boolean onFire){
        data.put(16,(byte)(onFire?1:0));
    }

    //Spider getters
    public boolean isSpiderClimbing(){
        return ((Byte)data.get(16))!=0;
    }
    //Spiger setters
    public void setSpiderClimbing(boolean climbing){
        data.put(16,(byte)(climbing?1:0));
    }

    //Creeper getters
    public byte getCreeperState(){
        return (Byte)data.get(16);
    }
    public boolean isCreeperInFuse(){
        return getCreeperState()!=-1;
    }
    public boolean isCreeperPowered(){
        return ((Byte)data.get(17))!=0;
    }
    //Creeper setters
    public void setCreeperState(byte state){
        data.put(16,state);
    }
    public void setCreeperInFuse(boolean inFuse){
        data.put(16,(byte)(inFuse?1:-1));
    }
    public void setCreeperPowered(boolean isPowered){
        data.put(17,(byte)(isPowered?1:0));
    }

    //Ghast getters
    public boolean isGhastAttacking(){
        return ((Byte)data.get(16))!=0;
    }
    //Ghast setters
    public void setGhastAttacking(boolean attacking){
        data.put(16, (byte)(attacking?1:0));
    }

    //Slime getters
    public byte getSize(){
        return (Byte)data.get(16);
    }
    //Slime setters
    public void setSize(byte size){
        data.put(16,size);
    }

    //Skeleton getters
    public boolean isSkeletonWither(){
        return ((Byte)data.get(13))==1;
    }
    //Skeleton setters
    public void setSkeletonWither(boolean wither){
        data.put(13,(byte)(wither?1:0));
    }

    //Witch getters
    public boolean isWitchAgressive(){
        return ((Byte)data.get(21))!=0;
    }
    //Witch setters
    public void setWitchAgressive(boolean isAgressive){
        data.put(21,(byte)(isAgressive?1:0));
    }

    //IronGolem getters
    public boolean isIronGolemPlayerCreated(){
        return ((Byte)data.get(16))!=0;
    }
    //IronGolem setters
    public void setIronGolemPlayerCreated(boolean isPlayerCreated){
        data.put(16,(byte)(isPlayerCreated?1:0));
    }

    //Wither getters
    public int getWitherWatchedTargetEntityId(int targetNumber){
        if (targetNumber<1||targetNumber>3)
            throw new IllegalArgumentException("Wither target number must be between 1(inclusively) and 3(inclusively)");
        return (Integer)data.get(16+targetNumber);
    }
    public int getWitherInvulnerableTime(){
        return (Integer)data.get(20);
    }
    //Wither setters
    public void setWitherWatchedTargetEntityId(int targetNumber,int targetEntityId){
        if (targetNumber<1||targetNumber>3)
            throw new IllegalArgumentException("Wither target number must be between 1(inclusively) and 3(inclusively)");
        data.put(16+targetNumber,targetEntityId);
    }
    public void setWitherInvulnerableTime(int invulnerableTime){
        data.put(20,invulnerableTime);
    }

    //Guardian getters
    public byte getGuardianIsElderly(){
        return (Byte)data.get(16);
    }
    public int getGuardianTargetEntityID(){
        return (Integer)data.get(17);
    }
    //Guardian setters
    public void setGuardianIsElderly(byte isElderly){
        data.put(16,isElderly);
    }
    public void setGuardianTargetEntityID(int targetEntityID){
        data.put(17,targetEntityID);
    }

    //Boat getters
    public int getBoatTimeSinceHit(){
        return (Integer)data.get(17);
    }
    public int getBoatForwardDirection(){
        return (Integer)data.get(18);

    }
    public float getBoatDamageTaken(){
        return (Float)data.get(19);
    }
    //Boat setters
    public void setBoatTimeSinceHit(int timeSinceHit){
        data.put(17,timeSinceHit);
    }
    public void setBoatForwardDirection(int forwardDirection){
        data.put(18,forwardDirection);
    }
    public void setBoatDamageTaken(float damageTaken){
        data.put(19,damageTaken);
    }


    //Minecart getters
    public boolean MinecartIsPowered(){
        return ((Byte)data.get(16))!=0;
    }
    //Minecart setters
    public void setMinecartIsPowered(boolean minecartIsPowered){
        data.put(16, minecartIsPowered?1:0);
    }

    //FurnaceMinecart getters
    public boolean FuranceMinecartIsPowered() {
        return ((Byte)data.get(16))!=0;
    }
    //FurnaceMinecart setters
    public void setFurnaceMinecartIsPowered(boolean furnaceMinecartIsPowered){
        data.put(16, furnaceMinecartIsPowered?1:0);
    }

    //Item getters
    public ItemStackWrapper getItem() {
        return (ItemStackWrapper) data.get(10);
    }
    //Item setters
    public void setItem(ItemStackWrapper item){
        data.put(10,item);
    }

    //Arrow getters
    public byte getArrowIsCritical(){
        return (Byte)data.get(16);
    }
    //Arrow setters
    public void setArrowIsCrutical(byte arrowIsCrutical){
        data.put(16, arrowIsCrutical);
    }
}
