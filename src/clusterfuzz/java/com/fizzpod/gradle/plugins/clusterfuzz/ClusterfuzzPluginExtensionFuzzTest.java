package com.fizzpod.gradle.plugins.clusterfuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

public class ClusterfuzzPluginExtensionFuzzTest {



    public static void fuzzerTestOneInput(FuzzedDataProvider data) {
        ClusterfuzzPluginExtension extension = new ClusterfuzzPluginExtension();
        extension.language(data.consumeString(10));
    }

}