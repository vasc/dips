# Dips

## Library Dependencies

Dips depends on the following libraries:

* [jep 2.3.0](https://s3-eu-west-1.amazonaws.com/tesebackup/jep-2.3.0.jar)
* [djep 1.0.0](https://s3-eu-west-1.amazonaws.com/tesebackup/djep-1.0.0.jar)
* [log4j 1.2.15](https://s3-eu-west-1.amazonaws.com/tesebackup/log4j-1.2.15.jar)
* [scala-library 2.9.1](https://s3-eu-west-1.amazonaws.com/tesebackup/scala-library.jar)

These libraries should be saved to the lib folder or otherwise be available in the classpath.

## Compile

**Dips** depends on Java 6 and [Scala 2.9.1](http://www.scala-lang.org/downloads).
  
**[sbt](https://github.com/harrah/xsbt)** is the recommend buid tool recommended.

The prefered way to compile is using **sbt**, once **sbt** is installed simply run:

	sbt compile

To package **Dips** to a jar (no dependencies will be added) run:

    sbt package

It is also possible to perform the compilation using the **scalac** compiler.


## Run

The simples way to run **Dips** is:
    
    sbt run

This will take care of all dependencies and run the simulator. However it lacks flexibility.

The prefered way to run the simulator is, form the root of the project:

    java -classpath lib/*:target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Dips

Default port is 7653.

Secondary instancies of the simulator must receive the host and port of one already runnig instances:

    java -classpath lib/*:target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Dips -h <host> -p <port>

In order for the simulator to actually perform a simulation it mus be configured, this command will and run a distributed simulation:

    java -classpath lib/*:target/scala-2.9.1.final/dips_2.9.1-1.0-alpha.jar dips.Coordinator -h <host> -p <port> <configfile>


