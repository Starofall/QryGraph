# QryGraph - Pig Query Manager
![Screenshot](screenshot.png?raw=true "Screenshot")

QryGraph is a platform for managing Pig queries and creating and modifying queries in a graphical user interface.

## Install
- Start local docker container with Hadoop
```
docker run -d --name hadoop starofall/hadoop-docker
```

- Run the QryGraph docker linked to the Hadoop container 
```
docker run -d --name qrygraph -p 9999:8080 --link hadoop:hadoop starofall/qrygraph
```

- Access it using the browser
```
http://localhost:9999
```
(In OSX, instead of localhost, use the IP assigned by docker, default: 192.168.99.100)

- Enter default configuration
```
Hadoop-User: root
Directory: /user/root/
HDFS: hdfs://hadoop:9000
MapReduce: hadoop:19888
```
   
## Run it locally

- download and install sbt from http://www.scala-sbt.org/
- cd into the project directory
- start ```> sbt``` in the console
- run ```> ~run``` command in the sbt tool
- the browser opens and sbt loads all dependencies and compiles files
- (windows only) [Hadoop Native Librarys](https://github.com/Starofall/hadoop-windows)  must be installed and in path & HADOOP_HOME set

## Modules
#### JavaScript
The frontend module that handles the query editor and the webSocket connection
#### JVM / Play
Backend server hosting the web application and manages the queries and their execution
on the hadoop backend. It also contains the XML parser for the user config.
#### Shared
Contains all code that is used in the frontend and the backend. Mainly messages that are
send over the webSocket and data formats used on both sides.

## Libraries
#### Play Framework
The Play Framework is used for the web server hosting the platform and the query editor.
#### Play Slick
Functional database query tool and ORM mapper.
#### Scala.JS
Scala.JS compiles all files of the javascript module into native js.
#### Pickler
Used to serialize objects into json and back to objects.
