-- :name- insert-payment-session :insert :raw
insert into payment_sessions (customer_id, correlation, client, expiration_date) values(:customer-id, :correlation, :client, :expiration-date)

-- :name- find-unique :? :1
select * from payment_sessions
where id = :id::integer

