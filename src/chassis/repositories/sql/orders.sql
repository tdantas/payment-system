-- :name- insert-order :insert :raw
insert into orders (session_id, items, payment_method, cost, billable, amount, type , uuid, payment_entity, payment_method_authorization)
values(:session-id::integer, :items::jsonb, :payment-method, :cost, :billable, :amount, :type , :uuid, :payment-entity, :payment-method-authorization)