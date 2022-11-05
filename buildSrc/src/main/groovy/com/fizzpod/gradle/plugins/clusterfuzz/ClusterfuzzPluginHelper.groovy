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

class ClusterfuzzPluginHelper {

    static getSourceSet(Project project) {
        def sourceSets = project.extensions.getByType(SourceSetContainer.class)
		def fuzzSourceSet = sourceSets.named(ClusterfuzzPlugin.CLUSTERFUZZ_SOURCESET_NAME)
		return fuzzSourceSet
    }

    static getExtension(Project project) {
        return project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME)
    }
    
    static createPath(Project project, String name) {
		String part = name.replace(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME, "").toLowerCase()
		return new File(project.getBuildDir(), ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "/" + part)
	}
}