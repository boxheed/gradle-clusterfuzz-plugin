package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

public class ClusterfuzzJarTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Jar"
    public static final String CLUSTERFUZZ_CLASSES_TASK_NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Classes"
    /**
    * Register the task. This isn't a real task, just configures 
    * a new Jar task
    */
    public static register(Project project) {
        project.getLogger().debug("Registering task {}", NAME)
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
        project.task([group: ClusterfuzzPlugin.CLUSTERFUZZ_GROUP, 
            type: Jar.class, 
            dependsOn: ['jar', CLUSTERFUZZ_CLASSES_TASK_NAME],
            description: 'Assembles a jar archive containing the fuzzer tests'], 
            NAME) {
                archiveAppendix = 'clusterfuzz'
                destinationDirectory = ClusterfuzzPluginHelper.createPath(project, NAME)
                from fuzzSourceSet.get().output
        }
    }

}