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
		def sourceSets = project.extensions.getByType(SourceSetContainer.class)
		sourceSets.register("clusterfuzz")
	}
}
