CREATE TABLE IF NOT EXISTS transactions (
   id             bigserial primary key,

   amount         decimal,
   billable       decimal,
   cost           decimal,
   payment_method varchar,
   type           varchar,
   payment_entity varchar,
   uuid           varchar,

   items            jsonb,
   gateway_response jsonb default '{}'::jsonb,

   status         varchar,
   session_id        integer references payment_sessions not null,
   created_at     timestamp default now());