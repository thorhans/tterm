package de.t2h.tterm.key;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.KeyEvent.*;

/** A programmable key's model.
 */
public class PKey {

  // ************************************************************
  // Constants
  // ************************************************************

  public enum Kind { send, write, special }

  // ************************************************************
  // Attributes
  // ************************************************************

  private static HashMap<String, PKey> sKeysByName = new HashMap<>();
  private static PKey[] sKeysByCode = new PKey[285];

  private Kind mKind;
  public Kind getKind () { return mKind; }

  private ArrayList<String> mNames = new ArrayList<>();
  public String getName () { return mNames.get(0); }

  private String mLabel;
  private PKey label (String label) { mLabel = label; return this; }
  public String getLabel () { return mLabel; }

  private String mNote;
  private PKey note (String note) { mNote = note; return this; }

  /** True when key auto-repeats.
   *
   * <p>E.g. cursor keys.</p>
   */
  private boolean mRepeat;
  private PKey repeat () { mRepeat = true; return this; }
  public boolean getRepeat () { return mRepeat; }

  /** Used for `Kind.send´. */
  private int mKeyCode;
  public int getKeyCode () {
    if(mKind != Kind.send) throw new RuntimeException("PKey tried to get unset keycode.");
    return mKeyCode;
  }

  /** Used for `Kind.write´. */
  private String mText;
  public String getText () {
    if(mKind != Kind.write) throw new RuntimeException("PKey tried to get unset Text.");
    return mText;
  }

  // ************************************************************
  // Methods
  // ************************************************************

  // TODO Why is this called twice on startup?
  //
  public static PKey[] parse (String keyString) {
    try {
      JSONArray array = (JSONArray) new JSONTokener(keyString).nextValue();
      int len = array.length();
      PKey[] keys = new PKey[len];
      for(int i = 0; i < len; ++i) {
        Object obj = array.get(i);
        if(obj instanceof String) {
          keys[i] = sKeysByName.get(obj);
        }
        if(keys[i] == null) keys[i] = sKeysByName.get("X"); // @@@ TODO Continue here.
      }
      return keys;
    } catch (JSONException e) {
      return new PKey[]{ Control }; // TODO Error handling.
    }
  }

  public PKey (String name) { mNames.add(name); mLabel = name; }

  private static void section (String name) {
    // TODO
  }

  // TODO Remove this method.
  //
  private static PKey send (String name, int keyCode) {
    PKey key = new PKey(name); key.mKind = Kind.send; key.mKeyCode = keyCode;
    sKeysByName.put(name, key);
    return key;
  }

  private static PKey send (int keyCode, String name, String description) {
    PKey key = new PKey(name); key.mKind = Kind.send; key.mKeyCode = keyCode;
    sKeysByCode[keyCode] = key;
    sKeysByName.put(name, key);
    return key;
  }

  // ------------------------------------------------------------

  private static PKey write (String name) {
    PKey key = new PKey(name); key.mKind = Kind.write; key.mText = name;
    sKeysByName.put(name, key);
    return key;
  }

  private static PKey special (String name) {
    PKey key = new PKey(name); key.mKind = Kind.special;
    sKeysByName.put(name, key);
    return key;
  }

  private PKey alias (String alias) {
    mNames.add(alias);
    sKeysByName.put(alias, this);
    return this;
  }

  // ************************************************************
  // Key constants
  // ************************************************************

  // TODO Merge with table below.
  //
  public static final PKey
    Control = special("Control").label("Ctrl"),
    Fn1     = special("Fn1"    ).label("Fn"),

    Esc     = send("Esc",   KEYCODE_ESCAPE),
    Tab     = send("Tab",   KEYCODE_TAB   ),

    Left    = send("Left" , KEYCODE_DPAD_LEFT ).label("◀").repeat(),
    Right   = send("Right", KEYCODE_DPAD_RIGHT).label("▶").repeat(),
    Up      = send("Up"   , KEYCODE_DPAD_UP   ).label("▲").repeat(),
    Down    = send("Down" , KEYCODE_DPAD_DOWN ).label("▼").repeat(),

    Minus   = write("-"),
    Slash   = write("/");

  private static final String
    note001 = "Usually situated below the display on phones and used as a multi-function feature key for "
            + "selecting a software defined function shown on the bottom left /right of the display.",
    note003 = "Handled by the framework, never delivered to applications.",
    note019 = "May also be synthesized from trackball motions.",
    note063 = "Enter alternate symbols.",
    note064 = "Launch a browser application.",
    note065 = "Launch a mail application.",
    note067 = "Deletes characters before the insertion point, unlike `FORWARD_DEL´.",
    note078 = "Used to enter numeric symbols. This key is not Num Lock, it is more like `ALT_LEFT´ and is "
            + "interpreted as an `ALT´ key by ´android.text.method.MetaKeyKeyListener´.",
    note091 = "Mutes the microphone, unlike `VOLUME_MUTE´.",
    note094 = "Switch symbol sets (Emoji, Kao-moji).",
    note095 = "Switch character sets (Kanji, Katakana).",
    note112 = "Deletes characters ahead of the insertion point, unlike `DEL´.",
    note122 = "Scrolling / moving the cursor to the start of a line / to the top of a list.",
    note123 = "Scrolling / moving the cursor to the end of a line / to the bottom of a list.",
    note124 = "Toggles insert / overwrite edit mode.",
    note125 = "Navigates forward in the history stack, complement of `BACK´.",
    note143 = "This is the Num Lock key, it is different from `NUM´. This key alters the behavior of other "
            + "keys on the numeric keypad.",
    note158 = "For decimals or digit grouping.",
    note164 = "Mutes the speaker, unlike `MUTE´. This key should normally be implemented as a toggle such "
            + "that the first press mutes the speaker and the second press restores the original volume.",
    note171 = "On TV remotes, toggles picture-in-picture mode or other windowing functions. On Android Wear "
            + "devices, triggers a display offset.",
    note204 = "Toggles the current input language such as switching between English and Japanese on a QWERTY "
            + "keyboard. On some devices, the same function may be performed by pressing Shift+Spacebar.",
    note205 = "Toggles silent or vibrate mode on and off. On some devices, the key may only operate when "
            + "long-pressed.",
    note219 = "Launche the global assist activity, not delivered to applications.",
    note223 = "Puts the device to sleep. Behaves somewhat like `POWER´ but it has no effect if the device is "
            + "already asleep.",
    note224 = "Wakes up the device. Behaves somewhat like `POWER´ but it has no effect if the device is "
            + "already awake.",
    note225 = "Initiates peripheral pairing mode.",
    note227 = "The HDMI-CEC standard specifies keys `11´ and `12´, but not `10´.",
    note234 = "Initiates to enter multi-digit channel nubmber when each digit key is assigned for selecting "
            + "separate channel. Corresponds to Number Entry Mode (0x1D) of CEC User Control Code.",
    note256 = "Goes to the title list. Corresponds to Contents Menu (0x0B) of CEC User Control Code.",
    note257 = "Goes to the context menu of media contents. Corresponds to Media Context-sensitive Menu "
            + "(0x11) of CEC User Control Code.",
    note258 = "Goes to the timer recording menu. Corresponds to Timer Programming (0x54) of CEC User Control "
            + "Code.",
    note264 = "Main power/reset button on watch.";

