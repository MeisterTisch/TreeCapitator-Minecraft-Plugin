package user.meistertisch.treeCapitator.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private final ResourceBundle bundle;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public LanguageManager(String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);
        this.bundle = ResourceBundle.getBundle("lang", locale);
    }

    public Component getMessage(String key, TagResolver... replacements) {
        // 1. String aus der Config laden. Falls der Key fehlt, wird der Key selbst ausgegeben.
        String raw = bundle.getString(key);

        // 2. Den String mit MiniMessage deserialisieren.
        // Die Replacements (Platzhalter) werden hier direkt verarbeitet.
        return MiniMessage.miniMessage().deserialize(raw, replacements);
    }
}
