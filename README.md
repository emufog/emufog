# EmuFog

[![Build Status](https://travis-ci.org/emufog/emufog.svg?branch=master)](https://travis-ci.org/emufog/emufog)

EmuFog helps to test fog computing applications more efficiently.
Instead of actual deploying large network topologies with your application to test, EmuFog helps to generate networks that can be emulated easily with [MaxiNet](https://maxinet.github.io/), a distributed version of the popular [Mininet](https://mininet.org/).
This provides more realistic results than simulations and is cheaper and faster than real deployments.
As an input EmuFog supports generated topologies from [BRITE](https://www.cs.bu.edu/brite/) or measured real world topologies from [Caida](https://www.caida.org).
In those networks EmuFog places fog nodes efficiently based on user definied constrains such as network latency thresholds or resource constraints.
Applications for clients and fog nodes can be anything shipped in a Docker container.

## Build EmuFog From Source

EmuFog is build using Java 8.
It uses [Gradle](https://gradle.org/) to include dependencies and build binaries from the source code.
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
./gradlew build
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
./gradlew run
````

or by running a precompiled jar file e.g.
````bash
java -jar build/libs/emufog-<version>-fat.jar
````


The [wiki](https://github.com/emufog/emufog/wiki) explains how to use EmuFog in more detail.

## License

Licensed under the [MIT](LICENSE) license.