package com.fizzpod.gradle.plugins.clusterfuzz;

public class ClusterfuzzTestConfig implements GroovyInterceptable {

    static def DEFAULTS = [
        "flags": [] as Set,
        "options": ["-runs": "100", 
                "-timeout":"10", 
                "-max_total_time":"300", 
                "--jvm_args":"-Xmx512m"],
        "jacoco": ["enabled": true, 
                "dumpfile":"jacoco.exec", 
                "reportfile": "jacoco.txt"],
        "corpus": null
    ]

    def name
    def config = [
        "flags": [] as Set,
        "options": [:],
        "jacoco": [:],
        "corpus": null
    ]

    ClusterfuzzTestConfig(final String name) {
        this.name = name
        config["name"] = name
    }

    def getConfig() {
        return this.config
    }

    void setProperty(String key, value) {
          config[key] = value
       }
   
       def getProperty(String key) {
        if(key.equalsIgnoreCase("config")) {
            return this.config
        } else {
              return this.config[key]
        }
       } 

    def invokeMethod(String key, args) {
        arguments[key] = args
    }

}
