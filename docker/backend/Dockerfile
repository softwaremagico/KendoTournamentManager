FROM eclipse-temurin:22-jdk-alpine

ARG release
ARG machine_domain
ARG protocol
ARG database_user
ARG database_password
ARG database_name
ARG database_type
ARG database_dialect
ARG database_port
ARG database_encryption_key
ARG jwt_expiration
ARG jwt_guest_expiration
ARG jwt_secret
ARG jwt_ip_check
ARG timezone
ARG enable_guest_user
ARG database_populate_default_data

ENV project_name kendo-tournament-backend
ENV EXTERNAL_CONFIG_FILE /opt/${project_name}/application.properties
ENV JAVA_HOME /usr/lib/jvm/java-16-openjdk
ENV PATH "$PATH:${JAVA_HOME}/bin"

#Set timezone.
ENV TZ=${timezone}
RUN ln -snf /usr/share/zoneinfo/"$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

#Copy libraries
RUN mkdir -p /opt/libraries/BOOT-INF/lib/
ADD libraries/* /opt/libraries/BOOT-INF/lib/

#Download project and install it
RUN apk --no-cache add curl openssl supervisor unzip zip && \
    mkdir -p /opt/${project_name} && \
    cd /opt/${project_name} && \
    wget https://github.com/softwaremagico/KendoTournamentManager/releases/download/${release}/kendo-tournament-backend.jar && \
    unzip -qqq /opt/${project_name}/${project_name}.jar -d /opt/${project_name}/${project_name} && \
    #rm /opt/${project_name}/${project_name}.jar && \
    # update postgresql connector
    #cp /opt/libraries/* /opt/${project_name}/${project_name}/BOOT-INF/lib/ && \
    #rm /opt/${project_name}/${project_name}/BOOT-INF/lib/mysql-connector* && \
    #Update properties
    sed -i "s|database\.encryption\.key.*|database\.encryption\.key=${database_encryption_key}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|jwt\.expiration.*|jwt.expiration=${jwt_expiration}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|jwt\.guest\.expiration.*|jwt.guest.expiration=${jwt_guest_expiration}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|jwt\.secret.*|jwt.secret=${jwt_secret}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|jwt\.ip\.check.*|jwt.ip.check=${jwt_ip_check}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.kendo\.datasource\.platform.*|spring.kendo.datasource.platform=${database_type}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.kendo\.datasource\.jpa\.hibernate\.ddl-auto.*|spring.kendo.datasource.jpa.hibernate.ddl-auto=update|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.sql\.init\.data-locations\.*|#spring.sql.init.data-locations|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.datasource\.data\.*||g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.jpa\.properties\.hibernate\.dialect.*|spring.jpa.properties.hibernate.dialect=${database_dialect}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.kendo\.datasource\.jdbc-url.*|spring.kendo.datasource.jdbc-url=jdbc:${database_type}://kendo-tournament-database:${database_port}/${database_name}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.kendo\.datasource\.username.*|spring.kendo.datasource.username=${database_user}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.kendo\.datasource\.password.*|spring.kendo.datasource.password=${database_password}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.jpa\.properties\.hibernate\.dialect.*|spring.jpa.properties.hibernate.dialect=${database_dialect}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|server\.domain.*|server.domain=${machine_domain}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|server\.schema.*|server.schema=${protocol}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|server\.servlet\.context-path.*|server.servlet.context-path=/${project_name}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|enable\.guest\.user.*|enable.guest.user=${enable_guest_user}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    sed -i "s|spring\.sql\.init\.mode.*|spring.sql.init.mode=${database_populate_default_data}|g" /opt/${project_name}/${project_name}/BOOT-INF/classes/application.properties  && \
    #Add the modified files to the jar again
    cd /opt/${project_name}/${project_name} && \
    zip /opt/${project_name}/kendo-tournament-backend.jar BOOT-INF/classes/application.properties && \
    cd /opt/libraries && \
    zip -0 /opt/${project_name}/kendo-tournament-backend.jar BOOT-INF/lib/* && \
    zip -d /opt/${project_name}/kendo-tournament-backend.jar BOOT-INF/lib/mysql-connector* && \
    apk del openssl zip unzip

#Copy configuration files.
COPY config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#Configure files
RUN  sed -i "s|PROJECT_FOLDER|/opt/${project_name}|g" /etc/supervisor/conf.d/supervisord.conf && \
     sed -i "s|PROJECT_NAME|${project_name}|g" /etc/supervisor/conf.d/supervisord.conf

EXPOSE 8080

# EXEC
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
