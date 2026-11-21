package com.azhi.mushoku.gametest;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import com.azhi.mushoku.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Mushoku.MOD_ID)
@PrefixGameTestTemplate(false)
public class WaterBallFlightGameTests {
    private static WaterBallEntity ball(GameTestHelper h, Vec3 origin, Vec3 motion) {
        var b = ModEntities.WATER_BALL.get().create(h.getLevel());
        b.setPos(origin);
        b.setDeltaMovement(motion);
        h.getLevel().addFreshEntity(b);
        return b;
    }

    private static Pig pig(GameTestHelper h, Vec3 pos) {
        var pig = EntityType.PIG.create(h.getLevel());
        pig.setPos(pos);
        pig.setNoAi(true);
        pig.setNoGravity(true);
        h.getLevel().addFreshEntity(pig);
        return pig;
    }

    @GameTest(template = "gametest_empty")
    public static void bothEndpointsOutsidePane(GameTestHelper h) {
        var block = new BlockPos(1, 1, 1);
        h.setBlock(block, Blocks.GLASS_PANE);
        Vec3 start = Vec3.atCenterOf(h.absolutePos(block)).add(-0.6, 0, 0);
        var target = pig(h, start.add(1.25, -0.25, 0));
        var b = ball(h, start, new Vec3(1.2, 0, 0));
        b.tick();
        h.assertTrue(b.isRemoved() && target.getHealth() == 10 && h.getBlockState(block).is(Blocks.GLASS_PANE),
                "Pane must stop shot before pig");
        h.succeed();
    }

    @GameTest(template = "gametest_empty")
    public static void rangeBoundary(GameTestHelper h) {
        Vec3 origin = Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 16, 1)));
        for (double distance : new double[] {23.99, 24, 24.01}) {
            var target = pig(h, origin.add(0, distance, 0));
            var b = ball(h, origin, new Vec3(0, 1.2, 0));
            for (int i = 0; i < 20 && !b.isRemoved(); i++) b.tick();
            h.assertTrue(b.isRemoved() && target.getHealth() == (distance <= 24 ? 4 : 10),
                    "Distance " + distance + " health " + target.getHealth() + " delta " + b.position().subtract(origin) + " removed " + b.isRemoved());
            target.discard();
        }
        h.succeed();
    }

    @GameTest(template = "gametest_empty")
    public static void stationaryExpiresOnFortiethWorldTick(GameTestHelper h) {
        var b = ball(h, Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 16, 1))), Vec3.ZERO);
        boolean[] observed39 = {false};
        h.onEachTick(() -> {
            // The level stops ticking discarded entities.
            if (b.tickCount == 39) observed39[0] = !b.isRemoved();
            if (b.isRemoved()) {
                h.assertTrue(observed39[0] && b.tickCount == 40, "Expired at tick " + b.tickCount);
                h.succeed();
            }
        });
    }

    @GameTest(template = "gametest_empty")
    public static void playerAttackLeavesFlightUnchanged(GameTestHelper h) {
        Vec3 origin = Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 16, 1)));
        Vec3 motion = new Vec3(0, 1.2, 0);
        var projectile = ball(h, origin, motion);
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        player.setPos(origin);
        player.attack(projectile);
        projectile.tick();
        h.assertTrue(!projectile.isRemoved() && projectile.getDeltaMovement().equals(motion)
                && projectile.position().distanceTo(origin.add(motion)) < 1e-8, "Attack altered flight");
        h.succeed();
    }

    @GameTest(template = "gametest_empty")
    public static void ignoresOwnerAndOnlyHitsFirstTarget(GameTestHelper h) {
        Vec3 origin = Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 16, 1)));
        var owner = pig(h, origin);
        var first = pig(h, origin.add(0, 1, 0));
        var second = pig(h, origin.add(0, 1.1, 0));
        var b = ball(h, origin, new Vec3(0, 1.2, 0));
        b.setOwner(owner);
        b.tick();
        h.assertTrue(owner.getHealth() == 10 && first.getHealth() == 4 && second.getHealth() == 10
                && b.isRemoved(), "Health " + owner.getHealth() + "/" + first.getHealth() + "/" + second.getHealth() + " removed " + b.isRemoved());
        h.assertTrue(first.getLastDamageSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
                && first.getLastDamageSource().getEntity() == owner, "Expected projectile tag and caster attribution");
        h.succeed();
    }
}
