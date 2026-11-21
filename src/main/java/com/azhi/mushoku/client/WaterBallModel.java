package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.runtime.WaterBallEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

final class WaterBallModel extends GeoModel<WaterBallEntity> {
    private static final ResourceLocation GEOMETRY = asset("geo/water_ball.geo.json");
    private static final ResourceLocation TEXTURE = asset("textures/entity/water_ball.png");
    private static final ResourceLocation ANIMATION = asset("animations/water_ball.animation.json");

    private static ResourceLocation asset(String path) {
        return ResourceLocation.fromNamespaceAndPath(Mushoku.MOD_ID, path);
    }

    @Override
    public ResourceLocation getModelResource(WaterBallEntity ball) {
        return GEOMETRY;
    }

    @Override
    public ResourceLocation getTextureResource(WaterBallEntity ball) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WaterBallEntity ball) {
        return ANIMATION;
    }

    @Override
    public RenderType getRenderType(WaterBallEntity ball, ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
