FROM traefik:latest

ARG email
ARG timezone

#Set timezone.
ENV TZ=${timezone}
RUN ln -snf /usr/share/zoneinfo/"$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

#Prepare folders structure
RUN mkdir -p /config

#Copy configuration
COPY config/* /config/
COPY traefik.yml /traefik.yml

# Replace email
RUN sed -i "s|CUSTOM_EMAIL|${email}|g" /traefik.yml


