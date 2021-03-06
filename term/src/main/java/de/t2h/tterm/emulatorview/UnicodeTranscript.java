/*
 * Copyright (C) 2011 Steven Luo
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

import android.util.Log;
import android.text.AndroidCharacter;

/** A backing store for a TranscriptScreen.
 *
 * <p>The text is stored as a circular buffer of rows. There are two types of row:</p>
 *
 * <ul>
 *   <li>"basic", which is a char[] array used to store lines which consist entirely of regular-width
 *   characters (no combining characters, zero-width characters, East Asian double-width characters, etc.) in
 *   the BMP; and</li>
 *
 *   <li>"full", which is a char[] array with extra trappings which can be used to store a line containing any
 *   valid Unicode sequence. An array of short[] is used to store the "offset" at which each column starts;
 *   for example, if column 20 starts at index 23 in the array, then mOffset[20] = 3.</li>
 * </ul>
 *
 * <p>Style information is stored in a separate circular buffer of StyleRows.</p>
 *
 * <p>Rows are allocated on demand, when a character is first stored into them. A "basic" row is allocated
 * unless the store which triggers the allocation requires a "full" row. "Basic" rows are converted to "full"
 * rows when needed. There is no conversion in the other direction -- a "full" row stays that way even if it
 * contains only regular-width BMP characters.</p>
 */
class UnicodeTranscript {
    // ************************************************************
    // Constants
    // ************************************************************

    private static final String TAG = "TTerm.UnicodeTranscript";

    /** Minimum API version for which we're willing to let Android try rendering conjoining Hangul jamo as
     *  composed syllable blocks.
     *
     * <p>This appears to work on Android 4.1.2, 4.3, and 4.4 (real devices only; the emulator's broken for
     * some reason), but not on 4.0.4 -- hence the choice of API 16 as the minimum.</p>
     */
    static final int HANGUL_CONJOINING_MIN_SDK = 16;

    // ************************************************************
    // Attributes
    // ************************************************************

    private Object[] mLines;
    private StyleRow[] mColor;
    private boolean[] mLineWrap;
    private int mTotalRows;
    private int mScreenRows;
    private int mColumns;

    private int mActiveTranscriptRows = 0;
    public int getActiveTranscriptRows () { return mActiveTranscriptRows; }

    public int getActiveRows () { return mActiveTranscriptRows + mScreenRows; }

    private int mDefaultStyle = 0;
    public int getDefaultStyle () { return mDefaultStyle; }
    public void setDefaultStyle (int defaultStyle) { mDefaultStyle = defaultStyle; }

    private int mScreenFirstRow = 0;

    private char[] tmpLine;
    private StyleRow tmpColor;

    // ************************************************************
    // Methods
    // ************************************************************

    public UnicodeTranscript (int columns, int totalRows, int screenRows, int defaultStyle) {
        mColumns = columns;
        mTotalRows = totalRows;
        mScreenRows = screenRows;
        mLines = new Object[totalRows];
        mColor = new StyleRow[totalRows];
        mLineWrap = new boolean[totalRows];
        tmpColor = new StyleRow(defaultStyle, mColumns);

        mDefaultStyle = defaultStyle;
    }


