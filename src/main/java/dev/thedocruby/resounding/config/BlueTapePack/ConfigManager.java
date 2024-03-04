package dev.thedocruby.resounding.config.BlueTapePack;

import dev.thedocruby.resounding.Cache;
import dev.thedocruby.resounding.Engine;
import dev.thedocruby.resounding.Utils;
import dev.thedocruby.resounding.config.PrecomputedConfig;
import dev.thedocruby.resounding.config.ResoundingConfig;
import dev.thedocruby.resounding.config.presets.ConfigPresets;
import dev.thedocruby.resounding.toolbox.MaterialData;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigManager {
    private ConfigManager() {}

    private static ConfigHolder<ResoundingConfig> holder;

    public static boolean resetOnReload;

    public static final String configVersion = "1.0.0-bc.8";

    @Environment(EnvType.CLIENT)
    public static final ResoundingConfig DEFAULT = Engine.env == EnvType.CLIENT ? new ResoundingConfig() : null;

    public static void registerAutoConfig() {
        if (holder != null) { throw new IllegalStateException("Configuration already registered"); }
        holder = AutoConfig.register(ResoundingConfig.class, JanksonConfigSerializer::new);

        if (Engine.env == EnvType.CLIENT) {
            try {
                GuiRegistryinit.register();
            } catch (final Throwable ignored) {
                Utils.LOGGER.error("Failed to register config menu unwrappers. Edit config that isn't working in the config file");
            }
        }

        holder.registerSaveListener((holder, config) -> onSave(config));
        holder.registerLoadListener((holder, config) -> onSave(config));
        reload(true);
    }

    public static ResoundingConfig getConfig() {
        if (holder == null) {
            return DEFAULT;
        } else {
            return holder.getConfig();
        }
    }

    public static void reload(final boolean load) {
        if (holder == null) { return; }

        if (load) {
            holder.load();
        }
        holder.getConfig().preset.setConfig();
        holder.save();
    }

    public static void save() {
        if (holder == null) {
            registerAutoConfig();
        } else {
            holder.save();
        }
    }

    @Environment(EnvType.CLIENT)
    public static void handleBrokenMaterials(final @NotNull ResoundingConfig config) {
        Utils.LOGGER.error("Critical materialProperties error. Resetting materialProperties");
        config.materials.blockWhiteList = Collections.emptyList();
    }

    public static void resetToDefault() {
        holder.resetToDefault();
        reload(false);
    }

    public static void handleUnstableConfig(final ResoundingConfig config) {
        Utils.LOGGER.error("Error: Config file is not from a compatible version! Resetting the config...");
        resetOnReload = true;
    }

    public static ActionResult onSave(final ResoundingConfig config) {
        final var configLoadFailed = Engine.env == EnvType.CLIENT
            && config.preset != ConfigPresets.LOAD_SUCCESS;
        if (configLoadFailed) {
            config.preset.configChanger.accept(config);
        }

        final var configUnstable = (config.version == null
            || !Objects.equals(config.version, configVersion)
            && !resetOnReload
        if (configUnstable) {
            handleUnstableConfig(c);
        }

        if (PrecomputedConfig.pC != null) {
            PrecomputedConfig.pC.deactivate();
        }
        try {
            PrecomputedConfig.pC = new PrecomputedConfig(c);
        } catch (final CloneNotSupportedException cause) {
            cause.printStackTrace();
            return ActionResult.FAIL;
        }

        if (Engine.env == EnvType.CLIENT && Engine.on) {
            Engine.updateRays();
            Engine.mc.getSoundManager().reloadSounds();
        }
        return ActionResult.SUCCESS;
    }
}
