package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Project
import java.util.zip.ZipFile

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
        writeRunScript()
        tests.each {
            def testClass = getTestClassName(it)
            writeScript(testClass, classpath)
        }
    }

    private writeRunScript() {
        def outputFolder = getOutputFolder()
        outputFolder.mkdirs()
        def template = new String(this.getClass().getResourceAsStream('/run_template.sh').readAllBytes(), "UTF-8")
        File outputFile = new File(outputFolder, "run.sh");
        outputFile.write(template)
    }

    private writeScript(testClass, classpath) {
        println(testClass)
        if(testClass != null) {
            def script = generateScript(testClass, classpath)
            saveScript(testClass, script)
        }
    }

    private saveScript(testClass, script) {
        def outputFolder = getOutputFolder()
        outputFolder.mkdirs()
        def name = testClass.split("\\.").last()
        File outputFile = new File(outputFolder, name + ".sh");
        int tries = 0;
        outputFile.write(script)
        File runFile = new File(outputFolder, "run.sh");
        runFile.append("bash " + name + ".sh")
    }

    private generateScript(className, classpath) {
        def template = new String(this.getClass().getResourceAsStream('/clusterfuzz_test_template.sh').readAllBytes(), "UTF-8")
        def binding = ["class": className, "classpath": classpath]
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
        return classpath.join(';')
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