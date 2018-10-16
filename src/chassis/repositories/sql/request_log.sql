-- :name- insert-request-log :insert :raw
insert into request_log (uuid, url, method, payload)
 values (:uuid, :url, :method, :payload::jsonb)