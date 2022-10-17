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
        //find all the test cases
        def tests = findTests()
        //for each test case create a script
        def classpath = getClasspath()
        def flags = getFlags()
        def options = getOptions()
        writeRunScript()
        tests.each {
            def testClass = getTestClassName(it)
            writeScript(testClass, classpath, flags, options)
        }
    }

    private getFlags() {
        ClusterfuzzPluginExtension extension = this.project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME);
        def flags = extension.getFlags();
        return flags.join(" ")
    }

    private getOptions() {
        ClusterfuzzPluginExtension extension = this.project.extensions.findByName(ClusterfuzzPlugin.CLUSTERFUZZ_PLUGIN_NAME);
        def options = extension.getOptions();

        def opts = [];
        options.each { kv ->
            opts.add(kv.key + "=" + kv.value)
        }
        return opts.join(" ")
    }

    private writeRunScript() {

        def template = IOUtils.resourceToString('/templates/run_template.sh', Charset.forName("UTF-8"))
        def binding = ["project": this.project]
        def engine = new groovy.text.SimpleTemplateEngine()
        def script = engine.createTemplate(template).make(binding)
        def outputFolder = getOutputFolder()
        def binFolder = new File(outputFolder, "bin")
        binFolder.mkdirs()
        File runFile = new File(binFolder, "run.sh")
        runFile.write(script.toString())
    }

    private writeScript(testClass, classpath, flags, options) {
        if(testClass != null) {
            def script = generateScript(testClass, classpath, flags, options)
            saveScript(testClass, script)
        }
    }

    private saveScript(testClass, script) {
        def outputFolder = getOutputFolder()
        outputFolder = new File(outputFolder, "tests")
        def name = testClass.split("\\.").last()
        def testFolder = new File(outputFolder, name)
        testFolder.mkdirs()
        File testFile = new File(testFolder, "test.sh");
        int tries = 0;
        testFile.write(script)
    }

    private generateScript(className, classpath, flags, options) {
        def template = IOUtils.resourceToString('/templates/test_template.sh', Charset.forName("UTF-8"))
        def binding = ["project": this.project, "class": className, "flags": flags, "options": options]
        def engine = new groovy.text.SimpleTemplateEngine()
        def script = engine.createTemplate(template).make(binding)
        return script.toString()
    }

    private getClasspath() {
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

    private getTestClassName(testClass) {
        if(testClass.endsWith(".class")) {
            return testClass.substring(0, testClass.length() - ".class".length()).replace('/', '.')
        }
        return null
    }

    private findTests() {
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