val Http4sVersion = "1.0.0-M23"
val CirceVersion = "0.14.1"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.2.3"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    organization := "com.github.rintcius",
    name := "http4s-clients-poc",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    libraryDependencies ++= Seq(
      "org.http4s"      %% "http4s-async-http-client" % Http4sVersion,
      "org.http4s"      %% "http4s-blaze-client"      % Http4sVersion,
      "org.http4s"      %% "http4s-ember-client"      % Http4sVersion,
      "org.http4s"      %% "http4s-jetty-client"      % Http4sVersion,
      "org.http4s"      %% "http4s-okhttp-client"     % Http4sVersion,
      "org.http4s"      %% "http4s-circe"             % Http4sVersion,
      "org.http4s"      %% "http4s-dsl"               % Http4sVersion,
      // "ch.qos.logback"  %  "logback-classic"          % LogbackVersion,
      "io.circe"        %% "circe-generic"            % CirceVersion,
      "org.scalameta"   %% "munit"                    % MunitVersion           % Test,
      "org.typelevel"   %% "munit-cats-effect-3"      % MunitCatsEffectVersion % Test
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3"),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework")
  )
