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

//import picocli.CommandLine
//import picocli.CommandLine.Option
//import picocli.CommandLine.ParameterException
//import java.nio.file.Path
//
///**
// * Arguments to read in from the command line.
// */
//class Arguments {
//
//    @Option(names = ["--config", "-c"], description = ["config file to use"])
//    var configPath: Path? = null
//
//    @Option(names = ["--type", "-t"], description = ["input format to read in"])
//    var inputType: String? = null
//
//    @Option(names = ["--output", "-o"], description = ["path to the output file"])
//    var output: Path? = null
//
//    @Option(names = ["--file", "-f"], description = ["files to read in"])
//    var files: MutableList<Path> = ArrayList()
//}
//
///**
// * Reads in the application arguments from the command line and returns an instance of [Arguments].
// * @throws ParameterException if the parsing of the arguments fails
// */
//internal fun getArguments(args: Array<String>): Arguments {
//    val arguments = Arguments()
//    CommandLine(arguments).parseArgs(*args)
//
//    return arguments
//}
