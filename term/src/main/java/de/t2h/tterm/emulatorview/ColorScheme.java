/*
 * Copyright (C) 2012 Steven Luo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.t2h.tterm.emulatorview;

/** A class describing a color scheme for an {@link EmulatorView}.
 *
 * <p>`EmulatorView´ supports changing its default foreground, background, and cursor colors. Passing a
 * `ColorScheme´ to {@link EmulatorView#setColorScheme setColorScheme} will cause the`EmulatorView´ to use the
 * specified colors as its defaults.</p>
 *
 * <p>Cursor colors can be omitted when specifying a color scheme; if no cursor colors are specified,
 * `ColorScheme´ will automatically select suitable cursor colors for you.</p>
 *
 * @see EmulatorView#setColorScheme
 */
public class ColorScheme {
    // ************************************************************
    // Constants
    // ************************************************************

    final private static int sDefaultCursorBackColor = 0xff808080;

    // ************************************************************
    // Attributes
    // ************************************************************

    /**
     * @return Color scheme's foreground color as an ARGB hex value.
     */
    public int getForeColor () {
        return foreColor;
    }
    private int foreColor;

    /**
     * @return Color scheme's background color as an ARGB hex value.
     */
    public int getBackColor () {
        return backColor;
    }
    private int backColor;

    /**
     * @return Color scheme's cursor foreground color as an ARGB hex value.
     */
    public int getCursorForeColor () {
        return cursorForeColor;
    }
    private int cursorForeColor;

    /**
     * @return Color scheme's cursor background color as an ARGB hex value.
     */
    public int getCursorBackColor () {
        return cursorBackColor;
    }
    private int cursorBackColor;

    // ************************************************************
    // Methods
    // ************************************************************

    private void setDefaultCursorColors () {
        cursorBackColor = sDefaultCursorBackColor;
        // Use the foreColor unless the foreColor is too similar to the cursorBackColor
        int foreDistance = distance(foreColor, cursorBackColor);
        int backDistance = distance(backColor, cursorBackColor);
        cursorForeColor = foreDistance * 2 >= backDistance ? foreColor : backColor;
    }

    private static int distance (int a, int b) {
        return channelDistance(a, b, 0) * 3 + channelDistance(a, b, 1) * 5 + channelDistance(a, b, 2);
    }

    private static int channelDistance (int a, int b, int channel) {
        return Math.abs(getChannel(a, channel) - getChannel(b, channel));
    }

    private static int getChannel (int color, int channel) {
        return 0xff & (color >> ((2 - channel) * 8));
    }

    /**
     * @param foreColor The foreground color as an ARGB hex value.
     * @param backColor The background color as an ARGB hex value.
     */
    public ColorScheme (int foreColor, int backColor) {
        this.foreColor = foreColor;
        this.backColor = backColor;
        setDefaultCursorColors();
    }

    /**
     * @param foreColor The foreground color as an ARGB hex value.
     * @param backColor The background color as an ARGB hex value.
     * @param cursorForeColor The cursor foreground color as an ARGB hex value.
     * @param cursorBackColor The cursor foreground color as an ARGB hex value.
     */
    public ColorScheme (int foreColor, int backColor, int cursorForeColor, int cursorBackColor) {
        this.foreColor = foreColor;
        this.backColor = backColor;
        this.cursorForeColor = cursorForeColor;
        this.cursorBackColor = cursorBackColor;
    }

    /**
     * @param scheme An integer array of <code>{ foreColor, backColor,
     *               optionalCursorForeColor, optionalCursorBackColor }</code>.
     */
    public ColorScheme (int[] scheme) {
        int schemeLength = scheme.length;
        if (schemeLength != 2 && schemeLength != 4) throw new IllegalArgumentException();

        this.foreColor = scheme[0];
        this.backColor = scheme[1];
        if (schemeLength == 2)  {
            setDefaultCursorColors();
        } else {
            this.cursorForeColor = scheme[2];
            this.cursorBackColor = scheme[3];
        }
    }
}
