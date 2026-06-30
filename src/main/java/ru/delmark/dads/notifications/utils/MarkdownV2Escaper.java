package ru.delmark.dads.notifications.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class MarkdownV2Escaper {

    private final String SPECIAL_CHARS = "\\_*[]()~`>#+-=|{}.!";

    private final CharSequenceTranslator ESCAPE = buildEscapeTranslator();
    private final CharSequenceTranslator UNESCAPE = buildUnescapeTranslator();

    public String escape(String text) {
        return text == null ? null : ESCAPE.translate(text);
    }

    public String unescape(String text) {
        return text == null ? null : UNESCAPE.translate(text);
    }

    private CharSequenceTranslator buildEscapeTranslator() {
        Map<CharSequence, CharSequence> lookup = new HashMap<>();
        for (int i = 0; i < SPECIAL_CHARS.length(); i++) {
            String ch = String.valueOf(SPECIAL_CHARS.charAt(i));
            lookup.put(ch, "\\" + ch);
        }
        return new LookupTranslator(lookup);
    }

    private CharSequenceTranslator buildUnescapeTranslator() {
        Map<CharSequence, CharSequence> lookup = new HashMap<>();
        for (int i = 0; i < SPECIAL_CHARS.length(); i++) {
            String ch = String.valueOf(SPECIAL_CHARS.charAt(i));
            lookup.put("\\" + ch, ch);
        }
        return new LookupTranslator(lookup);
    }
}
