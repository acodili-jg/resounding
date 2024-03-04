package dev.thedocruby.resounding.mixin;

import dev.thedocruby.resounding.Engine;
import dev.thedocruby.resounding.raycast.Renderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(DebugRenderer.class)
public class RendererMixin {
    @Inject(
        method = """
            render(
                Lnet/minecraft/client/util/math/MatrixStack;
                Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;
                D
                D
                D
            )V
        """,
        at = @At("HEAD")
    )
    private void onDrawBlockOutline(
        final MatrixStack matrices,
        final VertexConsumerProvider.Immediate vertexConsumers,
        final double cameraX,
        final double cameraY,
        final double cameraZ,
        final CallbackInfo callback
    ) {
        if (Engine.on) {
            Renderer.renderRays(cameraX, cameraY, cameraZ, MinecraftClient.getInstance().world);
        }
    }
}
