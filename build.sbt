import org.scalajs.sbtplugin.cross.CrossProject
import sbt.Keys._
import sbt.Project.projectToRef

import sys._

// define the hadoop version
val hadoopVersion = "2.7.1"

// Settings for client and server
lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "qrygraph",
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-encoding", "utf8"
  ),
  // add resolvers for dependencies
  resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases"),
  resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  resolvers += "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  resolvers += "theatr.us" at "http://repo.theatr.us",
  // allow compilation to custom target (e.g. ram-disk) when env var is set
  target <<= (organization, moduleName, baseDirectory) { (organization, moduleName, base) =>
    sys.env.get("SBT_COMPILE_TARGET") match {
      case Some(myTarget) =>
        val newTarget = s"$myTarget/compileTarget/$organization/$moduleName/"
        println(s"! Target Folder Overwrite: $newTarget")
        file(newTarget)
      case None           => base / "target"
    }
  }
)

// the root project used to aggregate both client and server
lazy val root = project.in(file(".")).
  aggregate(qrygraphJS, qrygraphJVM).
  settings(
    publish := {},
    publishLocal := {}
  )

/** codegen project containing the customized code generator */
lazy val codegen = project.in(file("codegen"))
  .settings(commonSettings: _*)
  .settings(
    moduleName := "codegen",
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick-codegen" % "3.1.1",
      "com.typesafe.slick" %% "slick" % "3.1.1",
      "com.h2database" % "h2" % "1.4.187"
    ))

/** list of all js projects */
lazy val jsProjects = Seq(qrygraphJS)

