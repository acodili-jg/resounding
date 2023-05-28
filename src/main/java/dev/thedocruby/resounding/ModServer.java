package dev.thedocruby.resounding;

import dev.thedocruby.resounding.config.BlueTapePack.ConfigManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;

/**
 * Resounding's initializer, ran only on {@link EnvType#SERVER}.
 */
public class ModServer implements DedicatedServerModInitializer {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onInitializeServer() {
		Engine.env = EnvType.SERVER;
		ConfigManager.registerAutoConfig();
	}
}

