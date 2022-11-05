package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import java.util.zip.ZipFile
import java.nio.charset.Charset
import org.apache.commons.io.IOUtils
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import static groovy.io.FileType.FILES

class ClusterfuzzFirstScriptsTask {

    private Project project

    ClusterfuzzFirstScriptsTask(Project project) {
        this.project = project
    }

    void doTask() {
        def params = ["project": this.project]
        def process = addExtension >> addOutputDir >> addClasspath >> addFlags >> addOptions >> findTests >> processTests
        process(params)
    }

    def processTests = { params ->
        def processTest = resolveCorpusFolders >> writeCorpus >> generateTestScript >> writeTestScript
        params.tests.each { test ->
            def scriptParams = ["class":getTestClassName(test)] + params
            scriptParams.test = test
            processTest(scriptParams)
        }
        return params
    }

    def generateTestScript = { params ->
    
        def template = IOUtils.resourceToString('/templates/test_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        params.testScript = engine.createTemplate(template).make(params).toString()
        return params
    }

    def writeTestScript = { params ->
        def testsFolder = new File(params.outputDir, "tests")
        def name = params["class"].split("\\.").last()
        def testFolder = new File(testsFolder, name)
        testFolder.mkdirs()
        File testFile = new File(testFolder, "test.sh");
        testFile.write(params.testScript)
        return params
    }
   
    def addOutputDir = { params ->
        params.outputDir = resolvePath(params.project.getBuildDir(), ClusterfuzzPlugin.CLUSTERFUZZ_SCRIPTS_TASK_NAME)
        return params;
    }

    def addOptions = { params ->
        def options = params.extension.getOptions()
        def opts = []
        options.each { kv ->
            opts.add(kv.key + "=" + kv.value)
        }
        params.options = opts.join(" ")
        return params
    }

    def addFlags = { params ->
        params.flags = params.extension.flags.join(" ")
        return params
    }

    def addExtension = { params ->
        params.extension = ClusterfuzzPluginHelper.getExtension(project)
        return params
    }

    def addClasspath = { params ->
        def classpath = []
        def libFolder = resolvePath(params.project.getBuildDir(), ClusterfuzzDependenciesTask.NAME)
        libFolder.eachFileRecurse(FILES) { jarFile ->
            classpath.add(jarFile.name)
        }
        libFolder = resolvePath(params.project.getBuildDir(), ClusterfuzzJarTask.NAME)
        libFolder.eachFileRecurse(FILES) { jarFile ->
            classpath.add(jarFile.name)
        }
        params.classpath = classpath
        //return classpath.join(':')
        return params
    }

    def resolveCorpusFolders = { params ->
        //get the corpus parameter
        def corpus = params.extension.corpus
        //find the corpus folders
        def sourceSets = params.project.extensions.getByType(SourceSetContainer.class)
		def fuzzSourceSet = sourceSets.named(ClusterfuzzPlugin.CLUSTERFUZZ_SOURCESET_NAME)
        def folders = [] as Set
        fuzzSourceSet.get().allSource.getSrcDirs().each {
            File corpusFolder = new File(it.getParentFile(), "corpus")
            if(corpusFolder.exists() && corpusFolder.isDirectory()) {
                folders.add(corpusFolder)
            }
        }
        params.corpusFolders = folders
        return params
    }

    def resolveCorpusFiles = { params ->
        //find the corpus folders
        def sourceSets = params.project.extensions.getByType(SourceSetContainer.class)
		def fuzzSourceSet = sourceSets.named(ClusterfuzzPlugin.CLUSTERFUZZ_SOURCESET_NAME)
        def folders = [] as Set
        fuzzSourceSet.get().allSource.getSrcDirs().each {
            File corpusFolder = new File(it.getParentFile(), "corpus")
            if(corpusFolder.exists() && corpusFolder.isDirectory()) {
                folders.add(corpusFolder)
            }
        }
        params.corpusFolders = folders
        return params
    }

    def resolveCorpusFile = { params ->
        def corpusResolvers = [extensionCorpusResolver, folderCorpusResolver, expressionCorpusResolver]
        def corpusFile = params.corpusFolders.find { folder ->

            return corpusResolvers.find { resolver ->
                return resolver.call(folder)
            }

        }
        params.corpus = corpusFile
        return params
    }



    def writeCorpus = { params ->
        return params
    }

    def writeRunScript = {params ->

        def template = IOUtils.resourceToString('/templates/run_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        def script = engine.createTemplate(template).make(params)
        def binFolder = new File(params.outputDir, "bin")
        binFolder.mkdirs()
        File runFile = new File(binFolder, "run.sh")
        runFile.write(script.toString())
        return params
    }

    def findTests = { params ->
        def tests = []
        def jarFolder = resolvePath(params.project.getBuildDir(), ClusterfuzzJarTask.NAME)
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
        params.tests = tests
        return params
    }
    
    private getTestClassName(classFile) {
        if(classFile.endsWith(".class")) {
            return classFile.substring(0, classFile.length() - ".class".length()).replace('/', '.')
        }
        return null
    }

    private File getOutputFolder(File root) {
        return resolvePath(params.project.getBuildDir(), ClusterfuzzPlugin.CLUSTERFUZZ_SCRIPTS_TASK_NAME)
    }

    private File resolvePath(File root, String name) {
		String part = name.replace(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME, "").toLowerCase()
		return new File(root, ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "/" + part)
	}
}