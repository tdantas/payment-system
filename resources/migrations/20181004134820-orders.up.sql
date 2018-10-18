CREATE TABLE IF NOT EXISTS orders (
   id          bigserial primary key,
   uuid        varchar,

   amount         decimal,
   billable       decimal,
   cost           decimal,

   payment_method_authorization varchar,
   payment_method varchar,
   payment_entity varchar,

   type           varchar,
   items          jsonb,
   currency       varchar,

   session_id    integer references payment_sessions not null,
   created_at    timestamp default now());