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