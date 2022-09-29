package com.fizzpod.gradle.plugins.clusterfuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

public class ClusterfuzzPluginExtensionFuzzTest {

    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        ClusterfuzzPluginExtension extension = new ClusterfuzzPluginExtension();
        String input = data.consumeString(10);
        extension.setLanguage(input);
        assert input.equals(extension.getLanguage());
    }

}