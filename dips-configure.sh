#!/bin/bash

CONFIG_FILE=""
HOST=""
PORT=""

while getopts ":c:h:p:" opt; do
  case $opt in
    h)
      HOST="-h $OPTARG"
      shift 2
      ;;
    p)
      PORT="-p $OPTARG"
      shift 2
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done

CONFIG_FILE=$@
java -classpath lib/*:target/scala-2.9.0.final/dips_2.9.0-1.0-alpha.jar dips.Coordinator $HOST $PORT $CONFIG_FILE