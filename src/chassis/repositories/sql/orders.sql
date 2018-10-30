-- :name- insert-sale-order :insert :raw
insert into orders (session_id, items, payment_method, cost, billable, amount, type , uuid, payment_entity, payment_method_authorization, status)
values(:session-id::integer, :items::jsonb, :payment-method, :cost, :billable, :amount, :type , :uuid, :payment-entity, :payment-method-authorization, :status)

-- :name- insert-refund-order :insert :raw
insert into orders (session_id, amount, type , uuid, status, payment_entity)
values(:session-id::integer, :amount, :type , :uuid, :status, :payment-entity)

-- :name- update-order :<! :1
update orders
set status = :status, created_at = now()
where id = :id returning *