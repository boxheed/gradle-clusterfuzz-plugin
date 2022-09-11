package com.fizzpod.gradle.plugins.clusterfuzz;

public class ClusterfuzzPluginExtension {

	String language = "jvm"

	def language(String language){
		this.language = language
		return this
	}

}
