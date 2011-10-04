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
SIMULATION_INSTANCES=`echo "$PUB_DNS" | grep ec2 | awk '{print $1}'`

SIMULATION_INSTANCES=`echo "$SIMULATION_INSTANCES" | head -n $COUNT`

echo "$SIMULATION_INSTANCES" >scripts/latest_simulations

for instance in $SIMULATION_INSTANCES
do
	echo "running instance"
	PRIV_DNS=`echo "$PUB_DNS" | grep "$instance" | awk '{print $2}'`
	if [ $FIRST -eq 1 ]
	then 
		COORDINATOR_PRIV=$PRIV_DNS
		COORDINATOR_PUB=$instance 
		#gnome-terminal -e "ssh -t ubuntu@$instance bash -c \"./dips-launch\"" -t Coordinator
		cat /dev/null >ec2.output
		ssh -t ubuntu@$instance bash -c "./dips-launch" &>>ec2.output &
		FIRST=0
		sleep 2s
	else
		echo "Connecting at: $COORDINATOR_PRIV"
		#gnome-terminal -e "ssh -t ubuntu@$instance source \"./dips-launch -h $COORDINATOR_PRIV -p 7653 -l 0\""
		ssh -t ubuntu@$instance source "./dips-launch -h $COORDINATOR_PRIV -p 7653 -l 0; exit" >>/tmp/dipstest &

	fi
done

RUNNING=`ps aux | grep ubuntu@ec2`
echo "$RUNNING"

#echo "Press ENTER when ready"
#read null

OUT=0

while [ $OUT -lt $COUNT ]
do
	OUT=`grep "running Coordinator act method" ec2.output | wc -l`
	echo "###$OUT"
	sleep 3s
done

echo "Sending Configuration"

cd ..
./dips-configure.sh -h $COORDINATOR_PUB tests/infection_40000_performance.sim >/dev/null

RUNNING_COUNT=$COUNT

while [ $RUNNING_COUNT -gt "0" ]
do
	RUNNING_COUNT=`ps aux | grep -v grep | grep "bash -c ./dips-launch" | wc -l`
	echo "Still running: $RUNNING_COUNT" 
	sleep 3s
done

echo "Simulation done"


read null