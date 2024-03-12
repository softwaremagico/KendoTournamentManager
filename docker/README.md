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

# Using REVERSE PROXY and SSL certificates generation

Since version 2.13.7, the reverse proxy engine has been changed from Nginx to Traefik. Traefik is more automated than
Nginx, and it will handle all _Let's encrypt_ certificate generation automatically.

Remember to change `email` value on the `docker/.env` file as it is used for the certificates' registration:

```
email=myemail@domain.com
```

Regenerate again the proxy if needed with the next command:

```
docker-compose build --no-cache kendo-tournament-rproxy && docker-compose up -d kendo-tournament-rproxy
```

## Disable the REVERSE PROXY

If you have your custom reverse proxy that is shared between other applications, maybe you need to disable the provided
one with this application, as some ports will collide. The best approach would be to force to not start the container in
the `docker-compose.yml` file. Search for the `kendo-tournament-rproxy` section and set the `donotstart` profile. You
can uncomment the lines that are already prepared for this purpose:

```
  kendo-tournament-rproxy:
      [...]
      # profiles:
      #      - donotstart
```

Uncomment to

```
  kendo-tournament-rproxy:
      [...]
       profiles:
            - donotstart
```

And the container will not be started automatically. Remember to configure your reverse proxy to include the `backend`
and `frontend` of this application or will not work properly. 
