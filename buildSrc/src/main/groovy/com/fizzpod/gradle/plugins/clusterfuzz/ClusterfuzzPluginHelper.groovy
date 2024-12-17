/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClusterfuzzPluginHelper {

    static getSourceSet(Project project) {
        def sourceSets = project.extensions.getByType(SourceSetContainer.class)
        def fuzzSourceSet = sourceSets.named(ClusterfuzzPlugin.CLUSTERFUZZ_SOURCESET_NAME)
        return fuzzSourceSet
    }

    static getLogger(Project project) {
        return project.getLogger()
    }

    static getConfig(Project project, String name) {
        //get the default config
        def config = project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME).getByName("config")
        def configs = project
            .extensions
            .findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME)
            .matching( entry -> 
                entry.name.equals(name) || name ==~ entry.name
            )
        
        if(config != null) {
            config = config.config
        } else {
            config = [:]
        }
        config = merge(ClusterfuzzTestConfig.DEFAULTS, config)
        configs.each { entry ->
            config = merge(config, entry.config)
        }
        return config
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
