FROM traefik:latest

ARG email

#Set timezone.
ENV TZ=Europe/Madrid
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

#Prepare folders structure
RUN mkdir -p /config

#Copy configuration
ADD config/* /config/
ADD traefik.yml /traefik.yml

# Replace email
RUN sed -i "s|CUSTOM_EMAIL|${email}|g" /traefik.yml


