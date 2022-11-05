package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

public class ClusterfuzzRunScriptTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Runscript"

	private Project project

	@Inject
	ClusterfuzzRunScriptTask(Project project) {
		this.project = project
	}

    static register(Project project) {
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
		def taskContainer = project.getTasks()


		taskContainer.create([name: NAME,
			type: ClusterfuzzRunScriptTask,
			group: ClusterfuzzPlugin.CLUSTERFUZZ_GROUP,
			description: 'Creates the main run script for running clusterfuzz'])

    }

	@TaskAction
	def runTask() {
        def template = IOUtils.resourceToString('/templates/run_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        def script = engine.createTemplate(template).make([project: this.project])
		def outputDir = ClusterfuzzPluginHelper.createPath(this.project, NAME)
        def binFolder = new File(outputDir, "bin")
        binFolder.mkdirs()
        File runFile = new File(binFolder, "run.sh")
        runFile.write(script.toString())
	}

}