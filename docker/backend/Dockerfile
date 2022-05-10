FROM openjdk:8-jdk-alpine

ENV project_name kendo-tournament-backend

#Set timezone.
ENV TZ=Europe/Madrid
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#Download project and install it
RUN apk --no-cache add supervisor

#Copy configuration files.
COPY config/supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#Configure files
RUN  sed -i "s|PROJECT_FOLDER|/opt/${project_name}|g" /etc/supervisor/conf.d/supervisord.conf && \
     sed -i "s|PROJECT_NAME|${project_name}|g" /etc/supervisor/conf.d/supervisord.conf

# EXEC
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/supervisord.conf"]
