#!/bin/bash

curDir=$(dirname "$0")
if [[ -e ${curDir}/params.conf ]]; then
  source "${curDir}/params.conf"
else
  echo "params.conf not found"
  exit 127
fi

if [[ -z "${SCALE_FACTOR}" ]]; then
  echo "please set SCALE_FACTOR in params.conf"
  exit 127
else
  scaleFactor=${SCALE_FACTOR}
fi

if [[ -z "${DATA_LOCATION}" ]]; then
  echo "please set DATA_LOCATION in params.conf"
  exit 127
else
  dataDir=${DATA_LOCATION}
fi

sparkMaster="local[*]"

if [[ -n "${DATA_GEN_SPARK_MASTER}" ]]; then
  sparkMaster=${DATA_GEN_SPARK_MASTER}
fi

baseDir=$(cd "$(dirname "$0")"/../.. || exit; pwd -P)

dsdGenDir=${baseDir}/resource/tpcds-kit/tools

cd "${baseDir}" || exit

while true; do
    read -p "Do you wish to generate TPC-DS data in ${dataDir}? It will be override! (Y/N) " yn
    case $yn in
        [Yy]* ) make install; break;;
        [Nn]* ) exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

build/sbt "test:runMain com.databricks.spark.sql.perf.tpcds.GenTPCDSData -m ${sparkMaster} -d ${dsdGenDir} -s ${scaleFactor} -l ${dataDir} -f parquet"