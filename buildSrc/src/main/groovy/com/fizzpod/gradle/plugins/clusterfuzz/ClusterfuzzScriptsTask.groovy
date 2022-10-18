package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import java.util.zip.ZipFile
import java.nio.charset.Charset
import org.apache.commons.io.IOUtils

import static groovy.io.FileType.FILES

class ClusterfuzzScriptsTask {

    private Project project

    ClusterfuzzScriptsTask(Project project) {
        this.project = project
    }

    void doTask() {
        def params = ["project": this.project]
        params.extension = this.project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME)
        //find all the test cases
        params.tests = findTests(params)
        //for each test case create a script
        params.classpath = getClasspath(params)
        params.flags = getFlags(params)
        params.options = getOptions(params)
        writeRunScript(params)
        params.tests.each {
            def scriptParams = ["class":getTestClassName(it)] + params
            
            writeScript(scriptParams)
        }
    }

    private getFlags(params) {
        return params.extension.flags.join(" ")
    }

    private getOptions(params) {
        def options = params.extension.getOptions()

        def opts = [];
        options.each { kv ->
            opts.add(kv.key + "=" + kv.value)
        }
        return opts.join(" ")
    }

    private writeRunScript(params) {

        def template = IOUtils.resourceToString('/templates/run_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        def script = engine.createTemplate(template).make(params)
        def outputFolder = getOutputFolder()
        def binFolder = new File(outputFolder, "bin")
        binFolder.mkdirs()
        File runFile = new File(binFolder, "run.sh")
        runFile.write(script.toString())
    }

    private writeScript(params) {
        if(params["class"] != null) {
            def script = generateScript(params)
            saveScript(params, script)
        }
    }

    private saveScript(params, script) {
        def outputFolder = getOutputFolder()
        outputFolder = new File(outputFolder, "tests")
        def name = params["class"].split("\\.").last()
        def testFolder = new File(outputFolder, name)
        testFolder.mkdirs()
        File testFile = new File(testFolder, "test.sh");
        int tries = 0;
        testFile.write(script)
    }

    private generateScript(params) {
        def template = IOUtils.resourceToString('/templates/test_template.sh', Charset.forName("UTF-8"))
        def engine = new groovy.text.SimpleTemplateEngine()
        def script = engine.createTemplate(template).make(params)
        return script.toString()
    }

    private getClasspath(params) {
        def classpath = []
        def libFolder = resolvePath(ClusterfuzzPlugin.CLUSTERFUZZ_DEPS_TASK_NAME)
        libFolder.eachFileRecurse(FILES) { jarFile ->
            classpath.add(jarFile.name)
        }
        libFolder = resolvePath(ClusterfuzzPlugin.CLUSTERFUZZ_JAR_TASK_NAME)
        libFolder.eachFileRecurse(FILES) { jarFile ->
            classpath.add(jarFile.name)
        }
        return classpath.join(':')
    }

    private getTestClassName(classFile) {
        if(classFile.endsWith(".class")) {
            return classFile.substring(0, classFile.length() - ".class".length()).replace('/', '.')
        }
        return null
    }

    private findTests(params) {
        def tests = []
        def jarFolder = resolvePath(ClusterfuzzPlugin.CLUSTERFUZZ_JAR_TASK_NAME)
        jarFolder.eachFileRecurse(FILES) { jarFile ->
            if(jarFile.name.endsWith('.jar')) { 
                println jarFile
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

    private File getOutputFolder() {
        return resolvePath(ClusterfuzzPlugin.CLUSTERFUZZ_SCRIPTS_TASK_NAME)
    }

    private File resolvePath(String name) {
		String part = name.replace(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME, "").toLowerCase()
		return new File(project.getBuildDir(), ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME + "/" + part)
	}
}