/** the qrygraph project */
lazy val qrygraph = (crossProject.crossType(CrossType.Full) in file("qrygraph")).settings(commonSettings: _*).
  // Settings for the Play Framework
  jvmSettings(
    moduleName := "qrygraphjvm",
    // add jar dependencies
    unmanagedBase := baseDirectory.value / "lib",
    // add dependencies
    libraryDependencies ++= Seq(
      // used for password bcrypting
      "org.mindrot" % "jbcrypt" % "0.3m",
      // Serialization
      "com.github.benhutchison" %% "prickle" % "1.1.10",
      // Hadoop
      "org.apache.hadoop" % "hadoop-common" % hadoopVersion exclude("org.slf4j", "slf4j-log4j12"),
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % hadoopVersion exclude("org.slf4j", "slf4j-log4j12"),
      "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % hadoopVersion exclude("org.slf4j", "slf4j-log4j12"),
      "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion exclude("org.slf4j", "slf4j-log4j12"),
      "org.apache.hadoop" % "hadoop-auth" % hadoopVersion exclude("org.slf4j", "slf4j-log4j12"),
      // Logging
      "org.fusesource.jansi" % "jansi" % "1.9",
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      // Pig
      "org.apache.pig" % "pig" % "0.16.0" classifier "h2",
      // ORM
      "com.typesafe.play" %% "play-slick" % "2.0.0",
      "com.typesafe.play" %% "play-slick-evolutions" % "2.0.0",
      "com.typesafe.slick" %% "slick-codegen" % "3.1.1",
      // Sample DB
      "com.h2database" % "h2" % "1.4.191",
      // Execution scheduling
      "us.theatr" %% "akka-quartz" % "0.3.0",
      // Testing
      "org.scalatest" %% "scalatest" % "3.0.0" % "test",
      //Logging
      "biz.enef" %% "slogging-slf4j" % "0.5.0"
    ),
    // remove duplicated logger implementations
    libraryDependencies ~= {
      _.map(_.exclude("org.slf4j", "slf4j-jdk12"))
    },
    // register manual sbt command
    slick <<= slickCodeGenTask,
    // register automatic code generation on every compile, remove for only manual use
    //sourceGenerators in Compile <+= slickCodeGenTask
    // add docker deployment
    imageNames in docker := Seq(
      // Sets the latest tag
      ImageName(s"starofall/qrygraph:latest"),
      // Sets a name with a tag that contains the project version
      ImageName(
        namespace = Some("starofall"),
        repository = "qrygraph",
        tag = Some("v" + version.value)
      )
    ),
    dockerfile in docker := {
      val appDir = stage.value
      println("appdir: " + appDir)
      val targetDir = "/app"
      val entry = s"$targetDir/bin/${executableScriptName.value}"
      new Dockerfile {
        from("java")
        copy(appDir, targetDir)
        run("mkdir", "/app/home")
        run("chmod", "+x", entry)
        entryPoint(entry, "-Dconfig.resource=application.prod.conf", "-Duser.home=/app/home")
        expose(8080)
      }
    },
    buildOptions in docker := BuildOptions(cache = false),
    // no documentation for docker
    sources in(Compile, doc) := Seq.empty,
    publishArtifact in(Compile, packageDoc) := false,
    // <fix> bug with to many dependencies on windows see: https://github.com/sbt/sbt-native-packager/issues/72
    scriptClasspath := {
      import com.typesafe.sbt.SbtNativePackager._
      import JavaAppPackaging.autoImport._
      val classpath = scriptClasspathOrdering.map(
        (mappings: Seq[(File, String)]) => for {(file, name) <- mappings} yield {
          if (name startsWith "lib/") name drop 4 else "../" + name
        }
      ).value
      val manifest = new java.util.jar.Manifest()
      manifest.getMainAttributes.putValue("Class-Path", classpath.mkString(" "))
      val classpathJar = (target in Universal).value / "lib" / "classpath.jar"
      IO.jar(Seq.empty, classpathJar, manifest)
      // also apply conf for the classpath of play
      Seq("../conf", "classpath.jar")
    },
    mappings in Universal += ((target in Universal).value / "lib" / "classpath.jar", "lib/classpath.jar")
    // <fix/>
  ).
  // Settings for the frontend
  jsSettings(
    moduleName := "qrygraphjs",
    // Create a launcher that starts our js
    persistLauncher := true,
    persistLauncher in Test := false,
    // Enable loading of akka.js
    // we currently use local folders, as there was no good availability on maven
    unmanagedBase := baseDirectory.value / "lib",
    unmanagedJars in Compile ++= (file("qrygraph/js/") ** "*.jar").classpath,
    jsDependencies += RuntimeDOM,
    libraryDependencies ++= Seq(
      // Dom Access and JQuery
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "be.doeraene" %%% "scalajs-jquery" % "0.8.1",
      // Data serialization
      "com.github.benhutchison" %%% "prickle" % "1.1.10",
      // Testing
      "org.scalatest" %%% "scalatest" % "3.0.0" % "test",
      // Logging
      "biz.enef" %%% "slogging" % "0.5.0"
    )
  ).
  // Enable ScalaJS, Play and Docker integration
  jsConfigure(_ enablePlugins(ScalaJSPlay, ScalaJSPlugin)).
  jvmConfigure(_ enablePlugins(PlayScala, sbtdocker.DockerPlugin, JavaAppPackaging))

// direct accessing the jvm and js parts of the project
lazy val qrygraphJS = qrygraph.js
lazy val qrygraphJVM = qrygraph.jvm.settings(
  scalaJSProjects := jsProjects,
  pipelineStages := Seq(scalaJSProd)
).aggregate(jsProjects.map(projectToRef): _*)
  .dependsOn(codegen)

// code generation task
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceDirectory, dependencyClasspath in Compile, runner in Compile, streams).map((dir, cp, r, s) => {
  val outputDir = new File("./qrygraph/jvm/app/").getPath
  toError(r.run("codegen.CustomizedCodeGenerator", cp.files, Array(outputDir), s.log))
  val fname = outputDir + "/Tables.scala"
  Seq(file(fname))
})

// loads the Play project at sbt startup
onLoad in Global := (Command.process("project qrygraphJVM", _: State)) compose (onLoad in Global).value