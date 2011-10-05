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

#PUB_DNS=`ec2-describe-instances | grep INSTANCE | awk '{print $4,$5}'`
#PUB_DNS_COUNT=`echo "$PUB_DNS" | grep ec2 | wc -l`

#if [ $PUB_DNS_COUNT -lt $COUNT ]
#then
#	echo "Running $((COUNT - PUB_DNS_COUNT)) new instances..."
#	ec2-run-instances $AMI  -k default-vaio -t m1.small -n $((COUNT - PUB_DNS_COUNT))
#fi

#while [ $PUB_DNS_COUNT -lt $COUNT ]
#do
#	PUB_DNS=`ec2-describe-instances | grep INSTANCE | awk '{print $4,$5}'`
#	PUB_DNS_COUNT=`echo "$PUB_DNS" | grep ec2 | wc -l`
#done

instances=""

for i in `seq 1 $COUNT`
do
	read instance
	instances=`echo -e "$instance\n$instances"`
done



FIRST=1
#SIMULATION_INSTANCES=`echo "$PUB_DNS" | grep ec2 | awk '{print $1}'`

#SIMULATION_INSTANCES=`echo "$SIMULATION_INSTANCES" | head -n $COUNT`

SIMULATION_INSTANCES="$instances"

for dns in $SIMULATION_INSTANCES
do
	instance=`echo "$dns" | awk 'BEGIN{FS=":"}{print $1}'`
	PRIV_DNS=`echo "$dns" | awk 'BEGIN{FS=":"}{print $2}'`

	touch "/tmp/$instance.lock"
	
	ssh -t ubuntu@$instance pkill -9 java

	if [ $FIRST -eq 1 ]
	then 
		COORDINATOR_PRIV=$PRIV_DNS
		COORDINATOR_PUB=$instance 

		echo "Running coordinator: $instance"
		ssh -t ubuntu@$instance "./dips-launch" &>/tmp/$instance.output &
		FIRST=0
		sleep 5s
	else
		echo "Running slave, $instance connecting at: $COORDINATOR_PRIV"

		ssh -t ubuntu@$instance "./dips-launch -h $COORDINATOR_PRIV -p 7653 -l 0" &>/tmp/$instance.output &

	fi
done


DONE=0
WAIT_COUNTER=0
while [ $DONE -lt $COUNT ]
do
	sleep 2s
	DONE=0
	for dns in $SIMULATION_INSTANCES
	do
		i=`echo "$dns" | awk 'BEGIN{FS=":"}{print $1}'`
		ACTMETHOD=`grep "running Coordinator act method" /tmp/$i.output`
		#echo "$ACTMETHOD"
		if [ "$ACTMETHOD" ]
		then
			DONE=`expr $DONE + 1`
			echo "Instance $i is ready"
		else
			echo "Instance $i is not ready"
		fi
		

		WAIT_COUNTER=$(($WAIT_COUNTER + 1))
		if [ $WAIT_COUNTER -gt 180 ]
		then
			for dns in $SIMULATION_INSTANCES
			do
				instance=`echo "$dns" | awk 'BEGIN{FS=":"}{print $1}'`
				ssh -t ubuntu@instance pkill -9 java
			done
			exit
		fi
			
	done
done

echo "Sending Configuration"
sleep 2s

cd ..
./dips-configure.sh -h $COORDINATOR_PUB "./tests/$CONF_FILE" >/dev/null

RUNNING_COUNT=$COUNT
WAIT_COUNTER=0
while [ $RUNNING_COUNT -gt "0" ]
do
	echo "Checking running..."
	sleep 2s
	RUNNING_COUNT=$COUNT
	for dns in $SIMULATION_INSTANCES
	do
		i=`echo "$dns" | awk 'BEGIN{FS=":"}{print $1}'`
		RUNNING=`ps aux | grep -v grep | grep "ssh -t ubuntu@$i ./dips-launch"`
		if [ "$RUNNING" ]
		then
			echo "Instance $i is still running"
		else
			RUNNING_COUNT=`expr $RUNNING_COUNT - 1`
		fi
	done


	WAIT_COUNTER=$(($WAIT_COUNTER + 1))
	if [ $WAIT_COUNTER -gt 240 ]
	then
		for dns in $SIMULATION_INSTANCES
		do
			instance=`echo "$dns" | awk 'BEGIN{FS=":"}{print $1}'`
			echo "Killing $instance"
			ssh -t ubuntu@$instance pkill -9 java
		done
		exit
	fi

done

#	RUNNING_COUNT=`ps aux | grep -v grep | grep "bash -c ./dips-launch" | wc -l`
#	echo "Still running: $RUNNING_COUNT" 
#	sleep 3s

echo "Simulation done"

cd ./tests

pub=`echo "$SIMULATION_INSTANCES" | awk 'BEGIN{FS=":"}{print $1}'`

echo "$pub" | python ./scripts/performance.py

echo "Simulation data saved"
