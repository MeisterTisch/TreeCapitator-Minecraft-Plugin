package user.meistertisch.treeCapitator;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

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

    public Component getMessage(String key, Object... args) {
        if (!bundle.containsKey(key)) {
            return miniMessage.deserialize("<red>Missing key: <gold>" + key);
        }

        String raw = bundle.getString(key);

        if (args.length > 0) {
            raw = MessageFormat.format(raw, args);
        }

        return miniMessage.deserialize(raw);
    }
}
