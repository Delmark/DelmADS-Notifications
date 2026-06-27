--liquibase formatted sql

--changeset delmark:init-access-mode
create table if not exists access_mode (
    name varchar(120) unique not null,
    access_level int primary key
);

insert into access_mode (name, access_level) values
('PUBLIC', 1),
('FRIEND', 2),
('OWNER', 3)
on conflict (name) do nothing;

--changeset delmark:init-core-tables
create table if not exists bot_user (
  id bigint primary key,
  username text not null,
  access_level int not null
        references access_mode (access_level),
  created_at timestamptz not null default current_timestamp
);

create table if not exists notification_topic (
    name varchar(120) primary key not null,
    min_access_level int not null
        references access_mode (access_level),
    created_at timestamptz not null default current_timestamp
);

create table if not exists notification_subscriptions (
    user_id bigint
        references bot_user (id) on delete cascade,
    topic_name varchar(120)
        references notification_topic (name)
        on delete cascade,
    created_at timestamptz not null default current_timestamp,
    primary key (user_id, topic_name)
);

--changeset delmark:notif-access-function splitStatements:false
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
                                     WHERE name = NEW.topic_name);

        if user_access_level < notification_access_level then
            raise exception '% user does not have access to this topic', new.user_id;
        end if;

        return new;
    end;
$notif_access_check$ language plpgsql;

--changeset delmark:notif-access-trigger
create trigger trg_notif_access_check before insert or update on notification_subscriptions
    for each row execute function check_user_notif_access();
