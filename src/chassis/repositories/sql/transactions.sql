-- :name- insert-sale-tx :insert :raw
insert into transactions (order_id, amount, status, response, uuid, type, gateway_id)
       values(:order-id::integer, :amount, :status, :response, :uuid, :type, :gateway-id)

-- :name- update-sale-tx :<! :1
update transactions
set status = :status, gateway_response = (gateway_response || :gateway-response::jsonb)
where id = :id returning *