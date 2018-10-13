-- :name- find-by :query :many
select row_number() over () as seq,
		    row_data as "before",
        (row_data || coalesce(changed_fields, ''::hstore)) as "after",
        changed_fields as changes,
        "action"
 from audit.logged_actions
 where
  table_name = :table-name
   and row_data-> :v:cond.0 :sql:cond.1 :v:cond.2
   order by seq asc;