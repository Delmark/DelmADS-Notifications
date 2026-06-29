--liquibase formatted sql

--changeset delmark:topic-varchar-fix
alter table public.notification_topic alter column name type varchar(60);