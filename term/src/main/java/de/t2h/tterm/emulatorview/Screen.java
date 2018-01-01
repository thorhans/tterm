/*
 * Copyright (C) 2007 The Android Open Source Project
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

/** An abstract screen interface.
 *
 * <p>A terminal screen stores lines of text. (The reason to abstract it is to allow different
 * implementations, and to hide implementation details from clients.)</p>
 */
// ThH: Cleaned up.
//
interface Screen {
    /** Set line wrap flag for a given row.
     *
     * <p>Affects how lines are logically wrapped when changing screen size or converting to a transcript.</p>
     */
    void setLineWrap (int row);

    /** Store a Unicode code point into the screen at location (x, y).
     *
     * @param x          x coordinate (also known as column)
     * @param y          y coordinate (also known as row)
     * @param codePoint  Unicode code point to store
     * @param style      the text style
     */
    void set (int x, int y, int codePoint, int style);

    /** Store byte b into the screen at location (x, y).
     *
     * @param x      x coordinate (also known as column)
     * @param y      y coordinate (also known as row)
     * @param b      ASCII character to store
     * @param style  the text style
     */
    void set (int x, int y, byte b, int style);

    /** Scroll the screen down one line.
     *
     * <p>To scroll the whole screen of a 24 line screen, the arguments would be (0, 24).</p>
     *
     * @param topMargin     first line that is scrolled
     * @param bottomMargin  one line after the last line that is scrolled
     * @param style         the style for the newly exposed line
     */
    void scroll (int topMargin, int bottomMargin, int style);

    /*** Block copy characters from one position in the screen to another.
     *
     * <p>The two positions can overlap. All characters of the source and destination must be within the
     * bounds of the screen, or else an InvalidParemeterException will be thrown.</p>
     *
     * @param sx  source x coordinate
     * @param sy  source y coordinate
     * @param w   width
     * @param h   height
     * @param dx  destination x coordinate
     * @param dy  destination y coordinate
     */
    void blockCopy (int sx, int sy, int w, int h, int dx, int dy);

    /** Block set characters.
     *
     * <p>All characters must be within the bounds of the screen, or else and InvalidParemeterException will
     * be thrown. Typically this is called with a "val" argument of 32 to clear a block of characters.</p>
     *
     * @param sx     source x
     * @param sy     source y
     * @param w      width
     * @param h      height
     * @param val    value to set.
     * @param style  the text style
     */
    void blockSet (int sx, int sy, int w, int h, int val, int style);

    /** Get the contents of the transcript buffer as a text string.
     *
     * @return  the contents of the transcript buffer
     */
    String getTranscriptText ();

    /** Get the contents of the transcript buffer as a text string with color information.
     *
     * @param colors  a GrowableIntArray which will hold the colors
     *
     * @return  the contents of the transcript buffer
     */
    String getTranscriptText (GrowableIntArray colors);

    /** Get the selected text inside transcript buffer as a text string.
     *
     * @param x1  selection start
     * @param y1  selection start
     * @param x2  selection end
     * @param y2  selection end
     *
     * @return  the contents of the transcript buffer.
     */
    String getSelectedText (int x1, int y1, int x2, int y2);

    /** Get the selected text inside transcript buffer as a text string with color information.
     *
     * @param colors  a StringBuilder which will hold the colors
     * @param x1      selection start
     * @param y1      selection start
     * @param x2      selection end
     * @param y2      selection end
     *
     * @return t he contents of the transcript buffer
     */
    String getSelectedText (GrowableIntArray colors, int x1, int y1, int x2, int y2);

    /** Get the number of "active" (in-use) screen rows, including any rows in a scrollback buffer. */
    int getActiveRows ();

    /** Try to resize the screen without losing its contents.
     *
     * @param columns
     * @param rows
     * @param cursor  An int[2] containing the current cursor position { col, row }.
     *
     *     If the resize succeeds, the array will be updated to reflect the new location.
     *
     * @return  Whether the resize succeeded.
     *
     *     If the operation fails, save the contents of the screen and then use the standard resize.
     */
    boolean fastResize (int columns, int rows, int[] cursor);

    /** Resize the screen.
     *
     * @param columns
     * @param rows
     * @param style
     */
    void resize (int columns, int rows, int style);
}
