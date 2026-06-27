--liquibase formatted sql

--changeset delmark:chats-add-fk
alter table user_chats
    add constraint user_chats_user_fk
        foreign key (user_id) references bot_user (id)