package com.fizzpod.gradle.plugins.clusterfuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

public class ClusterfuzzPluginExtensionFuzzTest {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        ClusterfuzzTestDefinition test = new ClusterfuzzTestDefinition();
        String input = data.consumeString(10);
        test.setTestName(input);
        assert input.equals(test.getTestName());
    }

}