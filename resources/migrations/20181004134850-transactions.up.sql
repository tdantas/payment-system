CREATE TABLE IF NOT EXISTS transactions (
   id             bigserial primary key,
   uuid           varchar,

   amount         decimal,
   type           varchar,
   gateway_id     varchar,
   response       jsonb default '{}'::jsonb,
   order_id       integer references orders not null,

   tx_parent_id   integer references transactions,

   status         varchar,
   created_at     timestamp default now());