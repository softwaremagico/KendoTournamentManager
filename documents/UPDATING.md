# From version 2.11.1 to 2.12.0

Duels duration column on database, has been changed. If you have a running database, execute on the database:

Postgresql:

```
alter table duels
    alter column duration type int using duration::bigint;
    
alter table duels
    alter column competitor_1_fault_time type bigint using competitor_1_fault_time::bigint;

alter table duels
    alter column competitor_2_fault_time type bigint using competitor_2_fault_time::bigint;
    
alter table competitor_1_score_time
    alter column competitor1scoretime type int using competitor1scoretime::bigint;
    
alter table competitor_2_score_time
    alter column competitor2scoretime type int using competitor2scoretime::bigint;
```
