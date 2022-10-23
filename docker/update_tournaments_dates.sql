DO
$$
    DECLARE real_date VARCHAR;
    DECLARE min_id NUMERIC;
    BEGIN
        real_date := '2015-11-22 10:00:00';
        min_id := 1;
        update tournaments set created_at = real_date where id > 1;
        update tournament_scores set created_at = real_date where id > 1;
        update tournament_groups set created_at = real_date where id > 1;
        update tournament_image set created_at = real_date where id > min_id;
        update roles set created_at = real_date where id > 29;
        update teams set created_at = real_date where id > 4;
        update fights set created_at = real_date where id >= 12;
        update duels set created_at = real_date where id >= 36;
    END
$$