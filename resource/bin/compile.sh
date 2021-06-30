unameOut="$(uname -s)"
case "${unameOut}" in
    Linux*)     machine=LINUX;;
    Darwin*)    machine=MACOS;;
    *)          echo "system not support"; exit 127;
esac
echo "compile tpcds-kit with ${machine}"

workDir=$(dirname "$0")/..

dsdGenToolsDir=${workDir}/tpcds-kit/tools
cd "${dsdGenToolsDir}" || exit

make clean
make OS=${machine}

echo "compile tpcds-kit success"