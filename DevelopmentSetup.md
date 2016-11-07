# How to create a dev environment

### Tools

* IntelliJ for editing
* SBT to build and run the application
    * `> run` to run the application
    * `> clean` to get rid of old files
    * `> genTables` to create Slick database abstractions
    
### Database

Slick is used as database abstraction. To generate the Slick DSL auto code generation is used.
This is part of the codegen project and can be started with `genTables`. 
Then the SQL in `qrygraph/jvm/conf/evolutions.default/full.sql` is applied to a in-memory database to create the new Schema code. 
    
### Hadoop

To run the tool a valid instance of Hadoop with the same version as specified in build.sbt is needed.
It is recommended to use the Hadoop Docker container from `starofall/hadoop-docker` as specified in the `Readme.md` .
Then the configuration used in the tool is depending on the Docker implementation.
```
Hadoop-User: root
Directory: /user/root/
HDFS: hdfs://$DOCKERHOST:9000
MapReduce: $DOCKERHOST:19888
```
