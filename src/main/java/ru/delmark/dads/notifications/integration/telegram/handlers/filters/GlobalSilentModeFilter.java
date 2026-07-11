package ru.delmark.dads.notifications.integration.telegram.handlers.filters;

import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Update;

import java.util.Optional;

public class GlobalSilentModeFilter implements CustomFilter {

    @Override
    public boolean check(Update update) {
        return Optional.of(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getData)
                .filter(data -> data.startsWith("silent_mode_"))
                .isPresent();
    }
}
