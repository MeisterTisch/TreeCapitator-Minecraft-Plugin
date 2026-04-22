package user.meistertisch.treeCapitator.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import user.meistertisch.treeCapitator.TreeCapitator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private ResourceBundle bundle;

    public LanguageManager(String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);
        this.bundle = ResourceBundle.getBundle("lang", locale);
    }

    public void reload(){
        String langCode = TreeCapitator.getPlugin().getConfig().getString("language", "en");
        Locale locale = Locale.forLanguageTag(langCode);
        this.bundle = ResourceBundle.getBundle("lang", locale);
    }

    public Component getMessage(String key, TagResolver... replacements) {
        String raw = bundle.getString(key);
        return MiniMessage.miniMessage().deserialize(raw, replacements);
    }

    public List<String> getLanguages(){
        List<String> languages = new ArrayList<>();
        languages.add("de");
        languages.add("en");
        return languages;
    }
}
