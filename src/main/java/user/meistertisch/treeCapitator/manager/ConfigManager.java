package user.meistertisch.treeCapitator.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.List;

public class ConfigManager {
    private final TreeCapitator plugin;
    private FileConfiguration config;

    // Saving config values in variables for better performance
    public boolean enabled;
    public String language;
    public boolean onlyAxe;
    public boolean onlySurvival;
    public int speed;
    public int limit;
    public int treeDetectionMode;
    public boolean treeDetectionDeep;

    public ConfigManager(TreeCapitator plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        validate();
        loadValues();
    }

    private void validate() {
        boolean changed = false;

        if(!config.isBoolean("enabled")){
            config.set("enabled", true);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "enabled", "true");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        }

        if(!config.isString("language")) {
            config.set("language", "en");
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "language", "en");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        } else {
            List<String> languages = List.of("en", "de");
            if (!languages.contains(config.getString("language"))) {
                config.set("language", "en");
                changed = true;

                Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "language", "en");
                TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
            }
        }

        if (!config.isBoolean("onlyAxe")) {
            config.set("onlyAxe", true);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "onlyAxe", "true");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        }

        if (!config.isBoolean("onlySurvival")) {
            config.set("onlySurvival", true);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "onlySurvival", "true");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        }

        if (!config.isInt("speed")) {
            config.set("speed", 3);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "speed", "3");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        } else {
            int i = Math.clamp(config.getInt("speed"), 1, 5);
            if(i != config.getInt("speed")) {
                config.set("speed", 3);
                changed = true;

                Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "speed", "3");
                TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
            }
        }

        if (!config.isInt("limit") || config.getInt("limit") < 0) {
            config.set("limit", 64);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "limit", "64");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        }

        if (!config.isInt("treeDetection.mode")) {
            config.set("treeDetection.mode", 0);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "treeDetection.mode", "0");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        } else {
            int i = Math.clamp(config.getInt("treeDetection.mode"), 0, 2);
            if(i != config.getInt("treeDetection.mode")) {
                config.set("treeDetection.mode", 0);
                changed = true;

                Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "treeDetection.mode", "0");
                TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
            }
        }

        if (!config.isBoolean("treeDetection.deep")) {
            config.set("treeDetection.deep", true);
            changed = true;

            Component invalidValueMsg = TreeCapitator.getPlugin().getLang().getMessage("config_invalid_value", "treeDetection.deep", "true");
            TreeCapitator.getPlugin().getComponentLogger().error(invalidValueMsg);
        }

        if (changed) {
            plugin.saveConfig();
        }
    }

    private void loadValues() {
        this.enabled = config.getBoolean("enabled", true);
        this.language = config.getString("language", "en");
        this.onlyAxe = config.getBoolean("onlyAxe", true);
        this.onlySurvival = config.getBoolean("onlySurvival", true);
        this.speed = config.getInt("speed", 3);
        this.limit = config.getInt("limit", 64);
        this.treeDetectionMode = config.getInt("treeDetection.mode", 0);
        this.treeDetectionDeep = config.getBoolean("treeDetection.deep", true);
    }
}
