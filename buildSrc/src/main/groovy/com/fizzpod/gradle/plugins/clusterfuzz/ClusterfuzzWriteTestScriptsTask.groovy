package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import groovy.json.JsonSlurper
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import java.util.zip.ZipFile
import java.nio.charset.Charset

import static groovy.io.FileType.FILES

public class ClusterfuzzWriteTestScriptsTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Scripts"

    private Project project

    @Inject
    ClusterfuzzWriteTestScriptsTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
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
        File testFile = new File(testFolder, "runTest.sh");
        testFile.write(script)
    }

    def generateTestScript(params) {
        def template = IOUtils.resourceToString('/templates/test_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        return engine.createTemplate(template).make(params).toString()
    }

    def loadTests() {
        //load the definition
        def defDir = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzDefinitionTask.NAME)
        File jsonFile = new File(defDir, "definition.json")
        return new JsonSlurper().parse(jsonFile).tests
    }

}