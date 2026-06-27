--liquibase formatted sql

--changeset delmark:create-user-chats
create table if not exists user_chats (
    chat_id bigint,
    user_id bigint,
    primary key (chat_id, user_id)
);
