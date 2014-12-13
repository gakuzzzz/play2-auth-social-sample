name := """play2-auth-social-sample"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalikejdbc"      %% "scalikejdbc"                      % "2.2.+",
  "org.scalikejdbc"      %% "scalikejdbc-test"                 % "2.2.+"   % "test",
  "org.scalikejdbc"      %% "scalikejdbc-syntax-support-macro" % "2.2.+",
  "org.scalikejdbc"      %% "scalikejdbc-play-plugin"          % "2.3.+",
  "org.scalikejdbc"      %% "scalikejdbc-play-fixture-plugin"  % "2.3.+",
  "com.github.tototoshi" %% "play-flyway"                      % "1.1.4",
  "jp.t2v"               %% "play2-auth"                       % "0.13.0",
  "jp.t2v"               %% "play2-auth-test"                  % "0.13.0"  % "test",
  "com.h2database"        % "h2"                               % "1.4.+",
  "ch.qos.logback"        % "logback-classic"                  % "1.1.+",
  "org.twitter4j"         % "twitter4j-core"                   % "4.0.2"
)
