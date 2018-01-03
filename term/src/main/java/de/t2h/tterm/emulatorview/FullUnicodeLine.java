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

/** A representation of a line that's capable of handling non-BMP characters, East Asian wide characters, and
 *  combining characters.
 *
 * <p>The text of the line is stored in an array of char[], allowing easy conversion to a String and/or reuse
 * by other string-handling functions. An array of short[] is used to keep track of the difference between a
 * column and the starting index corresponding to its contents in the char[] array (so if column 42 starts at
 * index 45 in the char[] array, the offset stored is 3). Column 0 always starts at index 0 in the char[]
 * array, so we use that element of the array to keep track of how much of the char[] array we're using at the
 * moment.</p>
 */
class FullUnicodeLine {
    // ************************************************************
    // Constants
    // ************************************************************

    private static final float SPARE_CAPACITY_FACTOR = 1.5f;

    // ************************************************************
    // Attributes
    // ************************************************************

    private char[] mText;
    public char[] getLine () {return mText; }

    private short[] mOffset;
    public int getSpaceUsed () { return mOffset[0]; }

    private int mColumns;

    // ************************************************************
    // Methods
    // ************************************************************

    public FullUnicodeLine (int columns) {
        commonConstructor(columns);
        char[] text = mText;
        // Fill in the line with blanks.
        for(int i = 0; i < columns; ++i) {
            text[i] = ' ';
        }
        // Store the space used.
        mOffset[0] = (short) columns;
    }

    public FullUnicodeLine (char[] basicLine) {
        commonConstructor(basicLine.length);
        System.arraycopy(basicLine, 0, mText, 0, mColumns);
        // Store the space used.
        mOffset[0] = (short) basicLine.length;
    }

    private void commonConstructor (int columns) {
        mColumns = columns;
        mOffset = new short[columns];
        mText = new char[(int)(SPARE_CAPACITY_FACTOR*columns)];
    }

    public int findStartOfColumn (int column) {
        if(column == 0) {
            return 0;
        } else {
            return column + mOffset[column];
        }
    }

    public boolean getChar (int column, int charIndex, char[] out, int offset) {
        int pos = findStartOfColumn(column);
        int length;
        if(column + 1 < mColumns) {
            length = findStartOfColumn(column + 1) - pos;
        } else {
            length = getSpaceUsed() - pos;
        }
        if(charIndex >= length) {
            throw new IllegalArgumentException();
        }
        out[offset] = mText[pos + charIndex];
        return (charIndex + 1 < length);
    }

