package ru.delmark.dads.notifications.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.LookupTranslator;

import java.util.HashMap;
import java.util.Map;

/**
 * Экранирование и де-экранирование текста под разметку Telegram MarkdownV2.
 * <p>
 * Согласно Bot API, в обычном тексте обязательны к экранированию символы
 * {@code _ * [ ] ( ) ~ ` > # + - = | { } . !}. Дополнительно экранируем сам
 * обратный слэш ({@code \}) — это служебный символ разметки, без его обработки
 * {@link #escape(String)} и {@link #unescape(String)} перестают быть взаимно
 * обратными на тексте с пользовательскими слэшами.
 * <p>
 * Применять нужно к динамическим/пользовательским данным (имена, теги рассылок,
 * текст ошибок и т.п.), которые подставляются в сообщения с
 * {@code parseMode = MARKDOWNV2}. Статические шаблоны с намеренной разметкой
 * экранировать не нужно.
 */
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
