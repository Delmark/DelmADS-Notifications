--liquibase formatted sql

--changeset delmark:topic-new-columns
alter table notification_topic add column description text;

alter table notification_topic add column readable_alias varchar(120)
    unique check ( name != readable_alias );

--changeset delmark:alias-trigger-func-definition splitStatements:false
create or replace function validate_alias() returns trigger as $alias_trg$
    begin
        if exists(select 1 from notification_topic where name = new.readable_alias or new.name = readable_alias)
        then
            raise exception 'alias cannot duplicate any existing topic name';
        end if;
        return new;
    end;
$alias_trg$ language plpgsql;

--changeset delmark:alias-trigger-create
create or replace trigger trg_topic_alias_check before insert or update on notification_topic
    for each row execute function validate_alias();

--changeset delmark:delmark-alias-default-update
-- таких случаев быть не должно так как алиасы создаются в одном прогоне миграций
-- однако на всякий случай прогоняю этот скрипт
update notification_topic set readable_alias = concat(name, '_alias_', topic_id)
                          where notification_topic.readable_alias is null;

--changeset delmark:set-not-null-alias
alter table notification_topic alter column readable_alias set not null;

--changeset delmark:topic-priority
alter table notification_topic add column display_priority numeric not null default 1;