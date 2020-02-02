/*
 * MIT License
 *
 * Copyright (c) 2018 emufog contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package emufog

import emufog.backbone.identifyBackbone
import emufog.config.Config
import emufog.config.readConfig
import emufog.device.assignDeviceNodes
import emufog.export.maxinet.MaxiNetExporter
import emufog.fog.findPossibleFogNodes
import emufog.reader.GraphReader
import emufog.reader.brite.BriteFormatReader
import emufog.reader.caida.CaidaFormatReader
import emufog.util.debugTiming
import emufog.util.getLogger
import emufog.util.infoSeparator
import picocli.CommandLine
import java.io.IOException
import java.nio.file.Paths

private val LOG = getLogger("Emufog")

/**
 * Main function call to start EmuFog.
 *
 * @param args arguments of the command line
 */
fun main(args: Array<String>) {
    LOG.infoSeparator()
    LOG.info("       ______                ______")
    LOG.info("      / ____/___ ___  __  __/ ____/___  ____ _")
    LOG.info("     / __/ / __ `__ \\/ / / / /_  / __ \\/ __ `/")
    LOG.info("    / /___/ / / / / / /_/ / __/ / /_/ / /_/ /")
    LOG.info("   /_____/_/ /_/ /_/\\__,_/_/    \\____/\\__, /")
    LOG.info("                                     /____/")
    LOG.info("")
    LOG.infoSeparator()

    try {
        runEmuFog(args)
    } catch (e: Exception) {
        LOG.error("An exception stopped EmuFog!", e)
    }

    LOG.infoSeparator()
    LOG.info("Closing EmuFog...")
    LOG.infoSeparator()
}

/**
 * Runs an execution of emufog with the given arguments from the command line.
 *
 * @param args arguments passed to the execution
 * @throws IOException thrown in case of a problem during the execution
 */
private fun runEmuFog(args: Array<String>) {
    // parse the command line arguments
    val arguments = Arguments()
    try {
        CommandLine(arguments).parseArgs(*args)
    } catch (e: Exception) {
        LOG.error("Failed to read in command line arguments.", e)
        return
    }

    // check the read in arguments
    if (!checkArguments(arguments)) {
        LOG.error("The arguments provided are invalid.")
        LOG.error("Please see https://github.com/emufog/emufog/wiki for further information.")
        return
    }

    // read in the config file
    val config: Config
    try {
        config = readConfig(arguments.configPath!!)
    } catch (e: Exception) {
        LOG.error("Failed to read in the configuration file: {}", arguments.configPath, e)
        return
    }

    // read in the graph object
    LOG.infoSeparator()
    LOG.info("Starting to read in the graph")
    val reader = getReader(arguments.inputType!!)
    val graph = LOG.debugTiming("Read in the Graph") {
        reader.readGraph(arguments.files, config.baseAddress)
    }
    // print graph details for information purposes
    LOG.info("Number of nodes in the graph: {}", graph.edgeNodes.size)
    LOG.info("Number of edges in the graph: {}", graph.edges.size)

    // compute the backbone of the network
    LOG.infoSeparator()
    LOG.info("Starting the backbone identification")
    LOG.debugTiming("Determine the backbone of the topology") { identifyBackbone(graph) }
    LOG.info("Finished the backbone identification")
    LOG.info("Number of backbone nodes identified: {}", graph.backboneNodes.size)

    // assign devices to the edge
    LOG.infoSeparator()
    LOG.info("Assigning edge devices to the network")
    assignDeviceNodes(graph, config)
    LOG.info("Number of devices assigned: {}", graph.hostDevices.size)

    // find the fog node placements
    LOG.infoSeparator()
    LOG.info("Starting the fog node placement algorithm")
    val result = LOG.debugTiming("Place fog nodes in topology") {
        findPossibleFogNodes(graph, config)
    }
    LOG.info("Finished the fog node placement algorithm")
    if (!result.status) {
        // no fog placement found, aborting
        LOG.warn("Unable to find a fog placement with the provided config.")
        LOG.warn("Consider using different config.")
        return
    }

    LOG.info("Number of fog nodes identified: {}", result.placements.size)
    result.placements.forEach { graph.placeFogNode(it.node, it.type); }

    LOG.infoSeparator()
    LOG.info("Starting the export to a MaxiNet experiment file")
    val exporter = MaxiNetExporter
    LOG.debugTiming("Export the topology to MaxiNet") {
        exporter.exportGraph(graph, arguments.output!!, config.overWriteOutputFile)
    }
    LOG.info("Finished the export to a MaxiNet experiment file")
    LOG.info("Wrote the experiment file to: {}", arguments.output)
}

/**
 * Checks the read in arguments from the command line. Either sets a default or prints an
 * error if no argument is specified.
 *
 * @param arguments arguments to check
 * @return `true` if arguments are valid to start, `false` if arguments are invalid
 */
private fun checkArguments(arguments: Arguments): Boolean {
    var valid = true
    if (arguments.configPath == null) {
        arguments.configPath = Paths.get("src", "main", "resources", "application.yaml")
        LOG.warn("No '--config' argument found. Will use {} as default.", arguments.configPath)
    }
    if (arguments.inputType.isNullOrBlank()) {
        valid = false
        LOG.error("No '--type' argument found. Specify a valid input format.")
    }
    if (arguments.output == null) {
        arguments.output = Paths.get("output.py")
        LOG.warn("No '--output' argument found. Will use {} as default.", arguments.output)
    }
    if (arguments.files.isEmpty()) {
        valid = false
        LOG.error("No '--file' argument found. Provide at least one input file.")
    }

    return valid
}

/**
 * Returns the reader matching the given type from the command line.
 *
 * @param type topology type to read in
 * @return graph reader matching the type
 * @throws IllegalArgumentException thrown if the type is unsupported
 */
private fun getReader(type: String): GraphReader {
    return when (type.trim().toLowerCase()) {
        "brite" -> BriteFormatReader
        "caida" -> CaidaFormatReader
        else -> throw IllegalArgumentException("Unsupported Input Format: $type")
    }
}
