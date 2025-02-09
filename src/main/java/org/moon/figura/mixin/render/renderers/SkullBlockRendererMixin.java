package org.moon.figura.mixin.render.renderers;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.moon.figura.avatars.Avatar;
import org.moon.figura.avatars.AvatarManager;
import org.moon.figura.trust.TrustContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin implements BlockEntityRenderer<SkullBlockEntity> {

    @Unique
    private static Avatar avatar;

    @Inject(at = @At("HEAD"), method = "renderSkull", cancellable = true)
    private static void renderSkull(Direction direction, float yaw, float animationProgress, PoseStack stack, MultiBufferSource bufferSource, int light, SkullModelBase model, RenderType renderLayer, CallbackInfo ci) {
        if (avatar == null || avatar.trust.get(TrustContainer.Trust.CUSTOM_HEADS) == 0)
            return;

        Avatar localAvatar = avatar;
        avatar = null;
        //render skull :3
        if (localAvatar.skullRender(stack, bufferSource, light, direction, yaw))
            ci.cancel();
    }

    @Inject(at=@At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/blockentity/SkullBlockRenderer;renderSkull(Lnet/minecraft/core/Direction;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/SkullModelBase;Lnet/minecraft/client/renderer/RenderType;)V"), method = "render(Lnet/minecraft/world/level/block/entity/SkullBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V")
    public void render(SkullBlockEntity skullBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
        if (avatar == null || avatar.trust.get(TrustContainer.Trust.CUSTOM_HEADS) == 0)
            return;

        avatar.skullRenderEvent(skullBlockEntity, f);
    }

    @Override
    public boolean shouldRenderOffScreen(SkullBlockEntity blockEntity) {
        return true;
    }

    @Inject(at = @At("HEAD"), method = "getRenderType")
    private static void getRenderType(SkullBlock.Type type, GameProfile profile, CallbackInfoReturnable<RenderType> cir) {
        avatar = null;

        if (profile != null && profile.getId() != null)
            avatar = AvatarManager.getAvatarForPlayer(profile.getId());
    }
}
