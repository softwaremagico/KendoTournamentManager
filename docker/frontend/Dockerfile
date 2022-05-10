FROM node:16-alpine

ARG frontend_path
ARG release

ENV project_name kendo-tournament-frontend

#Set timezone.
ENV TZ=Europe/Madrid
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create app directory
RUN mkdir -p /usr/src/

# Bundle app source
WORKDIR /usr/src/

# Download code
RUN apk --no-cache add supervisor openssl unzip && \
    mkdir -p /opt/${project_name} && \
    cd /opt/${project_name} && \
    wget https://github.com/softwaremagico/KendoTournamentManager/releases/download/${release}/kendo-tournament-frontend.zip && \
    unzip kendo-tournament-frontend.zip && \
    mv frontend/* . && \
    rm -r frontend && \
    rm kendo-tournament-frontend.zip && \
    apk del openssl

#Working in the app directory
WORKDIR /opt/${project_name}/

#Copy configuration files.
COPY config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#Configure files
RUN  sed -i "s|PROJECT_FOLDER|/opt/${project_name}|g" /etc/supervisor/conf.d/supervisord.conf && \
     sed -i "s|PROJECT_NAME|${project_name}|g" /etc/supervisor/conf.d/supervisord.conf

# Compile
RUN npm install -g @angular/cli && \
    npm install -timeout=240000 && \
    ng build --configuration=docker --base-href ${frontend_path}

# EXEC
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
