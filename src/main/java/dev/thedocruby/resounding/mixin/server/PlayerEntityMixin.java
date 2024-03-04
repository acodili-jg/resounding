package dev.thedocruby.resounding.mixin.server;

import dev.thedocruby.resounding.Cache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Entity.class)
public class PlayerEntityMixin {
    @Shadow
    @SuppressWarnings("SameReturnValue")
    public double getEyeY() {
        return 0.0d;
    }

    @ModifyArg(
        method = "playSound",
        at = @At(
            value = "INVOKE",
            target = """
                net/minecraft/world/World.playSound(
                    Lnet/minecraft/entity/player/PlayerEntity;
                    D
                    D
                    D
                    Lnet/minecraft/sound/SoundEvent;
                    Lnet/minecraft/sound/SoundCategory;
                    F
                    F
                )V
            """
        ),
        index = 2
    )
    private double eyeHeightOffsetInjector(
        final @Nullable PlayerEntity player,
        final double x,
        final double y,
        final double z,
        final @NotNull SoundEvent sound,
        final SoundCategory category,
        final float volume,
        final float pitch
    ) {
        final var stepSound = Cache.stepPattern.matcher(sound.getId().getPath()).matches();
        // TODO: step sounds
        if (stepSound) {
            return y;
        } else {
            return getEyeY();
        }
    }
}
