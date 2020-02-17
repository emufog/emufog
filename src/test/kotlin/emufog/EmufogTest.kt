/*
 * MIT License
 *
 * Copyright (c) 2020 emufog contributors
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

import com.github.ajalt.clikt.core.BadParameterValue
import com.github.ajalt.clikt.core.MissingParameter
import emufog.config.Config
import emufog.config.readConfig
import emufog.reader.brite.BriteFormatReader
import emufog.reader.caida.CaidaFormatReader
import emufog.util.getLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.unmockkConstructor
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Path

internal class EmufogTest {

    @TestInstance(Lifecycle.PER_CLASS)
    class EmuFogTests {

        private val log: Logger = mockk {
            every { error(any()) } returns Unit
            every { warn(any(), any<Any>()) } returns Unit
        }

        private val fs: FileSystem = mockk {
            every { getPath(outputString) } returns mockk()
            every { getPath("output.py") } returns mockk()
            every { getPath(configPathString) } returns configPath
        }

        private val config: Config = mockk()

        private val configPathString = "config.yaml"
        private val configPath: Path = mockk()

        private val typeString = "brite"
        private val type: InputFormatTypes = InputFormatTypes.BRITE

        private val outputString = "out.py"
        private val output: Path = mockk()

        private val file1String = "file1.txt"
        private val file2String = "file2.txt"
        private val files: List<Path> = listOf(mockk())

        @BeforeAll
        fun initialize() {
            mockkStatic("emufog.util.LoggingKt")
            every { getLogger("Emufog") } returns log

            mockkStatic("emufog.config.ConfigKt")
            every { readConfig(configPath) } returns config

            mockkStatic("java.nio.file.FileSystems")
            every { FileSystems.getDefault() } returns fs
        }

        @AfterAll
        fun cleanUp() {
            unmockkAll()
        }

        @Test
        fun `no parameter at all should abort`() {
            assertThrows<MissingParameter> {
                Emufog().parse(emptyArray())
            }
        }

        @Test
        fun `no --config at all should abort`() {
            val array = arrayOf("-t", typeString, "-f", file1String)
            assertThrows<BadParameterValue> {
                Emufog().parse(array)
            }
        }

        @Test
        fun `no --type at all should abort`() {
            val array = arrayOf("-c", configPathString, "-f", file1String)
            assertThrows<BadParameterValue> {
                Emufog().parse(array)
            }
        }

        @Test
        fun `no --file at all should abort`() {
            val array = arrayOf("-c", configPathString, "-t", typeString)
            assertThrows<BadParameterValue> {
                Emufog().parse(array)
            }
        }

        @Test
        fun `fully provided parameters should run the app`() {
            mockkConstructor(FileSystem::class)
            val array = arrayOf("-c", configPathString, "-t", typeString, "-f", file1String)
            every { anyConstructed<FileSystem>().getPath(configPathString) } returns configPath
            main(array)
            unmockkConstructor(FileSystem::class)
        }
    }

    class InputFormatsTests {

        @Test
        fun `getReader should return BriteReader for brite`() {
            val reader = InputFormatTypes.BRITE.getReader()

            assertTrue(reader is BriteFormatReader)
        }

        @Test
        fun `getReader should return CaidaReader for caida`() {
            val reader = InputFormatTypes.CAIDA.getReader()

            assertTrue(reader is CaidaFormatReader)
        }
    }
}