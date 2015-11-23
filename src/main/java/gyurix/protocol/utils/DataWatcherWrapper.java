package gyurix.protocol.utils;

import gyurix.protocol.Reflection;
import org.bukkit.DyeColor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DataWatcherWrapper {
    private static Field watchableDataObject;
    private static Field dataWatcherEntity;
    private static Field watchableObjectMap;
    private static Constructor watchableObjectConstructor;

    static {
        Class dw = Reflection.getNMSClass("DataWatcher");
        Class wo = Reflection.getNMSClass("DataWatcher$WatchableObject");
        watchableObjectMap = Reflection.getFirstFieldOfType(dw, Map.class, "java.lang.Integer", wo.getName());
        dataWatcherEntity = Reflection.getFirstFieldOfType(dw, Reflection.getNMSClass("Entity"));
        watchableDataObject = Reflection.getFirstFieldOfType(wo, Object.class);
        watchableObjectConstructor = wo.getConstructors()[0];
    }

    public HashMap<Integer, Object> data = new HashMap();

    public DataWatcherWrapper(Object vanillaWrapper) {
    }

    public byte getByte(int id) {
        return ((Byte) this.data.get(id)).byteValue();
    }

    public short getShort(int id) {
        return (Short) this.data.get(id);
    }

    public int getInt(int id) {
        return (Integer) this.data.get(id);
    }

    public float getFloat(int id) {
        return ((Byte) this.data.get(id)).byteValue();
    }

    public String getString(int id) {
        return (String) this.data.get(id);
    }

    public ItemStackWrapper getItemStack(int id) {
        return (ItemStackWrapper) this.data.get(id);
    }

    public BlockLocation getBlockLocation(int id) {
        return (BlockLocation) this.data.get(id);
    }

    public Rotation getRotation(int id) {
        return (Rotation) this.data.get(id);
    }

    public byte getStatus() {
        return this.getByte(0);
    }

    public void setStatus(byte status) {
        this.data.put(0, Byte.valueOf(status));
    }

    public boolean isOnFire() {
        byte b = this.getByte(0);
        return (b & 1) == 1;
    }

    public void setOnFire(boolean onFire) {
        byte b = this.getByte(0);
        this.data.put(0, Byte.valueOf((byte) (onFire ? b | 1 : b - (b & 1))));
    }

    public boolean isCrouched() {
        byte b = this.getByte(0);
        return (b & 2) == 2;
    }

    public void setCrouched(boolean crouched) {
        byte b = this.getByte(0);
        this.data.put(0, Byte.valueOf((byte) (crouched ? b | 2 : b - (b & 2))));
    }

    public boolean isSprinting() {
        byte b = this.getByte(0);
        return (b & 8) == 8;
    }

    public void setSprinting(boolean sprinting) {
        byte b = this.getByte(0);
        this.data.put(0, Byte.valueOf((byte) (sprinting ? b | 8 : b - (b & 8))));
    }

    public boolean isRightClicking() {
        byte b = this.getByte(0);
        return (b & 16) == 16;
    }

    public void setRightClicking(boolean rightClicking) {
        byte b = this.getByte(0);
        this.data.put(0, Byte.valueOf((byte) (rightClicking ? b | 16 : b - (b & 16))));
    }

    public boolean isInvisible() {
        byte b = this.getByte(0);
        return (b & 32) == 32;
    }

    public void setInvisible(boolean invisible) {
        byte b = this.getByte(0);
        this.data.put(0, Byte.valueOf((byte) (invisible ? b | 32 : b - (b & 32))));
    }

    public short getAir() {
        return this.getShort(1);
    }

    public void setAir(short air) {
        this.data.put(1, air);
    }

    public String getCustomName() {
        return (String) this.data.get(2);
    }

    public void setCustomName(String customName) {
        this.data.put(2, customName);
    }

    public boolean hasCustomName() {
        return ((Byte) this.data.get(3)).byteValue() != 0;
    }

    public float getHealth() {
        return ((Float) this.data.get(6)).floatValue();
    }

    public void setHealth(float health) {
        this.data.put(6, Float.valueOf(health));
    }

    public int getPotionEffectColor() {
        return (Integer) this.data.get(7);
    }

    public void setPotionEffectColor(int color) {
        this.data.put(7, color);
    }

    public boolean isPotionEffectAmbient() {
        return ((Byte) this.data.get(8)).byteValue() != 0;
    }

    public void setPotionEffectAmbient(boolean ambient) {
        this.data.put(8, Byte.valueOf((byte) (ambient ? 1 : 0)));
    }

    public byte getArrowCount() {
        return ((Byte) this.data.get(9)).byteValue();
    }

    public void setArrowCount(byte arrowCount) {
        this.data.put(9, Byte.valueOf(arrowCount));
    }

    public boolean hasNoAI() {
        return ((Byte) this.data.get(15)).byteValue() != 0;
    }

    public void setCustomNameVisible(boolean visible) {
        this.data.put(3, Byte.valueOf((byte) (visible ? 1 : 0)));
    }

    public void setNoAI(boolean noAI) {
        this.data.put(15, Byte.valueOf((byte) (noAI ? 1 : 0)));
    }

    public byte getAge() {
        return ((Byte) this.data.get(12)).byteValue();
    }

    public void setAge(byte age) {
        this.data.put(12, Byte.valueOf(age));
    }

    public byte getArmourStandFlags() {
        return ((Byte) this.data.get(10)).byteValue();
    }

    public void setArmourStandFlags(byte flags) {
        this.data.put(10, Byte.valueOf(flags));
    }

    public boolean isArmourStandSmall() {
        return (this.getArmourStandFlags() & 1) == 1;
    }

    public void setArmourStandSmall(boolean small) {
        byte b = ((Byte) this.data.get(10)).byteValue();
        this.data.put(10, Byte.valueOf((byte) (small ? b - (b & 1) : b | 1)));
    }

    public boolean hasArmourStandGravity() {
        return (this.getArmourStandFlags() & 2) == 2;
    }

    public boolean hasArmourStandArms() {
        return (this.getArmourStandFlags() & 4) == 4;
    }

    public boolean hasArmourStandBasePlate() {
        return (this.getArmourStandFlags() & 8) == 0;
    }

    public boolean hasArmourStandBoundingBox() {
        return (this.getArmourStandFlags() & 16) == 0;
    }

    public Rotation getArmourStandHeadRotation() {
        return (Rotation) this.data.get(11);
    }

    public void setArmourStandHeadRotation(Rotation headRotation) {
        this.data.put(11, headRotation);
    }

    public Rotation getArmourStandBodyRotation() {
        return (Rotation) this.data.get(12);
    }

    public void setArmourStandBodyRotation(Rotation bodyRotation) {
        this.data.put(12, bodyRotation);
    }

    public Rotation getArmourStandLeftArmRotation() {
        return (Rotation) this.data.get(13);
    }

    public void setArmourStandLeftArmRotation(Rotation leftArmRotation) {
        this.data.put(13, leftArmRotation);
    }

    public Rotation getArmourStandRightArmRotation() {
        return (Rotation) this.data.get(14);
    }

    public void setArmourStandRightArmRotation(Rotation rightArmRotation) {
        this.data.put(14, rightArmRotation);
    }

    public Rotation getArmourStandLeftLegRotation() {
        return (Rotation) this.data.get(15);
    }

    public void setArmourStandLeftLegRotation(Rotation leftLegRotation) {
        this.data.put(15, leftLegRotation);
    }

    public Rotation getArmourStandRightLegRotation() {
        return (Rotation) this.data.get(16);
    }

    public void setArmourStandRightLegRotation(Rotation rightLegRotation) {
        this.data.put(16, rightLegRotation);
    }

    public void setArmourStandGravity(boolean gravity) {
        byte b = ((Byte) this.data.get(10)).byteValue();
        this.data.put(10, Byte.valueOf((byte) (gravity ? b - (b & 1) : b | 1)));
    }

    public void setArmourStandArms(boolean arms) {
        byte b = ((Byte) this.data.get(10)).byteValue();
        this.data.put(10, Byte.valueOf((byte) (arms ? b - (b & 1) : b | 1)));
    }

    public void setArmourStandBasePlate(boolean basePlate) {
        byte b = ((Byte) this.data.get(10)).byteValue();
        this.data.put(10, Byte.valueOf((byte) (basePlate ? b | 1 : b - (b & 1))));
    }

    public void setArmourStandBoundingBox(boolean boundingBox) {
        byte b = ((Byte) this.data.get(10)).byteValue();
        this.data.put(10, Byte.valueOf((byte) (boundingBox ? b | 1 : b - (b & 1))));
    }

    public byte getHumanSkinFlags() {
        return ((Byte) this.data.get(10)).byteValue();
    }

    public void setHumanSkinFlags(byte skinParts) {
        this.data.put(10, Byte.valueOf(skinParts));
    }

    public byte getHumanHiddenSkinParts() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setHumanHiddenSkinParts(byte hiddenSkinParts) {
        this.data.put(16, Byte.valueOf(hiddenSkinParts));
    }

    public boolean isHumanCapeHidden() {
        return (this.getHumanHiddenSkinParts() & 2) == 2;
    }

    public void setHumanCapeHidden(boolean hideCape) {
        byte b = this.getHumanHiddenSkinParts();
        this.data.put(16, hideCape ? b | 2 : b - (b & 2));
    }

    public float getHumanAbsorptionHearts() {
        return ((Float) this.data.get(17)).floatValue();
    }

    public void setHumanAbsorptionHearts(float absorptionHearts) {
        this.data.put(17, Float.valueOf(absorptionHearts));
    }

    public int getHumanScore() {
        return (Integer) this.data.get(18);
    }

    public void setHumanScore(int score) {
        this.data.put(18, score);
    }

    public ItemStackWrapper getFireworkInfo() {
        return (ItemStackWrapper) this.data.get(8);
    }

    public void setFireworkInfo(ItemStackWrapper info) {
        this.data.put(8, info);
    }

    public ItemStackWrapper getItemFrameItem() {
        return (ItemStackWrapper) this.data.get(8);
    }

    public void setItemFrameItem(ItemStackWrapper item) {
        this.data.put(8, item);
    }

    public byte getItemFrameRotation() {
        return ((Byte) this.data.get(9)).byteValue();
    }

    public void setItemFrameRotation(byte rotation) {
        this.data.put(9, Byte.valueOf(rotation));
    }

    public int getEnderCrystalHealth() {
        return (Integer) this.data.get(8);
    }

    public void setEnderCrystalHealth(int enderCrystalHealth) {
        this.data.put(8, enderCrystalHealth);
    }

    public boolean isBatHanging() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setBatHanging(boolean hanging) {
        this.data.put(16, Byte.valueOf((byte) (hanging ? 1 : 0)));
    }

    public byte getTameableFlags() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setTameableFlags(byte flags) {
        this.data.put(16, Byte.valueOf(flags));
    }

    public boolean isTameableSitting() {
        return (this.getTameableFlags() & 1) == 1;
    }

    public void setTameableSitting(boolean sitting) {
        byte b = this.getTameableFlags();
        this.data.put(16, Byte.valueOf((byte) (sitting ? b | 1 : b - (b & 1))));
    }

    public boolean isTameableTame() {
        return (this.getTameableFlags() & 4) == 4;
    }

    public void setTameableTame(boolean tame) {
        byte b = this.getTameableFlags();
        this.data.put(16, Byte.valueOf((byte) (tame ? b | 4 : b - (b & 4))));
    }

    public String getTameableOwner() {
        return (String) this.data.get(17);
    }

    public void setTameableOwner(String owner) {
        this.data.put(17, owner);
    }

    public byte getOcelotType() {
        return ((Byte) this.data.get(18)).byteValue();
    }

    public void setOcelotType(byte ocelotType) {
        this.data.put(18, Byte.valueOf(ocelotType));
    }

    public byte getWolfFlags() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setWolfFlags(byte wolfFlags) {
        this.data.put(16, Byte.valueOf(wolfFlags));
    }

    public boolean isWolfAngry() {
        return (this.getWolfFlags() & 2) == 2;
    }

    public void setWolfAngry(boolean angry) {
        byte b = ((Byte) this.data.get(16)).byteValue();
        this.data.put(16, Byte.valueOf((byte) (angry ? b | 2 : b - (b & 2))));
    }

    public float getWolfHealth() {
        return ((Float) this.data.get(18)).floatValue();
    }

    public void setWolfHealth(float wolfHealth) {
        this.data.put(18, Float.valueOf(wolfHealth));
    }

    public boolean isWolfBegging() {
        return ((Byte) this.data.get(19)).byteValue() != 0;
    }

    public void setWolfBegging(boolean wolfBegging) {
        this.data.put(19, Byte.valueOf((byte) (wolfBegging ? 1 : 0)));
    }

    public DyeColor getWolfCollarColor() {
        return DyeColor.values()[((Byte) this.data.get(20)).byteValue()];
    }

    public void setWolfCollarColor(DyeColor wolfCollarColor) {
        this.data.put(20, Byte.valueOf((byte) wolfCollarColor.ordinal()));
    }

    public boolean hasPigSaddle() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setPigSaddle(boolean hasSaddle) {
        this.data.put(16, Byte.valueOf((byte) (hasSaddle ? 1 : 0)));
    }

    public byte getRabbitType() {
        return ((Byte) this.data.get(18)).byteValue();
    }

    public void setRabbitType(byte rabbitType) {
        this.data.put(18, Byte.valueOf(rabbitType));
    }

    public byte getSheepData() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setSheepData(byte sheepData) {
        this.data.put(16, Byte.valueOf(sheepData));
    }

    public DyeColor getSheepColor() {
        return DyeColor.values()[this.getSheepData() % 16];
    }

    public void setSheepColor(DyeColor dc) {
        byte b = this.getSheepData();
        this.data.put(16, Byte.valueOf((byte) (b - b % 16 + dc.ordinal())));
    }

    public boolean isSheepSheared() {
        return (this.getSheepData() & 16) == 16;
    }

    public void setSheepSheared(boolean sheared) {
        byte b = this.getSheepData();
        this.data.put(16, Byte.valueOf((byte) (sheared ? b | 16 : b - (b & 16))));
    }

    public VillagerType getVillagerType() {
        return VillagerType.values()[(Integer) this.data.get(16)];
    }

    public void setVillagerType(VillagerType type) {
        this.data.put(16, type.ordinal());
    }

    public short getEndermanCarriedBlock() {
        return (Short) this.data.get(16);
    }

    public void setEndermanCarriedBlock(short carriedBlock) {
        this.data.put(16, carriedBlock);
    }

    public byte getEndermanCarriedBlockData() {
        return ((Byte) this.data.get(17)).byteValue();
    }

    public void setEndermanCarriedBlockData(byte carriedBlockData) {
        this.data.put(17, Byte.valueOf(carriedBlockData));
    }

    public boolean isEndermanScreaming() {
        return ((Byte) this.data.get(18)).byteValue() != 0;
    }

    public void setEndermanScreaming(boolean screaming) {
        this.data.put(18, Byte.valueOf((byte) (screaming ? 1 : 0)));
    }

    public boolean isZombieChild() {
        return ((Byte) this.data.get(12)).byteValue() != 0;
    }

    public void setZombieChild(boolean isChild) {
        this.data.put(12, Byte.valueOf((byte) (isChild ? 1 : 0)));
    }

    public boolean isZombieVillager() {
        return ((Byte) this.data.get(13)).byteValue() != 0;
    }

    public void setZombieVillager(boolean isVillager) {
        this.data.put(13, Byte.valueOf((byte) (isVillager ? 1 : 0)));
    }

    public boolean isZombieConvering() {
        return ((Byte) this.data.get(14)).byteValue() != 0;
    }

    public void setZombieConvering(boolean isConvering) {
        this.data.put(14, Byte.valueOf((byte) (isConvering ? 1 : 0)));
    }

    public boolean isBlazeOnFire() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setBlazeOnFire(boolean onFire) {
        this.data.put(16, Byte.valueOf((byte) (onFire ? 1 : 0)));
    }

    public boolean isSpiderClimbing() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setSpiderClimbing(boolean climbing) {
        this.data.put(16, Byte.valueOf((byte) (climbing ? 1 : 0)));
    }

    public byte getCreeperState() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setCreeperState(byte state) {
        this.data.put(16, Byte.valueOf(state));
    }

    public boolean isCreeperInFuse() {
        return this.getCreeperState() != -1;
    }

    public void setCreeperInFuse(boolean inFuse) {
        this.data.put(16, Byte.valueOf((byte) (inFuse ? 1 : -1)));
    }

    public boolean isCreeperPowered() {
        return ((Byte) this.data.get(17)).byteValue() != 0;
    }

    public void setCreeperPowered(boolean isPowered) {
        this.data.put(17, Byte.valueOf((byte) (isPowered ? 1 : 0)));
    }

    public boolean isGhastAttacking() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setGhastAttacking(boolean attacking) {
        this.data.put(16, Byte.valueOf((byte) (attacking ? 1 : 0)));
    }

    public byte getSize() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setSize(byte size) {
        this.data.put(16, Byte.valueOf(size));
    }

    public boolean isSkeletonWither() {
        return ((Byte) this.data.get(13)).byteValue() == 1;
    }

    public void setSkeletonWither(boolean wither) {
        this.data.put(13, Byte.valueOf((byte) (wither ? 1 : 0)));
    }

    public boolean isWitchAgressive() {
        return ((Byte) this.data.get(21)).byteValue() != 0;
    }

    public void setWitchAgressive(boolean isAgressive) {
        this.data.put(21, Byte.valueOf((byte) (isAgressive ? 1 : 0)));
    }

    public boolean isIronGolemPlayerCreated() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setIronGolemPlayerCreated(boolean isPlayerCreated) {
        this.data.put(16, Byte.valueOf((byte) (isPlayerCreated ? 1 : 0)));
    }

    public int getWitherWatchedTargetEntityId(int targetNumber) {
        if (targetNumber < 1 || targetNumber > 3) {
            throw new IllegalArgumentException("Wither target number must be between 1(inclusively) and 3(inclusively)");
        }
        return (Integer) this.data.get(16 + targetNumber);
    }

    public int getWitherInvulnerableTime() {
        return (Integer) this.data.get(20);
    }

    public void setWitherInvulnerableTime(int invulnerableTime) {
        this.data.put(20, invulnerableTime);
    }

    public void setWitherWatchedTargetEntityId(int targetNumber, int targetEntityId) {
        if (targetNumber < 1 || targetNumber > 3) {
            throw new IllegalArgumentException("Wither target number must be between 1(inclusively) and 3(inclusively)");
        }
        this.data.put(16 + targetNumber, targetEntityId);
    }

    public byte getGuardianIsElderly() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setGuardianIsElderly(byte isElderly) {
        this.data.put(16, Byte.valueOf(isElderly));
    }

    public int getGuardianTargetEntityID() {
        return (Integer) this.data.get(17);
    }

    public void setGuardianTargetEntityID(int targetEntityID) {
        this.data.put(17, targetEntityID);
    }

    public int getBoatTimeSinceHit() {
        return (Integer) this.data.get(17);
    }

    public void setBoatTimeSinceHit(int timeSinceHit) {
        this.data.put(17, timeSinceHit);
    }

    public int getBoatForwardDirection() {
        return (Integer) this.data.get(18);
    }

    public void setBoatForwardDirection(int forwardDirection) {
        this.data.put(18, forwardDirection);
    }

    public float getBoatDamageTaken() {
        return ((Float) this.data.get(19)).floatValue();
    }

    public void setBoatDamageTaken(float damageTaken) {
        this.data.put(19, Float.valueOf(damageTaken));
    }

    public boolean MinecartIsPowered() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setMinecartIsPowered(boolean minecartIsPowered) {
        this.data.put(16, minecartIsPowered ? 1 : 0);
    }

    public boolean FuranceMinecartIsPowered() {
        return ((Byte) this.data.get(16)).byteValue() != 0;
    }

    public void setFurnaceMinecartIsPowered(boolean furnaceMinecartIsPowered) {
        this.data.put(16, furnaceMinecartIsPowered ? 1 : 0);
    }

    public ItemStackWrapper getItem() {
        return (ItemStackWrapper) this.data.get(10);
    }

    public void setItem(ItemStackWrapper item) {
        this.data.put(10, item);
    }

    public byte getArrowIsCritical() {
        return ((Byte) this.data.get(16)).byteValue();
    }

    public void setArrowIsCrutical(byte arrowIsCrutical) {
        this.data.put(16, Byte.valueOf(arrowIsCrutical));
    }

    public enum VillagerType {
        Farmer,
        Librarian,
        Priest,
        Blacksmith,
        Butcher;


        VillagerType() {
        }
    }

}

