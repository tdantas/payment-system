-- :name- insert-sale-tx :insert :raw
insert into transactions (order_id, amount, status, response, uuid, type, gateway_id, final_state, provider, tx_parent_id, created_at)
       values(:order-id::integer, :amount, :status, :response, :uuid, :type, :gateway-id, :final-state, :provider, :tx-parent-id, now())

-- :name- update-sale-tx :<! :1
update transactions
set status = :status, gateway_response = (gateway_response || :gateway-response::jsonb)
where id = :id returning *


-- :name- find-txs-by-session-id :query :many
select t.* from payment_sessions ps
join orders o on ps.id = o.session_id
join transactions t on t.order_id = o.id
where ps.id = :session-id and t.status <> 'FAILED'
order by COALESCE(t.tx_parent_id, t.id), id
