package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.tasks.Copy

public class ClusterfuzzAssembleTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Assemble"

    /**
    * Register the plugin. This isn't a real task, just configures 
    * a new Copy task
    */
    public static register(Project project) {
        project.getLogger().debug("Registering task {}", NAME)
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
        project.task([group: ClusterfuzzPlugin.CLUSTERFUZZ_GROUP,
        type: Copy.class, 
        dependsOn: [ClusterfuzzJarTask.NAME, 
            ClusterfuzzWriteTestScriptsTask.NAME, 
            ClusterfuzzWriteRunScriptTask.NAME, 
            ClusterfuzzDependenciesTask.NAME, 
            ClusterfuzzDefinitionTask.NAME,
            ClusterfuzzWriteCorpusTask.NAME],
        description: 'Assembles libraries and scripts for running with clusterfuzz'],
        NAME) {
            destinationDir = ClusterfuzzPluginHelper.createPath(project, NAME)
            includeEmptyDirs = false
            from(ClusterfuzzPluginHelper.createPath(project, ClusterfuzzWriteTestScriptsTask.NAME).getAbsolutePath())
            from(ClusterfuzzPluginHelper.createPath(project, ClusterfuzzWriteRunScriptTask.NAME).getAbsolutePath())
            from(ClusterfuzzPluginHelper.createPath(project, ClusterfuzzWriteCorpusTask.NAME).getAbsolutePath())

            into('libs', {
                from(ClusterfuzzPluginHelper.createPath(project, ClusterfuzzDependenciesTask.NAME).getAbsolutePath())
                from(ClusterfuzzPluginHelper.createPath(project, ClusterfuzzJarTask.NAME).getAbsolutePath())
            })

        }
    }

}