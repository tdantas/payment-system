create table if not exists movements (
  id serial               primary key,
  transaction_id          varchar,

  total                   decimal,
  billable                decimal,
  cost                    decimal,

  type                    varchar, -- credit | debit | authorization
  status                  varchar, -- refunded | cancelled | charged | settled

  movement_parent_id      integer references movements,
  created_at              timestamp default now()
);