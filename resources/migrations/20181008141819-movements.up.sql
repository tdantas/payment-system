create table if not exists movements (
  id serial               primary key,
  order_id                integer references orders,
  tx_id                   integer references transactions,


  amount                  decimal,
  type                    varchar, -- CREDIT     | DEBIT
  status                  varchar, -- PROCESSING | SUCCEED | FAILED
  description             varchar,

  created_at              timestamp default now()
);