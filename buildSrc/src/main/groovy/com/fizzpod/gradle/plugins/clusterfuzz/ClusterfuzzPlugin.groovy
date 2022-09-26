package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.Copy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClusterfuzzPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterfuzzPlugin.class)

	public static final String CLUSTERFUZZ_PLUGIN_NAME = "clusterfuzz"

	public static final String CLUSTERFUZZ_SOURCESET_NAME = CLUSTERFUZZ_PLUGIN_NAME

	public static final String CLUSTERFUZZ_IMPLEMENTATION_CONFIGURATION_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Implementation"

	public static final String CLUSTERFUZZ_GROUP = "build"

	public static final String CLUSTERFUZZ_CLASSES_TASK_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Classes"
	public static final String CLUSTERFUZZ_JAR_TASK_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Jar"
	public static final String CLUSTERFUZZ_DEPS_TASK_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Dependencies"
	public static final String CLUSTERFUZZ_SCRIPTS_TASK_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Scripts"
	public static final String CLUSTERFUZZ_ASSEMBLE_TASK_NAME = CLUSTERFUZZ_PLUGIN_NAME + "Assemble"

	void apply(Project project) {
		project.extensions.create(CLUSTERFUZZ_PLUGIN_NAME, ClusterfuzzPluginExtension)
		createSourceSet(project)
		createConfiguration(project)
		createTasks(project)
		manageArtifacts(project)
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
		def sourceSets = project.extensions.getByType(SourceSetContainer.class)
		def fuzzSourceSet = sourceSets.named(CLUSTERFUZZ_SOURCESET_NAME)
		def implementationConfiguration = project.getConfigurations().named(CLUSTERFUZZ_IMPLEMENTATION_CONFIGURATION_NAME)
		project.task([group: CLUSTERFUZZ_GROUP, 
			type: Jar.class, 
			dependsOn: ['jar', CLUSTERFUZZ_CLASSES_TASK_NAME],
			description: 'Assembles a jar archive containing the fuzzer tests'], 
			CLUSTERFUZZ_JAR_TASK_NAME) {
				archiveAppendix = 'fuzz'
				destinationDirectory = createPath(project, CLUSTERFUZZ_JAR_TASK_NAME)//new File(project.getBuildDir(), "clusterfuzz")
				from fuzzSourceSet.get().output
		}
		project.task([group: CLUSTERFUZZ_GROUP,
			type: Copy.class, 
			dependsOn: [],
			description: 'Assembles libraries for running with clusterfuzz'],
			CLUSTERFUZZ_DEPS_TASK_NAME) {
				from(fuzzSourceSet.get().runtimeClasspath){
         			include '**/*.jar'
				}
				includeEmptyDirs = false
				into(createPath(project, CLUSTERFUZZ_DEPS_TASK_NAME).getAbsolutePath())
		}
		project.task([group: CLUSTERFUZZ_GROUP,
			dependsOn: [CLUSTERFUZZ_JAR_TASK_NAME, CLUSTERFUZZ_DEPS_TASK_NAME],
			description: 'Creates the scripts for running clusterfuzz'],
			CLUSTERFUZZ_SCRIPTS_TASK_NAME) {

		}
		project.task([group: CLUSTERFUZZ_GROUP,
			dependsOn: [CLUSTERFUZZ_JAR_TASK_NAME, CLUSTERFUZZ_SCRIPTS_TASK_NAME, CLUSTERFUZZ_DEPS_TASK_NAME],
			description: 'Assembles libraries for running with clusterfuzz'],
			'clusterfuzzAssemble') {

		}
	}

	private void manageArtifacts(Project project) {

	}

	private File createPath(Project project, String name) {
		String part = name.replace(CLUSTERFUZZ_PLUGIN_NAME, "").toLowerCase()
		return new File(project.getBuildDir(), CLUSTERFUZZ_PLUGIN_NAME + "/" + part)
	}
}
