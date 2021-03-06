package WolfShotz.Wyrmroost.entities.dragon;

import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.ControlledAttackGoal;
import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.DefendHomeGoal;
import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.DragonBreedGoal;
import WolfShotz.Wyrmroost.entities.dragon.helpers.goals.MoveToHomeGoal;
import WolfShotz.Wyrmroost.entities.projectile.breath.FireBreathEntity;
import WolfShotz.Wyrmroost.entities.util.CommonGoalWrappers;
import WolfShotz.Wyrmroost.entities.util.EntityDataEntry;
import WolfShotz.Wyrmroost.entities.util.animation.Animation;
import WolfShotz.Wyrmroost.network.packets.AnimationPacket;
import WolfShotz.Wyrmroost.network.packets.KeybindPacket;
import WolfShotz.Wyrmroost.registry.WRItems;
import WolfShotz.Wyrmroost.registry.WRSounds;
import WolfShotz.Wyrmroost.util.Mafs;
import WolfShotz.Wyrmroost.util.TickFloat;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.Collection;

import static net.minecraft.entity.SharedMonsterAttributes.*;

public class RoyalRedEntity extends AbstractDragonEntity
{
    public static final Animation ROAR_ANIMATION = new Animation(70);
    public static final Animation SLAP_ATTACK_ANIMATION = new Animation(30);
    public static final Animation BITE_ATTACK_ANIMATION = new Animation(15);
    public static final DataParameter<Boolean> BREATHING_FIRE = EntityDataManager.createKey(RoyalRedEntity.class, DataSerializers.BOOLEAN);

    public final TickFloat flightTimer = new TickFloat().setLimit(0, 1);
    public final TickFloat sitTimer = new TickFloat().setLimit(0, 1);
    public final TickFloat breathTimer = new TickFloat().setLimit(0, 1);

    public RoyalRedEntity(EntityType<? extends AbstractDragonEntity> dragon, World world)
    {
        super(dragon, world);

        registerDataEntry("Gender", EntityDataEntry.BOOLEAN, GENDER, getRNG().nextBoolean());
        registerVariantData(0, true);
    }

    @Override
    protected void registerAttributes()
    {
        super.registerAttributes();

        getAttribute(MAX_HEALTH).setBaseValue(100d); // 50 hearts
        getAttribute(MOVEMENT_SPEED).setBaseValue(0.22d);
        getAttribute(KNOCKBACK_RESISTANCE).setBaseValue(10); // No Knockback
        getAttribute(FOLLOW_RANGE).setBaseValue(20d); // 20 blocks (?)
        getAttribute(ATTACK_KNOCKBACK).setBaseValue(2.25d); // normal * 2.25
        getAttributes().registerAttribute(ATTACK_DAMAGE).setBaseValue(10d); // 5 hearts
        getAttributes().registerAttribute(FLYING_SPEED).setBaseValue(0.0425d);
        getAttributes().registerAttribute(PROJECTILE_DAMAGE).setBaseValue(4d); // 2 hearts
    }

    @Override
    protected void registerData()
    {
        super.registerData();
        dataManager.register(BREATHING_FIRE, false);
    }

