/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClusterfuzzPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterfuzzPlugin.class)

    public static final String CLUSTERFUZZ_PLUGIN_NAME = "clusterfuzz"

    public static final String CLUSTERFUZZ_SOURCESET_NAME = CLUSTERFUZZ_PLUGIN_NAME

    public static final String CLUSTERFUZZ_IMPLEMENTATION_CONFIGURATION_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Implementation"

    public static final String CLUSTERFUZZ_GROUP = "build"

    void apply(Project project) {
        createExtension(project)
        createSourceSet(project)
        createConfiguration(project)
        createTasks(project)
    }

    private void createExtension(project) {
        def config = project.container(ClusterfuzzTestConfig) { name ->
            new ClusterfuzzTestConfig(name)
        }

        project.extensions.add(CLUSTERFUZZ_PLUGIN_NAME, config)
    }

    private void createSourceSet(Project project) {
        def sourceSets = project.extensions.getByType(SourceSetContainer.class)

        def fuzzSourceSet = sourceSets.register(CLUSTERFUZZ_PLUGIN_NAME)
        project.afterEvaluate {
            def main = sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME)
            fuzzSourceSet.get().compileClasspath = fuzzSourceSet.get().compileClasspath.plus(main.get().output)
            fuzzSourceSet.get().runtimeClasspath = fuzzSourceSet.get().runtimeClasspath.plus(main.get().output)
        }
    }

    private void createConfiguration(Project project) {
        def configurations = project.getConfigurations()
        configurations.named(CLUSTERFUZZ_IMPLEMENTATION_CONFIGURATION_NAME) {
            it.extendsFrom(configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).get())
        }
    }

    private void createTasks(Project project) {
        ClusterfuzzJarTask.register(project)
        ClusterfuzzDependenciesTask.register(project)
        ClusterfuzzWriteRunScriptTask.register(project)
        ClusterfuzzWriteTestScriptsTask.register(project)
        ClusterfuzzDefinitionTask.register(project)
        ClusterfuzzAssembleTask.register(project)
        ClusterfuzzWriteCorpusTask.register(project)
        ClusterfuzzTask.register(project)

    }

}
