CREATE TABLE IF NOT EXISTS request_log (
   id          bigserial primary key,

   type        varchar,
   url         varchar,
   method      varchar,
   payload     jsonb,
   response    jsonb,
   status      varchar,
   uuid        varchar,
   created_at  timestamp default now());