package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClusterfuzzPlugin implements Plugin<Project> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClusterfuzzPlugin.class)

	void apply(Project project) {
		project.extensions.create("clusterfuzz", ClusterfuzzPluginExtension)
	}
}
