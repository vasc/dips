#!/bin/bash

COUNT=""

while getopts ":c:h:p:" opt; do
  case $opt in
    c)
      COUNT=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done


gnome-terminal -e "java -classpath lib/*:target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Dips"

for i in `seq 2 $COUNT`
do
	gnome-terminal -e "java -classpath lib/*:target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Dips -h localhost -p 7653 -l 0"
done