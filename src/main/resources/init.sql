drop table if exists ts;

create table ts
(
    id bigserial primary key,
    s  text,
    t  timestamp,
    tz timestamptz,
    l  timestamp,
    lz timestamptz
);

set
timezone to 'Europe/Moscow';

select id,
       s,
       t,
       tz,
       l,
       lz,
       t = l   as "t == l",
       tz = lz as "tz == lz"
from ts;