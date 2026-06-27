--liquibase formatted sql

--changeset delmark:username-drop-not-null
alter table bot_user alter column username drop not null;
