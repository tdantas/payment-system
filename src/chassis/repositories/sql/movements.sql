-- :name- insert-movement :insert :raw
insert into movements (order_id, amount, type, tx_id)
       values(:order-id::integer, :amount::integer, :type, :tx-id)

-- :name movements-by-session :? :*
select m.* from payment_sessions ps
join transactions tx on tx.session_id = ps.id
join movements m on m.transaction_id = tx.id
where ps.id = :session-id
order by m.id asc

-- :name movements-by-session-and-status :? :*
select m.* from payment_sessions ps
join transactions tx on tx.session_id = ps.id
join movements m on m.transaction_id = tx.id
where ps.id = :session-id
      and m.status = :status
order by m.id asc

-- :name- find-movements-with-txs-by-session :? :*
select m.id, row_to_json(m.*)::json as movement, json_agg(t.*) as txs from orders o
join transactions t on o.id = t.order_id
join movements m on m.order_id = o.id
where o.session_id = :session-id
group by m.id;

-- :name find-movements :query :many
select * from orders o
join transactions t on o.id = t.order_id
join movements m on m.order_id = o.id
where o.session_id = :session-id