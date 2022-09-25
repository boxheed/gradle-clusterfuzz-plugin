package com.fizzpod.gradle.plugins.clusterfuzz;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;

class ClusterfuzzPluginExtensionFuzzTest {



    public static void testExtansionLang(FuzzedDataProvider data) {
        ClusterfuzzPluginExtension extension = new ClusterfuzzPluginExtension();
        extension.language(data.consumeString(10));
    }

}