package com.aliyun.emr.benchmark.perf

import com.databricks.spark.sql.perf.tpcds.{TPCDS, TPCDSTables}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, substring}

object QueryBenchmark {
  def main(args: Array[String]): Unit = {
    val scaleFactor = "1"
    val useDecimal = true
    val useDate = true
    val iterations = 2
    val timeout = 60
    val query_filter = Seq()
    val randomizeQueries = false
    val resultLocation = "/tmp/performance-datasets/tpcds/results"

    val format = "parquet"
    val filterNull = false
    val rootDir = s"/tmp/performance-datasets/tpcds/sf$scaleFactor-$format/useDecimal=$useDecimal,useDate=$useDate,filterNull=$filterNull"

    val spark = SparkSession
      .builder()
      .config("spark.sql.parquet.compression.codec", "snappy")
      .config("spark.sql.shuffle.partitions", "2000")
      .config("spark.sql.files.maxRecordsPerFile", "20000000")
      .config("spark.sql.broadcastTimeout", "10000")
      .appName("Spark SQL Query Benchmark")
      .getOrCreate()

    val tables = new TPCDSTables(
      spark.sqlContext,
      dsdgenDir = "",
      scaleFactor = scaleFactor,
      useDoubleForDecimal = !useDecimal,
      useStringForDate = !useDate
    )

    tables.createTemporaryTables(rootDir, format)

    val tpcds = new TPCDS(spark.sqlContext)

    def queries = {
      val filtered_queries = query_filter match {
        case Seq() => tpcds.tpcds2_4Queries
        case _ => tpcds.tpcds2_4Queries.filter(q => query_filter.contains(q.name))
      }
      if (randomizeQueries) scala.util.Random.shuffle(filtered_queries) else filtered_queries
    }

    val experiment = tpcds.runExperiment(
      queries,
      iterations = iterations,
      resultLocation = resultLocation,
      tags = Map("runtype" -> "benchmark", "database" -> "temp", "scale_factor" -> scaleFactor)
    )

    println(experiment.toString)
    experiment.waitForFinish(timeout * 60 * 60)
    val summary = experiment.getCurrentResults()
      .withColumn("Name", substring(col("name"), 2, 100))
      .withColumn("Runtime", (col("parsingTime") + col("analysisTime") + col("optimizationTime") + col("planningTime") + col("executionTime")) / 1000.0)
      .select("Name", "Runtime")

    summary.show(false)
  }
}
