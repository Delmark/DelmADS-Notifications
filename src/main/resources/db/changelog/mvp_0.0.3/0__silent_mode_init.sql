--liquibase formatted sql

--changeset delmark:user-global-silent-mode
alter table bot_user add column preferred_silent_mode bool not null default false;

--changeset delmark:topics-silent-mode-init
alter table notification_subscriptions add column silent_mode bool not null default false;
