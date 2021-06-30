#!/bin/bash

curDir=$(dirname "$0")
if [[ -e ${curDir}/params.conf ]]; then
  source "${curDir}/params.conf"
else
  echo "params.conf not found"
  exit 127
fi

if [[ -z "${SPARK_DIR}" ]]; then
  echo "please set SPARK_DIR in params.conf"
  exit 127
else
  echo "SPARK_DIR: ${SPARK_DIR}"
fi

if [[ -z "${SCALE_FACTOR}" ]]; then
  echo "please set SCALE_FACTOR in params.conf"
  exit 127
else
  echo "SCALE_FACTOR: ${SCALE_FACTOR}"
fi

if [[ -z "${ITERATIONS}" ]]; then
  echo "please set ITERATIONS in params.conf"
  exit 127
else
  echo "ITERATIONS: ${ITERATIONS}"
fi

if [[ -z "${DATA_LOCATION}" ]]; then
  echo "please set DATA_LOCATION in params.conf"
  exit 127
else
  echo "DATA_LOCATION: ${DATA_LOCATION}"
fi

if [[ -z "${RESULT_LOCATION}" ]]; then
  echo "please set RESULT_LOCATION in params.conf"
  exit 127
else
  echo "RESULT_LOCATION: ${RESULT_LOCATION}"
fi

sparkMaster="local[*]"
if [[ -n "${TPCDS_RUN_SPARK_MASTER}" ]]; then
  sparkMaster=${TPCDS_RUN_SPARK_MASTER}
fi

baseDir=$(cd "$(dirname "$0")"/../.. || exit; pwd -P)
cd "${baseDir}" || exit;
build/sbt package
sparkSqlPerfJar=$(find "${baseDir}/target" -type f -name "spark-sql-perf_*.jar")
scalaScript=${baseDir}/src/main/notebooks/tpcds_run_emr.scala

rm -rf tmp_tpcds_run.scala
{
  echo "val scaleFactor = \"${SCALE_FACTOR}\""
  echo "val rootDir = \"${DATA_LOCATION}\""
  echo "val resultLocation = \"${RESULT_LOCATION}\""
  echo "val iterations = ${ITERATIONS}"
  cat "${scalaScript}"
} >> tmp_tpcds_run.scala

export SPARK_LOCAL_IP="127.0.0.1"
"${SPARK_DIR}"/bin/spark-shell --master "$sparkMaster" --executor-memory 1G --driver-memory 1G --jars "${sparkSqlPerfJar}" -I tmp_tpcds_run.scala
rm -rf tmp_tpcds_run.scala
