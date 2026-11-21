package com.azhi.mushoku.gametest;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Mushoku.MOD_ID)
@PrefixGameTestTemplate(false)
public class WaterBallRegistryGameTests {
    @GameTest(template = "gametest_empty")
    public static void resolvesEntityFactoryAndProjectileDamage(GameTestHelper helper) {
        var id = ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, "water_ball");
        var type = BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElseThrow();
        helper.assertTrue(type.create(helper.getLevel()) instanceof WaterBallEntity, "Expected water ball factory");
        var water = ResourceKey.create(Registries.DAMAGE_TYPE,
                ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, "water"));
        var damage = helper.getLevel().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(water);
        helper.assertTrue(damage.is(DamageTypeTags.IS_PROJECTILE), "Expected water in minecraft:is_projectile");
        helper.succeed();
    }
}
