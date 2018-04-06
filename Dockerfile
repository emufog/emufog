FROM java:8-jre-alpine

LABEL maintainer="Robin Finkbeiner robin@finkbeiner.email"

RUN apk add --no-cache --update bash

COPY emufog.jar /usr/app/emufog/

WORKDIR /usr/app/emufog/

CMD ["bash"]