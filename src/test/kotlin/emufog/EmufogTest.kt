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

import emufog.reader.brite.BriteFormatReader
import emufog.reader.caida.CaidaFormatReader
import emufog.util.getLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.assertThrows
import org.slf4j.Logger
import java.nio.file.Paths

@TestInstance(Lifecycle.PER_CLASS)
internal class EmufogTest {

    private val log: Logger = mockk {
        every { error(any()) } returns Unit
        every { warn(any(), any<Any>()) } returns Unit
    }

    @BeforeAll
    fun initialize() {
        mockkStatic("emufog.util.LoggingKt")
        every { getLogger("Emufog") } returns log
    }

    @AfterAll
    fun cleanUp() {
        unmockkAll()
    }

    @Test
    fun `getReader should return BriteReader for brite`() {
        val reader = getReader("brite")

        assertTrue(reader is BriteFormatReader)
    }

    @Test
    fun `getReader should return BriteReader for BRITE`() {
        val reader = getReader("BRITE")

        assertTrue(reader is BriteFormatReader)
    }

    @Test
    fun `getReader should return BriteReader for brite with spaces`() {
        val reader = getReader(" brite   ")

        assertTrue(reader is BriteFormatReader)
    }

    @Test
    fun `getReader should return CaidaReader for caida`() {
        val reader = getReader("caida")

        assertTrue(reader is CaidaFormatReader)
    }

    @Test
    fun `getReader should return CaidaReader for CAIDA`() {
        val reader = getReader("CAIDA")

        assertTrue(reader is CaidaFormatReader)
    }

    @Test
    fun `getReader should return CaidaReader for caida with spaces`() {
        val reader = getReader(" caida   ")

        assertTrue(reader is CaidaFormatReader)
    }

    @Test
    fun `getReader should throw an exception for unsupported types`() {
        assertThrows<IllegalArgumentException> {
            getReader("unsupported type")
        }
    }

    @Test
    fun `checkArguments with configPath null should return false`() {
        assertFalse(checkArguments(Arguments()))

        verify { log.error("No '--config' argument found. Provide a path to a configuration file.") }
    }

    @Test
    fun `checkArguments with type null should return false`() {
        assertFalse(checkArguments(Arguments()))

        verify { log.error("No '--type' argument found. Specify a valid input format.") }
    }

    @Test
    fun `checkArguments with output null should set the default`() {
        val arguments = Arguments()
        arguments.inputType = "brite"
        arguments.configPath = Paths.get("dir", "config")
        arguments.files = mutableListOf(Paths.get("dir", "input"))

        assertTrue(checkArguments(arguments))
        assertEquals("output.py", arguments.output.toString())

        verify { log.error("No '--type' argument found. Specify a valid input format.") }
    }

    @Test
    fun `checkArguments should return true if all arguments are set`() {
        val arguments = Arguments()
        arguments.inputType = "brite"
        arguments.configPath = Paths.get("dir", "config")
        arguments.output = Paths.get("dir", "output")
        arguments.files = mutableListOf(Paths.get("dir", "input"))

        assertTrue(checkArguments(arguments))
    }
}