/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import static groovy.io.FileType.FILES

import groovy.json.JsonSlurper
import java.nio.file.Files
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class ClusterfuzzWriteCorpusTask extends DefaultTask {

    public static final String NAME = ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "Corpus"

    private Project project

    @Inject
    ClusterfuzzWriteCorpusTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().debug("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        taskContainer.create([name: NAME,
            type: ClusterfuzzWriteCorpusTask,
            dependsOn: [ClusterfuzzDefinitionTask.NAME],
            group: null,
            description: 'Creates the corpus folders for the scripts with specified test data'])
    }

    @TaskAction
    def runTask() {
        def tests = loadTests()
        tests.each { test ->
            def config = ClusterfuzzPluginHelper.getConfig(project, test.testName)
            if(config.corpus) {
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
        def srcDir = null
        fuzzSourceSet.each { it ->
            srcDir = it.get().resources.srcDirs[0]
        }
        def corpusDir = new File(srcDir.getParentFile(), "corpus")
        if(corpusDir.exists()) {
            def corpusPattern = java.util.regex.Pattern.compile(corpus)
            corpusDir.eachFileRecurse(FILES) { file ->
                if(corpusPattern.matcher(file.getName()).matches()) {
                    corpi += file
                }
            }
        }
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
