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
        return project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME).getByName("config")
    }

    static getConfig(Project project) {
        getConfig(project, "config")
    }

    static getConfig(Project project, String name) {
        def config = project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME).getByName(name)
        if(config != null) {
            config = config.config
        } else {
            config = [:]
        }
        return merge(ClusterfuzzTestConfig.DEFAULTS, config)
    }

    static merge(Map lhs, Map rhs) {
        return rhs.inject(lhs.clone()) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge(map[entry.key], entry.value)
            } else if (map[entry.key] instanceof Collection && entry.value instanceof Collection) {
                map[entry.key] += entry.value
            } else if(entry.value != null) {
                map[entry.key] = entry.value
            }
            return map
        }
    }
    
    static createPath(Project project, String name) {
		String part = name.replace(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME, "").toLowerCase()
		return new File(project.getBuildDir(), ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "/" + part)
	}
}