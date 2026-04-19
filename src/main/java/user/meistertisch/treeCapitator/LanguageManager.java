package user.meistertisch.treeCapitator;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LanguageManager {
    private final TreeCapitator plugin;
    private FileConfiguration langConfig;

    public LanguageManager(TreeCapitator plugin) {
        this.plugin = plugin;
        loadLanguage();
    }

    public void loadLanguage() {
        // Liest die Sprache aus der config.yml (z.B. "de" oder "en")
        String lang = plugin.getConfig().getString("language", "en");
        File langFile = new File(plugin.getDataFolder(), "lang_" + lang + ".properties");

        // Falls Datei nicht existiert, aus Resources kopieren
        if (!langFile.exists()) {
            plugin.saveResource("lang_" + lang + ".properties", false);
        }

        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getString(String key) {
        return langConfig.getString(key, "Missing key: " + key);
    }
}
