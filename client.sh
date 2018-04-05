#!/bin/bash 
set -e
. settings
#NODEIP="127.0.0.1"
#SEED="akka.tcp://testSystem@127.0.0.1:2551"
java -jar target/scala-2.12/clusterPlayground-assembly-1.0.jar -DSEEDNODE=$SEED -Dakka.remote.netty.tcp.port=0 -Dakka.remote.netty.tcp.hostname=$NODEIP