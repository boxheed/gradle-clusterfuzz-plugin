package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import groovy.json.JsonSlurper
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import java.util.zip.ZipFile
import java.nio.charset.Charset
import java.nio.file.Files

import static groovy.io.FileType.FILES

public class ClusterfuzzWriteCorpusTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Corpus"

	private Project project

	@Inject
	ClusterfuzzWriteCorpusTask(Project project) {
		this.project = project
	}

    static register(Project project) {
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
		def taskContainer = project.getTasks()

		taskContainer.create([name: NAME,
			type: ClusterfuzzWriteCorpusTask,
			dependsOn: [ClusterfuzzDefinitionTask.NAME],
			group: null,
			description: 'Creates the corpus folders for the scripts with specified test data'])
    }

	@TaskAction
	def runTask() {
        println("runTask")
		def tests = loadTests()
		tests.each { test ->
            def config = ClusterfuzzPluginHelper.getConfig(project, test.testName)
            if(config.corpus) {
                println("got corpus")
                def corpi = findCorpi(config.corpus)
                def targetFolder = getTargetFolder(test.testName)
                corpi.each() { file ->
                    targetFolder.mkdirs()
                    def targetFile = new File(targetFolder, file.getName())
                    if(targetFile.exists()) {
                        targetFile.delete()
                    }
                    Files.copy(file.toPath(), targetFile.toPath())
                }
            } else {
                println("no corpus")
            }
        }
	}

    def getTargetFolder(testName) {
        def root = ClusterfuzzPluginHelper.createPath(project, NAME)
        return new File(root, "tests/" + testName + "/corpus")
    }

    def findCorpi(corpus) {
        def corpi = [] as Set
        //get the source corpus folder
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
        println("SS" + fuzzSourceSet)
        def srcDir = null
        fuzzSourceSet.each { it ->
            println("GG " + it)
            println("QQ " + it.get().resources.srcDirs)
            srcDir = it.get().resources.srcDirs[0]
            println("LL" + srcDir)
        }
        println("KK" + srcDir)
        def corpusDir = new File(srcDir, "../corpus")
        println("BB" + corpusDir)
        if(corpusDir.exists()) {
            corpusDir.eachFileRecurse(FILES) { file ->
            println("PP" + file)
                if(file.getName() ==~ corpus) {
                    corpi += file
                }
            }
        }
        println(corpi)
        return corpi
        
    }

    def createCorpusFolder(testName) {
        def outputDir = ClusterfuzzPluginHelper.createPath(project, NAME)
        def testsFolder = new File(outputDir, "tests")
        def testFolder = new File(testsFolder, testName)
        def corpusFolder = new File(testFolder, "corpus")
        corpusFolder.mkdirs()
    }

    def loadTests() {
        //load the definition
        def defDir = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzDefinitionTask.NAME)
        File jsonFile = new File(defDir, "definition.json")
        return new JsonSlurper().parse(jsonFile).tests
    }

}