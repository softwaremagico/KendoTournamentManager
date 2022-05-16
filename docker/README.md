# Customizing the application

## Using a different database engine.

If you want to use a different database engine, add the correct jar dependency with the jdbc connector
in `backend/libraries`. Configure the specific parameters in the `.env` file:

```
database_type=postgresql  (hsqldb, h2, oracle, mysql, postgresql, ...)
database_name=database
database_password=mypass
database_user=myuser
database_port=5432
```

# Using REVERSE PROXY and Generating SSL certificates for the first time

The reverse proxy is prepared to use let's encrypt certificates. First time you run it, rproxy will listen to
ports `80` and `8080`. SSL configuration is commented, as we need to rproxy starts to generate the SSL certificates. 

First, try to generate your certificates, running in your machine where the docker containers are installed:
```
/usr/bin/docker run -t --rm -v  docker_ssl-certificates:/etc/letsencrypt -v  docker_ssl-certificates-data:/data/letsencrypt deliverous/certbot certonly -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot --webroot-path=/data/letsencrypt -d <<my-domain>>
```
Remember to change `<<my-email>>` and `<<my-domain>>` with valid values.

If the certificates are success, now you must configure the reverse proxy to use them. Edit the file `rproxy/config/servers.conf` and:

Change the port from 8080 to 442:
```
  listen 443 ssl;
  #listen 8080;
```

Uncomment the certificate placeholders:

```
  ssl_certificate /etc/letsencrypt/live/MACHINE_DOMAIN/fullchain.pem;
  ssl_certificate_key /etc/letsencrypt/live/MACHINE_DOMAIN/privkey.pem;
  ssl_trusted_certificate /etc/letsencrypt/live/MACHINE_DOMAIN/chain.pem;
```

Next thing to do, is enabling http to https redirection, uncommenting these lines:
```
  # Configure http to redirect to https;
  location / {
    return 301 https://$host$request_uri;
  }
```

Finally, recreate the container and launch it again:

```
docker-compose build --no-cache kendo-tournament-rproxy && docker-compose up -d kendo-tournament-rproxy
```

## Renewal of SSL certificates

Your certificates usually expire in 90 days. For the renewal of the SSL certificates, you need to configure your cron system to ask for the renewal time by time. An
example, where is asking for a renewal each sunday at 5am:

```
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

0 5 * * 7 /usr/bin/docker run -t --rm -v  docker_ssl-certificates:/etc/letsencrypt -v  docker_ssl-certificates-data:/data/letsencrypt deliverous/certbot certonly -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot --webroot-path=/data/letsencrypt -d <<my-domain>> && /usr/bin/docker restart kendo-tournament-rproxy >/dev/null 2>&1
```

If using this example, as before remember to change `<<my-email>>` and `<<my-domain>>` with valid values.