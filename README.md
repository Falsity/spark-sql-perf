# 天池EMR TPC-DS性能大赛Benchmark工具

Benchmark工具基于 [spark-sql-perf](https://github.com/databricks/spark-sql-perf) 定制修改，用于天池EMR TPC-DS性能大赛选手进行Spark优化自测。选手在天池提交Spark包进行评测时，也会使用相同的Benchmark工具在比赛提供的EMR集群上运行。TPC-DS测试数据集由 [tpcds-kit](https://github.com/databricks/tpcds-kit) 工具生成，已经集成在该项目`resource/tpcds-kit`目录中。

## 使用说明
### tpcds-kit工具编译
进入`resource/bin`目录，运行`compile.sh`脚本进行编译步骤，该编译脚本目前仅支持在LINUX和MACOS系统上进行编译，其他操作系统请自行参考 [tpcds-kit](https://github.com/databricks/tpcds-kit) 的使用说明进入`resource/tpcds-kit/tools`目录手动编译。
```bash
cd resource/bin
bash compile.sh
```

### 生成数据集
进入`resource/bin`目录，修改`params.conf`配置文件，指定如下配置项：
* SCALE_FACTOR：生成数据集规模，本地测试可选择1
* DATA_LOCATION：生成数据集的存储路径，需要预留有足够的存储空间
* DATA_GEN_SPARK_MASTER：生成数据集运行的spark程序的master，本地测试需要填写local[N]，其中N为所使用的CPU核心数

配置完成后，运行`datagen.sh`脚本进行数据生成（执行过程中dsdgen工具输出的stderr日志会被sbt判定为[error]日志，可以忽略）。
```bash
cd resource/bin
vim params.conf
bash datagen.sh
```

### 运行TPC-DS Benchmark测试
进入`resource/bin`目录，修改`params.conf`配置文件，指定如下配置项：
* SCALE_FACTOR：生成数据集规模，与生成数据集时配置内容相同，生成数据集配置后无需修改
* ITERATIONS：Benchmark执行轮数
* DATA_LOCATION：数据集存储目录，与生成数据集时配置内容相同，生成数据集配置后无需修改
* SPARK_DIR：选手优化后Spark安装目录
* TPCDS_RUN_SPARK_MASTER：Benchmark运行的spark程序的master，本地测试需要填写local[N]，其中N为所使用的CPU核心数

配置完成后，运行`run_tpcds.sh`脚本执行测试。
```bash
cd resource/bin
vim params.conf
bash run_tpcds.sh
```

Benchmark默认会执行所有生成的query，可以修改`src/main/notebooks/tpcds_run_emr.scala`脚本中的query_filter变量进行query指定。
```scala
val query_filter = Seq() // Seq() == all queries
val query_filter = Seq("q1-v2.4", "q2-v2.4") // run subset of queries
```

如果希望排除某些query不允许，可以将`exclude`变量设置为true。
```scala
val exclude = true
val query_filter = Seq("q77-v2.4") // all queries except q77
```
