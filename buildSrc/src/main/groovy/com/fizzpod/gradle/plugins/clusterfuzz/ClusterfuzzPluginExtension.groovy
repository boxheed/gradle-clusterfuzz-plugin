package com.fizzpod.gradle.plugins.clusterfuzz;

public class ClusterfuzzPluginExtension {

	String language = "jvm"
	String[] flags = []
	Map<String, String> options = ["-runs": "100", "-timeout":"10", "-max_total_time":"300", "--jvm_args":"-Xmx2048m"]
	def jacoco = ["enabled": true, "dumpfile":"jacoco.exec", "reportfile": "jacoco.html"]

}
