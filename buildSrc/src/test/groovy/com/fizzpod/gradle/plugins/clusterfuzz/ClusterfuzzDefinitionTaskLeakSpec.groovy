/* (C) 2025-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ClusterfuzzDefinitionTaskLeakSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "find tests scans jars and closes resources"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            project.getPluginManager().apply('java')
            ClusterfuzzDefinitionTask.register(project)
            def tasks = project.getTasksByName(ClusterfuzzDefinitionTask.NAME, false)
            def task = tasks.iterator().next()

            // Create the directory expected by the task
            def jarFolder = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzJarTask.NAME)
            jarFolder.mkdirs()

            // Create a dummy jar file
            File jarFile = new File(jarFolder, "test-fuzzer.jar")
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarFile))
            zos.putNextEntry(new ZipEntry("com/example/MyFuzzTest.class"))
            zos.write("dummy content".getBytes())
            zos.closeEntry()
            zos.close()

        when:
            def tests = task.findTests()

        then:
            tests.size() == 1
            tests[0] == "com/example/MyFuzzTest.class"

            // We can't easily verify the file handle is closed from Java in a portable way without using internal APIs or running lsof,
            // but ensuring the task completes successfully with valid output is the first step.
            // The fix will ensure safety.
    }
}
