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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import picocli.CommandLine.ParameterException

internal class ArgumentsTest {

    @Test
    fun `empty init should be null and empty list`() {
        val arguments = Arguments()

        assertNull(arguments.configPath)
        assertNull(arguments.inputType)
        assertNull(arguments.output)
        assertTrue(arguments.files.isEmpty())
    }

    @Test
    fun `empty input should be null and empty list`() {
        val arguments = getArguments(emptyArray())

        assertNull(arguments.configPath)
        assertNull(arguments.inputType)
        assertNull(arguments.output)
        assertTrue(arguments.files.isEmpty())
    }

    @Test
    fun `parse -c should update the path`() {
        val arguments = getArguments(arrayOf("-c", "dir/test.yaml"))

        assertEquals("dir/test.yaml", arguments.configPath.toString())
    }

    @Test
    fun `parse --config should update the path`() {
        val arguments = getArguments(arrayOf("--config", "dir/test.yaml"))

        assertEquals("dir/test.yaml", arguments.configPath.toString())
    }

    @Test
    fun `parse empty -c should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("-c"))
        }
    }

    @Test
    fun `parse empty --config should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("--config"))
        }
    }

    @Test
    fun `parse -t should update the type`() {
        val arguments = getArguments(arrayOf("-t", "brite"))

        assertEquals("brite", arguments.inputType)
    }

    @Test
    fun `parse --type should update the type`() {
        val arguments = getArguments(arrayOf("--type", "brite"))

        assertEquals("brite", arguments.inputType)
    }

    @Test
    fun `parse empty -t should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("-t"))
        }
    }

    @Test
    fun `parse empty --type should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("--type"))
        }
    }

    @Test
    fun `parse -o should update the output path`() {
        val arguments = getArguments(arrayOf("-o", "out/test.py"))

        assertEquals("out/test.py", arguments.output.toString())
    }

    @Test
    fun `parse --output should update the output path`() {
        val arguments = getArguments(arrayOf("--output", "out/test.py"))

        assertEquals("out/test.py", arguments.output.toString())
    }

    @Test
    fun `parse empty -o should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("-o"))
        }
    }

    @Test
    fun `parse empty --output should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("--output"))
        }
    }

    @Test
    fun `parse -f should update the input path`() {
        val arguments = getArguments(arrayOf("-f", "in/test.brite"))

        assertEquals("in/test.brite", arguments.files[0].toString())
    }

    @Test
    fun `parse --file should update the input path`() {
        val arguments = getArguments(arrayOf("--file", "in/test.brite"))

        assertEquals("in/test.brite", arguments.files[0].toString())
    }

    @Test
    fun `multiple files should be added to list`() {
        val arguments = getArguments(arrayOf("--file", "in/test.brite", "-f", "in/test2.brite"))

        assertEquals(2, arguments.files.size)
        assertNotNull(arguments.files.firstOrNull { it.toString() == "in/test.brite" })
        assertNotNull(arguments.files.firstOrNull { it.toString() == "in/test2.brite" })
    }

    @Test
    fun `parse empty -f should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("-f"))
        }
    }

    @Test
    fun `parse empty --file should throw an exception`() {
        assertThrows<ParameterException> {
            getArguments(arrayOf("--file"))
        }
    }
}