/* =========================================================================================
 * Copyright © 2013-2019 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

val kamonVersion = "2.6.0"

val kamonCore    = "io.kamon" %% "kamon-core"                   % kamonVersion
val kamonTestkit = "io.kamon" %% "kamon-testkit"                % kamonVersion
val kamonCommon  = "io.kamon" %% "kamon-instrumentation-common" % kamonVersion

// copy pasted from https://github.com/kamon-io/kamon-sbt-umbrella
val logbackClassic = "ch.qos.logback"   %  "logback-classic" % "1.2.3"
val scalatest      = "org.scalatest"    %% "scalatest"       % "3.0.9"
def compileScope(deps: ModuleID*): Seq[ModuleID]  = deps map (_ % "compile")
def testScope(deps: ModuleID*): Seq[ModuleID]     = deps map (_ % "test")
// end copy pasted section

def http4sDeps(version: String) = Seq(
  "org.http4s" %% "http4s-client"       % version % Provided,
  "org.http4s" %% "http4s-server"       % version % Provided,
  "org.http4s" %% "http4s-ember-client" % version % Test,
  "org.http4s" %% "http4s-ember-server" % version % Test,
  "org.http4s" %% "http4s-dsl"          % version % Test
)

lazy val shared = Seq(
  scalaVersion := "2.13.10",
  
  moduleName := name.value,
  publishTo := sonatypePublishToBundle.value,
  scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => Seq("-Ypartial-unification", "-language:higherKinds")
    case _             => "-language:higherKinds" :: Nil
  }),
  libraryDependencies ++=
    compileScope(kamonCore, kamonCommon) ++
      testScope(scalatest, kamonTestkit, logbackClassic),
  Test / parallelExecution := false,
  scmInfo := Some(
    ScmInfo(url("https://github.com/codacy/kamon-http4s"), "scm:git@github.com:codacy/kamon-http4s.git")
  ),
  // this setting is not picked up properly from the plugin
  pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)
) ++ publicMvnPublish

lazy val `kamon-http4s-0_23` = project
  .in(file("modules/0.23"))
  .settings(
    shared,
    crossScalaVersions := Seq("2.12.17", "2.13.10"),
    name := "kamon-http4s-0.23",
    libraryDependencies ++= http4sDeps("0.23.18")
  )

lazy val `kamon-http4s-1_0` = project
  .in(file("modules/1.0"))
  .settings(
    shared,
    crossScalaVersions := Seq("2.13.10"),
    name := "kamon-http4s-1.0",
    libraryDependencies ++= http4sDeps("1.0.0-M38")
  )

lazy val root = project
  .in(file("."))
  .settings(
    shared,
    name := "kamon-http4s",
    publish / skip := true,
    Test / parallelExecution := false,
    Global / concurrentRestrictions += Tags.limit(Tags.Test, 1),
    pgpPassphrase := Option(System.getenv("SONATYPE_GPG_PASSPHRASE")).map(_.toCharArray)
  )
  .aggregate(`kamon-http4s-0_23`, `kamon-http4s-1_0`)
