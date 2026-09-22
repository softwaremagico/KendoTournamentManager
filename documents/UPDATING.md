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

# From version 2.14.X to 2.15.0

```               
alter table public.tournament_extra_properties
    drop constraint tournament_extra_properties_property_key_check;

alter table public.tournament_extra_properties
    add constraint tournament_extra_properties_property_key_check
        check ((property_key)::text = ANY
               (ARRAY [('MAXIMIZE_FIGHTS'::character varying)::text, ('AVOID_DUPLICATES'::character varying)::text, ('KING_INDEX'::character varying)::text, ('KING_DRAW_RESOLUTION'::character varying)::text, ('DIPLOMA_NAME_HEIGHT'::character varying)::text, ('NUMBER_OF_WINNERS'::character varying)::text, ('LEAGUE_FIGHTS_ORDER_GENERATION'::character varying)::text, ('ODD_FIGHTS_RESOLVED_ASAP'::character varying)::text]));

```


# From version 2.15.X to 2.16.0

```
alter table public.tournaments
    drop constraint tournaments_tournament_type_check;

alter table public.tournaments
    add constraint tournaments_tournament_type_check
        check ((tournament_type)::text = ANY
               (ARRAY [('CHAMPIONSHIP'::character varying)::text, ('TREE'::character varying)::text, ('LEAGUE'::character varying)::text, ('LOOP'::character varying)::text, ('CUSTOM_CHAMPIONSHIP'::character varying)::text, ('KING_OF_THE_MOUNTAIN'::character varying)::text, ('CUSTOMIZED'::character varying)::text, ('BUBBLE_SORT'::character varying)::text]));

```

# From version 2.16.0 to 2.17.0


```
alter table public.tournaments
    drop constraint tournaments_tournament_type_check;

alter table public.tournaments
    add constraint tournaments_tournament_type_check
        check ((tournament_type)::text = ANY
               (ARRAY [('CHAMPIONSHIP'::character varying)::text, ('TREE'::character varying)::text, ('LEAGUE'::character varying)::text, ('LOOP'::character varying)::text, ('CUSTOM_CHAMPIONSHIP'::character varying)::text, ('KING_OF_THE_MOUNTAIN'::character varying)::text, ('CUSTOMIZED'::character varying)::text, ('SENBATSU'::character varying)::text, ('BUBBLE_SORT'::character varying)::text]));
               
               
alter table public.tournament_extra_properties
    drop constraint tournament_extra_properties_property_key_check;

alter table public.tournament_extra_properties
    add constraint tournament_extra_properties_property_key_check
        check ((property_key)::text = ANY
               (ARRAY [('MAXIMIZE_FIGHTS'::character varying)::text, ('AVOID_DUPLICATES'::character varying)::text, ('KING_INDEX'::character varying)::text, ('KING_DRAW_RESOLUTION'::character varying)::text, ('DIPLOMA_NAME_HEIGHT'::character varying)::text, ('NUMBER_OF_WINNERS'::character varying)::text, ('LEAGUE_FIGHTS_ORDER_GENERATION'::character varying)::text, ('ODD_FIGHTS_RESOLVED_ASAP'::character varying)::text, ('SENBATSU_CHALLENGE_DISTANCE'::character varying)::text]));

```

# From version 3.5.0 to 3.6.0

Version 3.6.0 introduces tenant isolation. Back up the database, stop the
application, run exactly one platform-specific script, then start 3.6.0:

| Database | Script |
|----------|--------|
| PostgreSQL 14+ | [`migrations/3.6.0-tenancy-postgresql.sql`](migrations/3.6.0-tenancy-postgresql.sql) |
| MySQL 8+ | [`migrations/3.6.0-tenancy-mysql.sql`](migrations/3.6.0-tenancy-mysql.sql) |

The migration creates `Legacy organization` with ID `1` and assigns all
existing records to it. The PostgreSQL script aborts when that invariant cannot
be established. MySQL prints the required ID check: stop immediately unless it
returns `1`. MySQL removes its prior global unique indexes automatically before
adding the tenant-local uniqueness constraints.

The scripts are single-use migration scripts. MySQL DDL commits implicitly, so
restore the verified backup rather than rerunning a partially completed script.

On a new empty installation, the application creates `Legacy organization`
automatically. On an installation that already has users, clubs or tournaments,
startup fails until the manual 3.6.0 script has been executed.

After the first start, create the platform administrator by supplying both
properties once through the deployment environment, then remove them:

```
bootstrap.super-admin.username=platform-admin@example.com
bootstrap.super-admin.password=<strong-unique-password>
```

Only `SUPER_ADMIN` users can create, edit, activate or deactivate tenants.
Tenant `ADMIN` users manage only their own organization and cannot assign the
platform role. Deactivating a tenant invalidates new logins, existing JWT
requests and new WebSocket subscriptions for that tenant.
