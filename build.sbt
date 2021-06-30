// Your sbt build file. Guides on how to write one can be found at
// http://www.scala-sbt.org/0.13/docs/index.html

name := "spark-sql-perf"

organization := "com.databricks"

scalaVersion := "2.12.10"

crossScalaVersions := Seq("2.12.10")

fullResolvers := Seq(
  "aliyun-maven-public" at "https://maven.aliyun.com/repository/public",
  "aliyun-maven-central" at "https://maven.aliyun.com/repository/central"
)

// sparkPackageName := "databricks/spark-sql-perf"

// All Spark Packages need a license
licenses := Seq("Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0"))

val sparkVersion = "3.0.0"

// sparkComponents ++= Seq("sql", "hive", "mllib")


initialCommands in console :=
  """
    |import org.apache.spark.sql._
    |import org.apache.spark.sql.functions._
    |import org.apache.spark.sql.types._
    |import org.apache.spark.sql.hive.test.TestHive
    |import TestHive.implicits
    |import TestHive.sql
    |
    |val sqlContext = TestHive
    |import sqlContext.implicits._
  """.stripMargin

libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.1"

libraryDependencies += "com.twitter" %% "util-jvm" % "6.45.0" % "provided"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

libraryDependencies += "org.yaml" % "snakeyaml" % "1.23"

libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"

libraryDependencies += "org.apache.spark" %% "spark-hive" % sparkVersion % "provided"

fork := true
