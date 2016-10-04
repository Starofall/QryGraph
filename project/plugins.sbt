// Comment to get more information during initialization
logLevel := Level.Warn

// resolvers
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.4")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.10")
addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.3.1")

// deployment
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")