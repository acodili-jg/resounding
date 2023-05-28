package dev.thedocruby.resounding;

import dev.thedocruby.resounding.config.BlueTapePack.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;

/**
 * Resounding's initializer, ran only on {@link EnvType#CLIENT}.
 */
public class ModClient implements ClientModInitializer {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onInitializeClient() {
		Engine.env = EnvType.CLIENT;
		ConfigManager.registerAutoConfig();
	}
}
