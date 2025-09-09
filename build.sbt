lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  //.enablePlugins(PlayNettyServer).disablePlugins(PlayPekkoHttpServer) // uncomment to use the Netty backend
  .settings(
    name := """open-agriculture-diary""",
    version := "1.0-SNAPSHOT",
    crossScalaVersions := Seq("2.13.16", "3.3.5"),
    scalaVersion := crossScalaVersions.value.head,
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      ws, // Web Service client for HTTP requests
      "org.postgresql" % "postgresql" % "42.7.3",
      "org.mindrot" % "jbcrypt" % "0.4",
      "me.gosimple" % "nbvcxz" % "1.5.1",
      // Play Mailer for email
      "org.playframework" %% "play-mailer" % "10.0.0",
      "org.playframework" %% "play-mailer-guice" % "10.0.0",
      // WebJars
      "org.webjars" % "bootstrap" % "5.3.0",
      "org.webjars.npm" % "zxcvbn" % "4.4.2",
      // Testing libraries for dealing with CompletionStage...
      "com.h2database" % "h2" % "2.3.232" % Test,
      "org.assertj" % "assertj-core" % "3.26.3" % Test,
      "org.awaitility" % "awaitility" % "4.2.2" % Test,
      "org.mockito" % "mockito-core" % "5.8.0" % Test,
    ),
    javacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation",
      "-Werror"
    ),
    (Test / javaOptions) += "-Dtestserver.port=19001",
    // Make verbose tests
    (Test / testOptions) := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
  )