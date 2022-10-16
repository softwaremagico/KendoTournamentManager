DO
$$
    DECLARE
        real_date      TIMESTAMP;
        DECLARE min_id NUMERIC;
    BEGIN
        real_date := '2015-10-25T10:00:00.00';
        min_id := 1;
        update tournaments set created_at = real_date where id >= min_id;
        update tournament_scores set created_at = real_date where id >= min_id;
        update tournament_groups set created_at = real_date where id >= min_id;
        update roles set created_at = real_date where id >= min_id;
        update teams set created_at = real_date where id >= min_id;
        update fights set created_at = real_date where id >= min_id;
        update duels set created_at = real_date where id >= min_id;
    END
$$