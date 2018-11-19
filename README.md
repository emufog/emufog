# EmuFog

[![Build Status](https://travis-ci.org/emufog/emufog.svg?branch=master)](https://travis-ci.org/emufog/emufog)

## Dependencies

EmuFog is build using Java 8.
Get the JDK to compile from sources.

## Build EmuFog From Source

The EmuFog project uses [Gradle](https://gradle.org/) to include dependencies and build binaries from the source code.
This repository contains a Gradle wrapper file `gradlew` for Linux and macOS and a `gradlew.bat` for Windows.
Local installations of Gradle can be used too.

To build EmuFog simply clone the git repository
```bash
git clone https://github.com/emufog/emufog.git
```
    
change the directory to the newly added emufog directory
```bash
cd emufog/
```
    
Run the Gradle build task to compile the sources.
```bash
chmod +x gradlew && ./gradlew build
```

The compiled binaries can be found in `build/libs`.
Those include a regular compiled version and a `-fat` jar.

````bash
libs/
├── emufog-<version>-fat.jar
└── emufog-<version>.jar
````

## Running EmuFog

EmuFog can be started using Gradle

````bash
gradlew run
````

or by running a precompiled jar file e.g.
````bash
java -jar build/libs/emufog-<version>-fat.jar
````


The [wiki](https://github.com/emufog/emufog/wiki) explains how to use EmuFog in more detail.

## License

Licensed under the [MIT](LICENSE) license.