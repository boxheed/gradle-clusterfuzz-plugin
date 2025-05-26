/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import static groovy.io.FileType.FILES

import groovy.json.JsonBuilder
import java.nio.charset.Charset
import java.util.zip.ZipFile
import javax.inject.Inject
import org.apache.commons.io.IOUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public class ClusterfuzzDefinitionTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Definition"

    private Project project

    private def logger

    @Inject
    ClusterfuzzDefinitionTask(Project project) {
        this.project = project
        this.logger = project.getLogger()
    }

    static register(Project project) {
        def taskContainer = project.getTasks()

        project.getLogger().debug("Registering task {}", NAME)

        taskContainer.create([name: NAME,
            type: ClusterfuzzDefinitionTask,
            dependsOn: [ClusterfuzzJarTask.NAME],
            group: null,
            description: 'Creates the definition of the tests'])
    }

    @TaskAction
    def runTask() {
        ClusterfuzzTestDefinitions data = new ClusterfuzzTestDefinitions()
        def tests = findTests()
        tests.each { test ->
            ClusterfuzzTestDefinition testData = new ClusterfuzzTestDefinition()
            testData.testName = getTestName(test)
            testData.testClass = getTestClass(test)
            testData.options = getOptions(testData.testName)
            testData.flags = getFlags(testData.testName)
            testData.jacoco = getJacoco(testData.testName)
            testData.corpus = getCorpus(testData.testName)
            data.tests.add(testData)
        }
        def json = new JsonBuilder( data ).toPrettyString()
        logger.info("Clusterfuzz definition: {}", json)
        writeJson(json)
    }

    def getCorpus(testName) {
        def config = ClusterfuzzPluginHelper.getConfig(project, testName)
        return config.corpus
    }

    def getJacoco(testName) {
        def config = ClusterfuzzPluginHelper.getConfig(project, testName)
        return config.jacoco
    }

    def getOptions(testName) {
        def config = ClusterfuzzPluginHelper.getConfig(project, testName)
        def opts = []
        config.options.each { kv ->
           opts.add(kv.key + "=" + kv.value)
        }
        return opts.join(" ")
    }

    def getFlags(testName) {
        def config = ClusterfuzzPluginHelper.getConfig(project, testName)
        return config.flags.join(" ")
    }

    def findTests() {
        def tests = []
        def jarFolder = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzJarTask.NAME)
        jarFolder.exists() && jarFolder.eachFileRecurse(FILES) { jarFile ->
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
        File jsonFile = new File(outputDir, "definition.json")
        jsonFile.write(json)
    }
}
