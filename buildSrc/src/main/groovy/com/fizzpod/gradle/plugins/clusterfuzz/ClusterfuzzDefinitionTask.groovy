package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import java.util.zip.ZipFile
import java.nio.charset.Charset
import groovy.json.JsonBuilder

import static groovy.io.FileType.FILES

public class ClusterfuzzDefinitionTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Definition"

    private Project project

    @Inject
    ClusterfuzzDefinitionTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        def fuzzSourceSet = ClusterfuzzPluginHelper.getSourceSet(project)
        def taskContainer = project.getTasks()

        taskContainer.create([name: NAME,
            type: ClusterfuzzDefinitionTask,
            dependsOn: [ClusterfuzzJarTask.NAME],
            group: null,
            description: 'Creates the definition of the tests'])

    }

    @TaskAction
    def runTask() {
        ClusterfuzzTests data = new ClusterfuzzTests()
        def tests = findTests()
        tests.each { test ->
            ClusterfuzzTest testData = new ClusterfuzzTest()
            testData.testName = getTestName(test)
            testData.testClass = getTestClass(test)
            testData.options = getOptions(testData.testName)
            testData.flags = getFlags(testData.testName)
            testData.jacoco = getJacoco(testData.testName)
            data.tests.add(testData)
        }
        def json = new JsonBuilder( data ).toPrettyString()
        println(json)
        writeJson(json)
    }

    def getJacoco(testName) {
        def extension = ClusterfuzzPluginHelper.getConfig(project, testName)
        return extension.jacoco
    }

    def getOptions(testName) {
        def extension = ClusterfuzzPluginHelper.getConfig(project, testName)
        def opts = []
        extension.options.each { kv ->
            opts.add(kv.key + "=" + kv.value)
        }
        return opts
    }

    def getFlags(testName) {
        def extension = ClusterfuzzPluginHelper.getConfig(project, testName)
        return extension.flags.join(" ")
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

    def getTestClass(classFile) {
        if(classFile.endsWith(".class")) {
            return classFile.substring(0, classFile.length() - ".class".length()).replace('/', '.')
        }
        return null
    }

    def getTestName(classFile) {
        return getTestClass(classFile).split("\\.").last()
    }

    def writeJson(json) {
        def outputDir = ClusterfuzzPluginHelper.createPath(project, NAME)
        outputDir.mkdirs()
        File jsonFile = new File(outputDir, "definition.json");
        jsonFile.write(json)
    }
}