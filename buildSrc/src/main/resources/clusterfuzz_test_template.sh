#!/usr/bin/env bash

#define classpath
CP='$classpath'

#define test class
TARGET='$class'

JZR_DRIVER=`which jazzer_driver`
JZR_AGENT=`which jazzer_agent_deploy.jar`

#run the test
\$JZR_DRIVER --agent_path=\$JZR_AGENT --cp=\$CP --target_class=\$TARGET