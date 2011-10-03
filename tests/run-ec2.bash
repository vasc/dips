#!/bin/bash

CONF_FILE=""
COUNT=1
AMI=ami-40053734

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
export SIMULATION_INSTANCES=`echo "$PUB_DNS" | grep ec2 | awk '{print $1}'`
for instance in $SIMULATION_INSTANCES
do
	echo "running instance"
	PRIV_DNS=`echo "$PUB_DNS" | grep "$instance" | awk '{print $2}'`
	if [ $FIRST -eq 1 ]
	then 
		COORDINATOR_PRIV=$PRIV_DNS
		COORDINATOR_PUB=$instance 
		gnome-terminal -e "ssh -t ubuntu@$instance bash -c \"./dips-launch\"" -t Coordinator
		FIRST=0
		sleep 2s
	else
		echo "Connecting at: $COORDINATOR_PRIV"
		gnome-terminal -e "ssh -t ubuntu@$instance source \"./dips-launch -h $COORDINATOR_PRIV -p 7653 -l 0\""
	fi
done

echo "Press ENTER when ready"
read null

cd ..
./dips-configure.sh -h $COORDINATOR_PUB tests/infection_10000_performance.sim


echo "$SIMULATION_INSTANCES"