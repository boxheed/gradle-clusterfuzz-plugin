/* (C) 2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.clusterfuzz

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ClusterfuzzTestConfigSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    def "set property"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            project.getPluginManager().apply('java')

        when:
            def config = new ClusterfuzzTestConfig()
            config.hello = "world"

        then:
            config.hello == "world"
    }

}
