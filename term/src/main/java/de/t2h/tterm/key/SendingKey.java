package de.t2h.tterm.key;

import android.view.KeyEvent;

public class SendingKey extends Key {

  public int mKeyCode;

  public SendingKey (String name, int keyCode) {
    super(name);
    mLabel = name;
    mKeyCode = keyCode;
  }
}
