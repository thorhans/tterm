package de.t2h.tterm.key;

import android.view.KeyEvent;

/** A programmable key's model.
 */
public class PKey {

  // ************************************************************
  // Constants
  // ************************************************************

  public static enum Kind { send, write, special }

  public static final PKey
    Control = special("Control").label("Ctrl"),
    Fn1     = special("Fn1"    ).label("Fn"),

    Esc     = send("Esc",   KeyEvent.KEYCODE_ESCAPE),
    Tab     = send("Tab",   KeyEvent.KEYCODE_TAB   ),

    Left    = send("Left" , KeyEvent.KEYCODE_DPAD_LEFT ).label("◀").repeat(),
    Right   = send("Right", KeyEvent.KEYCODE_DPAD_RIGHT).label("▶").repeat(),
    Up      = send("Up"   , KeyEvent.KEYCODE_DPAD_UP   ).label("▲").repeat(),
    Down    = send("Down" , KeyEvent.KEYCODE_DPAD_DOWN ).label("▼").repeat(),

    Minus   = write("-"),
    Slash   = write("/");

  // ************************************************************
  // Attributes
  // ************************************************************

  Kind mKind;

  String mName;

  String mLabel;
  PKey label (String label) { mLabel = label; return this; }
  public String getLabel () { return mLabel; }

  /** True when key auto-repeats.
   *
   * <p>E.g. cursor keys.</p>
   */
  boolean mRepeat;
  PKey repeat () { mRepeat = true; return this; }

  /** Used for `Kind.send´. */
  public int mKeyCode;
  public int getKeyCode () {
    if(mKind != Kind.send) throw new RuntimeException("PKey tried to get unset keycode.");
    return mKeyCode;
  }

  /** Used for `Kind.write´. */
  public String mText;
  public String getText () {
    if(mKind != Kind.write) throw new RuntimeException("PKey tried to get unset Text.");
    return mText;
  }


  // ************************************************************
  // Methods
  // ************************************************************

  public PKey (String name) { mName = name; mLabel = name; }

  static PKey send (String name, int keyCode) { 
    PKey key = new PKey(name); key.mKind = Kind.send; key.mKeyCode = keyCode; 
    return key;
  }

  static PKey write (String name) {
    PKey key = new PKey(name); key.mKind = Kind.write; key.mText = name;       
    return key;
  }

  static PKey special (String name) { 
    PKey key = new PKey(name); key.mKind = Kind.special; 
    return key;
  }
}

