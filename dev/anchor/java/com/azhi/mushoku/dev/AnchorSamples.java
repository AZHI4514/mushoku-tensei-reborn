package com.azhi.mushoku.dev;

import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class AnchorSamples {
    static final Logger LOG = LogUtils.getLogger();

    public static void note(Player p, String event) {
        LOG.info("ANCHOR {} player={} tick={} pos={}", event, p.getScoreboardName(), p.level().getGameTime(), p.position());
    }

    public static void particle(Player p, Vec3 hand, Level level, ParticleOptions particle,
                                double x, double y, double z, double vx, double vy, double vz) {
        LOG.info("ANCHOR particle player={} tick={} main={} xyz={} H={}", p.getScoreboardName(),
                level.getGameTime(), p.getMainArm(), new Vec3(x, y, z), hand);
        level.addParticle(particle, x, y, z, vx, vy, vz);
    }

    public static void launch(Player p, Vec3 hand, Vec3 target, WaterBallEntity ball) {
        LOG.info("ANCHOR launch player={} tick={} main={} id={} E={} H={} T={} pos={} dir={}",
                p.getScoreboardName(), p.level().getGameTime(), p.getMainArm(), ball.getUUID(),
                p.getEyePosition(), hand, target, ball.position(), ball.getDeltaMovement().normalize());
    }

    public static void impact(WaterBallEntity ball, Vec3 point) {
        LOG.info("ANCHOR impact id={} P={}", ball.getUUID(), point);
    }
}
