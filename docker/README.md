# Customizing the application

On the file `docker/.env` you can customize easily some properties. Please, take a look on the next sections:

## Domain

### Using localhost

You can deploy the application in your desktop computer for testing it. It is not recommended for a real environment but
is an easy way to check the state of the art of this application and if it fits to your needs or not.

On `docker/.env` set:

```
machine_domain=localhost
[...]
#Frontend
protocol=http
``` 

And deploy the application using `docker-compose` command as usual. Ignore any information about SSL certificates as
they are not available for localhost domain.

### Using a custom domain

By default, I am assuming that you are deploying this application as a docker container on a server on the cloud. That
means that probably you already have a domain that you must set on the application. Please, update `machine_domain`
variable on `docker/.env` to match your existing domain.

```
machine_domain=mydomain.com
[...]
#Frontend
protocol=https
``` 

Parameter `protocol` is assuming that you have also an SSL certificate. If is not the case, please change to `http`.

## Using a different database engine.

If you want to use a different database engine, add the correct jar dependency with the jdbc connector
in `backend/libraries`. Configure these specific variables in the `.env` file:

```
database_type=postgresql  (hsqldb, h2, oracle, mysql, postgresql, ...)
database_name=database
database_password=mypass
database_user=myuser
database_port=5432
```

## Security passwords

Variable `jwt_secret` is used for encrypting JWT token related to the REST API authorization. Please change it and avoid
using the default one. If `jwt_secret` is left empty, the system will generate a random one on start. Random is more
secure, but any user will be forced to log in into the system again if the server is restarted.

Variable `database_encryption_key` will encrypt the database content, to ensure a higher level of privacy. If you want
to check the content of your database using any other external software, please leave this variable with a blank value.
Otherwise, change the default value to any other that only you know.

# Using REVERSE PROXY and Generating SSL certificates for the first time

The reverse proxy is prepared to use let's encrypt certificates. First time you run it, rproxy will listen to
ports `80` and `8080`. SSL configuration is commented, as we need to rproxy starts to generate the SSL certificates.

Create a folder where store the certificates on the server:

```
mkdir -p /etc/letsencrypt
```

Update two files:

- On docker-compose.yml, enable the port redirection `80` on `kendo-tournament-rproxy` definition by uncommenting the
  line `# - "80:80"`
- On `docker/rproxy/config/servers.conf` change the server listening on port `80` to:

```
server {
  listen       80;
  listen [::]:80;
  server_name MACHINE_DOMAIN;

  location ^~ /.well-known {
      allow all;
      root  /data/letsencrypt/;
  }
  
  # Configure http to redirect to https;
  #location / {
  #  return 301 https://$host$request_uri;
  #}
}
```

Now, try to generate your certificates, running in your machine where the docker containers are installed:

```
/usr/bin/docker run -t --rm -v  /etc/letsencrypt:/etc/letsencrypt deliverous/certbot certonly -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot --webroot-path=/data/letsencrypt -d <<my-domain>>
```

Remember to change `<<my-email>>` and `<<my-domain>>` with valid values.

## Configure reverse proxy server

If the certificates are generated successfully, now you must configure the reverse proxy to use them. Edit the
file `rproxy/config/servers.conf` and:

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

Next thing to do, is enabling http to https redirection, uncommenting these lines (that are commented if you are
deploying certificates using a docker container, as described above):

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

Your certificates usually expire in 90 days. For the renewal of the SSL certificates, you need to configure your cron
system to ask for the renewal time by time. An
example, where is asking for a renewal each sunday at 5am:

```
PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

0 5 * * 7 /usr/bin/docker run -t --rm -v  docker_ssl-certificates:/etc/letsencrypt -v  docker_ssl-certificates-data:/data/letsencrypt deliverous/certbot certonly -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot --webroot-path=/data/letsencrypt -d <<my-domain>> && /usr/bin/docker restart kendo-tournament-rproxy >/dev/null 2>&1
```

If using this example, as before remember to change `<<my-email>>` and `<<my-domain>>` with valid values.

# Creating SSL Certificates on a RaspberryPi (arm architecture)

Certbot docker container is not available for `arm` architectures, then we need to use the standard `certbot`
application.

Install it as any other Ubuntu standard application (or use the correct command for other Linux distributions):

```
sudo apt install certbot
```

Ensure that port 80 is not used by any other docker container as the reverse proxy, and also ensure that is available on
the server and not blocked by any firewall. Let's Encrypt must access to your port 80. That means that must be
publicly visible on Internet.

And execute the certbot:

```
certbot certonly --standalone -d <<my-domain>> -m <<my-email>> --agree-tos --force-renew --no-eff-email --webroot-path=/data/letsencrypt
```

Remember to change `<<my-email>>` and `<<my-domain>>` with valid values.

After this, if you see the success message, you can configure the reverse proxy server as explained above.

# Ignoring SSL Certificates in Reverse Proxy

If you do not want to use SSL certificates, you need to change a few lines of the NGINX reverse proxy. Please edit
the `rproxy/config/severs.conf` file and change the next lines:

```
server {                            
  listen 80; 
  server_name <your ip>;
  
  ...
}    
```

And comment completely the second server configuration that includes the `location ^~ /.well-known` rule.

Remember also to change the protocol to `http` as described above in this document.

You can also modify the final file `/etc/nginx/conf.d/default.conf` located inside the `kendo-tournament-rproxy` docker
container. But be careful as
any rebuild of the container will destroy any change you do here.
