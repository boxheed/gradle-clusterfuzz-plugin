package com.fizzpod.gradle.plugins.clusterfuzz;

public class ClusterfuzzPluginExtension {

	String language = "jvm"
	String[] flags = []
	Map<String, String> options = ["-runs": "100", "-timeout":"10", "-max_total_time":"300"]

}
