[![CircleCI](https://circleci.com/gh/boxheed/gradle-clusterfuzz-plugin/tree/main.svg?style=shield)](https://circleci.com/gh/boxheed/gradle-clusterfuzz-plugin/tree/main)

# Gradle Clusterfuzz Plugin

Gradle plugin that provides tasks and configuration to support clusterfuzz testing.

## Overview

The plugin creates additional tasks within your build script and supports the creation of tests within a new `clusterfuzz` source set.

## Usage

### Project Structure

The plugin creates a new source set extending the default source set within which you can contain your clusterfuzz tests, resources and corpus. The following demonstrates the layout with this plugin.

```
project
|   build.gradle
|   settings.gradle
|   
└─── src
     |
     └─── main
     |    └─── java...
     |    └─── resources...
     └─── test
     |    └─── java...
     |    └─── resources...
     |
     └─── clusterfuzz
          └─── java...
          └─── corpus...
```

### Tasks

The plugin creates a number of tasks that create the output folders for running with an appropriate container. The main task that encapsulates all of the tasks is `clusterfuzz`.

Running the `clusterfuzz` task performs the following steps
1. compiles the clusterfuzz tests in the sourceset
2. assembles the libraries together
3. generates shell scripts for running the tests (based on configuration)
4. assembles the corpus files for each test
5. assembles all of the items together into a runnable structure

To generate the output run the `clusterfuzz` task i.e. `./gradlew clusterfuzz`

### Output Structure

The plugin uses the `clusterfuzz` folder within the `build` folder. The plugin generates a number of intermediate folders, but ultimately it assembles all of the parts together into the `./build/clusterfuzz/assemble` folder.

```
project
|   
└─── build
     |
     └─── clusterfuzz
          └─── assemble
          └─── corpus
          └─── definition
          └─── dependencies
          └─── jar
          └─── runscript
          └─── scripts
```

### Running the tests

The contents of the `assemble` folder contains the libraries and scripts required to run the tests. The scripts require a container that has the Jazzer already installed, the `circleci` build that this project uses uses the `gcr.io/oss-fuzz-base/base-builder-jvm` container.

To run the tests use the `bin/run.sh` script.

### Configuration

The plugin creates a `clusterfuzz` configuration block which supports generic configuration and more specific configuration for different tests.

```
clusterfuzz {
    config { <- global configuration
        options = ["--instrumentation_includes":"com.fizzpod.**","-runs": "200"] 
    }
    "myTestNameregex.*" { 
        options = ["--instrumentation_includes": "com.fizzpod.gradle.**"] 
        corpus = "myTestCorpus.*"
    }
}
```

| Option      | Description | Example |
| ----------- | ----------- | -- |
| options | Map of key value pairs to be passed through to Jazzer | `options = ["--instrumentation_includes":"com.fizzpod.**","-runs": "200"]` |
| flags | Array of flags to be passed through to Jazzer | `flags = []` |
| jacoco | Flags for Jacoco, by default Jacoco coverage is enabled, allows the specifying of the name of the dump files | `jacoco: ["enabled": true, "dumpfile":"jacoco.exec", "reportfile": "jacoco.txt"]` |
| corpus | The regex to select the corpus files for the test | `"corpus": "myCorpusFile.*"` |

You can specify sub configurations that uses a regex for the key in order to override configurations for different test cases. This can be useful when tests require different corpus files to seed the tests.

See the Jazzer documentation for all supported options.

## References

* [ClusterfuzzLite](https://google.github.io/clusterfuzzlite/)
* [OSS-fuzz](https://google.github.io/oss-fuzz/)
* [Jazzer](https://github.com/CodeIntelligenceTesting/jazzer)
* [libfuzzer](https://llvm.org/docs/LibFuzzer.html)

