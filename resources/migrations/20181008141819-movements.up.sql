create table if not exists movements (
  id serial               primary key,
  transaction_id          integer references transactions,

  amount                  decimal,

  type                    varchar, -- credit | debit | authorization
  status                  varchar, -- refunded | cancelled | charged | settled

  gateway_id              varchar,

  movement_parent_id      integer references movements,
  created_at              timestamp default now()
);