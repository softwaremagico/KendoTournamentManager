## Generating SSL certificates for the first time

## Renewal of SSL certificates

For the renewal of the SSL certificates, you need to configure your cron system to ask for the renewal time by time. An
example, where is asking for a renewal each sunday at 5am:

```
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

0 5 * * 7 /usr/bin/docker run -t --rm -v  docker_ssl-certificates:/etc/letsencrypt -v  docker_ssl-certificates-data:/data/letsencrypt deliverous/certbot certonly -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot --webroot-path=/data/letsencrypt -d <<my-domain>> && /usr/bin/docker restart kendo-tournament-rproxy >/dev/null 2>&1
```

If using this example, remember to change `<<my-email>>` and `<<my-domain>>` with valid values. 

# Using a different database engine.
If you want to use a different database engine, add the correct jar dependency with the jdbc connector in `backend/libraries`. Configure the specific parameters in the `.env` file:
```
database_type=postgresql  (hsqldb, h2, oracle, mysql, postgresql, ...)
database_name=database
database_password=mypass
database_user=myuser
database_port=5432
```