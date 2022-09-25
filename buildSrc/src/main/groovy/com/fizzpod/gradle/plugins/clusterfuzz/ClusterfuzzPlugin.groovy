package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClusterfuzzPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterfuzzPlugin.class)

	void apply(Project project) {
		project.extensions.create("clusterfuzz", ClusterfuzzPluginExtension)
		createSourceSet(project)
		createConfiguration(project)
		
		manageArtifacts(project)
	}
	private void createSourceSet(Project project) {
		def sourceSets = project.extensions.getByType(SourceSetContainer.class)

		def fuzzSourceSet = sourceSets.register("clusterfuzz")
		project.afterEvaluate {
			def main = sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME)
			fuzzSourceSet.get().compileClasspath = fuzzSourceSet.get().compileClasspath.plus(main.get().output)
			fuzzSourceSet.get().runtimeClasspath = fuzzSourceSet.get().runtimeClasspath.plus(main.get().output)
		}
		
	}

	private void createConfiguration(Project project) {
		def configurations = project.getConfigurations()
		configurations.named("clusterfuzzImplementation") {
            it.extendsFrom(configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).get())
        }
	}

	private void manageArtifacts(Project project) {

	}
}
