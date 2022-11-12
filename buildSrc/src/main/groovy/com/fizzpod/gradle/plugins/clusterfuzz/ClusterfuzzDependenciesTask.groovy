package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.tasks.Copy

public class ClusterfuzzDependenciesTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Dependencies"

    /**
    * Register the plugin. This isn't a real task, just configures 
    * a new Copy task to pull the dependencies together
    */
    public static register(Project project) {
        project.getLogger().debug("Registering task {}", NAME)
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
        project.task([group: null,
            type: Copy.class, 
            dependsOn: [],
            description: 'Assembles libraries for running with clusterfuzz'],
            NAME) {
                from(fuzzSourceSet.get().runtimeClasspath){
                     include '**/*.jar'
                }
                from(project.getTasksByName('jar', false)[0].archiveFile)
                includeEmptyDirs = false
                into(ClusterfuzzPluginHelper.createPath(project, NAME).getAbsolutePath())
        }
    }

}