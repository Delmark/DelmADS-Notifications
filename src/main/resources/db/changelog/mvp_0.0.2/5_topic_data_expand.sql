--liquibase formatted sql

--changeset delmark:topic-new-columns
alter table notification_topic add column description text;

alter table notification_topic add column readable_alias varchar(120)
    unique check ( name != readable_alias );

--changeset delmark:set-not-null-alias
alter table notification_topic alter column readable_alias set not null;