#!/bin/bash

## This script will run both master and slave programs. It will get their pid's and print out the memory usage.
## The idea is to test them with diferent memory sizes in the jvm and diferent number of nodes.
## A file simulations/dist_sed.sim should be present with a string '@NODES@' where the number of nodes should be 
## placed. This will ensure that sed modifies the file correctly.

MEMORY_SIZES=( 128 256 512 1024 2048 )
NUMBER_OF_NODES=( 10000 50000 100000 200000 500000 1000000 2000000 4000000 6000000 8000000 )

RESULT_DIRECTORY="../simulations"
CONFIG_FILE="$RESULT_DIRECTORY/dist_sed.sim"

MASTER_PID=-1
SLAVER_PID=-1
SLAVER_PORT=-1

INVALIDATE=0

for size in ${MEMORY_SIZES[@]}
do
    for n in ${NUMBER_OF_NODES[@]}
    do
	echo ">> Memory size: $size Number of nodes: $n"
	#Prepare configuration file
	sed -e "s/@NODES@/$n/g" < $CONFIG_FILE > sed.out
	
	#Launch slaver and grab pid
	java -Xmx${size}M -jar dips.jar sed.out &> slaver.out &
	SLAVER_PID=$!
	echo "Slaver pid: $SLAVER_PID"

	#Give it some time to open slaver.out and write it
	sleep 3

	#Get port number
	SLAVER_PORT=`grep "PostOffice listening on port:" slaver.out | awk '{print $5}'`
	echo "Slaver port: $SLAVER_PORT"

	#Launch master and grab pid
	java -Xmx${size}M -jar dips.jar sed.out -h localhost -p $SLAVER_PORT &> master.out &
	MASTER_PID=$!
	echo "Master pid: $MASTER_PID"
	
	while true
	do
	    RES_S=`grep "Distributed simulation finished." slaver.out`
	    RES_M=`grep "Distributed simulation finished." master.out`
	    if [ "$RES_S" = "Distributed simulation finished." ] && [ "$RES_M" = "Distributed simulation finished." ]
	    then
		RES_S=""
		RES_M=""
		INVALIDATE=0
		break
	    fi
	    RES_S=`grep "OutOfMemoryError" slaver.out`
	    RES_M=`grep "OutOfMemoryError" master.out`
	    if [ "$RES_S" = "Exception in thread \"main\" java.lang.OutOfMemoryError: Java heap space" ] || [ "$RES_M" = "Exception in thread \"main\" java.lang.OutOfMemoryError: Java heap space" ]
	    then
		INVALIDATE=1
		RES_S=""
		RES_M=""
		break;
	    fi
	done

	#Kill both programs
	kill -9 $MASTER_PID 
	kill -9 $SLAVER_PID

	#Get memory usage in B
	if [ $INVALIDATE -eq 0 ]
	then
            grep "control.mo:" slaver.out > "$RESULT_DIRECTORY/${n}_nodes_with_${size}M_of_jvm_slaver.data"
	    grep "control.mo:" master.out > "$RESULT_DIRECTORY/${n}_nodes_with_${size}M_of_jvm_master.data"
	fi
    done
done

rm -rf sed.out slaver.out master.out

echo "FINISHED"