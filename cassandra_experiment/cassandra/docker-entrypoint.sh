#!/bin/bash
set -e

echo "Sleeping for ${SLEEP_TIME}s"
sleep $SLEEP_TIME

# start the interface
echo "IP: ${IP}"
echo "Node Name: ${HOSTNAME}"
echo "XXXXXXXXXXXXXX"


# set @ again as maxinet calls differently
set -- /cassandra/bin/cassandra -f

echo 'First: '$@
# first arg is `-f` or `--some-option`
if [ "${1:0:1}" = '-' ]; then
  set -- /cassandra/bin/cassandra -f "$@"
fi

echo 'Second: '$@
# allow the container to be started with `--user`
#if [ "$1" = 'cassandra' -a "$(id -u)" = '0' ]; then
# chown -R cassandra /var/lib/cassandra /var/log/cassandra "$CASSANDRA_CONFIG"
# exec gosu cassandra "$BASH_SOURCE" "$@"
#fi

if [ "$1" = '/cassandra/bin/cassandra' ]; then
  : ${CASSANDRA_RPC_ADDRESS="$IP"}

  : ${CASSANDRA_LISTEN_ADDRESS='auto'}
  if [ "$CASSANDRA_LISTEN_ADDRESS" = 'auto' ]; then
    CASSANDRA_LISTEN_ADDRESS="$IP"
  fi

  : ${CASSANDRA_BROADCAST_ADDRESS="$CASSANDRA_LISTEN_ADDRESS"}

  if [ "$CASSANDRA_BROADCAST_ADDRESS" = 'auto' ]; then
    CASSANDRA_BROADCAST_ADDRESS="$IP"
  fi
  : ${CASSANDRA_BROADCAST_RPC_ADDRESS:=$CASSANDRA_BROADCAST_ADDRESS}

  if [ -n "${CASSANDRA_NAME:+1}" ]; then
    : ${CASSANDRA_SEEDS:="cassandra"}
  fi
  : ${CASSANDRA_SEEDS:="$CASSANDRA_BROADCAST_ADDRESS"}

  echo "CASSANDRA_BROADCAST_ADDRESS: ${CASSANDRA_BROADCAST_ADDRESS}"
  echo "CASSANDRA_LISTEN_ADDRESS: ${CASSANDRA_LISTEN_ADDRESS}"
  echo "CASSANDRA_RPC_ADDRESS: ${CASSANDRA_RPC_ADDRESS}"

  sed -ri 's/(- seeds:).*/\1 "'"$CASSANDRA_SEEDS"'"/' "$CASSANDRA_CONFIG/cassandra.yaml"

    sed -i '1 i MAX_HEAP_SIZE="6144M"' $CASSANDRA_CONFIG/cassandra-env.sh
    sed -i '1 i HEAP_NEWSIZE="4096M"' $CASSANDRA_CONFIG/cassandra-env.sh


  for yaml in \
    broadcast_address \
    broadcast_rpc_address \
    cluster_name \
    endpoint_snitch \
    listen_address \
    num_tokens \
    rpc_address \
    start_rpc \
    partitioner \
    initial_token \
  ; do
    var="CASSANDRA_${yaml^^}"
    val="${!var}"
    if [ "$val" ]; then
      sed -ri 's/^(# )?('"$yaml"':).*/\2 '"$val"'/' "$CASSANDRA_CONFIG/cassandra.yaml"
    fi
  done

  for rackdc in dc rack; do
    var="CASSANDRA_${rackdc^^}"
    val="${!var}"
    if [ "$val" ]; then
      sed -ri 's/^('"$rackdc"'=).*/\1 '"$val"'/' "$CASSANDRA_CONFIG/cassandra-rackdc.properties"
    fi
  done
fi

# Works only if you define Tables like: CreaTe if noT exisTs - oTherwise following loop runs infiniTely!

for f in docker-entrypoint-initdb.d/*; do
  case "$f" in
    *.cql) echo "$0: running $f" && until /cassandra/bin/cqlsh -f "$f"; do >&2 echo "Cassandra is unavailable - sleeping"; sleep 2; done & ;;
  esac
  echo
done

echo 'Third: '$@
exec "$@"