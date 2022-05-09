## Generating SSL certificates for the first time

## Renewal of SSL certificates

For the renewal of the SSL certificates, you need to configure your cron system to ask for the renewal time by time. An
example, where is asking for a renewal each sunday at 5am:

```
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

0 5 * * 7 /usr/bin/docker run -t --rm -v  docker_ssl-certificates:/etc/letsencrypt -v  docker_ssl-certificates-data:/data/letsencrypt deliverous/certbot certonly -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot --webroot-path=/data/letsencrypt -d <<my-domain>> && /usr/bin/docker restart kendo-tournament-rproxy >/dev/null 2>&1
```

If using this example, remember to change `<<my-email>>` and `<<my-domain>>` with valid values. 