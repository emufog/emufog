FROM phusion/baseimage

LABEL maintainer="Robin Finkbeiner robin@finkbeiner.email"

# Set correct environment variables.
ENV  HOME /root
ENV  LANG en_US.UTF-8
ENV  LC_ALL en_US.UTF-8
ENV  DEBIAN_FRONTEND  noninteractive

# set sane locale
RUN locale-gen en_US.UTF-8

RUN apt-get -y update \
  && apt-get -y install software-properties-common python-software-properties \
  && add-apt-repository ppa:webupd8team/java \
  && apt-get -y update \
  && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections \
  && apt-get -y install oracle-java8-installer \
  && apt-get clean \
  && update-alternatives --display java \
  && rm -rf /build \
  && rm -rf /tmp/* /var/tmp/* \
  && rm -rf /var/lib/apt/lists/* \
  && apt-get remove -y x11-common software-properties-common python-software-properties python3-software-properties

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle


RUN apt-get update && apt-get install -y \
    net-tools \
    iputils-ping \
    iproute

ENV YCSB_VERSION 0.12.0

RUN curl --progress-bar -sLo /tmp/ycsb-${YCSB_VERSION}.tar.gz  https://github.com/brianfrankcooper/YCSB/releases/download/${YCSB_VERSION}/ycsb-${YCSB_VERSION}.tar.gz \
  && cd /opt \
  && tar -xvf /tmp/ycsb-${YCSB_VERSION}.tar.gz \
  && mv ycsb-${YCSB_VERSION} /opt/ycsb \
  && rm -rf /tmp/ycsb-${YCSB_VERSION}.tar.gz \
  && ln -s /opt/ycsb/bin/ycsb /usr/local/bin/ycsb

COPY docker-entrypoint.sh /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]