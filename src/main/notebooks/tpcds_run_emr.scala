val format = "parquet"
val timeout = 2 // timeout in hours
val exclude = false
val query_filter = Seq() // Seq() == all queries
//val query_filter = Seq("q1-v2.4", "q2-v2.4") // run subset of queries

import com.databricks.spark.sql.perf.tpcds.{TPCDSTables, TPCDS}

val tables = new TPCDSTables(spark.sqlContext, dsdgenDir = "", scaleFactor = scaleFactor)

spark.sqlContext.setConf("spark.sql.parquet.compression.codec", "snappy")
spark.conf.set("spark.sql.shuffle.partitions", "2000")
spark.sqlContext.setConf("spark.sql.files.maxRecordsPerFile", "20000000")
spark.conf.set("spark.sql.broadcastTimeout", "10000") // good idea for Q14, Q88.

tables.createTemporaryTables(rootDir, format)

val randomizeQueries = false // run queries in a random order. Recommended for parallel runs.

val tpcds = new TPCDS(spark.sqlContext)
def queries = {
  val filtered_queries = query_filter match {
    case Seq() => tpcds.tpcds2_4Queries
    case _ => tpcds.tpcds2_4Queries.filter(q => if (exclude) !query_filter.contains(q.name) else query_filter.contains(q.name))
  }
  if (randomizeQueries) scala.util.Random.shuffle(filtered_queries) else filtered_queries
}
val experiment = tpcds.runExperiment(
  queries,
  iterations = iterations,
  resultLocation = resultLocation,
  tags = Map("runtype" -> "benchmark", "database" -> "temp", "scale_factor" -> scaleFactor))

println(experiment.toString)
experiment.waitForFinish(timeout * 60 * 60)

import org.apache.spark.sql.functions.{col, substring}
val summary = experiment.getCurrentResults().withColumn("Name", substring(col("name"), 2, 100)).withColumn("Runtime", (col("parsingTime") + col("analysisTime") + col("optimizationTime") + col("planningTime") + col("executionTime")) / 1000.0).select("Name", "Runtime")
summary.show(summary.count.toInt, truncate = false)