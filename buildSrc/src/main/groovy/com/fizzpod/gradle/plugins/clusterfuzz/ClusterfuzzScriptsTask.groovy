package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import java.util.zip.ZipFile
import java.nio.charset.Charset

import static groovy.io.FileType.FILES

public class ClusterfuzzScriptsTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Scripts"

	private Project project

	@Inject
	ClusterfuzzScriptsTask(Project project) {
		this.project = project
	}

    static register(Project project) {
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
		def taskContainer = project.getTasks()


		taskContainer.create([name: NAME,
			type: ClusterfuzzScriptsTask,
			dependsOn: [ClusterfuzzJarTask.NAME],
			group: ClusterfuzzPlugin.CLUSTERFUZZ_GROUP,
			description: 'Creates the main scripts for running each of the clusterfuzz tests'])

    }

	@TaskAction
	def runTask() {
		def tests = findTests()
		tests.each { test ->
            def params = ["class": getTestClassName(test),
				"project": project] 
            params.test = test
			addExtension(params)
			addOptions(params)
			addFlags(params)
            def script = generateTestScript(params)
        }
		println(tests)
	}

	def writeTestScript(script) {
		def outputDir = ClusterfuzzPluginHelper.createPath(project, NAME)
        def testsFolder = new File(outputDir, "tests")
        def name = params["class"].split("\\.").last()
        def testFolder = new File(testsFolder, name)
        testFolder.mkdirs()
        File testFile = new File(testFolder, "test.sh");
        testFile.write(script)
    }

	def addExtension(params) {
        params.extension = ClusterfuzzPluginHelper.getExtension(project)
        return params
    }

	def addOptions(params) {
        def options = params.extension.getOptions()
        def opts = []
        options.each { kv ->
            opts.add(kv.key + "=" + kv.value)
        }
        params.options = opts.join(" ")
        return params
    }

	def addFlags(params) {
        params.flags = params.extension.flags.join(" ")
        return params
    }

	def generateTestScript(params) {
        def template = IOUtils.resourceToString('/templates/test_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        return engine.createTemplate(template).make(params).toString()
    }

	def findTests() {
        def tests = []
        def jarFolder = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzJarTask.NAME)
        jarFolder.eachFileRecurse(FILES) { jarFile ->
            if(jarFile.name.endsWith('.jar')) { 
                def zf = new ZipFile(jarFile)
                zf.entries().findAll { !it.directory }.each {
                    if(it.name.endsWith("Test.class")) {
                        tests.add(it.name)
                    }
                }
            }
        }
        return tests
    }

    def getTestClassName(classFile) {
        if(classFile.endsWith(".class")) {
            return classFile.substring(0, classFile.length() - ".class".length()).replace('/', '.')
        }
        return null
    }

}