--liquibase formatted sql

--changeset delmark:topic-add-id-and-swap-pk
alter table notification_topic
    add column topic_id serial;

--changeset delmark:notif-access-function-by-id splitStatements:false
-- фикс триггера
create or replace function check_user_notif_access() returns trigger as $notif_access_check$
declare
    user_access_level int;
    notification_access_level int;
begin
    user_access_level = (SELECT u.access_level
                         FROM bot_user u
                         WHERE u.id = NEW.user_id);

    notification_access_level = (SELECT notification_topic.min_access_level
                                 FROM notification_topic
                                 WHERE notification_topic.topic_id = new.topic_id);

    if user_access_level < notification_access_level then
        raise exception '% user does not have access to this topic', new.user_id;
    end if;

    return new;
end;
$notif_access_check$ language plpgsql;

--changeset delmark:topic-id-swap-pk
alter table notification_subscriptions
    drop constraint notification_subscriptions_topic_name_fkey;

alter table notification_topic
    drop constraint notification_topic_pkey;

alter table notification_topic
    add constraint notification_topic_pkey primary key (topic_id);

alter table notification_topic
    add constraint unique_topic_name unique (name);

--changeset delmark:subscriptions-switch-to-topic-id
-- рокировка на существующих завязанных табличках

alter table notification_subscriptions
    add column topic_id integer
        references notification_topic(topic_id) on delete cascade;

update notification_subscriptions ns
set topic_id = nt.topic_id
from notification_topic nt
where ns.topic_name = nt.name;

alter table notification_subscriptions
    alter column topic_id set not null;

alter table notification_subscriptions
    drop column topic_name;

alter table notification_subscriptions
    add constraint notif_subs_pk primary key (user_id, topic_id)
