package ru.delmark.dads.notifications.integration.telegram.handlers.filters;

import io.github.natanimn.telebof.filters.CustomFilter;
import io.github.natanimn.telebof.types.updates.CallbackQuery;
import io.github.natanimn.telebof.types.updates.Update;
import ru.delmark.dads.notifications.integration.telegram.dto.TopicOps;

import java.util.Optional;

public class TopicSubControlFilter implements CustomFilter {

    @Override
    public boolean check(Update update) {
        return Optional.of(update)
                .map(Update::getCallbackQuery)
                .map(CallbackQuery::getData)
                .filter(data -> {
                    int opEndIndex = data.indexOf("_");
                    if (opEndIndex == -1 || opEndIndex + 1 >= data.length()) {
                        return false;
                    }
                    String op = data.substring(0, opEndIndex + 1);
                    return TopicOps.getStringTopicOps().contains(op);
                })
                .isPresent();
    }
}
