create table if not exists payment_sessions (
   id serial     primary key,
   correlation   varchar,
   customer_id   varchar(50) not null,
   client        json,
   currency      varchar,

   expiration_date timestamp,
   created_at timestamp default now());