    @Override
    protected void registerGoals()
    {
        super.registerGoals();

        goalSelector.addGoal(4, new MoveToHomeGoal(this));
        goalSelector.addGoal(5, new ControlledAttackGoal(this, 1, true, 2.1, d ->
        {
        }));
        goalSelector.addGoal(6, CommonGoalWrappers.followOwner(this, 1.2d, 12f, 3f));
        goalSelector.addGoal(7, new DragonBreedGoal(this, true));
        goalSelector.addGoal(9, new WaterAvoidingRandomWalkingGoal(this, 1));
        goalSelector.addGoal(10, CommonGoalWrappers.lookAt(this, 10f));
        goalSelector.addGoal(11, new LookRandomlyGoal(this));

        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new DefendHomeGoal(this));
        targetSelector.addGoal(5, CommonGoalWrappers.nonTamedTarget(this, LivingEntity.class, false, true, e -> e instanceof PlayerEntity || e instanceof AnimalEntity));
        targetSelector.addGoal(4, new HurtByTargetGoal(this)
        {
            @Override
            public boolean shouldExecute() { return super.shouldExecute() && !isChild(); }
        });
    }

    @Override
    public void livingTick()
    {
        super.livingTick();
        flightTimer.add(isFlying()? 0.1f : -0.05f);
        sitTimer.add(isSitting()? 0.1f : -0.1f);
        sleepTimer.add(isSleeping()? 0.035f : -0.1f);
        breathTimer.add(isBreathingFire()? 0.15f : -0.2f);

        if (!world.isRemote)
        {
            if (isBreathingFire() && getControllingPlayer() == null && getAttackTarget() == null)
                setBreathingFire(false);

            if (breathTimer.get() == 1)
            {
                world.addEntity(new FireBreathEntity(this));
                if (ticksExisted % 10 == 0) playSound(WRSounds.ENTITY_ROYALRED_BREATH.get(), 1, 0.5f);
            }

            if (!world.isRemote && !isSleeping() && !isBreathingFire() && !isChild() && getRNG().nextInt(2250) == 0 && noActiveAnimation())
                AnimationPacket.send(this, ROAR_ANIMATION);
        }

        Animation anim = getAnimation();

        if (anim == ROAR_ANIMATION)
        {
            for (LivingEntity entity : getEntitiesNearby(10))
                if (isOnSameTeam(entity)) entity.addPotionEffect(new EffectInstance(Effects.STRENGTH, 60));
        }
        else if (anim == SLAP_ATTACK_ANIMATION && (getAnimationTick() == 10 || getAnimationTick() == 15))
            attackInFront(0.2);
        else if (anim == BITE_ATTACK_ANIMATION && getAnimationTick() == 4) attackInFront(-0.3);
    }

    @Override
    public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack stack)
    {
        if (super.playerInteraction(player, hand, stack)) return true;

        if (isTamed() && !isChild())
        {
            if (!world.isRemote) player.startRiding(this);
            return true;
        }

        return false;
    }

    @Override
    public void recievePassengerKeybind(int key, int mods, boolean pressed)
    {
        if (!noActiveAnimation()) return;

        if (key == KeybindPacket.MOUNT_KEY1 && pressed)
        {
            if ((mods & GLFW.GLFW_MOD_CONTROL) != 0) setAnimation(ROAR_ANIMATION);
            else meleeAttack();
        }

        if (key == KeybindPacket.MOUNT_KEY2) setBreathingFire(pressed);
    }

    public void meleeAttack()
    {
        if (world.isRemote) return;

        Animation anim = BITE_ATTACK_ANIMATION;
        if (rand.nextBoolean() && !isFlying()) anim = SLAP_ATTACK_ANIMATION;
        AnimationPacket.send(this, anim);
    }

    @Override
    public Vec3d getApproximateMouthPos()
    {
        Vec3d rot = new Vec3d(0, 0, (getWidth() / 2) + 3.5d).rotatePitch(-rotationPitch * Mafs.PI / 180f).rotateYaw(-rotationYaw * Mafs.PI / 180f);
        return rot.add(getPosX(), getPosYEye() - 1, getPosZ());
    }

    @Override
    public EntitySize getSize(Pose poseIn)
    {
        EntitySize size = getType().getSize().scale(getRenderScale());
        float heightFactor = isSleeping()? 0.5f : isSitting()? 0.9f : 1;
        return size.scale(1, heightFactor);
    }

    @Override
    protected float getStandingEyeHeight(Pose poseIn, EntitySize sizeIn) { return getHeight() * (isFlying()? 0.95f : 1.13f); }

    @Override
    protected boolean canBeRidden(Entity entityIn) { return isTamed(); }

    @Override
    protected boolean canFitPassenger(Entity passenger) { return getPassengers().size() < 2; }

    @Override
    public Vec3d getPassengerPosOffset(Entity entity, int index) { return new Vec3d(0, getHeight() * 0.95f, index == 0? 0.5f : -1); }

    @Override
    public float getRenderScale() { return isChild()? 0.3f : isMale()? 0.8f : 1f; }

    @Override
    public int getHorizontalFaceSpeed() { return isFlying()? 5 : 8; }

    @Override
    public int getSpecialChances() { return 0; }

    public boolean isBreathingFire() { return dataManager.get(BREATHING_FIRE); }

    public void setBreathingFire(boolean b) { dataManager.set(BREATHING_FIRE, b); }

    @Override
    public boolean isImmuneToArrows() { return true; }

    @Override
    public Collection<Item> getFoodItems() { return WRItems.Tags.MEATS.getAllElements(); }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() { return WRSounds.ENTITY_ROYALRED_IDLE.get(); }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) { return WRSounds.ENTITY_ROYALRED_HURT.get(); }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() { return WRSounds.ENTITY_ROYALRED_DEATH.get(); }

    @Override
    public Animation[] getAnimations() { return new Animation[] {NO_ANIMATION, ROAR_ANIMATION, SLAP_ATTACK_ANIMATION, BITE_ATTACK_ANIMATION}; }

    @Override
    public void setAnimation(Animation animation)
    {
        super.setAnimation(animation);
        if (animation == ROAR_ANIMATION) playSound(WRSounds.ENTITY_ROYALRED_ROAR.get(), 6, 1);
    }
}
