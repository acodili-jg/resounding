package dev.thedocruby.resounding.config.BlueTapePack;

import dev.thedocruby.resounding.Cache;
import dev.thedocruby.resounding.Engine;
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

    private enum State {

        INITIAL {

            @Override
            public void registerAutoConfig() {
                holder = AutoConfig.register(ResoundingConfig.class, JanksonConfigSerializer::new);

                if (Engine.env == EnvType.CLIENT) try {GuiRegistryinit.register();} catch (Throwable ignored){
                    Engine.LOGGER.error("Failed to register config menu unwrappers. Edit config that isn't working in the config file");}

                holder.registerSaveListener((holder, config) -> onSave(config));
                holder.registerLoadListener((holder, config) -> onSave(config));
                reload(true);

                ConfigManager.state = REGISTERED;
            }

            @Override
            public ResoundingConfig getConfig() {
                return ConfigManager.DEFAULT;
            }

            @Override
            public void reload(boolean load) {
                // Do nothing
            }

            @Override
            public void save() {
                ConfigManager.registerAutoConfig();
            }
        },
        REGISTERED {

            @Override
            public void registerAutoConfig() {
                throw new IllegalStateException("Configuration already registered");
            }
            
            @Override
            public ResoundingConfig getConfig() {
                return holder.getConfig();
            }

            @Override
            public void reload(boolean load) {
                if(load) holder.load();
                holder.getConfig().preset.setConfig();
                holder.save();
            }

            @Override
            public void save() {
                holder.save();
            }
        };

        public abstract void registerAutoConfig();

        public abstract ResoundingConfig getConfig();

        public abstract void reload(boolean load);

        public abstract void save();
    }

    private ConfigManager() {}

    private static ConfigHolder<ResoundingConfig> holder;

    private static State state = State.INITIAL;

    public static boolean resetOnReload;

    public static final String configVersion = "1.0.0-bc.8";

    @Environment(EnvType.CLIENT)
    public static final ResoundingConfig DEFAULT = Engine.env == EnvType.CLIENT ? new ResoundingConfig(){{
        Map<String, MaterialData> map = Cache.nameToGroup.keySet().stream()
                .collect(Collectors.toMap(e -> e, e -> new MaterialData(e, 0.5, 0.5)));
        map.putIfAbsent("DEFAULT", new MaterialData("DEFAULT", 0.5, 0.5));
        materials.materialProperties = map;
    }} : null;

    public static void registerAutoConfig() { state.registerAutoConfig(); }

    public static ResoundingConfig getConfig() { return state.getConfig(); }

    public static void reload(boolean load) { state.reload(load); }

    public static void save() { state.save(); }

    @Environment(EnvType.CLIENT)
    public static void handleBrokenMaterials(@NotNull ResoundingConfig c ){
        Engine.LOGGER.error("Critical materialProperties error. Resetting materialProperties");
        c.materials.materialProperties = Cache.materialDefaults;
        c.materials.blockWhiteList = Collections.emptyList();
    }

    public static void resetToDefault() {
        holder.resetToDefault();
        reload(false);
    }

    public static void handleUnstableConfig( ResoundingConfig c ){
        Engine.LOGGER.error("Error: Config file is not from a compatible version! Resetting the config...");
        resetOnReload = true;
    }

    public static ActionResult onSave(ResoundingConfig c) {
        if (Engine.env == EnvType.CLIENT && (c.materials.materialProperties == null || c.materials.materialProperties.get("DEFAULT") == null)) handleBrokenMaterials(c);
        if (Engine.env == EnvType.CLIENT && c.preset != ConfigPresets.LOAD_SUCCESS) c.preset.configChanger.accept(c);
        if ((c.version == null || !Objects.equals(c.version, configVersion)) && !resetOnReload) handleUnstableConfig(c);
        if (PrecomputedConfig.pC != null) PrecomputedConfig.pC.deactivate();
        try {PrecomputedConfig.pC = new PrecomputedConfig(c);} catch (CloneNotSupportedException e) {e.printStackTrace(); return ActionResult.FAIL;}
        if (Engine.env == EnvType.CLIENT && !Engine.isOff) {
            Engine.updateRays();
            Engine.mc.getSoundManager().reloadSounds();
        }
        return ActionResult.SUCCESS;
    }
}
