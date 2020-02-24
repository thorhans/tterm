// © 2004-2018 Thorbjørn Hansen (http://t2h.de). Licensed as open source, see ‘/de/t2h/license.txt’.
// These files are distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
// FITNESS FOR A PARTICULAR PURPOSE. See the license for details.

package de.t2h.util.text;

/** Text utilities.
 */
public class Text {
    // ************************************************************
    // Methods
    // ************************************************************

    /** True iff string is null or empty. */
    public static boolean isUnset (String s) {
        if(s == null) return true;
        return s.isEmpty();
    }

    /** True if string is set, false iff string is null or empty. */
    public static boolean isSet (String s) {
        if(s == null) return false;
        return ! s.isEmpty();
    }
}
