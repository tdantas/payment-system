-- :name- insert-movement :insert :raw
insert into movements (order_id, amount, type, status, movement_parent_id)
       values(:order-id::integer, :amount::integer, :type, :status, :movement_parent_id::integer)

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