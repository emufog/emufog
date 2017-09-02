# EmuFog

## Dependencies

EmuFog is build using the [JDK 8+](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to build and run.
Make sure to have Java 8 installed on your device.

## Build EmuFog From Source

The EmuFog project uses [Gradle](https://gradle.org/) to include dependencies and build binaries from the source code.
In case you have Gradle version 3.4+ installed you can use the local installation otherwise the already included Gradle wrapper.

To build EmuFog simply clone the git repository

    git clone https://github.com/emufog/emufog.git
    
change the directory to the newly added emufog directory

    cd emufog/

### Build EmuFog With Gradle

With an installed version of Gradle simply call:

    gradle build
    
### Build EmuFog Without Gradle

For Linux and macOS call:
    
    ./gradlew build
    
For Microsoft Windows call: 

    gradlew build

## Running EmuFog

Issue command `gradle installApp` which packages the application into a binary with all libraries.

Packaged binary is found in `build/install/emufog/bin`. Execute it with the command line arguments to main () .

## EmuFog Uses Open Source Software

* [jCommander](http://jcommander.org/)
* [gson](https://github.com/google/gson)
