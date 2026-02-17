/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import static groovy.io.FileType.FILES

import groovy.json.JsonSlurper
import java.nio.charset.Charset
import java.util.zip.ZipFile
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class ClusterfuzzWriteTestScriptsTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Scripts"

    private Project project

    @Inject
    ClusterfuzzWriteTestScriptsTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().debug("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        taskContainer.create([name: NAME,
            type: ClusterfuzzWriteTestScriptsTask,
            dependsOn: [ClusterfuzzJarTask.NAME, ClusterfuzzDefinitionTask.NAME],
            group: null,
            description: 'Creates the main scripts for running each of the clusterfuzz tests'])
    }

    @TaskAction
    def runTask() {
        def tests = loadTests()
        tests.each { test ->
            test.project = project
            test.config = ClusterfuzzPluginHelper.getConfig(project, test.testName)
            def script = generateTestScript(test)
            writeTestScript(test.testName, script)
        }
    }

    def writeTestScript(testName, script) {
        def outputDir = ClusterfuzzPluginHelper.createPath(project, NAME)
        def testsFolder = new File(outputDir, "tests")
        def testFolder = new File(testsFolder, testName)
        testFolder.mkdirs()
        File testFile = new File(testFolder, "runTest.sh")
        testFile.write(script)
    }

    private groovy.text.Template template

    private synchronized groovy.text.Template getTemplate() {
        if (template == null) {
            def templateContent = IOUtils.resourceToString('/templates/test_template.sh', Charset.forName("UTF-8"))
            def engine = new groovy.text.SimpleTemplateEngine()
            template = engine.createTemplate(templateContent)
        }
        return template
    }

    def generateTestScript(params) {
        return getTemplate().make(params).toString()
    }

    def loadTests() {
        //load the definition
        def defDir = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzDefinitionTask.NAME)
        File jsonFile = new File(defDir, "definition.json")
        return new JsonSlurper().parse(jsonFile).tests
    }

}
