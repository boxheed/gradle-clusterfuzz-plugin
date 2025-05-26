/* (C) 2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ClusterfuzzTaskSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "testTaskRegistrationSuccess"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        
        when:
            ClusterfuzzTask.register(project)

        then:
            project.getTasksByName(ClusterfuzzTask.NAME, false) != null
    }
}
