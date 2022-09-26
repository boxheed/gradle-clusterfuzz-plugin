package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClusterfuzzPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterfuzzPlugin.class)

	void apply(Project project) {
		project.extensions.create("clusterfuzz", ClusterfuzzPluginExtension)
		createSourceSet(project)
		createConfiguration(project)
		createTasks(project)
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

	private void createTasks(Project project) {
		def sourceSets = project.extensions.getByType(SourceSetContainer.class)
		def fuzzSourceSet = sourceSets.named("clusterfuzz")
		project.task([group: 'build', 
			type: Jar.class, 
			dependsOn: ['jar', 'clusterfuzzClasses'],
			description: 'Assembles a jar archive containing the fuzzer tests'], 
			'clusterfuzzJar') {
				archiveAppendix = 'fuzz'
				destinationDirectory = new File(project.getBuildDir(), "clusterfuzz")
				from fuzzSourceSet.get().output
		}
		project.task([group: 'build',
			dependsOn: ['clusterfuzzJar'],
			description: 'Assembles libraries for running with clusterfuzz'],
			'clusterfuzzLibs') {

		}
		project.task([group: 'build',
			dependsOn: ['clusterfuzzLibs'],
			description: 'Creates the scripts for running clusterfuzz'],
			'clusterfuzzScripts') {

		}
		project.task([group: 'build',
			dependsOn: ['clusterfuzzJar', 'clusterfuzzScripts', 'clusterfuzzLibs'],
			description: 'Assembles libraries for running with clusterfuzz'],
			'clusterfuzzAssemble') {

		}
	}

	private void manageArtifacts(Project project) {

	}
}
