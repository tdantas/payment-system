-- :name- insert-tx :insert :raw
insert into transactions (amount, billable, cost, items, status, payment_method, session_id) values(:amount, :billable, :cost, :items::jsonb, :status, :payment-method, :session-id)

-- :name- update-tx :<! :1
update transactions
set status = :status, gateway_response = (gateway_response || :gateway-response::jsonb)
where id = :id returning *