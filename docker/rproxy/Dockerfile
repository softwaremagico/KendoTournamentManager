FROM nginx:stable-alpine

ARG machine_domain
ARG frontend_path

#Set timezone.
ENV TZ=Europe/Madrid
RUN apk add --no-cache curl tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY config/servers.conf  /etc/nginx/conf.d/default.conf

#Update Domain
RUN sed -i "s|MACHINE_DOMAIN|${machine_domain}|g" /etc/nginx/conf.d/default.conf && \
    sed -i "s|FRONTEND_PATH|${frontend_path}|g" /etc/nginx/conf.d/default.conf

CMD ["nginx", "-g", "daemon off;"]
