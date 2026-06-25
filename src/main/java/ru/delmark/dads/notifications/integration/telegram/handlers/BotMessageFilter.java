package ru.delmark.dads.notifications.integration.telegram.handlers;

import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.Update;

import java.util.Optional;
import java.util.function.Predicate;

public class BotMessageFilter implements CustomFilter {

    @Override
    public boolean check(Update update) {
        return Optional.of(update)
                .map(Update::getMessage)
                .map(message -> message.getFrom().getIsBot())
                .filter(Predicate.isEqual(false))
                .isPresent();
    }

}