  static {

    section("General I");
    // ------------------------------------------------------------

    send(  0 , "Unknown"       , "Unknown key code"          );
    send(  1 , "Soft_Left"     , "Soft Left"                 ).note(note001);
    send(  2 , "Soft_Right"    , "Soft Right"                ).note(note001);
    send(  3 , "Home"          , "Home"                      ).note(note003);
    send(  4 , "Back"          , "Back"                      );
    send(  5 , "Call"          , "Call"                      );
    send(  6 , "Endcall"       , "End Call"                  );
    send(  7 , "0"             , "'0'"                       );
    send(  8 , "1"             , "'1'"                       );
    send(  9 , "2"             , "'2'"                       );
    send( 10 , "3"             , "'3'"                       );
    send( 11 , "4"             , "'4'"                       );
    send( 12 , "5"             , "'5'"                       );
    send( 13 , "6"             , "'6'"                       );
    send( 14 , "7"             , "'7'"                       );
    send( 15 , "8"             , "'8'"                       );
    send( 16 , "9"             , "'9'"                       );
    send( 17 , "Star"          , "'*'"                       );
    send( 18 , "Pound"         , "'#'"                       );
    send( 19 , "Dpad_Up"       , "Directional Pad Up"        ).note(note019);
    send( 20 , "Dpad_Down"     , "Directional Pad Down"      ).note(note019);
    send( 21 , "Dpad_Left"     , "Directional Pad Left"      ).note(note019);
    send( 22 , "Dpad_Right"    , "Directional Pad Right"     ).note(note019);
    send( 23 , "Dpad_Center"   , "Directional Pad Center"    ).note(note019);
    send( 24 , "Volume_Up"     , "Volume Up"                 );
    send( 25 , "Volume_Down"   , "Volume Down"               );
    send( 26 , "Power"         , "Power"                     );
    send( 27 , "Camera"        , "Camera"                    );
    send( 28 , "Clear"         , "Clear"                     );
    send( 29 , "A"             , "'A'"                       );
    send( 30 , "B"             , "'B'"                       );
    send( 31 , "C"             , "'C'"                       );
    send( 32 , "D"             , "'D'"                       );
    send( 33 , "E"             , "'E'"                       );
    send( 34 , "F"             , "'F'"                       );
    send( 35 , "G"             , "'G'"                       );
    send( 36 , "H"             , "'H'"                       );
    send( 37 , "I"             , "'I'"                       );
    send( 38 , "J"             , "'J'"                       );
    send( 39 , "K"             , "'K'"                       );
    send( 40 , "L"             , "'L'"                       );
    send( 41 , "M"             , "'M'"                       );
    send( 42 , "N"             , "'N'"                       );
    send( 43 , "O"             , "'O'"                       );
    send( 44 , "P"             , "'P'"                       );
    send( 45 , "Q"             , "'Q'"                       );
    send( 46 , "R"             , "'R'"                       );
    send( 47 , "S"             , "'S'"                       );
    send( 48 , "T"             , "'T'"                       );
    send( 49 , "U"             , "'U'"                       );
    send( 50 , "V"             , "'V'"                       );
    send( 51 , "W"             , "'W'"                       );
    send( 52 , "X"             , "'X'"                       );
    send( 53 , "Y"             , "'Y'"                       );
    send( 54 , "Z"             , "'Z'"                       );
    send( 55 , "Comma"         , "','"                       );
    send( 56 , "Period"        , "'.'"                       );
    send( 57 , "Alt_Left"      , "Left Alt modifier"         );
    send( 58 , "Alt_Right"     , "Right Alt modifier"        );
    send( 59 , "Shift_Left"    , "Left Shift modifier"       );
    send( 60 , "Shift_Right"   , "Right Shift modifier"      );
    send( 61 , "Tab"           , "Tab"                       );
    send( 62 , "Space"         , "Space"                     );
    send( 63 , "Sym"           , "Symbol modifier"           ).note(note063);
    send( 64 , "Explorer"      , "Explorer special function" ).note(note064);
    send( 65 , "Envelope"      , "Envelope special function" ).note(note065);
    send( 66 , "Enter"         , "Enter"                     );
    send( 67 , "Del"           , "Backspace"                 ).note(note067);
    send( 68 , "Grave"         , "'`' (backtick"             );
    send( 69 , "Minus"         , "'-'"                       );
    send( 70 , "Equals"        , "'='"                       );
    send( 71 , "Left_Bracket"  , "'['"                       );
    send( 72 , "Right_Bracket" , "']'"                       );
    send( 73 , "Backslash"     , "'\'"                       );
    send( 74 , "Semicolon"     , "';'"                       );
    send( 75 , "Apostrophe"    , "''' (apostrophe)"          );
    send( 76 , "Slash"         , "'/'"                       );
    send( 77 , "At"            , "'@'"                       );
    send( 78 , "Num"           , "Number modifier"           ).note(note078);
    send( 79 , "Headsethook"   , "Headset Hook"              );
    send( 80 , "Focus"         , "Camera Focus"              );
    send( 81 , "Plus"          , "'+'"                       );
    send( 82 , "Menu"          , "Menu"                      );
    send( 83 , "Notification"  , "Notification"              );
    send( 84 , "Search"        , "Search"                    );

    section("Media I");
    // ------------------------------------------------------------

    send( 85 , "Media_Play_Pause"   , "Play/Pause media"         );
    send( 86 , "Media_Stop"         , "Stop media"               );
    send( 87 , "Media_Next"         , "Play Next media"          );
    send( 88 , "Media_Previous"     , "Play Previous media"      );
    send( 89 , "Media_Rewind"       , "Rewind media"             );
    send( 90 , "Media_Fast_Forward" , "Fast Forward media"       );
    send( 91 , "Mute"               , "Mute"                     ).note(note091);
    send( 92 , "Page_Up"            , "Page Up"                  );
    send( 93 , "Page_Down"          , "Page Down"                );
    send( 94 , "Pictsymbols"        , "Picture Symbols modifier" ).note(note094);
    send( 95 , "Switch_Charset"     , "Switch Charset modifier"  ).note(note095);

    section("Game pad I");
    // ------------------------------------------------------------

    send( 96 , "Button_A"      , "A Button"           );
    send( 97 , "Button_B"      , "B Button"           );
    send( 98 , "Button_C"      , "C Button"           );
    send( 99 , "Button_X"      , "X Button"           );
    send(100 , "Button_Y"      , "Y Button"           );
    send(101 , "Button_Z"      , "Z Button"           );
    send(102 , "Button_L1"     , "L1 Button"          );
    send(103 , "Button_R1"     , "R1 Button"          );
    send(104 , "Button_L2"     , "L2 Button"          );
    send(105 , "Button_R2"     , "R2 Button"          );
    send(106 , "Button_ThumbL" , "Left Thumb Button"  );
    send(107 , "Button_ThumbR" , "Right Thumb Button" );
    send(108 , "Button_Start"  , "Start Button"       );
    send(109 , "Button_Select" , "Select Button"      );
    send(110 , "Button_Mode"   , "Mode Button"        );

    section("General II");
    // ------------------------------------------------------------

    send(111 , "Escape"      , "Escape"                        );
    send(112 , "Forward_Del" , "Forward Delete"                ).note(note112);
    send(113 , "Ctrl_Left"   , "Left Control modifier"         );
    send(114 , "Ctrl_Right"  , "Right Control modifier"        );
    send(115 , "Caps_Lock"   , "Caps Lock"                     );
    send(116 , "Scroll_Lock" , "Scroll Lock"                   );
    send(117 , "Meta_Left"   , "Left Meta modifier"            );
    send(118 , "Meta_Right"  , "Right Meta modifier"           );
    send(119 , "Function"    , "Function modifier"             );
    send(120 , "Sysrq"       , "System Request / Print Screen" );
    send(121 , "Break"       , "Break / Pause"                 );
    send(122 , "Move_Home"   , "Home Movement"                 ).note(note122);
    send(123 , "Move_End"    , "End Movement"                  ).note(note123);
    send(124 , "Insert"      , "Insert"                        ).note(note124);
    send(125 , "Forward"     , "Forward"                       ).note(note125);

    section("Media II");
    // ------------------------------------------------------------

    send(126 , "Media_Play"   , "Play media"   );
    send(127 , "Media_Pause"  , "Pause media"  );
    send(128 , "Media_Close"  , "Close media"  );
    send(129 , "Media_Eject"  , "Eject media"  );
    send(130 , "Media_Record" , "Record media" );

    section("Function keys");
    // ------------------------------------------------------------

    send(131 , "F1"  , "F1"  );
    send(132 , "F2"  , "F2"  );
    send(133 , "F3"  , "F3"  );
    send(134 , "F4"  , "F4"  );
    send(135 , "F5"  , "F5"  );
    send(136 , "F6"  , "F6"  );
    send(137 , "F7"  , "F7"  );
    send(138 , "F8"  , "F8"  );
    send(139 , "F9"  , "F9"  );
    send(140 , "F10" , "F10" );
    send(141 , "F11" , "F11" );
    send(142 , "F12" , "F12" );

    section("Number pad");
    // ------------------------------------------------------------

    send(143 , "Num_Lock"           , "Num Lock"     ).note(note143);
    send(144 , "Numpad_0"           , "numpad '0'"   );
    send(145 , "Numpad_1"           , "numpad '1'"   );
    send(146 , "Numpad_2"           , "numpad '2'"   );
    send(147 , "Numpad_3"           , "numpad '3'"   );
    send(148 , "Numpad_4"           , "numpad '4'"   );
    send(149 , "Numpad_5"           , "numpad '5'"   );
    send(150 , "Numpad_6"           , "numpad '6'"   );
    send(151 , "Numpad_7"           , "numpad '7'"   );
    send(152 , "Numpad_8"           , "numpad '8'"   );
    send(153 , "Numpad_9"           , "numpad '9'"   );
    send(154 , "Numpad_Divide"      , "numpad '/'"   );
    send(155 , "Numpad_Multiply"    , "numpad '*'"   );
    send(156 , "Numpad_Subtract"    , "numpad '-'"   );
    send(157 , "Numpad_Add"         , "numpad '+'"   );
    send(158 , "Numpad_Dot"         , "numpad '.'"   ).note(note158);
    send(159 , "Numpad_Comma"       , "numpad ','"   ).note(note158);
    send(160 , "Numpad_Enter"       , "numpad Enter" );
    send(161 , "Numpad_Equals"      , "numpad '='"   );
    send(162 , "Numpad_Left_Paren"  , "numpad '('"   );
    send(163 , "Numpad_Right_Paren" , "numpad '      )'");

    section("TV I");
    // ------------------------------------------------------------

    send(164 , "Volume_Mute"  , "Volume Mute"         ).note(note164);
    send(165 , "Info"         , "Info"                );
    send(166 , "Channel_Up"   , "Channel up"          );
    send(167 , "Channel_Down" , "Channel down"        );
    send(168 , "Zoom_In"      , "Zoom in"             );
    send(169 , "Zoom_Out"     , "Zoom out"            );
    send(170 , "TV"           , "TV"                  );
    send(171 , "Window"       , "Window"              ).note(note171);
    send(172 , "Guide"        , "Guide"               );
    send(173 , "DVR"          , "DVR"                 );
    send(174 , "Bookmark"     , "Bookmark"            );
    send(175 , "Captions"     , "Toggle captions"     );
    send(176 , "Settings"     , "Settings"            );
    send(177 , "TV_Power"     , "TV power"            );
    send(178 , "TV_Input"     , "TV input"            );
    send(179 , "STB_Power"    , "Set-top-box power"   );
    send(180 , "STB_Input"    , "Set-top-box input"   );
    send(181 , "AVR_Power"    , "A/V Receiver power"  );
    send(182 , "AVR_Input"    , "A/V Receiver input"  );
    send(183 , "Prog_Red"     , "Red programmable"    );
    send(184 , "Prog_Green"   , "Green programmable"  );
    send(185 , "Prog_Yellow"  , "Yellow programmable" );
    send(186 , "Prog_Blue"    , "Blue programmable"   );

    section("General III");
    // ------------------------------------------------------------

    send(187 , "App_Switch" , "App switch");

    section("Game pad II");
    // ------------------------------------------------------------

    send(188 , "Button_1"  , "Generic Game Pad Button #1"  );
    send(189 , "Button_2"  , "Generic Game Pad Button #2"  );
    send(190 , "Button_3"  , "Generic Game Pad Button #3"  );
    send(191 , "Button_4"  , "Generic Game Pad Button #4"  );
    send(192 , "Button_5"  , "Generic Game Pad Button #5"  );
    send(193 , "Button_6"  , "Generic Game Pad Button #6"  );
    send(194 , "Button_7"  , "Generic Game Pad Button #7"  );
    send(195 , "Button_8"  , "Generic Game Pad Button #8"  );
    send(196 , "Button_9"  , "Generic Game Pad Button #9"  );
    send(197 , "Button_10" , "Generic Game Pad Button #10" );
    send(198 , "Button_11" , "Generic Game Pad Button #11" );
    send(199 , "Button_12" , "Generic Game Pad Button #12" );
    send(200 , "Button_13" , "Generic Game Pad Button #13" );
    send(201 , "Button_14" , "Generic Game Pad Button #14" );
    send(202 , "Button_15" , "Generic Game Pad Button #15" );
    send(203 , "Button_16" , "Generic Game Pad Button #16" );

    section("General IV");
    // ------------------------------------------------------------

    send(204 , "Language_Switch"   , "Language Switch"                  ).note(note204);
    send(205 , "Manner_Mode"       , "Manner Mode"                      ).note(note205);
    send(206 , "3D_Mode"           , "3D Mode"                          );
    send(207 , "Contacts"          , "Contacts special function"        );
    send(208 , "Calendar"          , "Calendar special function"        );
    send(209 , "Music"             , "Music special function"           );
    send(210 , "Calculator"        , "Calculator special function"      );
    send(211 , "Zenkaku_Hankaku"   , "Japanese full-width / half-width" );
    send(212 , "Eisu"              , "Japanese alphanumeric"            );
    send(213 , "Muhenkan"          , "Japanese non-conversion"          );
    send(214 , "Henkan"            , "Japanese conversion"              );
    send(215 , "Katakana_Hiragana" , "Japanese katakana / hiragana"     );
    send(216 , "Yen"               , "Japanese Yen"                     );
    send(217 , "Ro"                , "Japanese Ro"                      );
    send(218 , "Kana"              , "Japanese kana"                    );
    send(219 , "Assist"            , "Assist"                           ).note(note219);
    send(220 , "Brightness_Down"   , "Brightness Down"                  );
    send(221 , "Brightness_Up"     , "Brightness Up"                    );
    send(222 , "Media_Audio_Track" , "Audio Track"                      );
    send(223 , "Sleep"             , "Sleep"                            ).note(note223);
    send(224 , "Wakeup"            , "Wakeup"                           ).note(note224);
    send(225 , "Pairing"           , "Pairing"                          ).note(note225);

    section("TV II");
    // ------------------------------------------------------------

    send(226 , "Media_Top_Menu"                , "Media Top Menu"                       );
    send(227 , "11"                            , "'11'"                                 ).note(note227);
    send(228 , "12"                            , "'12'"                                 ).note(note227);
    send(229 , "Last_Channel"                  , "Last Channel"                         );
    send(230 , "TV_Data_Service"               , "TV data service"                      );
    send(231 , "Voice_Assist"                  , "Voice Assist"                         );
    send(232 , "TV_Radio_Service"              , "Radio"                                );
    send(233 , "TV_Teletext"                   , "Teletext"                             );
    send(234 , "TV_Number_Entry"               , "Number entry"                         ).note(note234);
    send(235 , "TV_Terrestrial_Analog"         , "Analog Terrestrial"                   );
    send(236 , "TV_Terrestrial_Digital"        , "Digital Terrestrial"                  );
    send(237 , "TV_Satellite"                  , "Satellite"                            );
    send(238 , "TV_Satellite_BS"               , "BS"                                   );
    send(239 , "TV_Satellite_CS"               , "CS"                                   );
    send(240 , "TV_Satellite_Service"          , "BS/CS"                                );
    send(241 , "TV_Network"                    , "Toggle Network"                       );
    send(242 , "TV_Antenna_Cable"              , "Antenna/Cable"                        );
    send(243 , "TV_Input_HDMI_1"               , "HDMI #1"                              );
    send(244 , "TV_Input_HDMI_2"               , "HDMI #2"                              );
    send(245 , "TV_Input_HDMI_3"               , "HDMI #3"                              );
    send(246 , "TV_Input_HDMI_4"               , "HDMI #4"                              );
    send(247 , "TV_Input_Composite_1"          , "Composite #1"                         );
    send(248 , "TV_Input_Composite_2"          , "Composite #2"                         );
    send(249 , "TV_Input_Component_1"          , "Component #1"                         );
    send(250 , "TV_Input_Component_2"          , "Component #2"                         );
    send(251 , "TV_Input_VGA_1"                , "VGA #1"                               );
    send(252 , "TV_Audio_Description"          , "Audio description"                    );
    send(253 , "TV_Audio_Description_Mix_Up"   , "Audio description mixing volume up"   );
    send(254 , "TV_Audio_Description_Mix_Down" , "Audio description mixing volume down" );
    send(255 , "TV_Zoom_Mode"                  , "Zoom mode"                            );
    send(256 , "TV_Contents_Menu"              , "Contents menu"                        ).note(note256);
    send(257 , "TV_Media_Context_Menu"         , "Media context menu"                   ).note(note257);
    send(258 , "TV_Timer_Programming"          , "Timer programming"                    ).note(note258);

    section("General V");
    // ------------------------------------------------------------

    send(259 , "Help"                    , "Help"                                          );
    send(260 , "Navigate_Previous"       , "Navigate to previous"                          );
    send(261 , "Navigate_Next"           , "Navigate to next"                              );
    send(262 , "Navigate_In"             , "Navigate in"                                   );
    send(263 , "Navigate_Out"            , "Navigate out"                                  );
    send(264 , "Stem_Primary"            , "Primary stem for Wear"                         ).note(note264);
    send(265 , "Stem_1"                  , "Generic stem 1 for Wear"                       );
    send(266 , "Stem_2"                  , "Generic stem 2 for Wear"                       );
    send(267 , "Stem_3"                  , "Generic stem 3 for Wear"                       );
    send(268 , "Dpad_Up_Left"            , "Directional Pad Up-Left"                       );
    send(269 , "Dpad_Down_Left"          , "Directional Pad Down-Left"                     );
    send(270 , "Dpad_Up_Right"           , "Directional Pad Up-Right"                      );
    send(271 , "Dpad_Down_Right"         , "Directional Pad Down-Right"                    );
    send(272 , "Media_Skip_Forward"      , "Skip forward media"                            );
    send(273 , "Media_Skip_Backward"     , "Skip backward media"                           );
    send(274 , "Media_Step_Forward"      , "Step forward media"                            );
    send(275 , "Media_Step_Backward"     , "Step backward media"                           );
    send(276 , "Soft_Sleep"              , "put device to sleep unless a wakelock is held" );
    send(277 , "Cut"                     , "Cut"                                           );
    send(278 , "Copy"                    , "Copy"                                          );
    send(279 , "Paste"                   , "Paste"                                         );
    send(280 , "System_Navigation_Up"    , "Consumed by the system for navigation up"      );
    send(281 , "System_Navigation_Down"  , "Consumed by the system for navigation down"    );
    send(282 , "System_Navigation_Left"  , "Consumed by the system for navigation left"    );
    send(283 , "System_Navigation_Right" , "Consumed by the system for navigation right"   );
    send(284 , "All_Apps"                , "Show all apps"                                 );


    // section("General I");
    // ------------------------------------------------------------

    sKeysByCode[  0].alias("KEYCODE_UNKNOWN");
    sKeysByCode[  1].alias("KEYCODE_SOFT_LEFT");
    sKeysByCode[  2].alias("KEYCODE_SOFT_RIGHT");
    sKeysByCode[  3].alias("KEYCODE_HOME");
    sKeysByCode[  4].alias("KEYCODE_BACK");
    sKeysByCode[  5].alias("KEYCODE_CALL");
    sKeysByCode[  6].alias("KEYCODE_ENDCALL");
    sKeysByCode[  7].alias("KEYCODE_0");
    sKeysByCode[  8].alias("KEYCODE_1");
    sKeysByCode[  9].alias("KEYCODE_2");
    sKeysByCode[ 10].alias("KEYCODE_3");
    sKeysByCode[ 11].alias("KEYCODE_4");
    sKeysByCode[ 12].alias("KEYCODE_5");
    sKeysByCode[ 13].alias("KEYCODE_6");
    sKeysByCode[ 14].alias("KEYCODE_7");
    sKeysByCode[ 15].alias("KEYCODE_8");
    sKeysByCode[ 16].alias("KEYCODE_9");
    sKeysByCode[ 17].alias("KEYCODE_STAR");
    sKeysByCode[ 18].alias("KEYCODE_POUND");
    sKeysByCode[ 19].alias("KEYCODE_DPAD_UP");
    sKeysByCode[ 20].alias("KEYCODE_DPAD_DOWN");
    sKeysByCode[ 21].alias("KEYCODE_DPAD_LEFT");
    sKeysByCode[ 22].alias("KEYCODE_DPAD_RIGHT");
    sKeysByCode[ 23].alias("KEYCODE_DPAD_CENTER");
    sKeysByCode[ 24].alias("KEYCODE_VOLUME_UP");
    sKeysByCode[ 25].alias("KEYCODE_VOLUME_DOWN");
    sKeysByCode[ 26].alias("KEYCODE_POWER");
    sKeysByCode[ 27].alias("KEYCODE_CAMERA");
    sKeysByCode[ 28].alias("KEYCODE_CLEAR");
    sKeysByCode[ 29].alias("KEYCODE_A");
    sKeysByCode[ 30].alias("KEYCODE_B");
    sKeysByCode[ 31].alias("KEYCODE_C");
    sKeysByCode[ 32].alias("KEYCODE_D");
    sKeysByCode[ 33].alias("KEYCODE_E");
    sKeysByCode[ 34].alias("KEYCODE_F");
    sKeysByCode[ 35].alias("KEYCODE_G");
    sKeysByCode[ 36].alias("KEYCODE_H");
    sKeysByCode[ 37].alias("KEYCODE_I");
    sKeysByCode[ 38].alias("KEYCODE_J");
    sKeysByCode[ 39].alias("KEYCODE_K");
    sKeysByCode[ 40].alias("KEYCODE_L");
    sKeysByCode[ 41].alias("KEYCODE_M");
    sKeysByCode[ 42].alias("KEYCODE_N");
    sKeysByCode[ 43].alias("KEYCODE_O");
    sKeysByCode[ 44].alias("KEYCODE_P");
    sKeysByCode[ 45].alias("KEYCODE_Q");
    sKeysByCode[ 46].alias("KEYCODE_R");
    sKeysByCode[ 47].alias("KEYCODE_S");
    sKeysByCode[ 48].alias("KEYCODE_T");
    sKeysByCode[ 49].alias("KEYCODE_U");
    sKeysByCode[ 50].alias("KEYCODE_V");
    sKeysByCode[ 51].alias("KEYCODE_W");
    sKeysByCode[ 52].alias("KEYCODE_X");
    sKeysByCode[ 53].alias("KEYCODE_Y");
    sKeysByCode[ 54].alias("KEYCODE_Z");
    sKeysByCode[ 55].alias("KEYCODE_COMMA");
    sKeysByCode[ 56].alias("KEYCODE_PERIOD");
    sKeysByCode[ 57].alias("KEYCODE_ALT_LEFT");
    sKeysByCode[ 58].alias("KEYCODE_ALT_RIGHT");
    sKeysByCode[ 59].alias("KEYCODE_SHIFT_LEFT");
    sKeysByCode[ 60].alias("KEYCODE_SHIFT_RIGHT");
    sKeysByCode[ 61].alias("KEYCODE_TAB");
    sKeysByCode[ 62].alias("KEYCODE_SPACE");
    sKeysByCode[ 63].alias("KEYCODE_SYM");
    sKeysByCode[ 64].alias("KEYCODE_EXPLORER");
    sKeysByCode[ 65].alias("KEYCODE_ENVELOPE");
    sKeysByCode[ 66].alias("KEYCODE_ENTER");
    sKeysByCode[ 67].alias("KEYCODE_DEL");
    sKeysByCode[ 68].alias("KEYCODE_GRAVE");
    sKeysByCode[ 69].alias("KEYCODE_MINUS");
    sKeysByCode[ 70].alias("KEYCODE_EQUALS");
    sKeysByCode[ 71].alias("KEYCODE_LEFT_BRACKET");
    sKeysByCode[ 72].alias("KEYCODE_RIGHT_BRACKET");
    sKeysByCode[ 73].alias("KEYCODE_BACKSLASH");
    sKeysByCode[ 74].alias("KEYCODE_SEMICOLON");
    sKeysByCode[ 75].alias("KEYCODE_APOSTROPHE");
    sKeysByCode[ 76].alias("KEYCODE_SLASH");
    sKeysByCode[ 77].alias("KEYCODE_AT");
    sKeysByCode[ 78].alias("KEYCODE_NUM");
    sKeysByCode[ 79].alias("KEYCODE_HEADSETHOOK");
    sKeysByCode[ 80].alias("KEYCODE_FOCUS");
    sKeysByCode[ 81].alias("KEYCODE_PLUS");
    sKeysByCode[ 82].alias("KEYCODE_MENU");
    sKeysByCode[ 83].alias("KEYCODE_NOTIFICATION");
    sKeysByCode[ 84].alias("KEYCODE_SEARCH");

    // section("Media I");
    // ------------------------------------------------------------

    sKeysByCode[ 85].alias("KEYCODE_MEDIA_PLAY_PAUSE");
    sKeysByCode[ 86].alias("KEYCODE_MEDIA_STOP");
    sKeysByCode[ 87].alias("KEYCODE_MEDIA_NEXT");
    sKeysByCode[ 88].alias("KEYCODE_MEDIA_PREVIOUS");
    sKeysByCode[ 89].alias("KEYCODE_MEDIA_REWIND");
    sKeysByCode[ 90].alias("KEYCODE_MEDIA_FAST_FORWARD");
    sKeysByCode[ 91].alias("KEYCODE_MUTE");
    sKeysByCode[ 92].alias("KEYCODE_PAGE_UP");
    sKeysByCode[ 93].alias("KEYCODE_PAGE_DOWN");
    sKeysByCode[ 94].alias("KEYCODE_PICTSYMBOLS");
    sKeysByCode[ 95].alias("KEYCODE_SWITCH_CHARSET");

    // section("Game pad I");
    // ------------------------------------------------------------

    sKeysByCode[ 96].alias("KEYCODE_BUTTON_A");
    sKeysByCode[ 97].alias("KEYCODE_BUTTON_B");
    sKeysByCode[ 98].alias("KEYCODE_BUTTON_C");
    sKeysByCode[ 99].alias("KEYCODE_BUTTON_X");
    sKeysByCode[100].alias("KEYCODE_BUTTON_Y");
    sKeysByCode[101].alias("KEYCODE_BUTTON_Z");
    sKeysByCode[102].alias("KEYCODE_BUTTON_L1");
    sKeysByCode[103].alias("KEYCODE_BUTTON_R1");
    sKeysByCode[104].alias("KEYCODE_BUTTON_L2");
    sKeysByCode[105].alias("KEYCODE_BUTTON_R2");
    sKeysByCode[106].alias("KEYCODE_BUTTON_THUMBL");
    sKeysByCode[107].alias("KEYCODE_BUTTON_THUMBR");
    sKeysByCode[108].alias("KEYCODE_BUTTON_START");
    sKeysByCode[109].alias("KEYCODE_BUTTON_SELECT");
    sKeysByCode[110].alias("KEYCODE_BUTTON_MODE");

    // section("General II");
    // ------------------------------------------------------------

    sKeysByCode[111].alias("KEYCODE_ESCAPE");
    sKeysByCode[112].alias("KEYCODE_FORWARD_DEL");
    sKeysByCode[113].alias("KEYCODE_CTRL_LEFT");
    sKeysByCode[114].alias("KEYCODE_CTRL_RIGHT");
    sKeysByCode[115].alias("KEYCODE_CAPS_LOCK");
    sKeysByCode[116].alias("KEYCODE_SCROLL_LOCK");
    sKeysByCode[117].alias("KEYCODE_META_LEFT");
    sKeysByCode[118].alias("KEYCODE_META_RIGHT");
    sKeysByCode[119].alias("KEYCODE_FUNCTION");
    sKeysByCode[120].alias("KEYCODE_SYSRQ");
    sKeysByCode[121].alias("KEYCODE_BREAK");
    sKeysByCode[122].alias("KEYCODE_MOVE_HOME");
    sKeysByCode[123].alias("KEYCODE_MOVE_END");
    sKeysByCode[124].alias("KEYCODE_INSERT");
    sKeysByCode[125].alias("KEYCODE_FORWARD");

    // section("Media II");
    // ------------------------------------------------------------

    sKeysByCode[126].alias("KEYCODE_MEDIA_PLAY");
    sKeysByCode[127].alias("KEYCODE_MEDIA_PAUSE");
    sKeysByCode[128].alias("KEYCODE_MEDIA_CLOSE");
    sKeysByCode[129].alias("KEYCODE_MEDIA_EJECT");
    sKeysByCode[130].alias("KEYCODE_MEDIA_RECORD");

    // section("Function keys");
    // ------------------------------------------------------------

    sKeysByCode[131].alias("KEYCODE_F1");
    sKeysByCode[132].alias("KEYCODE_F2");
    sKeysByCode[133].alias("KEYCODE_F3");
    sKeysByCode[134].alias("KEYCODE_F4");
    sKeysByCode[135].alias("KEYCODE_F5");
    sKeysByCode[136].alias("KEYCODE_F6");
    sKeysByCode[137].alias("KEYCODE_F7");
    sKeysByCode[138].alias("KEYCODE_F8");
    sKeysByCode[139].alias("KEYCODE_F9");
    sKeysByCode[140].alias("KEYCODE_F10");
    sKeysByCode[141].alias("KEYCODE_F11");
    sKeysByCode[142].alias("KEYCODE_F12");

    // section("Number pad");
    // ------------------------------------------------------------

    sKeysByCode[143].alias("KEYCODE_NUM_LOCK");
    sKeysByCode[144].alias("KEYCODE_NUMPAD_0");
    sKeysByCode[145].alias("KEYCODE_NUMPAD_1");
    sKeysByCode[146].alias("KEYCODE_NUMPAD_2");
    sKeysByCode[147].alias("KEYCODE_NUMPAD_3");
    sKeysByCode[148].alias("KEYCODE_NUMPAD_4");
    sKeysByCode[149].alias("KEYCODE_NUMPAD_5");
    sKeysByCode[150].alias("KEYCODE_NUMPAD_6");
    sKeysByCode[151].alias("KEYCODE_NUMPAD_7");
    sKeysByCode[152].alias("KEYCODE_NUMPAD_8");
    sKeysByCode[153].alias("KEYCODE_NUMPAD_9");
    sKeysByCode[154].alias("KEYCODE_NUMPAD_DIVIDE");
    sKeysByCode[155].alias("KEYCODE_NUMPAD_MULTIPLY");
    sKeysByCode[156].alias("KEYCODE_NUMPAD_SUBTRACT");
    sKeysByCode[157].alias("KEYCODE_NUMPAD_ADD");
    sKeysByCode[158].alias("KEYCODE_NUMPAD_DOT");
    sKeysByCode[159].alias("KEYCODE_NUMPAD_COMMA");
    sKeysByCode[160].alias("KEYCODE_NUMPAD_ENTER");
    sKeysByCode[161].alias("KEYCODE_NUMPAD_EQUALS");
    sKeysByCode[162].alias("KEYCODE_NUMPAD_LEFT_PAREN");
    sKeysByCode[163].alias("KEYCODE_NUMPAD_RIGHT_PAREN");

    // section("TV I");
    // ------------------------------------------------------------

    sKeysByCode[164].alias("KEYCODE_VOLUME_MUTE");
    sKeysByCode[165].alias("KEYCODE_INFO");
    sKeysByCode[166].alias("KEYCODE_CHANNEL_UP");
    sKeysByCode[167].alias("KEYCODE_CHANNEL_DOWN");
    sKeysByCode[168].alias("KEYCODE_ZOOM_IN");
    sKeysByCode[169].alias("KEYCODE_ZOOM_OUT");
    sKeysByCode[170].alias("KEYCODE_TV");
    sKeysByCode[171].alias("KEYCODE_WINDOW");
    sKeysByCode[172].alias("KEYCODE_GUIDE");
    sKeysByCode[173].alias("KEYCODE_DVR");
    sKeysByCode[174].alias("KEYCODE_BOOKMARK");
    sKeysByCode[175].alias("KEYCODE_CAPTIONS");
    sKeysByCode[176].alias("KEYCODE_SETTINGS");
    sKeysByCode[177].alias("KEYCODE_TV_POWER");
    sKeysByCode[178].alias("KEYCODE_TV_INPUT");
    sKeysByCode[179].alias("KEYCODE_STB_POWER");
    sKeysByCode[180].alias("KEYCODE_STB_INPUT");
    sKeysByCode[181].alias("KEYCODE_AVR_POWER");
    sKeysByCode[182].alias("KEYCODE_AVR_INPUT");
    sKeysByCode[183].alias("KEYCODE_PROG_RED");
    sKeysByCode[184].alias("KEYCODE_PROG_GREEN");
    sKeysByCode[185].alias("KEYCODE_PROG_YELLOW");
    sKeysByCode[186].alias("KEYCODE_PROG_BLUE");

    // section("General III");
    // ------------------------------------------------------------

    sKeysByCode[187].alias("KEYCODE_APP_SWITCH");

    // section("Game pad II");
    // ------------------------------------------------------------

    sKeysByCode[188].alias("KEYCODE_BUTTON_1");
    sKeysByCode[189].alias("KEYCODE_BUTTON_2");
    sKeysByCode[190].alias("KEYCODE_BUTTON_3");
    sKeysByCode[191].alias("KEYCODE_BUTTON_4");
    sKeysByCode[192].alias("KEYCODE_BUTTON_5");
    sKeysByCode[193].alias("KEYCODE_BUTTON_6");
    sKeysByCode[194].alias("KEYCODE_BUTTON_7");
    sKeysByCode[195].alias("KEYCODE_BUTTON_8");
    sKeysByCode[196].alias("KEYCODE_BUTTON_9");
    sKeysByCode[197].alias("KEYCODE_BUTTON_10");
    sKeysByCode[198].alias("KEYCODE_BUTTON_11");
    sKeysByCode[199].alias("KEYCODE_BUTTON_12");
    sKeysByCode[200].alias("KEYCODE_BUTTON_13");
    sKeysByCode[201].alias("KEYCODE_BUTTON_14");
    sKeysByCode[202].alias("KEYCODE_BUTTON_15");
    sKeysByCode[203].alias("KEYCODE_BUTTON_16");

    // section("General IV");
    // ------------------------------------------------------------

    sKeysByCode[204].alias("KEYCODE_LANGUAGE_SWITCH");
    sKeysByCode[205].alias("KEYCODE_MANNER_MODE");
    sKeysByCode[206].alias("KEYCODE_3D_MODE");
    sKeysByCode[207].alias("KEYCODE_CONTACTS");
    sKeysByCode[208].alias("KEYCODE_CALENDAR");
    sKeysByCode[209].alias("KEYCODE_MUSIC");
    sKeysByCode[210].alias("KEYCODE_CALCULATOR");
    sKeysByCode[211].alias("KEYCODE_ZENKAKU_HANKAKU");
    sKeysByCode[212].alias("KEYCODE_EISU");
    sKeysByCode[213].alias("KEYCODE_MUHENKAN");
    sKeysByCode[214].alias("KEYCODE_HENKAN");
    sKeysByCode[215].alias("KEYCODE_KATAKANA_HIRAGANA");
    sKeysByCode[216].alias("KEYCODE_YEN");
    sKeysByCode[217].alias("KEYCODE_RO");
    sKeysByCode[218].alias("KEYCODE_KANA");
    sKeysByCode[219].alias("KEYCODE_ASSIST");
    sKeysByCode[220].alias("KEYCODE_BRIGHTNESS_DOWN");
    sKeysByCode[221].alias("KEYCODE_BRIGHTNESS_UP");
    sKeysByCode[222].alias("KEYCODE_MEDIA_AUDIO_TRACK");
    sKeysByCode[223].alias("KEYCODE_SLEEP");
    sKeysByCode[224].alias("KEYCODE_WAKEUP");
    sKeysByCode[225].alias("KEYCODE_PAIRING");

    // section("TV II");
    // ------------------------------------------------------------

    sKeysByCode[226].alias("KEYCODE_MEDIA_TOP_MENU");
    sKeysByCode[227].alias("KEYCODE_11");
    sKeysByCode[228].alias("KEYCODE_12");
    sKeysByCode[229].alias("KEYCODE_LAST_CHANNEL");
    sKeysByCode[230].alias("KEYCODE_TV_DATA_SERVICE");
    sKeysByCode[231].alias("KEYCODE_VOICE_ASSIST");
    sKeysByCode[232].alias("KEYCODE_TV_RADIO_SERVICE");
    sKeysByCode[233].alias("KEYCODE_TV_TELETEXT");
    sKeysByCode[234].alias("KEYCODE_TV_NUMBER_ENTRY");
    sKeysByCode[235].alias("KEYCODE_TV_TERRESTRIAL_ANALOG");
    sKeysByCode[236].alias("KEYCODE_TV_TERRESTRIAL_DIGITAL");
    sKeysByCode[237].alias("KEYCODE_TV_SATELLITE");
    sKeysByCode[238].alias("KEYCODE_TV_SATELLITE_BS");
    sKeysByCode[239].alias("KEYCODE_TV_SATELLITE_CS");
    sKeysByCode[240].alias("KEYCODE_TV_SATELLITE_SERVICE");
    sKeysByCode[241].alias("KEYCODE_TV_NETWORK");
    sKeysByCode[242].alias("KEYCODE_TV_ANTENNA_CABLE");
    sKeysByCode[243].alias("KEYCODE_TV_INPUT_HDMI_1");
    sKeysByCode[244].alias("KEYCODE_TV_INPUT_HDMI_2");
    sKeysByCode[245].alias("KEYCODE_TV_INPUT_HDMI_3");
    sKeysByCode[246].alias("KEYCODE_TV_INPUT_HDMI_4");
    sKeysByCode[247].alias("KEYCODE_TV_INPUT_COMPOSITE_1");
    sKeysByCode[248].alias("KEYCODE_TV_INPUT_COMPOSITE_2");
    sKeysByCode[249].alias("KEYCODE_TV_INPUT_COMPONENT_1");
    sKeysByCode[250].alias("KEYCODE_TV_INPUT_COMPONENT_2");
    sKeysByCode[251].alias("KEYCODE_TV_INPUT_VGA_1");
    sKeysByCode[252].alias("KEYCODE_TV_AUDIO_DESCRIPTION");
    sKeysByCode[253].alias("KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP");
    sKeysByCode[254].alias("KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN");
    sKeysByCode[255].alias("KEYCODE_TV_ZOOM_MODE");
    sKeysByCode[256].alias("KEYCODE_TV_CONTENTS_MENU");
    sKeysByCode[257].alias("KEYCODE_TV_MEDIA_CONTEXT_MENU");
    sKeysByCode[258].alias("KEYCODE_TV_TIMER_PROGRAMMING");

    // section("General V");
    // ------------------------------------------------------------

    sKeysByCode[259].alias("KEYCODE_HELP");

    sKeysByCode[260].alias("KEYCODE_NAVIGATE_PREVIOUS");
    sKeysByCode[261].alias("KEYCODE_NAVIGATE_NEXT");
    sKeysByCode[262].alias("KEYCODE_NAVIGATE_IN");
    sKeysByCode[263].alias("KEYCODE_NAVIGATE_OUT");
    sKeysByCode[264].alias("KEYCODE_STEM_PRIMARY");
    sKeysByCode[265].alias("KEYCODE_STEM_1");
    sKeysByCode[266].alias("KEYCODE_STEM_2");
    sKeysByCode[267].alias("KEYCODE_STEM_3");
    sKeysByCode[268].alias("KEYCODE_DPAD_UP_LEFT");
    sKeysByCode[269].alias("KEYCODE_DPAD_DOWN_LEFT");
    sKeysByCode[270].alias("KEYCODE_DPAD_UP_RIGHT");
    sKeysByCode[271].alias("KEYCODE_DPAD_DOWN_RIGHT");
    sKeysByCode[272].alias("KEYCODE_MEDIA_SKIP_FORWARD");
    sKeysByCode[273].alias("KEYCODE_MEDIA_SKIP_BACKWARD");
    sKeysByCode[274].alias("KEYCODE_MEDIA_STEP_FORWARD");
    sKeysByCode[275].alias("KEYCODE_MEDIA_STEP_BACKWARD");
    sKeysByCode[276].alias("KEYCODE_SOFT_SLEEP");
    sKeysByCode[277].alias("KEYCODE_CUT");
    sKeysByCode[278].alias("KEYCODE_COPY");
    sKeysByCode[279].alias("KEYCODE_PASTE");
    sKeysByCode[280].alias("KEYCODE_SYSTEM_NAVIGATION_UP");
    sKeysByCode[281].alias("KEYCODE_SYSTEM_NAVIGATION_DOWN");
    sKeysByCode[282].alias("KEYCODE_SYSTEM_NAVIGATION_LEFT");
    sKeysByCode[283].alias("KEYCODE_SYSTEM_NAVIGATION_RIGHT");
    sKeysByCode[284].alias("KEYCODE_ALL_APPS");
  }
}
