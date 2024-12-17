/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

class ClusterfuzzTestDefinition {

    String testClass
    String testName
    String options
    String flags
    String corpus
    Map jacoco
    
}
