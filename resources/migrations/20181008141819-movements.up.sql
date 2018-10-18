create table if not exists movements (
  id serial               primary key,
  order_id                integer references orders,

  amount                  decimal,
  type                    varchar, -- credit | debit | authorization
  status                  varchar, -- refunded | cancelled | charged | settled

  movement_parent_id      integer references movements,
  created_at              timestamp default now()
);