package de.t2h.tterm.key;

import android.view.KeyEvent;

/**
 */
public class Key {
  String mName;
  String mLabel;
  boolean mRepeat;

  public Key (String name) {
    mName = name;
  }
  Key repeat ()             { mRepeat = true; return this; }
  Key label  (String label) { mLabel = label; return this; }

  public static final Key
    Control = new Key("Control").label("Ctrl"),
    Fn1     = new Key("Fn1"    ).label("Fn");

  public static final Key
    Esc   = new SendingKey("Esc",   KeyEvent.KEYCODE_ESCAPE),
    Tab   = new SendingKey("Tab",   KeyEvent.KEYCODE_TAB   ),

    Left  = new SendingKey("Left" , KeyEvent.KEYCODE_DPAD_LEFT ).label("◀").repeat(),
    Right = new SendingKey("Right", KeyEvent.KEYCODE_DPAD_RIGHT).label("▶").repeat(),
    Up    = new SendingKey("Up"   , KeyEvent.KEYCODE_DPAD_UP   ).label("▲").repeat(),
    Down  = new SendingKey("Down" , KeyEvent.KEYCODE_DPAD_DOWN ).label("▼").repeat();

  public static final Key
    Minus = new WritingKey("-"),
    Slash = new WritingKey("/");
}

