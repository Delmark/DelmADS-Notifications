package ru.delmark.dads.notifications.data.repository;

import org.apache.ibatis.annotations.Mapper;
import ru.delmark.dads.notifications.data.model.AccessMode;
import ru.delmark.dads.notifications.data.model.BotUser;

import java.util.Optional;

@Mapper
public interface BotUserDAO {
    Optional<BotUser> findById(Long userId);
    void save(BotUser botUser);
    void updateUserAccess(Long userId, AccessMode accessMode);
}
