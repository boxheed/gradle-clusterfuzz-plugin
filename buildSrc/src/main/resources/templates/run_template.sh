#!/usr/bin/env bash

# SPDX-License-Identifier: Apache-2.0
SCRIPT_DIR=\$( cd -- "\$( dirname -- "\${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

cd \$SCRIPT_DIR

echo -e "###################################################"
echo -e  "#  __ _ _ __ __ _  __| | | ___                    #"
echo -e  "# / _\\` | '__/ _\\` |/ _\\` | |/ _ \\                   #"
echo -e  "#| (_| | | | (_| | (_| | |  __/                   #"
echo -e  "# \\__, |_|  \\__,_|\\__,_|_|\\___|                   #"
echo -e  "# |___/                                           #"
echo -e  "#      _           _             __               #"
echo -e  "#  ___| |_   _ ___| |_ ___ _ __ / _|_   _ ________#"
echo -e  "# / __| | | | / __| __/ _ \\ '__| |_| | | |_  /_  /#"
echo -e  "#| (__| | |_| \\__ \\ ||  __/ |  |  _| |_| |/ / / / #"
echo -e  "# \\___|_|\\__,_|___/\\__\\___|_|  |_|  \\__,_/___/___|#"
echo -e  "#                                                 #"
echo -e  "#       _             _                           #"
echo -e  "# _ __ | |_   _  __ _(_)_ __                      #"
echo -e  "#| '_ \\| | | | |/ _\\` | | '_ \\                     #"
echo -e  "#| |_) | | |_| | (_| | | | | |                    #"
echo -e  "#| .__/|_|\\__,_|\\__, |_|_| |_|                    #"
echo -e  "#|_|            |___/                             #"
echo -e  "###################################################"
echo -e  " Project: $project.name"
echo -e  " Version: $project.version"
echo -e  " Start  :" `date`
echo -e  " Dir    : \$SCRIPT_DIR"
echo -e  "###################################################"

set -e

find ../ -name 'runTest.sh' | while read line; do
    echo "Processing file '$line'"
    bash $line
done


echo -e  "###################################################"
echo -e  " Finshed:" `date`
echo -e  "###################################################"