    /** Convert a row value from the public external coordinate system to our internal private coordinate
     *  system.
     *
     * <p>External coordinate system: -mActiveTranscriptRows to mScreenRows-1, with the screen being
     * 0..mScreenRows-1</p>.
     *
     * <p>Internal coordinate system: the mScreenRows lines starting at mScreenFirstRow comprise the screen,
     * while the mActiveTranscriptRows lines ending at mScreenRows-1 form the transcript (as a circular
     * buffer).</p>
     *
     * @param extRow  a row in the external coordinate system.
     *
     * @return The row corresponding to the input argument in the private coordinate system.
     */
    private int externalToInternalRow (int extRow) {
        if(extRow < -mActiveTranscriptRows || extRow > mScreenRows) {
            String errorMessage = "externalToInternalRow "+ extRow +
                " " + mScreenRows + " " + mActiveTranscriptRows;
            Log.e(TAG, errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if(extRow >= 0) {
            return (mScreenFirstRow + extRow) % mTotalRows;
        } else {
            if(-extRow > mScreenFirstRow) {
                return mTotalRows + mScreenFirstRow + extRow;
            } else {
                return mScreenFirstRow + extRow;
            }
        }
    }

    public void setLineWrap (int row) {
        mLineWrap[externalToInternalRow(row)] = true;
    }

    public boolean getLineWrap (int row) {
        return mLineWrap[externalToInternalRow(row)];
    }

    /** Resize the screen which this transcript backs.
     *
     * <p>Currently, this only works if the number of columns does not change.</p>
     *
     * @param newColumns  The number of columns the screen should have.
     * @param newRows     The number of rows the screen should have.
     * @param cursor      An int[2] containing the current cursor location.
     *
     *      If the resize succeeds, this will be updated with the new cursor location. If null, don't do
     *      cursor-position-dependent tasks such as trimming blank lines during the resize.
     *
     * @return Whether or not the resize succeeded.
     *
     *     If the resize failed, the caller may "resize" the screen by copying out all the data and placing it
     *     into a new transcript of the correct size.
     */
    public boolean resize (int newColumns, int newRows, int[] cursor) {
        if(newColumns != mColumns || newRows > mTotalRows) {
            return false;
        }

        int screenRows = mScreenRows;
        int activeTranscriptRows = mActiveTranscriptRows;
        int shift = screenRows - newRows;
        if(shift < -activeTranscriptRows) {
            // We want to add blank lines at the bottom instead of at the top.
            Object[] lines = mLines;
            Object[] color = mColor;
            boolean[] lineWrap = mLineWrap;
            int screenFirstRow = mScreenFirstRow;
            int totalRows = mTotalRows;
            for(int i = 0; i < activeTranscriptRows - shift; ++i) {
                int index = (screenFirstRow + screenRows + i) % totalRows;
                lines[index] = null;
                color[index] = null;
                lineWrap[index] = false;
            }
            shift = -activeTranscriptRows;
        } else if(shift > 0 && cursor != null && cursor[1] != screenRows - 1) {
            // When shrinking the screen, we want to hide blank lines at the bottom in preference to lines at
            // the top of the screen.
            Object[] lines = mLines;
            for(int i = screenRows - 1; i > cursor[1]; --i) {
                int index = externalToInternalRow(i);
                if(lines[index] == null) {
                    // Line is blank
                    --shift;
                    if(shift == 0) {
                        break;
                    } else {
                        continue;
                    }
                }

                char[] line;
                if(lines[index] instanceof char[]) {
                    line = (char[]) lines[index];
                } else {
                    line = ((FullUnicodeLine) lines[index]).getLine();
                }

                int len = line.length;
                int j;
                for(j = 0; j < len; ++j) {
                    if(line[j] == 0) {
                        // We've reached the end of the line.
                        j = len;
                        break;
                    } else if(line[j] != ' ') {
                        // Line is not blank.
                        break;
                    }
                }

                if(j == len) {
                    // Line is blank.
                    --shift;
                    if(shift == 0) {
                        break;
                    } else {
                        continue;
                    }
                } else {
                    // Line not blank -- we keep it and everything above.
                    break;
                }
            }
        }

        if(shift > 0 || (shift < 0 && mScreenFirstRow >= -shift)) {
            // All we're doing is moving the top of the screen.
            mScreenFirstRow = (mScreenFirstRow + shift) % mTotalRows;
        } else if(shift < 0) {
            // The new top of the screen wraps around the top of the array.
            mScreenFirstRow = mTotalRows + mScreenFirstRow + shift;
        }

        if(mActiveTranscriptRows + shift < 0) {
            mActiveTranscriptRows = 0;
        } else {
            mActiveTranscriptRows += shift;
        }
        if(cursor != null) {
            cursor[1] -= shift;
        }
        mScreenRows = newRows;

        return true;
    }

    /** Block copy lines and associated metadata from one location to another in the circular buffer, taking
     *  wraparound into account.
     *
     * @param src    The first line to be copied.
     * @param len    The number of lines to be copied.
     * @param shift  The offset of the destination from the source.
     */
    private void blockCopyLines (int src, int len, int shift) {
        int totalRows = mTotalRows;

        int dst;
        if(src + shift >= 0) {
            dst = (src + shift) % totalRows;
        } else {
            dst = totalRows + src + shift;
        }

        if(src + len <= totalRows && dst + len <= totalRows) {
            // Fast path -- no wraparound.
            System.arraycopy(mLines, src, mLines, dst, len);
            System.arraycopy(mColor, src, mColor, dst, len);
            System.arraycopy(mLineWrap, src, mLineWrap, dst, len);
            return;
        }

        if(shift < 0) {
            // Do the copy from top to bottom.
            for(int i = 0; i < len; ++i) {
                mLines[(dst + i) % totalRows] = mLines[(src + i) % totalRows];
                mColor[(dst + i) % totalRows] = mColor[(src + i) % totalRows];
                mLineWrap[(dst + i) % totalRows] = mLineWrap[(src + i) % totalRows];
            }
        } else {
            // Do the copy from bottom to top.
            for(int i = len - 1; i >= 0; --i) {
                mLines[(dst + i) % totalRows] = mLines[(src + i) % totalRows];
                mColor[(dst + i) % totalRows] = mColor[(src + i) % totalRows];
                mLineWrap[(dst + i) % totalRows] = mLineWrap[(src + i) % totalRows];
            }
        }
    }

    /** Scroll the screen down one line.
     *
     * <p>To scroll the whole screen of a 24 line screen, the arguments would be (0, 24).</p>
     *
     * @param topMargin     First line that is scrolled.
     * @param bottomMargin  One line after the last line that is scrolled.
     * @param style         the style for the newly exposed line.
     */
    public void scroll (int topMargin, int bottomMargin, int style) {
        // Separate out reasons so that stack crawls help us figure out which condition was violated.
        if(topMargin > bottomMargin - 1) throw new IllegalArgumentException();
        if(topMargin < 0) throw new IllegalArgumentException();
        if(bottomMargin > mScreenRows) throw new IllegalArgumentException();

        int screenRows = mScreenRows;
        int totalRows = mTotalRows;

        if(topMargin == 0 && bottomMargin == screenRows) {
            // Fast path -- scroll the entire screen.
            mScreenFirstRow = (mScreenFirstRow + 1) % totalRows;
            if(mActiveTranscriptRows < totalRows - screenRows) {
                ++mActiveTranscriptRows;
            }

            // Blank the bottom margin.
            int blankRow = externalToInternalRow(bottomMargin - 1);
            mLines[blankRow] = null;
            mColor[blankRow] = new StyleRow(style, mColumns);
            mLineWrap[blankRow] = false;

            return;
        }

        int screenFirstRow = mScreenFirstRow;
        int topMarginInt = externalToInternalRow(topMargin);
        int bottomMarginInt = externalToInternalRow(bottomMargin);

        // Save the scrolled line, move the lines above it on the screen down one line, move the lines on
        // screen below the bottom margin down one line, then insert the scrolled line into the transcript.
        Object[] lines = mLines;
        StyleRow[] color = mColor;
        boolean[] lineWrap = mLineWrap;
        Object scrollLine = lines[topMarginInt];
        StyleRow scrollColor = color[topMarginInt];
        boolean scrollLineWrap = lineWrap[topMarginInt];
        blockCopyLines(screenFirstRow, topMargin, 1);
        blockCopyLines(bottomMarginInt, screenRows - bottomMargin, 1);
        lines[screenFirstRow] = scrollLine;
        color[screenFirstRow] = scrollColor;
        lineWrap[screenFirstRow] = scrollLineWrap;

        // Update the screen location.
        mScreenFirstRow = (screenFirstRow + 1) % totalRows;
        if(mActiveTranscriptRows < totalRows - screenRows) {
            ++mActiveTranscriptRows;
        }

        // Blank the bottom margin.
        int blankRow = externalToInternalRow(bottomMargin - 1);
        lines[blankRow] = null;
        color[blankRow] = new StyleRow(style, mColumns);
        lineWrap[blankRow] = false;

        return;
    }

    /** Block copy characters from one position in the screen to another.
     *
     * <p>The two positions can overlap. All characters of the source and destination must be within the
     * bounds of the screen, or else an InvalidParameterException will be thrown.</p>
     *
     * @param sx  source X coordinate
     * @param sy  source Y coordinate
     * @param w   width
     * @param h   height
     * @param dx  destination X coordinate
     * @param dy  destination Y coordinate
     */
    public void blockCopy (int sx, int sy, int w, int h, int dx, int dy) {
        if(sx < 0 || sx + w > mColumns || sy < 0 || sy + h > mScreenRows
            || dx < 0 || dx + w > mColumns || dy < 0 || dy + h > mScreenRows
        ) {
            throw new IllegalArgumentException();
        }
        Object[] lines = mLines;
        StyleRow[] color = mColor;
        if(sy > dy) {
            // Move in increasing order.
            for(int y = 0; y < h; y++) {
                int srcRow = externalToInternalRow(sy + y);
                int dstRow = externalToInternalRow(dy + y);
                if(lines[srcRow] instanceof char[] && lines[dstRow] instanceof char[]) {
                    System.arraycopy(lines[srcRow], sx, lines[dstRow], dx, w);
                } else {
                    // XXX There has to be a faster way to do this ...
                    int extDstRow = dy + y;
                    char[] tmp = getLine(sy + y, sx, sx + w, true);
                    if(tmp == null) {
                        // Source line was blank
                        blockSet(dx, extDstRow, w, 1, ' ', mDefaultStyle);
                        continue;
                    }
                    char cHigh = 0;
                    int x = 0;
                    int columns = mColumns;
                    for(int i = 0; i < tmp.length; ++i) {
                        if(tmp[i] == 0 || dx + x >= columns) {
                            break;
                        }
                        if(Character.isHighSurrogate(tmp[i])) {
                            cHigh = tmp[i];
                            continue;
                        } else if(Character.isLowSurrogate(tmp[i])) {
                            int codePoint = Character.toCodePoint(cHigh, tmp[i]);
                            setChar(dx + x, extDstRow, codePoint);
                            x += charWidth(codePoint);
                        } else {
                            setChar(dx + x, extDstRow, tmp[i]);
                            x += charWidth(tmp[i]);
                        }
                    }
                }
                color[srcRow].copy(sx, color[dstRow], dx, w);
            }
        } else {
            // Move in decreasing order.
            for(int y = 0; y < h; y++) {
                int y2 = h - (y + 1);
                int srcRow = externalToInternalRow(sy + y2);
                int dstRow = externalToInternalRow(dy + y2);
                if(lines[srcRow] instanceof char[] && lines[dstRow] instanceof char[]) {
                    System.arraycopy(lines[srcRow], sx, lines[dstRow], dx, w);
                } else {
                    int extDstRow = dy + y2;
                    char[] tmp = getLine(sy + y2, sx, sx + w, true);
                    if(tmp == null) {
                        // Source line was blank
                        blockSet(dx, extDstRow, w, 1, ' ', mDefaultStyle);
                        continue;
                    }
                    char cHigh = 0;
                    int x = 0;
                    int columns = mColumns;
                    for(int i = 0; i < tmp.length; ++i) {
                        if(tmp[i] == 0 || dx + x >= columns) {
                            break;
                        }
                        if(Character.isHighSurrogate(tmp[i])) {
                            cHigh = tmp[i];
                            continue;
                        } else if(Character.isLowSurrogate(tmp[i])) {
                            int codePoint = Character.toCodePoint(cHigh, tmp[i]);
                            setChar(dx + x, extDstRow, codePoint);
                            x += charWidth(codePoint);
                        } else {
                            setChar(dx + x, extDstRow, tmp[i]);
                            x += charWidth(tmp[i]);
                        }
                    }
                }
                color[srcRow].copy(sx, color[dstRow], dx, w);
            }
        }
    }

    /** Block set characters.
     *
     * <p>All characters must be within the bounds of the screen, or else and InvalidParemeterException will
     * be thrown. Typically this is called with a "val" argument of 32 to clear a block of characters.</p>
     *
     * @param sx   source X
     * @param sy   source Y
     * @param w    width
     * @param h    height
     * @param val  value to set.
     */
    public void blockSet (int sx, int sy, int w, int h, int val, int style) {
        if(sx < 0 || sx + w > mColumns || sy < 0 || sy + h > mScreenRows) {
            Log.e(TAG, "illegal arguments! " + sx + " " + sy + " " + w + " " + h + " " + val + " " + mColumns + " " + mScreenRows);
            throw new IllegalArgumentException();
        }

        for(int y = 0; y < h; y++) {
            for(int x = 0; x < w; x++) {
                setChar(sx + x, sy + y, val, style);
            }
        }
    }

    /** Gives the display width of the code point in a monospace font.
     *
     * <p>Nonspacing combining marks, format characters, and control characters have display width zero. East
     * Asian fullwidth and wide characters have display width two. All other characters have display width
     * one.</p>
     *
     * <p>Known issues:</p>
     *
     * <ul>
     *   <li>Assigning all East Asian "ambiguous" characters a width of 1 may not be correct if Android
     *   renders those characters as wide in East Asiancontext (as the Unicode standard permits).<li>
     *
     *   <li>Isolated Hangul conjoining medial vowels and final consonants are treated as combining characters
     *   (they should only be combining when part of a Korean syllable block).<li>
     * /ul>
     *
     * @param codePoint  A Unicode code point.
     *
     * @return  The display width of the Unicode code point.
     */
    public static int charWidth (int codePoint) {
        // Early out for ASCII printable characters.
        if(codePoint > 31 && codePoint < 127) return 1;

        // HACK: We're using ASCII ESC to save the location of the cursor across screen resizes, so we need to
        // pretend that it has width 1.
        if(codePoint == 27) return 1;

        switch(Character.getType(codePoint)) {
        case Character.CONTROL:
        case Character.FORMAT:
        case Character.NON_SPACING_MARK:
        case Character.ENCLOSING_MARK:
            return 0;
        }

        if(    (0x1160 <= codePoint && codePoint <= 0x11FF)
            || (0xD7B0 <= codePoint && codePoint <= 0xD7FF)) {
            // Treat Hangul jamo medial vowels and final consonants as combining characters with width 0 to
            // make jamo composition work correctly.
            //
            // XXX: This is wrong for medials/finals outside a Korean syllable block, but there's no easy
            // solution to that problem, and we may as well at least get the common case right.
            return 0;
        }
        if(Character.charCount(codePoint) == 1) {
            // Android's getEastAsianWidth() only works for BMP characters.
            switch(AndroidCharacter.getEastAsianWidth((char) codePoint)) {
            case AndroidCharacter.EAST_ASIAN_WIDTH_FULL_WIDTH:
            case AndroidCharacter.EAST_ASIAN_WIDTH_WIDE:
                return 2;
            }
        } else {
            // Outside the BMP, only the ideographic planes contain wide chars.
            switch((codePoint >> 16) & 0xf) {
            case 2: // Supplementary Ideographic Plane.
            case 3: // Tertiary Ideographic Plane.
                return 2;
            }
        }

        return 1;
    }

    public static int charWidth (char cHigh, char cLow) {
        return charWidth(Character.toCodePoint(cHigh, cLow));
    }

    /** Gives the display width of a code point in a char array in a monospace font.
     *
     * @param chars  The array containing the code point in question.
     * @param index  The index into the array at which the code point starts.
     *
     * @return  The display width of the Unicode code point.
     */
    public static int charWidth (char[] chars, int index) {
        char c = chars[index];
        if(Character.isHighSurrogate(c)) {
            return charWidth(c, chars[index+1]);
        } else {
            return charWidth(c);
        }
    }

    /** Get the contents of a line (or part of a line) of the transcript.
     *
     * <p>The char[] array returned may be part of the internal representation of the line -- make a copy
     * first if you want to modify it. The returned array may be longer than the requested portion of the
     * transcript; in this case, the last character requested will be followed by a NUL, and the contents of
     * the rest of the array could potentially be garbage.</p>
     * *
     * @param row  The row number to get (-mActiveTranscriptRows..mScreenRows-1)
     * @param x1   The first screen position that's wanted
     * @param x2   One after the last screen position that's wanted
     *
     * @return A   char[] array containing the requested contents
     */
    public char[] getLine (int row, int x1, int x2) {
        return getLine(row, x1, x2, false);
    }

    /** Get the whole contents of a line of the transcript. */
    public char[] getLine (int row) {
        return getLine(row, 0, mColumns, true);
    }

    private char[] getLine (int row, int x1, int x2, boolean strictBounds) {
        if(row < -mActiveTranscriptRows || row > mScreenRows-1) throw new IllegalArgumentException();

        int columns = mColumns;
        row = externalToInternalRow(row);
        if(mLines[row] == null) {
            // Line is blank.
            return null;
        }
        if(mLines[row] instanceof char[]) {
            // Line contains only regular-width BMP characters.
            if(x1 == 0 && x2 == columns) {
                // Want the whole row? Easy.
                return (char[]) mLines[row];
            } else {
                if(tmpLine == null || tmpLine.length < columns + 1) {
                    tmpLine = new char[columns+1];
                }
                int length = x2 - x1;
                System.arraycopy(mLines[row], x1, tmpLine, 0, length);
                tmpLine[length] = 0;
                return tmpLine;
            }
        }

        // Figure out how long the array needs to be.
        FullUnicodeLine line = (FullUnicodeLine) mLines[row];
        char[] rawLine = line.getLine();

        if(x1 == 0 && x2 == columns) {
            // We can return the raw line after ensuring it's NUL-terminated at the appropriate place.
            int spaceUsed = line.getSpaceUsed();
            if(spaceUsed < rawLine.length) {
                rawLine[spaceUsed] = 0;
            }
            return rawLine;
        }

        x1 = line.findStartOfColumn(x1);
        if(x2 < columns) {
            int endCol = x2;
            x2 = line.findStartOfColumn(endCol);
            if(!strictBounds && endCol > 0 && endCol < columns - 1) {
                // If the end column is the middle of an East Asian wide character, include that character in
                // the bounds.
                if(x2 == line.findStartOfColumn(endCol - 1)) {
                    x2 = line.findStartOfColumn(endCol + 1);
                }
            }
        } else {
            x2 = line.getSpaceUsed();
        }
        int length = x2 - x1;

        if(tmpLine == null || tmpLine.length < length + 1) {
            tmpLine = new char[length+1];
        }
        System.arraycopy(rawLine, x1, tmpLine, 0, length);
        tmpLine[length] = 0;
        return tmpLine;
    }

    /** Get color/formatting information for a particular line.
     *
     * <p>The returned object may be a pointer to a temporary buffer, only good until the next call to
     * getLineColor.</p>
     */
    public StyleRow getLineColor (int row, int x1, int x2) {
        return getLineColor(row, x1, x2, false);
    }

    public StyleRow getLineColor (int row) {
        return getLineColor(row, 0, mColumns, true);
    }

    private StyleRow getLineColor (int row, int x1, int x2, boolean strictBounds) {
        if(row < -mActiveTranscriptRows || row > mScreenRows-1) throw new IllegalArgumentException();

        row = externalToInternalRow(row);
        StyleRow color = mColor[row];
        StyleRow tmp = tmpColor;
        if(color != null) {
            int columns = mColumns;
            if(!strictBounds && mLines[row] != null &&
                    mLines[row] instanceof FullUnicodeLine) {
                FullUnicodeLine line = (FullUnicodeLine) mLines[row];
                // If either the start or the end column is in the middle of an East Asian wide character,
                // include the appropriate column of style information.
                if(x1 > 0 && line.findStartOfColumn(x1-1) == line.findStartOfColumn(x1)) {
                    --x1;
                }
                if(x2 < columns - 1 && line.findStartOfColumn(x2+1) == line.findStartOfColumn(x2)) {
                    ++x2;
                }
            }
            if(x1 == 0 && x2 == columns) {
                return color;
            }
            color.copy(x1, tmp, 0, x2-x1);
            return tmp;
        } else {
            return null;
        }
    }

    boolean isBasicLine (int row) {
        if(row < -mActiveTranscriptRows || row > mScreenRows-1)  throw new IllegalArgumentException();

        return (mLines[externalToInternalRow(row)] instanceof char[]);
    }

    public boolean getChar (int row, int column) {
        return getChar(row, column, 0);
    }

    public boolean getChar (int row, int column, int charIndex) {
        return getChar(row, column, charIndex, new char[1], 0);
    }

    /** Get a character at a specific position in the transcript.
     *
     * @param row        The row of the character to get.
     * @param column     The column of the character to get.
     * @param charIndex  The index of the character in the column to get (0 for the first character, 1 for the
     *                   next, etc.)
     * @param out        The char[] array into which the character will be placed.
     * @param offset     The offset in the array at which the character will be placed.

     * @return  Whether or not there are characters following this one in the column.
     */
    public boolean getChar (int row, int column, int charIndex, char[] out, int offset) {
        if(row < -mActiveTranscriptRows || row > mScreenRows-1) {
            throw new IllegalArgumentException();
        }
        row = externalToInternalRow(row);

        if(mLines[row] instanceof char[]) {
            // Fast path: all regular-width BMP chars in the row.
            char[] line = (char[]) mLines[row];
            out[offset] = line[column];
            return false;
        }

        FullUnicodeLine line = (FullUnicodeLine) mLines[row];
        return line.getChar(column, charIndex, out, offset);
    }

    private boolean isBasicChar (int codePoint) {
        return ! (charWidth(codePoint) != 1 || Character.charCount(codePoint) != 1);
    }

    private char[] allocateBasicLine (int row, int columns) {
        char[] line = new char[columns];

        // Fill the line with blanks.
        for(int i = 0; i < columns; ++i) {
            line[i] = ' ';
        }

        mLines[row] = line;
        if(mColor[row] == null) {
            mColor[row] = new StyleRow(0, columns);
        }
        return line;
    }

    private FullUnicodeLine allocateFullLine (int row, int columns) {
        FullUnicodeLine line = new FullUnicodeLine(columns);

        mLines[row] = line;
        if(mColor[row] == null) {
            mColor[row] = new StyleRow(0, columns);
        }
        return line;
    }

    public boolean setChar (int column, int row, int codePoint, int style) {
        if(!setChar(column, row, codePoint)) {
            return false;
        }

        row = externalToInternalRow(row);
        mColor[row].set(column, style);

        return true;
    }

    public boolean setChar (int column, int row, int codePoint) {
        if(row >= mScreenRows || column >= mColumns) {
            Log.e(TAG, "illegal arguments! " + row + " " + column + " " + mScreenRows + " " + mColumns);
            throw new IllegalArgumentException();
        }
        row = externalToInternalRow(row);

        // Whether data contains non-BMP or characters with charWidth != 1.
        // 0 - false; 1 - true; -1 - undetermined
        int basicMode = -1;

        // Allocate a row on demand.
        if(mLines[row] == null) {
            if(isBasicChar(codePoint)) {
                allocateBasicLine(row, mColumns);
                basicMode = 1;
            } else {
                allocateFullLine(row, mColumns);
                basicMode = 0;
            }
        }

        if(mLines[row] instanceof char[]) {
            char[] line = (char[]) mLines[row];

            if(basicMode == -1) {
                if(isBasicChar(codePoint)) {
                    basicMode = 1;
                } else {
                    basicMode = 0;
                }
            }

            if(basicMode == 1) {
                // Fast path -- just put the char in the array.
                line[column] = (char) codePoint;
                return true;
            }

            // Need to switch to the full-featured mode.
            mLines[row] = new FullUnicodeLine(line);
        }

        FullUnicodeLine line = (FullUnicodeLine) mLines[row];
        line.setChar(column, codePoint);
        return true;
    }
}

