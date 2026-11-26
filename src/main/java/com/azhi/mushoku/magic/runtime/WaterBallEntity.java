package com.azhi.mushoku.magic.runtime;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.core.WaterBallFlight;
import com.azhi.mushoku.magic.core.CastGeometry;
import com.azhi.mushoku.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class WaterBallEntity extends Projectile implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.water_ball.idle");
    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private static final ResourceKey<DamageType> WATER = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, "water"));
    private double travelled;
    private int age;

    public WaterBallEntity(EntityType<? extends WaterBallEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "flight", 0, state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        // Projectile restores its owner; the renderer also needs the initial flight vector.
        setDeltaMovement(packet.getXa(), packet.getYa(), packet.getZa());
    }

    static void launch(ServerPlayer caster) {
        ServerLevel level = caster.serverLevel();
        var ball = new WaterBallEntity(ModEntities.WATER_BALL.get(), level);
        Vec3 forward = caster.getLookAngle();
        Vec3 eye = caster.getEyePosition();
        Vec3 hand = CastAnchor.hand(caster, 1, 1);
        Vec3 end = eye.add(forward.scale(WaterBallFlight.RANGE));
        Vec3 target = ball.nearestHit(level, eye, end, caster).getLocation();
        var direction = CastGeometry.aim(CastAnchor.vector(forward), CastAnchor.vector(target.subtract(hand)),
                eye.distanceTo(target));
        var obstruction = level.clip(new ClipContext(eye, hand,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster));
        ball.setOwner(caster);
        ball.setPos(obstruction.getLocation());
        ball.setDeltaMovement(new Vec3(direction.x(), direction.y(), direction.z()).scale(WaterBallFlight.SPEED));
        level.addFreshEntity(ball);
        if (obstruction.getType() == HitResult.Type.BLOCK) ball.impact(level, obstruction);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        if (WaterBallFlight.expired(++age)) {
            discard();
            return;
        }

        double step = WaterBallFlight.nextStep(travelled);
        Vec3 start = position();
        Vec3 motion = getDeltaMovement().normalize().scale(step);
        Vec3 end = start.add(motion);
        HitResult hit = nearestHit(level, start, end, getOwner());
        if (hit.getType() != HitResult.Type.MISS) {
            impact(level, hit);
            return;
        }

        setPos(end);
        travelled += motion.length();
        if (WaterBallFlight.remainingDistance(travelled) == 0) discard();
    }

    private HitResult nearestHit(ServerLevel level, Vec3 start, Vec3 end, Entity excluded) {
        HitResult nearest = level.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        double limit = nearest.getType() == HitResult.Type.MISS
                ? Double.POSITIVE_INFINITY : start.distanceToSqr(nearest.getLocation());
        // Pad only the broad phase so targets touching the endpoint are considered.
        AABB candidates = new AABB(start, end).inflate(1.0E-7);
        for (Entity target : level.getEntities(this, candidates, e -> e != excluded && canHitEntity(e))) {
            AABB box = target.getBoundingBox();
            Vec3 point = box.contains(start) ? start : box.clip(start, end).orElse(null);
            if (point == null && box.distanceToSqr(end) == 0) point = end;
            if (point == null) continue;
            double distance = start.distanceToSqr(point);
            if (distance < limit) {
                nearest = new EntityHitResult(target, point);
                limit = distance;
            }
        }
        return nearest;
    }

    private void impact(ServerLevel level, HitResult hit) {
        if (hit instanceof EntityHitResult entityHit) onHitEntity(entityHit);
        Vec3 point = hit.getLocation();
        level.sendParticles(ParticleTypes.SPLASH, point.x, point.y, point.z, 8, 0.08, 0.08, 0.08, 0.02);
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        var holder = registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(WATER);
        hit.getEntity().hurt(new DamageSource(holder, this, getOwner()), 6.0F);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("Travelled", travelled);
        tag.putInt("FlightAge", age);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        travelled = tag.getDouble("Travelled");
        age = tag.getInt("FlightAge");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}
}
