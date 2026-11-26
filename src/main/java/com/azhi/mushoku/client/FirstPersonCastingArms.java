package com.azhi.mushoku.client;

import com.azhi.mushoku.Mushoku;
import com.azhi.mushoku.magic.core.CastGeometry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.joml.Vector3f;

@EventBusSubscriber(modid = Mushoku.MOD_ID, value = Dist.CLIENT)
public final class FirstPersonCastingArms {
    private static PoseStack lastPass;

    private FirstPersonCastingArms() {}

    @SubscribeEvent
    public static void render(RenderHandEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        float partial = event.getPartialTick();
        if (player == null || ClientChant.weight(player, partial) <= 0) return;
        event.setCanceled(true);
        PoseStack pose = event.getPoseStack();
        // Vanilla may select either or both hands. Draw the pair once per hand pass.
        if (lastPass == pose) return;
        lastPass = pose;
        var renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().<AbstractClientPlayer>getRenderer(player);
        var model = renderer.getModel();
        model.setupAnim(player, player.walkAnimation.position(partial), player.walkAnimation.speed(partial),
                player.tickCount + partial, 0, player.getViewXRot(partial));
        pose.pushPose();
        // Undo ItemInHandRenderer's turn lag in reverse order. Keep camera bob and hurt transforms.
        float lagYaw = player.getViewYRot(partial) - Mth.lerp(partial, player.yBobO, player.yBob);
        float lagPitch = player.getViewXRot(partial) - Mth.lerp(partial, player.xBobO, player.xBob);
        pose.mulPose(Axis.YP.rotationDegrees(-lagYaw * 0.1f));
        pose.mulPose(Axis.XP.rotationDegrees(-lagPitch * 0.1f));
        Vector3f target = cameraPalm(player, partial);
        Vector3f palm = modelPalm(model, player);
        pose.translate(target.x, target.y, target.z);
        float scale = (float) CastGeometry.PLAYER_MODEL_SCALE * 0.68f;
        pose.scale(-scale, -scale, scale);
        // The shoulders extend towards the near, lower edges of the hand pass.
        pose.mulPose(Axis.XP.rotationDegrees(-25));
        pose.translate(-palm.x, -palm.y, -palm.z);
        drawHand(event, player, model, HumanoidArm.RIGHT);
        drawHand(event, player, model, HumanoidArm.LEFT);
        pose.popPose();
    }

    static Vector3f cameraPalm(AbstractClientPlayer player, float partial) {
        // Camera-space grip for the hand pass. Both endpoints lie in front of the near plane
        // throughout lift and release, so even an immediate launch has a visible origin.
        float weight = ClientChant.weight(player, partial);
        return new Vector3f(Mth.lerp(weight, 0.28f, 0.13f), Mth.lerp(weight, -0.34f, -0.25f), -0.60f);
    }

    private static Vector3f modelPalm(PlayerModel<?> model, AbstractClientPlayer player) {
        PoseStack palmPose = new PoseStack();
        model.rightArm.translateAndRotate(palmPose);
        float palmX = player.getSkin().model() == PlayerSkin.Model.SLIM ? -0.5f : -1;
        return palmPose.last().pose().transformPosition(new Vector3f(palmX / 16, 10f / 16, 0));
    }

    private static void drawHand(RenderHandEvent event, AbstractClientPlayer player,
                                 PlayerModel<?> model, HumanoidArm side) {
        var pose = event.getPoseStack();
        var buffers = event.getMultiBufferSource();
        boolean left = side == HumanoidArm.LEFT;
        var texture = player.getSkin().texture();
        var arm = left ? model.leftArm : model.rightArm;
        var sleeve = left ? model.leftSleeve : model.rightSleeve;
        if (!player.isInvisible()) {
            arm.render(pose, buffers.getBuffer(RenderType.entitySolid(texture)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            if (player.isModelPartShown(left ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE)) {
                sleeve.render(pose, buffers.getBuffer(RenderType.entityTranslucent(texture)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
            }
        }
        var stack = side == player.getMainArm() ? player.getMainHandItem() : player.getOffhandItem();
        if (stack.isEmpty()) return;
        pose.pushPose();
        model.translateToHand(side, pose);
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate((left ? -1 : 1) / 16.0, 0.125, -0.625);
        var context = left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderItem(player, stack, context, left,
                pose, buffers, event.getPackedLight());
        pose.popPose();
    }
}
