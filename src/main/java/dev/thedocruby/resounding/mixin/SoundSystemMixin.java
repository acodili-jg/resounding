package dev.thedocruby.resounding.mixin;

import dev.thedocruby.resounding.Engine;
import dev.thedocruby.resounding.config.PrecomputedConfig;
import dev.thedocruby.resounding.toolbox.SourceAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.*; // FIXME: must expand
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(SoundSystem.class)
public class SoundSystemMixin {
    @Final
    @Shadow
    private SoundListener listener;

    @Inject(
    	method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
        at = @At(
            value = "FIELD",
            target = "net/minecraft/client/sound/SoundSystem.sounds : Lcom/google/common/collect/Multimap;"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void soundInfoYeeter(
        final SoundInstance instance,
        final CallbackInfo callback,
        final WeightedSoundSet weightedSoundSet,
        final Identifier identifier,
        final Sound sound,
        final float f,
        final float g,
        final SoundCategory category
    ) {
        if (Engine.on) {
            // TODO: do this better maybe
            Engine.recordLastSound(instance, this.listener);
        }
    }

//  @Inject(method = "tick()V", at = @At(value = "HEAD"))
//  private void ticker(final CallbackInfo callback) {
//      Air.updateSmoothedRain();
//  }

    @ModifyArg(
        method = "getAdjustedVolume",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/util/math/MathHelper.clamp (FFF)F"
        ),
        index = 0
    )
    private float volumeMultiplierInjector(final float vol) {
        if (Engine.on) {
            return vol * PrecomputedConfig.globalVolumeMultiplier;
        } else {
            return vol;
        }
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(
        method = "tick()V",
        at = @At(
            value = "JUMP",
            opcode = Opcodes.IFEQ,
            ordinal = 3
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void recalculate(
        final CallbackInfo callback,
        final Iterator<?> iterator,
        final Map.Entry<?, ?> entry,
        final Channel.SourceManager f,
        final SoundInstance g,
        final float vec3d
    ) {
	if (!Engine.on) { return; }
	final var world = Engine.mc.world;
        if (world != null && world.getTime() % .srcRefrRate == 0) {
            f.run(source -> ((SourceAccessor) source).calculateReverb(g, this.listener));
            /*world
	        .getRegistryManager()
	        .get(Registry.BLOCK_KEY)
	        .streamTags()
	        .forEachOrdered(tagKey -> Engine.LOGGER.info(tagKey.registry().getValue()));*/
        }
    }
}
