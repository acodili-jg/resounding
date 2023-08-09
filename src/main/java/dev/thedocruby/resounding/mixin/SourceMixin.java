package dev.thedocruby.resounding.mixin;

import dev.thedocruby.resounding.Engine;
import dev.thedocruby.resounding.toolbox.SourceAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundListener;
import net.minecraft.client.sound.Source;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Environment(EnvType.CLIENT)
@Mixin(Source.class)
public class SourceMixin implements SourceAccessor {

	@Shadow
	@Final
	private int pointer;

	private Vec3d soundPos;

	@Inject(method = "setPosition", at = @At("HEAD"))
	private void savePosition(Vec3d pos, CallbackInfo ci) { if (!Engine.on) return; this.soundPos = pos; }

	@Inject(method = "play", at = @At("HEAD"))
	private void onPlay(CallbackInfo ci) {
		if (!Engine.on) return;
		// TODO make context dynamic
		Engine.play(Engine.root, soundPos, pointer, false);
	}

	public void calculateReverb(SoundInstance sound, SoundListener listener) {
		if (!Engine.on) return;
		Engine.recordLastSound(sound, listener);
		// TODO make context dynamic
		Engine.play(Engine.root, soundPos, pointer, false);
	}
}
