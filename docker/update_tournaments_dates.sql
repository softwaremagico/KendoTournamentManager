DO
$$
    DECLARE real_date VARCHAR;
    DECLARE from_date VARCHAR;
    DECLARE min_id NUMERIC;
    BEGIN
        real_date := '2015-12-13 10:00:00';
        from_date := '2022-11-26 00:00:00';
        min_id := 1;
        update tournaments set created_at = real_date where created_at > from_date;
        update tournament_scores set created_at = real_date where created_at > from_date;
        update tournament_groups set created_at = real_date where created_at > from_date;
        update tournament_image set created_at = real_date where created_at > from_date;
        update roles set created_at = real_date where created_at > from_date;
        update teams set created_at = real_date where created_at > from_date;
        update fights set created_at = real_date where created_at > from_date;
        update duels set created_at = real_date where created_at > from_date;
    END
$$