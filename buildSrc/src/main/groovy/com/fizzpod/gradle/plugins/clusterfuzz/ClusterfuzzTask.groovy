package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy

public class ClusterfuzzTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME

    /**
    * Register the task. This isn't a real task, just configures 
    * a new default task that creates the task dependencies to create the full assembled output
    */
    public static register(Project project) {
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
		project.task([group: ClusterfuzzPlugin.CLUSTERFUZZ_GROUP,
		type: DefaultTask.class, 
		dependsOn: [ClusterfuzzAssembleTask.NAME],
		description: 'Assembles libraries, scripts, corpus data etc for running with clusterfuzz'],
		NAME)
    }

}