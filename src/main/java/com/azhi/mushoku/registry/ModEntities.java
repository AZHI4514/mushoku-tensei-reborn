package com.azhi.mushoku.registry;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private static final String WATER_BALL_ID = "water_ball";
    public static final DeferredRegister<EntityType<?>> TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Mushoku.MOD_ID);
    public static final DeferredHolder<EntityType<?>, EntityType<WaterBallEntity>> WATER_BALL =
            TYPES.register(WATER_BALL_ID, () -> EntityType.Builder
                    .<WaterBallEntity>of(WaterBallEntity::new, MobCategory.MISC)
                    .clientTrackingRange(4) // chunks
                    .updateInterval(1)
                    .sized(EntityType.SNOWBALL.getWidth(), EntityType.SNOWBALL.getHeight())
                    .build(Mushoku.MOD_ID + ":" + WATER_BALL_ID));

    private ModEntities() {}
}
