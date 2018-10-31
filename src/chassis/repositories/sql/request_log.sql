-- :name- insert-request-log :insert :raw
insert into request_log (uuid, url, method, payload, status, response)
 values (:uuid, :url, :method, :payload::jsonb, :status, :response::jsonb)
