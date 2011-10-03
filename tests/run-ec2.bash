#!/bin/bash

CONF_FILE=""
COUNT=1
AMI=ami-36003242

while getopts ":a:c:n:" opt; do
  case $opt in
    a)
      AMI=$OPTARG
      ;;
    n)
      COUNT=$OPTARG
      ;;
    c)
      CONF_FILE=$OPTARG
      ;;
    \?)
      echo "Invalid option: -$OPTARG" >&2
      ;;
  esac
done

PUB_DNS=`ec2-describe-instances | grep INSTANCE | awk '{print $4,$5}'`
PUB_DNS_COUNT=`echo "$PUB_DNS" | grep ec2 | wc -l`

if [ $PUB_DNS_COUNT -lt $COUNT ]
then
	echo "Running $((COUNT - PUB_DNS_COUNT)) new instances..."
	ec2-run-instances $AMI  -k default-vaio -t m1.small -n $((COUNT - PUB_DNS_COUNT))
fi

while [ $PUB_DNS_COUNT -lt $COUNT ]
do
	PUB_DNS=`ec2-describe-instances | grep INSTANCE | awk '{print $4,$5}'`
	PUB_DNS_COUNT=`echo "$PUB_DNS" | grep ec2 | wc -l`
done

FIRST=1
for instance in `echo "$PUB_DNS" | grep ec2 | awk '{print $1}'`
do
	echo "running instance"
	PRIV_DNS=`echo "$PUB_DNS" | grep "$instance" | awk '{print $2}'`
	if [ $FIRST -eq 1 ]
	then 
		COORDINATOR_PRIV=$PRIV_DNS
		COORDINATOR_PUB=$instance 
		gnome-terminal -e "ssh ubuntu@$instance bash -c \"cd dips; java -classpath dips/lib/*:dips/target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Dips\"" -t Coordinator
		FIRST=0
	else
		echo "Connecting at: $COORDINATOR_PRIV"
		gnome-terminal -e "ssh ubuntu@$instance source \"cd dips; java -classpath dips/lib/*:dips/target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Dips -h $COORDINATOR_PRIV -p 7653 -l 0\""
	fi
done

ssh ubuntu@$COORDINATOR_PUB source "cd dips; ./dips-configure.sh tests/infection_single_instance_100000.sim"
