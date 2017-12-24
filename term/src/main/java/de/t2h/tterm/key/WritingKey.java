package de.t2h.tterm.key;

public class WritingKey extends Key {
  String mText;

  public WritingKey (String name) {
    super(name);
    mLabel = name;
    mText  = name;
  }
}
