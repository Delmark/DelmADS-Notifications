package ru.delmark.dads.notifications.integration.telegram.handlers.filters;

import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Update;

import java.util.Optional;

public class TopicSilentModeEditFilter implements CustomFilter {

    @Override
    public boolean check(Update update) {
        return Optional.ofNullable(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getData)
                .filter(data -> data.startsWith("topic_silent_mode_"))
                .isPresent();
    }

}
