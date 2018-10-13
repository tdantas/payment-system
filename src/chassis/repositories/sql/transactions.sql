-- :name- insert-sale-tx :insert :raw
insert into transactions (type, uuid, amount, billable, cost, items, status, payment_method, session_id, payment_entity) values(:type, :uuid, :amount, :billable, :cost, :items::jsonb, :status, :payment-method, :session-id , :payment-entity)

-- :name- update-sale-tx :<! :1
update transactions
set status = :status, gateway_response = (gateway_response || :gateway-response::jsonb)
where id = :id returning *