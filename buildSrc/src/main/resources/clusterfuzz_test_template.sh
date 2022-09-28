#!/usr/bin/env bash

#define classpath
CP='$classpath'

#define test class
TARGET='$class'

JZR='./jazzer'

#run the test
\$JZR --cp=\$CP --target_class=\$TARGET