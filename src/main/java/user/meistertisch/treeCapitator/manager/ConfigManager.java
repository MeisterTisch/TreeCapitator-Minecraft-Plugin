package user.meistertisch.treeCapitator.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public boolean treeDetectionEnabled;
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
            reportInvalid("enabled", true);
            changed = true;
        }

        if(!config.isString("language")) {
            reportInvalid("language", "en");
            changed = true;
        } else {
            List<String> languages = List.of("en", "de");
            if (!languages.contains(config.getString("language"))) {
                reportInvalid("language", "en");
                changed = true;
            }
        }

        if (!config.isBoolean("onlyAxe")) {
            reportInvalid("onlyAxe", true);
            changed = true;
        }

        if (!config.isBoolean("onlySurvival")) {
            reportInvalid("onlySurvival", true);
            changed = true;
        }

        if (!config.isInt("speed")) {
            reportInvalid("speed", 3);
            changed = true;
        } else {
            int i = Math.clamp(config.getInt("speed"), 0, 5);
            if(i != config.getInt("speed")) {
                reportInvalid("speed", 3);
                changed = true;
            }
        }

        if (!config.isInt("limit") || config.getInt("limit") < 0) {
            reportInvalid("limit", 64);
            changed = true;
        }

        if (!config.isBoolean("treeDetection.enabled")) {
            reportInvalid("treeDetection.enabled", false);
            changed = true;
        }

        if (!config.isInt("treeDetection.mode")) {
            reportInvalid("treeDetection.mode", 1);
            changed = true;
        } else {
            int i = Math.clamp(config.getInt("treeDetection.mode"), 1, 2);
            if(i != config.getInt("treeDetection.mode")) {
                reportInvalid("treeDetection.mode", 1);
                changed = true;
            }
        }

        if (!config.isBoolean("treeDetection.deep")) {
            reportInvalid("treeDetection.deep", true);
            changed = true;
        }

        if (changed) {
            plugin.saveConfig();
        }
    }

    private void reportInvalid(String path, Object defaultValue){
        config.set(path, defaultValue);
        Component msg = plugin.getLang().getMessage(
                "config_invalid_value",
                Placeholder.unparsed("path", path),
                Placeholder.unparsed("default", String.valueOf(defaultValue))
        );
        plugin.getComponentLogger().error(msg);
    }

    private void loadValues() {
        this.enabled = config.getBoolean("enabled", true);
        this.language = config.getString("language", "en");
        this.onlyAxe = config.getBoolean("onlyAxe", true);
        this.onlySurvival = config.getBoolean("onlySurvival", true);
        this.speed = config.getInt("speed", 3);
        this.limit = config.getInt("limit", 64);
        this.treeDetectionEnabled = config.getBoolean("treeDetection.enabled", false);
        this.treeDetectionMode = config.getInt("treeDetection.mode", 1);
        this.treeDetectionDeep = config.getBoolean("treeDetection.deep", true);
    }

    private void updateConfig(String path, Object value) {
        config.set(path, value);
        plugin.saveConfig();
    }

    public Component getAllSettings(){
        Component message = Component.empty();

        Map<String, Object> settings = getSettingsMap();

        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            String key = entry.getKey();
            String valueString = String.valueOf(entry.getValue());
            Component value;

            if(entry.getValue() instanceof Boolean val){
                value = val
                        ? Component.text("true", NamedTextColor.GREEN)
                        : Component.text("false", NamedTextColor.RED);
            } else {
                value = Component.text(valueString);
            }

            String langKey = "command.tc.settings.key." + key.replace(" ", ".");

            Component displayName = plugin.getLang().getMessage(langKey);

            // Make line out of template
            Component line = plugin.getLang().getMessage("command.tc.settings.template",
                    Placeholder.component("key", displayName),
                    Placeholder.component("value", value));

            // Add Hover Event
            line = line.hoverEvent(HoverEvent.showText(
                    plugin.getLang().getMessage("command.tc.settings.hover", Placeholder.component("name", displayName))
            ));

            // Adding Click Event with two special cases
            if(key.equalsIgnoreCase("enabled")){
                line = line.clickEvent(ClickEvent.suggestCommand("/tc set status "));
            } else if(key.equalsIgnoreCase("treeDetection enabled")) {
                line = line.clickEvent(ClickEvent.suggestCommand("/tc set treeDetection status "));
            } else {
                line = line.clickEvent(ClickEvent.suggestCommand("/tc set " + key + " "));
            }

            if (!message.equals(Component.empty())) message = message.appendNewline();
            message = message.append(line);
        }

        return message;
    }

    private @NonNull Map<String, Object> getSettingsMap() {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("enabled", enabled);
        settings.put("language", language);
        settings.put("onlyAxe", onlyAxe);
        settings.put("onlySurvival", onlySurvival);
        settings.put("speed", speed);
        settings.put("limit", limit);
        settings.put("treeDetection enabled", treeDetectionEnabled);
        settings.put("treeDetection mode", getModeName()); // Method to get mode name instead of integer
        settings.put("treeDetection deep", treeDetectionDeep);
        return settings;
    }

    private String getModeName() {
        return switch (this.treeDetectionMode) {
            case 1 -> "leaf";
            case 2 -> "coreprotect";
            default -> "";
        };
    }

    // SETTERS
    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        updateConfig("enabled", enabled);
    }

    public synchronized void setLanguage(String language) {
        this.language = language;
        updateConfig("language", language);
        plugin.getLang().reload();
    }

    public synchronized void setOnlyAxe(boolean onlyAxe) {
        this.onlyAxe = onlyAxe;
        updateConfig("onlyAxe", onlyAxe);
    }

    public synchronized void setOnlySurvival(boolean onlySurvival) {
        this.onlySurvival = onlySurvival;
        updateConfig("onlySurvival", onlySurvival);
    }

    public synchronized void setSpeed(int speed) {
        this.speed = speed;
        updateConfig("speed", speed);
    }

    public synchronized void setLimit(int limit) {
        this.limit = limit;
        updateConfig("limit", limit);
    }

    public synchronized void setTreeDetectionEnabled(boolean treeDetectionEnabled) {
        this.treeDetectionEnabled = treeDetectionEnabled;
        updateConfig("treeDetection.enabled", treeDetectionEnabled);
        plugin.reloadLogChecker();
    }

    public synchronized void setTreeDetectionMode(int treeDetectionMode) {
        this.treeDetectionMode = treeDetectionMode;
        updateConfig("treeDetection.mode", treeDetectionMode);
        plugin.reloadLogChecker();
    }

    public synchronized void setTreeDetectionDeep(boolean treeDetectionDeep) {
        this.treeDetectionDeep = treeDetectionDeep;
        updateConfig("treeDetection.deep", treeDetectionDeep);
    }
}
