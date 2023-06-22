# From version 2.11.1 to 2.12.0

Duels duration column on database, has been changed. If you have a running database, execute on the database:

Postgresql:

```
alter table duels
    alter column duration type int using duration::bigint;
```
