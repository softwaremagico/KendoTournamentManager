FROM node:14.20.0-alpine

ARG version

ARG release

ENV project_name kendo-tournament-frontend

#Enable more RAM to compile libsass
ENV NODE_OPTIONS="--max-old-space-size=750"

#Set timezone.
ENV TZ=Europe/Madrid
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create app directory
RUN mkdir -p /usr/src/

# Bundle app source
WORKDIR /usr/src/

# Download code
RUN apk --no-cache add supervisor openssl curl unzip build-base gcc && \
    mkdir -p /opt/${project_name} && \
    cd /opt/${project_name} && \
    wget https://github.com/softwaremagico/KendoTournamentManager/archive/refs/tags/${release}.zip && \
    unzip ${release}.zip && \
    mv KendoTournamentManager-${version}/frontend/* . && \
    rm -r KendoTournamentManager-${version} && \
    rm ${release}.zip && \
    apk del openssl

#Working in the app directory
WORKDIR /opt/${project_name}/

#Copy configuration files.
COPY config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#Configure files
RUN  sed -i "s|PROJECT_FOLDER|/opt/${project_name}|g" /etc/supervisor/conf.d/supervisord.conf && \
     sed -i "s|PROJECT_NAME|${project_name}|g" /etc/supervisor/conf.d/supervisord.conf

# Compile
RUN npm install -g @angular/cli --unsafe-perm=true --allow-root && \
    npm install -timeout=360000 && \
    ng build --configuration=docker --base-href /kendo-tournament-frontend

# EXEC
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
