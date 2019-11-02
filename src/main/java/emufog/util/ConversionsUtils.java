/*
 * MIT License
 *
 * Copyright (c) 2019 emufog contributors
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
package emufog.util;

import java.util.concurrent.TimeUnit;

public class ConversionsUtils {

    /**
     * Converts an interval of start and endpoint in nanoseconds to a
     * string representing the duration in the format e.g. '1h 1min 1s 1ms'.
     * Leading 0's will be dropped except for '0ms'. A negative interval will
     * set to duration 0ns.
     *
     * @param start start point of the interval in ns
     * @param end   end point of the interval in ns
     * @return string representation of the interval to print
     */
    public static String formatTimeInterval(long start, long end) {
        long duration = Math.max(end - start, 0);
        StringBuilder sb = new StringBuilder();
        boolean started = false;

        long hours = TimeUnit.HOURS.convert(duration, TimeUnit.NANOSECONDS);
        if (hours > 0) {
            sb.append(hours).append("h ");
            started = true;
        }

        long minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.NANOSECONDS) % 60;
        if (started || minutes > 0) {
            sb.append(minutes).append("min ");
            started = true;
        }

        long seconds = TimeUnit.SECONDS.convert(duration, TimeUnit.NANOSECONDS) % 60;
        if (started || seconds > 0) {
            sb.append(seconds).append("s ");
        }

        long milliseconds = TimeUnit.MILLISECONDS.convert(duration, TimeUnit.NANOSECONDS) % 1000;
        sb.append(milliseconds).append("ms");

        return sb.toString();
    }
}
