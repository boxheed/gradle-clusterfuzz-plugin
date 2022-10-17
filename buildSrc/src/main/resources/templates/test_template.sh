#!/usr/bin/env bash

# SPDX-License-Identifier: Apache-2.0

# working directory
SCRIPT_DIR=\$( cd -- "\$( dirname -- "\${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

# define test class
TARGET='$class'

echo -e  "---------------------------------------------------"
echo -e  " Project: $project.name"
echo -e  " Version: $project.version"
echo -e  " Dir    :" `basename \$SCRIPT_DIR`
echo -e  " Target : $class"
echo -e  " Start  :" `date`
echo -e  "---------------------------------------------------"

set -e


cd \$SCRIPT_DIR
# define classpath
CP=''
for FILE in ../libs/*; do
  CP="\$FILE:\$CP"
done
# remove the last character as this will be a ':' seperator
CP=\${CP%?}


# Jazzer flags
FLGS='$flags'

# Jazzer options
OPTS='$options'

# Find the jazzer driver and agent these should be on the path
JZR_DRIVER=`which jazzer_driver`
JZR_AGENT=`which jazzer_agent_deploy.jar`


export TEST_TIMEOUT=30
#run the test
\$JZR_DRIVER --agent_path=\$JZR_AGENT --cp=\$CP --target_class=\$TARGET \$FLGS \$OPTS

echo -e  "---------------------------------------------------"
echo -e  " Target : $class"
echo -e  " Finshed:" `date`
echo -e  "---------------------------------------------------"