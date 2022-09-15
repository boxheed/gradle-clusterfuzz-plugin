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
		manageSourceSets(project)
		manageConfigurations(project)
		manageArtifacts(project)
	}
	private void manageSourceSets(Project project) {
		def sourceSets = project.extensions.getByType(SourceSetContainer.class)
		def main = sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME)
		sourceSets.register("clusterfuzz") {
			it.compileClasspath.plus(main.get().output)
            it.runtimeClasspath.plus(main.get().output)
		}
	}

	private void manageConfigurations(Project project) {
		def configurations = project.getConfigurations()
		configurations.named("clusterfuzzImplementation") {
            it.extendsFrom(configurations.named(JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME).get())
        }
	}

	private void manageArtifacts(Project project) {

	}
}
