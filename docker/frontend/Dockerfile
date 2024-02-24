FROM nginx:alpine

ARG release
ARG machine_domain
ARG protocol
ARG websocket_protocol

ENV HTML_ROOT_FOLDER /www

#Set timezone.
ENV TZ=Europe/Madrid
RUN apk add --no-cache curl tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /${HTML_ROOT_FOLDER}/errors

# Download code
RUN apk --no-cache add supervisor openssl curl unzip && \
    mkdir -p /opt/${project_name} && \
    cd /opt/${project_name} && \
    wget https://github.com/softwaremagico/KendoTournamentManager/releases/download/${release}/kendo-tournament-frontend-docker.zip && \
    unzip kendo-tournament-frontend-docker.zip && \
    mv frontend/* ${HTML_ROOT_FOLDER} && \
    rm -r frontend && \
    rm kendo-tournament-frontend-docker.zip && \
    apk del openssl

COPY files/404/* ${HTML_ROOT_FOLDER}/errors/

EXPOSE 4200

COPY config/servers.conf  /etc/nginx/conf.d/default.conf

#Update Root Folder
RUN sed -i "s|ROOT_FOLDER|${HTML_ROOT_FOLDER}|g" /etc/nginx/conf.d/default.conf && \
    sed -i "s|MACHINE_DOMAIN|${machine_domain}|g" ${HTML_ROOT_FOLDER}/main.*.js && \
    sed -i "s|PROTOCOL|${protocol}|g" ${HTML_ROOT_FOLDER}/main.*.js && \
    sed -i "s|WEBSOCKET_PROTOCOL|${websocket_protocol}|g" ${HTML_ROOT_FOLDER}/main.*.js

#Working in the app directory
WORKDIR ${HTML_ROOT_FOLDER}

CMD ["nginx", "-g", "daemon off;"]