    public void setChar (int column, int codePoint) {
        int columns = mColumns;
        if(column < 0 || column >= columns) {
            throw new IllegalArgumentException();
        }

        char[] text = mText;
        short[] offset = mOffset;
        int spaceUsed = offset[0];

        int pos = findStartOfColumn(column);

        int charWidth = UnicodeTranscript.charWidth(codePoint);
        int oldCharWidth = UnicodeTranscript.charWidth(text, pos);

        if(charWidth == 2 && column == columns - 1) {
            // A width 2 character doesn't fit in the last column.
            codePoint = ' ';
            charWidth = 1;
        }

        boolean wasExtraColForWideChar = false;
        if(oldCharWidth == 2 && column > 0) {
            // If the previous screen column starts at the same offset in the array as this one, this column
            // must be the second column used by an East Asian wide character.
            wasExtraColForWideChar = (findStartOfColumn(column - 1) == pos);
        }

        // Get the number of elements in the mText array this column uses now.
        int oldLen;
        if(wasExtraColForWideChar && column + 1 < columns) {
            oldLen = findStartOfColumn(column + 1) - pos;
        } else if(column + oldCharWidth < columns) {
            oldLen = findStartOfColumn(column+oldCharWidth) - pos;
        } else {
            oldLen = spaceUsed - pos;
        }

        // Find how much space this column will need.
        int newLen = Character.charCount(codePoint);
        if(charWidth == 0) {
            // Combining characters are added to the contents of the column instead of overwriting them, so
            // that they modify the existing contents.
            newLen += oldLen;
        }
        int shift = newLen - oldLen;

        // Shift the rest of the line right to make room if necessary.
        if(shift > 0) {
            if(spaceUsed + shift > text.length) {
                // We need to grow the array.
                char[] newText = new char[text.length + columns];
                System.arraycopy(text, 0, newText, 0, pos);
                System.arraycopy(text, pos + oldLen, newText, pos + newLen, spaceUsed - pos - oldLen);
                mText = text = newText;
            } else {
                System.arraycopy(text, pos + oldLen, text, pos + newLen, spaceUsed - pos - oldLen);
            }
        }

        // Store the character.
        if(charWidth > 0) {
            Character.toChars(codePoint, text, pos);
        } else {
            // Store a combining character at the end of the existing contents, so that it modifies them.
            Character.toChars(codePoint, text, pos + oldLen);
        }

        // Shift the rest of the line left to eliminate gaps if necessary.
        if(shift < 0) {
            System.arraycopy(text, pos + oldLen, text, pos + newLen, spaceUsed - pos - oldLen);
        }

        // Update space used.
        if(shift != 0) {
            spaceUsed += shift;
            offset[0] = (short) spaceUsed;
        }

        // Handle cases where we need to pad with spaces to preserve column alignment.
        //
        // Width 2 -> width 1: pad with a space before or after the new character, depending on which of the
        // two previously-occupied columns we wrote into.
        //
        // Inserting width 2 character into the second column of an existing width 2 character: pad with a
        // space before the new character
        //
        if(oldCharWidth == 2 && charWidth == 1 || wasExtraColForWideChar && charWidth == 2) {
            int nextPos = pos + newLen;
            char[] newText = text;
            if(spaceUsed + 1 > text.length) {
                // Array needs growing.
                newText = new char[text.length + columns];
                System.arraycopy(text, 0, newText, 0, wasExtraColForWideChar ? pos : nextPos);
            }

            if(wasExtraColForWideChar) {
                // Padding goes before the new character.
                System.arraycopy(text, pos, newText, pos + 1, spaceUsed - pos);
                newText[pos] = ' ';
            } else {
                // Padding goes after the new character.
                System.arraycopy(text, nextPos, newText, nextPos + 1, spaceUsed - nextPos);
                newText[nextPos] = ' ';
            }

            if(newText != text) {
                // Update mText to point to the newly grown array.
                mText = text = newText;
            }

            // Update space used.
            spaceUsed = ++offset[0];

            // Correct the offset for the just-modified column to reflect width change.
            if(wasExtraColForWideChar) {
                ++offset[column];
                ++pos;
            } else {
                if(column == 0) {
                    offset[1] = (short) (newLen - 1);
                } else if(column + 1 < columns) {
                    offset[column + 1] = (short) (offset[column] + newLen - 1);
                }
                ++column;
            }

            ++shift;
        }
        
        // Handle cases where we need to clobber the contents of the next column in order to preserve column
        // alignment.
        //
        // Width 1 -> width 2: should clobber the contents of the next column (if next column contains wide
        // char, need to pad with a space).
        //
        // Inserting width 2 character into the second column of an existing width 2 character: same.
        //
        if(oldCharWidth == 1 && charWidth == 2 || wasExtraColForWideChar && charWidth == 2) {
            if(column == columns - 2) {
                // Correct offset for the next column to reflect width change.
                offset[column + 1] = (short) (offset[column] - 1);

                // Truncate the line after this character.
                offset[0] = (short) (pos + newLen);
                shift = 0;
            } else {
                // Overwrite the contents of the next column.
                int nextPos = pos + newLen;
                int nextWidth = UnicodeTranscript.charWidth(text, nextPos);
                int nextLen;
                if(column + nextWidth + 1 < columns) {
                    nextLen = findStartOfColumn(column + nextWidth + 1) + shift - nextPos;
                } else {
                    nextLen = spaceUsed - nextPos;
                }

                if(nextWidth == 2) {
                    text[nextPos] = ' ';
                    // Shift the array to match.
                    if(nextLen > 1) {
                        System.arraycopy(text, nextPos + nextLen, text, nextPos + 1, spaceUsed - nextPos - nextLen);
                        shift -= nextLen - 1;
                        offset[0] -= nextLen - 1;
                    }
                } else {
                    // Shift the array leftwards.
                    System.arraycopy(text, nextPos + nextLen, text, nextPos, spaceUsed - nextPos - nextLen);
                    shift -= nextLen;

                    // Truncate the line
                    offset[0] -= nextLen;
                }

                // Correct the offset for the next column to reflect width change.
                if(column == 0) {
                    offset[1] = -1;
                } else {
                    offset[column + 1] = (short) (offset[column] - 1);
                }
                ++column;
            }
        }

        // Update offset table.
        if(shift != 0) {
            for(int i = column + 1; i < columns; ++i) {
                offset[i] += shift;
            }
        }
    }
}

