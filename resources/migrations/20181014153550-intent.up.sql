CREATE TABLE IF NOT EXISTS request_log (
   id          bigserial primary key,

   url         varchar,
   method      varchar,
   payload     jsonb,
   response    jsonb,
   uuid        varchar,
   created_at  timestamp default now());