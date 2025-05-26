/* (C) 2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ClusterfuzzDefinitionTaskSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "get ClusterfuzzDefinitionTask task"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            project.getPluginManager().apply('java')

        when:
            ClusterfuzzDefinitionTask.register(project)
            def tasks = project.getTasksByName(ClusterfuzzDefinitionTask.NAME, false)

        then:
            tasks != null
            tasks.size() == 1
            tasks.toArray()[0] instanceof Task

    }

    def "find tests"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            project.getPluginManager().apply('java')
            ClusterfuzzDefinitionTask.register(project)
            def tasks = project.getTasksByName(ClusterfuzzDefinitionTask.NAME, false)
            def task = tasks.iterator().next()
            println(tasks)

        when:
            def tests = task.findTests()
            println(tests)
        

        then:
            tests.size() == 0
    }

    def "run task"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            project.getPluginManager().apply('java')
            ClusterfuzzDefinitionTask.register(project)
            def tasks = project.getTasksByName(ClusterfuzzDefinitionTask.NAME, false)
            def task = tasks.iterator().next()
            def outputDir = ClusterfuzzPluginHelper.createPath(project, ClusterfuzzDefinitionTask.NAME)
            println(tasks)

        when:
            task.runTask()

        then:
            outputDir.exists()

    }
}
