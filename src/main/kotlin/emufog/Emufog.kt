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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
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
import java.nio.file.Path
import java.nio.file.Paths

internal val LOG = getLogger("Emufog")

/**
 * Main function call to start EmuFog.
 *
 * @param args arguments of the command line
 */
fun main(args: Array<String>) = EmufogCommand().main(args)

/**
 * Internal class to represent the command line call of EmuFog. Mainly focuses on the parsing of command line arguments.
 */
internal class EmufogCommand : CliktCommand() {

    private companion object {
        const val defaultOutput = "output.py"
    }

    val configPath: Path by option(
        names = *arrayOf("-c", "--config"),
        help = "config file to use"
    ).path(exists = true).required()

    val inputType: InputFormatTypes by option(
        names = *arrayOf("-t", "--type"),
        help = "input format to read in"
    ).enum<InputFormatTypes>().required()

    val output: Path by option(
        names = *arrayOf("-o", "--output"),
        help = "path to the output file (defaults to $defaultOutput)"
    ).path().default(Paths.get(defaultOutput))

    val files: List<Path> by option(
        names = *arrayOf("-f", "--file"),
        help = "files to read in"
    ).path(exists = true).multiple(required = true)

    override fun run() {
        EmufogExecution(configPath, inputType, output, files).safelyRun()
    }
}

/**
 * This class represents the actual execution of EmuFog.
 *
 * @property configPath path to the config file
 * @property inputType type of the input files
 * @property output path to the output file
 * @property files list of input files
 */
internal class EmufogExecution(
    private val configPath: Path,
    private val inputType: InputFormatTypes,
    private val output: Path,
    private val files: List<Path>
) {

    fun safelyRun() {
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
            run()
        } catch (e: Exception) {
            LOG.error("An exception stopped EmuFog!", e)
        }

        LOG.infoSeparator()
        LOG.info("Closing EmuFog...")
        LOG.infoSeparator()
    }

    fun run() {
        // read in the config file
        val config: Config
        try {
            config = readConfig(configPath)
        } catch (e: Exception) {
            LOG.error("Failed to read in the configuration file: {}", configPath, e)
            return
        }

        // read in the graph object
        LOG.infoSeparator()
        LOG.info("Starting to read in the graph")
        val reader = inputType.getReader()
        val graph = LOG.debugTiming("Read in the Graph") {
            reader.readGraph(files, config.baseAddress)
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
            exporter.exportGraph(graph, output, config.overWriteOutputFile)
        }
        LOG.info("Finished the export to a MaxiNet experiment file")
        LOG.info("Wrote the experiment file to: {}", output)
    }
}

/**
 * This enumeration class represents the supported input formats of EmuFog. Each format should return a [GraphReader]
 * via [getReader].
 */
enum class InputFormatTypes {
    BRITE,
    CAIDA;

    /**
     * Returns the respective [GraphReader] instance matching the given format type.
     */
    fun getReader(): GraphReader {
        return when (this) {
            BRITE -> BriteFormatReader
            CAIDA -> CaidaFormatReader
        }
    }
}
