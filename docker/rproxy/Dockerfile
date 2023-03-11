FROM nginx:stable-alpine

ARG machine_domain
ENV project_name kendo-tournament-rproxy

#Set timezone.
ENV TZ=Europe/Madrid
RUN apk add --no-cache curl tzdata supervisor && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#Copy configuration files.
COPY config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY config/servers.conf  /etc/nginx/conf.d/default.conf

#Configure files
RUN  sed -i "s|PROJECT_NAME|${project_name}|g" /etc/supervisor/conf.d/supervisord.conf

#Update Domain
RUN sed -i "s|MACHINE_DOMAIN|${machine_domain}|g" /etc/nginx/conf.d/default.conf

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
