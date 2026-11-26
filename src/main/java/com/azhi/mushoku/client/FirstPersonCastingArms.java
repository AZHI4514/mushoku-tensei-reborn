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
        float bodyYaw = Mth.rotLerp(partial, player.yBodyRotO, player.yBodyRot);
        model.setupAnim(player, player.walkAnimation.position(partial), player.walkAnimation.speed(partial),
                player.tickCount + partial, 0, player.getViewXRot(partial));
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(player.getViewXRot(partial)));
        pose.mulPose(Axis.YP.rotationDegrees(player.getViewYRot(partial) - bodyYaw));
        pose.translate(0, -player.getEyeHeight() - (player.isCrouching() ? 0.125 * player.getScale() : 0), 0);
        float scale = (float) CastGeometry.PLAYER_MODEL_SCALE * player.getScale();
        pose.scale(-scale, -scale, scale);
        pose.translate(0, -CastGeometry.MODEL_ORIGIN, 0);
        // Foreshorten the first-person arms about the physical palm so the supporting
        // hand and both held items fit in the hand pass without moving the muzzle.
        PoseStack palmPose = new PoseStack();
        model.rightArm.translateAndRotate(palmPose);
        float palmX = player.getSkin().model() == PlayerSkin.Model.SLIM ? -0.5f : -1;
        Vector3f palm = palmPose.last().pose().transformPosition(new Vector3f(palmX / 16, 10f / 16, 0));
        pose.translate(palm.x, palm.y, palm.z);
        pose.mulPose(Axis.XP.rotationDegrees(58 * ClientChant.weight(player, partial)));
        pose.scale(0.55f, 0.55f, 0.55f);
        pose.translate(-palm.x, -palm.y, -palm.z);
        drawHand(event, player, model, HumanoidArm.RIGHT);
        drawHand(event, player, model, HumanoidArm.LEFT);
        pose.popPose();
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
