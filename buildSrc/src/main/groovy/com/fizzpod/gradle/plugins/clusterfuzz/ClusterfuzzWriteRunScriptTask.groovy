/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import java.nio.charset.Charset
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class ClusterfuzzWriteRunScriptTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Runscript"

    private Project project

    @Inject
    ClusterfuzzWriteRunScriptTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().debug("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        taskContainer.create([name: NAME,
            type: ClusterfuzzWriteRunScriptTask,
            dependsOn: [ClusterfuzzDefinitionTask.NAME],
            group: null,
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
