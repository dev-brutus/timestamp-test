create table ts
(
    id bigserial primary key,
    t  timestamp,
    tz timestamptz,
    s  text
);
