-- :name- insert-payment-session :insert :raw
insert into payment_sessions (customer_id, correlation, client,  currency)
  values(:customer-id, :correlation, :client, :currency)

-- :name- find-unique :? :1
select * from payment_sessions
where id = :id::integer

