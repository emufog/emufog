# EmuFog

[![Build Status](https://travis-ci.org/emufog/emufog.svg?branch=master)](https://travis-ci.org/emufog/emufog)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/eb3e7eba854d4ebd9ce1afc2f29d5ea3)](https://www.codacy.com/manual/unly/emufog?utm_source=github.com&utm_medium=referral&utm_content=emufog/emufog&utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/emufog/emufog/branch/master/graph/badge.svg)](https://codecov.io/gh/emufog/emufog)
[![MIT License](https://img.shields.io/badge/license-MIT-green "MIT License")](LICENSE)

EmuFog helps to test fog computing applications more efficiently.
Instead of actual deploying large network topologies with your application to test, EmuFog helps to generate networks that can be emulated easily with [MaxiNet](https://maxinet.github.io/), a distributed version of the popular [Mininet](https://mininet.org/).
This provides more realistic results than simulations and is cheaper and faster than real deployments.
As an input EmuFog supports generated topologies from [BRITE](https://www.cs.bu.edu/brite/) or measured real world topologies from [Caida](https://www.caida.org).
In those networks EmuFog places fog nodes efficiently based on user defined constrains such as network latency thresholds or resource constraints.
Applications for clients and fog nodes can be anything shipped in a Docker container.

## Build EmuFog From Source

EmuFog is build using [Kotlin 1.4](https://github.com/JetBrains/kotlin/releases/tag/v1.4.10) on the JDK 11.
It uses [Gradle](https://gradle.org/) to include dependencies and build binaries from the source code.
Therefore, this repository contains a Gradle wrapper file `gradlew` for Linux and macOS and a `gradlew.bat` for Windows.

To build EmuFog simply clone the git repository

```bash
git clone https://github.com/emufog/emufog.git
```

change the directory to the newly added emufog directory

```bash
cd emufog/
```

Run the Gradle `build` task to compile the sources and run the tests.

```bash
./gradlew build
```

The distributions archives can be found in `build/distributions`.

```bash
distributions/
├── emufog.tar
└── emufog.zip
```

## Running EmuFog

Download the latest release as an archive from the [here](https://github.com/emufog/emufog/releases).

Extract the files from the archive e.g.

```bash
unzip -q emufog.zip
cd emufog/bin
```

Run the script depending on your operating system.
For Linux and macOS:

```bash
./emufog --help
```

and for Microsoft Windows:

```bash
./emufog.bat --help
```

The following steps explain how to use EmuFog in more detail.

### Get A Network Topology

EmuFog currently supports two different graph data formats:
1. The [BRITE](https://www.cs.bu.edu/brite/) network generator supporting the following models:
  * _Routing of Multipoint Connections_ by Waxman, DOI: [10.1109/infcom.2002.1019309](https://doi.org/10.1109%2F49.12889)
  * _On Distinguishing between Power-Law Internet Topology Generators_ by Bu and Towsley, DOI: [10.1109/infcom.2002.1019309](https://doi.org/10.1109%2Finfcom.2002.1019309)
  * _Emergence of Scaling in Random Networks_ by Barabási and Albert, DOI: [10.1126/science.286.5439.509](https://doi.org/10.1126%2Fscience.286.5439.509)
  * _Topology of Evolving Networks: Local Events and Universality_ by Barabási and Albert, DOI: [10.1103/physrevlett.85.5234](https://doi.org/10.1103%2Fphysrevlett.85.5234)
  
2. The [Macroscopic Internet Topology Data Kit](https://www.caida.org/data/internet-topology-data-kit/) from Caida including measured real world internet topologies

### Write The Configuration File

To specify the hardware capabilities and the software to test, EmuFog uses a configuration file containing all necessary information.
The EmuFog repository contains an [exemplary configuration file](src/dist/example-config.yaml) to get started.
The parameters used are explained in the tables below in more detail.

| Parameter                 | Description                                                                                                                               |
| ------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| base-address              | The base IP address of the network containing all nodes. This is the starting point and first address assigned. Format: `XXX.XXX.XXX.XXX` |
| overwrite-experiment-file | Indicates whether the output file should be overwritten in case it already exists. `true` to overwrite file, `false` to keep it.          |
| max-fog-nodes             | The maximum number of fog nodes to place in the network.                                                                                  |
| cost-threshold            | The cost function's threshold. Depends on the cost function chosen. The current implementation uses latency as a cost function.           |
| host-device-latency       | Latency to use between a placed client device at the edge of the network and its associated edge node.                                    |
| host-device-bandwidth     | Bandwidth to use between a placed client device at the edge of the network and its associated edge node. Measured in MB/s.                |
| device-node-types         | List of device containers that get assigned to the edge of the network. See below.                                                        |
| fog-node-types            | List of possible fog node types that can be placed in the topology. See below.                                                            |

The device containers use the following parameters:

| Parameter            | Description                                                                                  |
| -------------------- | -------------------------------------------------------------------------------------------- |
| container-image      | Docker image to use. See below.                                                              |
| scaling-factor       | Scales the workload of this device higher than 1. Defaults to `1`                            |
| average-device-count | The average number of devices of this type connected to an edge node.                        |
| memory-limit         | Memory size of this container in Bytes.                                                      |
| cpu-share            | Scaling of the CPU power. Container receives share of its value in respect to the total sum. |

Fog nodes share some generic parameters with client devices. Still, all parameters are listed below.

| Parameter           | Description                                                                                  |
| ------------------- | -------------------------------------------------------------------------------------------- |
| container-image     | Docker image to use. See below.                                                              |
| maximum-connections | The maximum number of connections from client devices this container can handle.             |
| costs               | Deployment costs of this fog node in the network.                                            |
| memory-limit        | Memory size of this container in Bytes.                                                      |
| cpu-share           | Scaling of the CPU power. Container receives share of its value in respect to the total sum. |

A Docker container consists of:

| Parameter | Description                                                  |
| --------- | ------------------------------------------------------------ |
| name      | Name of the Docker container to use.                         |
| version   | Version of the Docker container to use. Defaults to `latest` |

### Execute EmuFog

EmuFog requires input by the user to run.
Therefore, the information can be passed via a command line interface as arguments.
The following table lists the required arguments, their shortcut and their respective description.

| Argument | Shortcut | Description                                                                                         |
| -------- | -------- | --------------------------------------------------------------------------------------------------- |
| --Config | -c       | Path to the configuration file to use.                                                              |
| --Type   | -t       | The type of reader to use. Currently supported: BRITE and CAIDA. This argument is case insensitive. |
| --File   | -f       | Path to a topology file to read in. This argument can be used multiple times.                       |
| --Output | -o       | Path to the output file to write. Defaults to `output.py`.                                          |

A call of EmuFog could look like this for Linux and macOS:

```bash
./emufog -c config.yaml -t brite -f topology.brite -o out.py
```

## Deploy An Experiment

The final outcome of EmuFog is the network graph with the defined devices and fog nodes placed in the topology.
In order to run an experiment it requires a network emulation tool executing the defined software.
Currently, EmuFog supports an export for the [MaxiNet](https://maxinet.github.io/) emulator.
Follow the instructions to set it up and run the generated experiment file.

## License

Licensed under the [MIT](LICENSE) license.
