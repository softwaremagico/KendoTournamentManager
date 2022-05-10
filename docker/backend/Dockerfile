FROM openjdk:8-jdk-alpine

ARG release
ARG machine_domain
ARG database_user
ARG database_password
ARG database_name
ARG database_type
ARG database_dialect
ARG database_port
ARG database_encryption_key
ARG jwt_secret

ENV project_name kendo-tournament-backend
ENV EXTERNAL_CONFIG_FILE /opt/${project_name}/application.properties

#Set timezone.
ENV TZ=Europe/Madrid
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#Copy libraries
RUN mkdir -p /opt/libraries
ADD libraries/* /opt/libraries

#Download project and install it
RUN apk --no-cache add supervisor openssl zip unzip && \
    mkdir -p /opt/${project_name} && \
    cd /opt/${project_name} && \
    wget https://github.com/softwaremagico/KendoTournamentManager/releases/download/${release}/kendo-tournament-backend.jar && \
    unzip -qqq kendo-tournament-backend.jar -d /opt/${project_name}/${project_name} && \
    cp /opt/libraries/* /opt/${project_name}/${project_name}/BOOT-INF/lib/ && \
    zip kendo-tournament-backend.jar kendo-tournament-backend && \
    rm -r /opt/${project_name}/${project_name} && \
    rm -r /opt/libraries && \
    apk del openssl zip unzip

#Copy configuration files.
COPY config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf
COPY config/application.properties ${EXTERNAL_CONFIG_FILE}

#Configure files
RUN  sed -i "s|PROJECT_FOLDER|/opt/${project_name}|g" /etc/supervisor/conf.d/supervisord.conf && \
     sed -i "s|PROJECT_NAME|${project_name}|g" /etc/supervisor/conf.d/supervisord.conf && \
     sed -i "s|MACHINE_DOMAIN|${machine_domain}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_USER|${database_user}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_PASSWORD|${database_password}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_NAME|${database_name}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_TYPE|${database_type}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_DIALECT|${database_dialect}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_PORT|${database_port}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|DATABASE_ENCRYPTION_KEY|${database_encryption_key}|g" ${EXTERNAL_CONFIG_FILE} && \
     sed -i "s|JWT_SECRET|${jwt_secret}|g" ${EXTERNAL_CONFIG_FILE}

# EXEC
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
