package de.t2h.tterm.key;

import static android.view.KeyEvent.*;

/** A programmable key's model.
 */
public class PKey {

  // ************************************************************
  // Constants
  // ************************************************************

  public enum Kind { send, write, special }

  // TODO Merge with table below.
  //
  public static final PKey
    Control = special("Control").label("Ctrl"),
    Fn1     = special("Fn1"    ).label("Fn"),

    Esc     = send("Esc",   KEYCODE_ESCAPE),
    Tab     = send("Tab",   KEYCODE_TAB   ),

    Left    = send("Left" , KEYCODE_DPAD_LEFT ).label("?").repeat(),
    Right   = send("Right", KEYCODE_DPAD_RIGHT).label("?").repeat(),
    Up      = send("Up"   , KEYCODE_DPAD_UP   ).label("?").repeat(),
    Down    = send("Down" , KEYCODE_DPAD_DOWN ).label("?").repeat(),

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
            + "Code.";
    // note264 = "Main power/reset button on watch.";

  static {
    section("General I");
    // ------------------------------------------------------------

    send(  0 , "Unknown"       , KEYCODE_UNKNOWN       , "Unknown key code");
    send(  1 , "Soft_Left"     , KEYCODE_SOFT_LEFT     , "Soft Left", note001);
    send(  2 , "Soft_Right"    , KEYCODE_SOFT_RIGHT    , "Soft Right", note001);
    send(  3 , "Home"          , KEYCODE_HOME          , "Home", note003);
    send(  4 , "Back"          , KEYCODE_BACK          , "Back");
    send(  5 , "Call"          , KEYCODE_CALL          , "Call");
    send(  6 , "Endcall"       , KEYCODE_ENDCALL       , "End Call");
    send(  7 , "0"             , KEYCODE_0             , "'0'");
    send(  8 , "1"             , KEYCODE_1             , "'1'");
    send(  9 , "2"             , KEYCODE_2             , "'2'");
    send( 10 , "3"             , KEYCODE_3             , "'3'");
    send( 11 , "4"             , KEYCODE_4             , "'4'");
    send( 12 , "5"             , KEYCODE_5             , "'5'");
    send( 13 , "6"             , KEYCODE_6             , "'6'");
    send( 14 , "7"             , KEYCODE_7             , "'7'");
    send( 15 , "8"             , KEYCODE_8             , "'8'");
    send( 16 , "9"             , KEYCODE_9             , "'9'");
    send( 17 , "Star"          , KEYCODE_STAR          , "'*'");
    send( 18 , "Pound"         , KEYCODE_POUND         , "'#'");
    send( 19 , "Dpad_Up"       , KEYCODE_DPAD_UP       , "Directional Pad Up", note019);
    send( 20 , "Dpad_Down"     , KEYCODE_DPAD_DOWN     , "Directional Pad Down", note019);
    send( 21 , "Dpad_Left"     , KEYCODE_DPAD_LEFT     , "Directional Pad Left", note019);
    send( 22 , "Dpad_Right"    , KEYCODE_DPAD_RIGHT    , "Directional Pad Right", note019);
    send( 23 , "Dpad_Center"   , KEYCODE_DPAD_CENTER   , "Directional Pad Center", note019);
    send( 24 , "Volume_Up"     , KEYCODE_VOLUME_UP     , "Volume Up");
    send( 25 , "Volume_Down"   , KEYCODE_VOLUME_DOWN   , "Volume Down");
    send( 26 , "Power"         , KEYCODE_POWER         , "Power");
    send( 27 , "Camera"        , KEYCODE_CAMERA        , "Camera");
    send( 28 , "Clear"         , KEYCODE_CLEAR         , "Clear");
    send( 29 , "A"             , KEYCODE_A             , "'A'");
    send( 30 , "B"             , KEYCODE_B             , "'B'");
    send( 31 , "C"             , KEYCODE_C             , "'C'");
    send( 32 , "D"             , KEYCODE_D             , "'D'");
    send( 33 , "E"             , KEYCODE_E             , "'E'");
    send( 34 , "F"             , KEYCODE_F             , "'F'");
    send( 35 , "G"             , KEYCODE_G             , "'G'");
    send( 36 , "H"             , KEYCODE_H             , "'H'");
    send( 37 , "I"             , KEYCODE_I             , "'I'");
    send( 38 , "J"             , KEYCODE_J             , "'J'");
    send( 39 , "K"             , KEYCODE_K             , "'K'");
    send( 40 , "L"             , KEYCODE_L             , "'L'");
    send( 41 , "M"             , KEYCODE_M             , "'M'");
    send( 42 , "N"             , KEYCODE_N             , "'N'");
    send( 43 , "O"             , KEYCODE_O             , "'O'");
    send( 44 , "P"             , KEYCODE_P             , "'P'");
    send( 45 , "Q"             , KEYCODE_Q             , "'Q'");
    send( 46 , "R"             , KEYCODE_R             , "'R'");
    send( 47 , "S"             , KEYCODE_S             , "'S'");
    send( 48 , "T"             , KEYCODE_T             , "'T'");
    send( 49 , "U"             , KEYCODE_U             , "'U'");
    send( 50 , "V"             , KEYCODE_V             , "'V'");
    send( 51 , "W"             , KEYCODE_W             , "'W'");
    send( 52 , "X"             , KEYCODE_X             , "'X'");
    send( 53 , "Y"             , KEYCODE_Y             , "'Y'");
    send( 54 , "Z"             , KEYCODE_Z             , "'Z'");
    send( 55 , "Comma"         , KEYCODE_COMMA         , "','");
    send( 56 , "Period"        , KEYCODE_PERIOD        , "'.'");
    send( 57 , "Alt_Left"      , KEYCODE_ALT_LEFT      , "Left Alt modifier");
    send( 58 , "Alt_Right"     , KEYCODE_ALT_RIGHT     , "Right Alt modifier");
    send( 59 , "Shift_Left"    , KEYCODE_SHIFT_LEFT    , "Left Shift modifier");
    send( 60 , "Shift_Right"   , KEYCODE_SHIFT_RIGHT   , "Right Shift modifier");
    send( 61 , "Tab"           , KEYCODE_TAB           , "Tab");
    send( 62 , "Space"         , KEYCODE_SPACE         , "Space");
    send( 63 , "Sym"           , KEYCODE_SYM           , "Symbol modifier", note063);
    send( 64 , "Explorer"      , KEYCODE_EXPLORER      , "Explorer special function", note064);
    send( 65 , "Envelope"      , KEYCODE_ENVELOPE      , "Envelope special function", note065);
    send( 66 , "Enter"         , KEYCODE_ENTER         , "Enter");
    send( 67 , "Del"           , KEYCODE_DEL           , "Backspace", note067);
    send( 68 , "Grave"         , KEYCODE_GRAVE         , "'`' (backtick)");
    send( 69 , "Minus"         , KEYCODE_MINUS         , "'-'");
    send( 70 , "Equals"        , KEYCODE_EQUALS        , "'='");
    send( 71 , "Left_Bracket"  , KEYCODE_LEFT_BRACKET  , "'['");
    send( 72 , "Right_Bracket" , KEYCODE_RIGHT_BRACKET , "']'");
    send( 73 , "Backslash"     , KEYCODE_BACKSLASH     , "'\'");
    send( 74 , "Semicolon"     , KEYCODE_SEMICOLON     , "';'");
    send( 75 , "Apostrophe"    , KEYCODE_APOSTROPHE    , "''' (apostrophe)");
    send( 76 , "Slash"         , KEYCODE_SLASH         , "'/'");
    send( 77 , "At"            , KEYCODE_AT            , "'@'");
    send( 78 , "Num"           , KEYCODE_NUM           , "Number modifier", note078);
    send( 79 , "Headsethook"   , KEYCODE_HEADSETHOOK   , "Headset Hook");
    send( 80 , "Focus"         , KEYCODE_FOCUS         , "Camera Focus");
    send( 81 , "Plus"          , KEYCODE_PLUS          , "'+'");
    send( 82 , "Menu"          , KEYCODE_MENU          , "Menu");
    send( 83 , "Notification"  , KEYCODE_NOTIFICATION  , "Notification");
    send( 84 , "Search"        , KEYCODE_SEARCH        , "Search");

    section("Media I");
    // ------------------------------------------------------------

    send( 85 , "Media_Play_Pause"   , KEYCODE_MEDIA_PLAY_PAUSE   , "Play/Pause media");
    send( 86 , "Media_Stop"         , KEYCODE_MEDIA_STOP         , "Stop media");
    send( 87 , "Media_Next"         , KEYCODE_MEDIA_NEXT         , "Play Next media");
    send( 88 , "Media_Previous"     , KEYCODE_MEDIA_PREVIOUS     , "Play Previous media");
    send( 89 , "Media_Rewind"       , KEYCODE_MEDIA_REWIND       , "Rewind media");
    send( 90 , "Media_Fast_Forward" , KEYCODE_MEDIA_FAST_FORWARD , "Fast Forward media");
    send( 91 , "Mute"               , KEYCODE_MUTE               , "Mute", note091);
    send( 92 , "Page_Up"            , KEYCODE_PAGE_UP            , "Page Up");
    send( 93 , "Page_Down"          , KEYCODE_PAGE_DOWN          , "Page Down");
    send( 94 , "Pictsymbols"        , KEYCODE_PICTSYMBOLS        , "Picture Symbols modifier", note094);
    send( 95 , "Switch_Charset"     , KEYCODE_SWITCH_CHARSET     , "Switch Charset modifier", note095);

    section("Game pad I");
    // ------------------------------------------------------------

    send( 96 , "Button_A"      , KEYCODE_BUTTON_A      , "A Button");
    send( 97 , "Button_B"      , KEYCODE_BUTTON_B      , "B Button");
    send( 98 , "Button_C"      , KEYCODE_BUTTON_C      , "C Button");
    send( 99 , "Button_X"      , KEYCODE_BUTTON_X      , "X Button");
    send(100 , "Button_Y"      , KEYCODE_BUTTON_Y      , "Y Button");
    send(101 , "Button_Z"      , KEYCODE_BUTTON_Z      , "Z Button");
    send(102 , "Button_L1"     , KEYCODE_BUTTON_L1     , "L1 Button");
    send(103 , "Button_R1"     , KEYCODE_BUTTON_R1     , "R1 Button");
    send(104 , "Button_L2"     , KEYCODE_BUTTON_L2     , "L2 Button");
    send(105 , "Button_R2"     , KEYCODE_BUTTON_R2     , "R2 Button");
    send(106 , "Button_ThumbL" , KEYCODE_BUTTON_THUMBL , "Left Thumb Button");
    send(107 , "Button_ThumbR" , KEYCODE_BUTTON_THUMBR , "Right Thumb Button");
    send(108 , "Button_Start"  , KEYCODE_BUTTON_START  , "Start Button");
    send(109 , "Button_Select" , KEYCODE_BUTTON_SELECT , "Select Button");
    send(110 , "Button_Mode"   , KEYCODE_BUTTON_MODE   , "Mode Button");

    section("General II");
    // ------------------------------------------------------------

    send(111 , "Escape"      , KEYCODE_ESCAPE      , "Escape");
    send(112 , "Forward_Del" , KEYCODE_FORWARD_DEL , "Forward Delete", note112);
    send(113 , "Ctrl_Left"   , KEYCODE_CTRL_LEFT   , "Left Control modifier");
    send(114 , "Ctrl_Right"  , KEYCODE_CTRL_RIGHT  , "Right Control modifier");
    send(115 , "Caps_Lock"   , KEYCODE_CAPS_LOCK   , "Caps Lock");
    send(116 , "Scroll_Lock" , KEYCODE_SCROLL_LOCK , "Scroll Lock");
    send(117 , "Meta_Left"   , KEYCODE_META_LEFT   , "Left Meta modifier");
    send(118 , "Meta_Right"  , KEYCODE_META_RIGHT  , "Right Meta modifier");
    send(119 , "Function"    , KEYCODE_FUNCTION    , "Function modifier");
    send(120 , "Sysrq"       , KEYCODE_SYSRQ       , "System Request / Print Screen");
    send(121 , "Break"       , KEYCODE_BREAK       , "Break / Pause");
    send(122 , "Move_Home"   , KEYCODE_MOVE_HOME   , "Home Movement, note122");
    send(123 , "Move_End"    , KEYCODE_MOVE_END    , "End Movement", note123);
    send(124 , "Insert"      , KEYCODE_INSERT      , "Insert", note124);
    send(125 , "Forward"     , KEYCODE_FORWARD     , "Forward", note125);

    section("Media II");
    // ------------------------------------------------------------

    send(126 , "Media_Play"   , KEYCODE_MEDIA_PLAY   , "Play media");
    send(127 , "Media_Pause"  , KEYCODE_MEDIA_PAUSE  , "Pause media");
    send(128 , "Media_Close"  , KEYCODE_MEDIA_CLOSE  , "Close media");
    send(129 , "Media_Eject"  , KEYCODE_MEDIA_EJECT  , "Eject media");
    send(130 , "Media_Record" , KEYCODE_MEDIA_RECORD , "Record media");

    section("Function keys");
    // ------------------------------------------------------------

    send(131 , "F1"  , KEYCODE_F1  , "F1");
    send(132 , "F2"  , KEYCODE_F2  , "F2");
    send(133 , "F3"  , KEYCODE_F3  , "F3");
    send(134 , "F4"  , KEYCODE_F4  , "F4");
    send(135 , "F5"  , KEYCODE_F5  , "F5");
    send(136 , "F6"  , KEYCODE_F6  , "F6");
    send(137 , "F7"  , KEYCODE_F7  , "F7");
    send(138 , "F8"  , KEYCODE_F8  , "F8");
    send(139 , "F9"  , KEYCODE_F9  , "F9");
    send(140 , "F10" , KEYCODE_F10 , "F10");
    send(141 , "F11" , KEYCODE_F11 , "F11");
    send(142 , "F12" , KEYCODE_F12 , "F12");

    section("Number pad");
    // ------------------------------------------------------------

    send(143 , "Num_Lock"           , KEYCODE_NUM_LOCK           , "Num Lock", note143);
    send(144 , "Numpad_0"           , KEYCODE_NUMPAD_0           , "numpad '0'");
    send(145 , "Numpad_1"           , KEYCODE_NUMPAD_1           , "numpad '1'");
    send(146 , "Numpad_2"           , KEYCODE_NUMPAD_2           , "numpad '2'");
    send(147 , "Numpad_3"           , KEYCODE_NUMPAD_3           , "numpad '3'");
    send(148 , "Numpad_4"           , KEYCODE_NUMPAD_4           , "numpad '4'");
    send(149 , "Numpad_5"           , KEYCODE_NUMPAD_5           , "numpad '5'");
    send(150 , "Numpad_6"           , KEYCODE_NUMPAD_6           , "numpad '6'");
    send(151 , "Numpad_7"           , KEYCODE_NUMPAD_7           , "numpad '7'");
    send(152 , "Numpad_8"           , KEYCODE_NUMPAD_8           , "numpad '8'");
    send(153 , "Numpad_9"           , KEYCODE_NUMPAD_9           , "numpad '9'");
    send(154 , "Numpad_Divide"      , KEYCODE_NUMPAD_DIVIDE      , "numpad '/'");
    send(155 , "Numpad_Multiply"    , KEYCODE_NUMPAD_MULTIPLY    , "numpad '*'");
    send(156 , "Numpad_Subtract"    , KEYCODE_NUMPAD_SUBTRACT    , "numpad '-'");
    send(157 , "Numpad_Add"         , KEYCODE_NUMPAD_ADD         , "numpad '+'");
    send(158 , "Numpad_Dot"         , KEYCODE_NUMPAD_DOT         , "numpad '.'", note158);
    send(159 , "Numpad_Comma"       , KEYCODE_NUMPAD_COMMA       , "numpad ','", note158);
    send(160 , "Numpad_Enter"       , KEYCODE_NUMPAD_ENTER       , "numpad Enter");
    send(161 , "Numpad_Equals"      , KEYCODE_NUMPAD_EQUALS      , "numpad '='");
    send(162 , "Numpad_Left_Paren"  , KEYCODE_NUMPAD_LEFT_PAREN  , "numpad '('");
    send(163 , "Numpad_Right_Paren" , KEYCODE_NUMPAD_RIGHT_PAREN , "numpad ')'");

    section("TV I");
    // ------------------------------------------------------------

    send(164 , "Volume_Mute"  , KEYCODE_VOLUME_MUTE  , "Volume Mute", note164);
    send(165 , "Info"         , KEYCODE_INFO         , "Info");
    send(166 , "Channel_Up"   , KEYCODE_CHANNEL_UP   , "Channel up");
    send(167 , "Channel_Down" , KEYCODE_CHANNEL_DOWN , "Channel down");
    send(168 , "Zoom_In"      , KEYCODE_ZOOM_IN      , "Zoom in");
    send(169 , "Zoom_Out"     , KEYCODE_ZOOM_OUT     , "Zoom out");
    send(170 , "TV"           , KEYCODE_TV           , "TV");
    send(171 , "Window"       , KEYCODE_WINDOW       , "Window", note171);
    send(172 , "Guide"        , KEYCODE_GUIDE        , "Guide");
    send(173 , "DVR"          , KEYCODE_DVR          , "DVR");
    send(174 , "Bookmark"     , KEYCODE_BOOKMARK     , "Bookmark");
    send(175 , "Captions"     , KEYCODE_CAPTIONS     , "Toggle captions");
    send(176 , "Settings"     , KEYCODE_SETTINGS     , "Settings");
    send(177 , "TV_Power"     , KEYCODE_TV_POWER     , "TV power");
    send(178 , "TV_Input"     , KEYCODE_TV_INPUT     , "TV input");
    send(179 , "STB_Power"    , KEYCODE_STB_POWER    , "Set-top-box power");
    send(180 , "STB_Input"    , KEYCODE_STB_INPUT    , "Set-top-box input");
    send(181 , "AVR_Power"    , KEYCODE_AVR_POWER    , "A/V Receiver power");
    send(182 , "AVR_Input"    , KEYCODE_AVR_INPUT    , "A/V Receiver input");
    send(183 , "Prog_Red"     , KEYCODE_PROG_RED     , "Red programmable");
    send(184 , "Prog_Green"   , KEYCODE_PROG_GREEN   , "Green programmable");
    send(185 , "Prog_Yellow"  , KEYCODE_PROG_YELLOW  , "Yellow programmable");
    send(186 , "Prog_Blue"    , KEYCODE_PROG_BLUE    , "Blue programmable");

    section("General III");
    // ------------------------------------------------------------

    send(187 , "App_Switch" , KEYCODE_APP_SWITCH , "App switch");

    section("Game pad II");
    // ------------------------------------------------------------

    send(188 , "Button_1"  , KEYCODE_BUTTON_1  , "Generic Game Pad Button #1");
    send(189 , "Button_2"  , KEYCODE_BUTTON_2  , "Generic Game Pad Button #2");
    send(190 , "Button_3"  , KEYCODE_BUTTON_3  , "Generic Game Pad Button #3");
    send(191 , "Button_4"  , KEYCODE_BUTTON_4  , "Generic Game Pad Button #4");
    send(192 , "Button_5"  , KEYCODE_BUTTON_5  , "Generic Game Pad Button #5");
    send(193 , "Button_6"  , KEYCODE_BUTTON_6  , "Generic Game Pad Button #6");
    send(194 , "Button_7"  , KEYCODE_BUTTON_7  , "Generic Game Pad Button #7");
    send(195 , "Button_8"  , KEYCODE_BUTTON_8  , "Generic Game Pad Button #8");
    send(196 , "Button_9"  , KEYCODE_BUTTON_9  , "Generic Game Pad Button #9");
    send(197 , "Button_10" , KEYCODE_BUTTON_10 , "Generic Game Pad Button #10");
    send(198 , "Button_11" , KEYCODE_BUTTON_11 , "Generic Game Pad Button #11");
    send(199 , "Button_12" , KEYCODE_BUTTON_12 , "Generic Game Pad Button #12");
    send(200 , "Button_13" , KEYCODE_BUTTON_13 , "Generic Game Pad Button #13");
    send(201 , "Button_14" , KEYCODE_BUTTON_14 , "Generic Game Pad Button #14");
    send(202 , "Button_15" , KEYCODE_BUTTON_15 , "Generic Game Pad Button #15");
    send(203 , "Button_16" , KEYCODE_BUTTON_16 , "Generic Game Pad Button #16");

    section("General IV");
    // ------------------------------------------------------------

    send(204 , "Language_Switch"   , KEYCODE_LANGUAGE_SWITCH   , "Language Switch", note204);
    send(205 , "Manner_Mode"       , KEYCODE_MANNER_MODE       , "Manner Mode", note205);
    send(206 , "3D_Mode"           , KEYCODE_3D_MODE           , "3D Mode");
    send(207 , "Contacts"          , KEYCODE_CONTACTS          , "Contacts special function");
    send(208 , "Calendar"          , KEYCODE_CALENDAR          , "Calendar special function");
    send(209 , "Music"             , KEYCODE_MUSIC             , "Music special function");
    send(210 , "Calculator"        , KEYCODE_CALCULATOR        , "Calculator special function");
    send(211 , "Zenkaku_Hankaku"   , KEYCODE_ZENKAKU_HANKAKU   , "Japanese full-width / half-width");
    send(212 , "Eisu"              , KEYCODE_EISU              , "Japanese alphanumeric");
    send(213 , "Muhenkan"          , KEYCODE_MUHENKAN          , "Japanese non-conversion");
    send(214 , "Henkan"            , KEYCODE_HENKAN            , "Japanese conversion");
    send(215 , "Katakana_Hiragana" , KEYCODE_KATAKANA_HIRAGANA , "Japanese katakana / hiragana");
    send(216 , "Yen"               , KEYCODE_YEN               , "Japanese Yen");
    send(217 , "Ro"                , KEYCODE_RO                , "Japanese Ro");
    send(218 , "Kana"              , KEYCODE_KANA              , "Japanese kana");
    send(219 , "Assist"            , KEYCODE_ASSIST            , "Assist", note219);
    send(220 , "Brightness_Down"   , KEYCODE_BRIGHTNESS_DOWN   , "Brightness Down");
    send(221 , "Brightness_Up"     , KEYCODE_BRIGHTNESS_UP     , "Brightness Up");
    send(222 , "Media_Audio_Track" , KEYCODE_MEDIA_AUDIO_TRACK , "Audio Track");
    send(223 , "Sleep"             , KEYCODE_SLEEP             , "Sleep", note223);
    send(224 , "Wakeup"            , KEYCODE_WAKEUP            , "Wakeup", note224);
    send(225 , "Pairing"           , KEYCODE_PAIRING           , "Pairing", note225);

    section("TV II");
    // ------------------------------------------------------------

    send(226 , "Media_Top_Menu"         , KEYCODE_MEDIA_TOP_MENU         , "Media Top Menu");
    send(227 , "11"                     , KEYCODE_11                     , "'11'", note227);
    send(228 , "12"                     , KEYCODE_12                     , "'12'", note227);
    send(229 , "Last_Channel"           , KEYCODE_LAST_CHANNEL           , "Last Channel");
    send(230 , "TV_Data_Service"        , KEYCODE_TV_DATA_SERVICE        , "TV data service");
    send(231 , "Voice_Assist"           , KEYCODE_VOICE_ASSIST           , "Voice Assist");
    send(232 , "TV_Radio_Service"       , KEYCODE_TV_RADIO_SERVICE       , "Radio");
    send(233 , "TV_Teletext"            , KEYCODE_TV_TELETEXT            , "Teletext");
    send(234 , "TV_Number_Entry"        , KEYCODE_TV_NUMBER_ENTRY        , "Number entry", note234);
    send(235 , "TV_Terrestrial_Analog"  , KEYCODE_TV_TERRESTRIAL_ANALOG  , "Analog Terrestrial");
    send(236 , "TV_Terrestrial_Digital" , KEYCODE_TV_TERRESTRIAL_DIGITAL , "Digital Terrestrial");
    send(237 , "TV_Satellite"           , KEYCODE_TV_SATELLITE           , "Satellite");
    send(238 , "TV_Satellite_BS"        , KEYCODE_TV_SATELLITE_BS        , "BS");
    send(239 , "TV_Satellite_CS"        , KEYCODE_TV_SATELLITE_CS        , "CS");
    send(240 , "TV_Satellite_Service"   , KEYCODE_TV_SATELLITE_SERVICE   , "BS/CS");
    send(241 , "TV_Network"             , KEYCODE_TV_NETWORK             , "Toggle Network");
    send(242 , "TV_Antenna_Cable"       , KEYCODE_TV_ANTENNA_CABLE       , "Antenna/Cable");
    send(243 , "TV_Input_HDMI_1"        , KEYCODE_TV_INPUT_HDMI_1        , "HDMI #1");
    send(244 , "TV_Input_HDMI_2"        , KEYCODE_TV_INPUT_HDMI_2        , "HDMI #2");
    send(245 , "TV_Input_HDMI_3"        , KEYCODE_TV_INPUT_HDMI_3        , "HDMI #3");
    send(246 , "TV_Input_HDMI_4"        , KEYCODE_TV_INPUT_HDMI_4        , "HDMI #4");
    send(247 , "TV_Input_Composite_1"   , KEYCODE_TV_INPUT_COMPOSITE_1   , "Composite #1");
    send(248 , "TV_Input_Composite_2"   , KEYCODE_TV_INPUT_COMPOSITE_2   , "Composite #2");
    send(249 , "TV_Input_Component_1"   , KEYCODE_TV_INPUT_COMPONENT_1   , "Component #1");
    send(250 , "TV_Input_Component_2"   , KEYCODE_TV_INPUT_COMPONENT_2   , "Component #2");
    send(251 , "TV_Input_VGA_1"         , KEYCODE_TV_INPUT_VGA_1         , "VGA #1");
    send(252 , "TV_Audio_Description"   , KEYCODE_TV_AUDIO_DESCRIPTION   , "Audio description");

    send(253,
      "TV_Audio_Description_Mix_Up",
        KEYCODE_TV_AUDIO_DESCRIPTION_MIX_UP,
          "Audio description mixing volume up");
    send(254,
      "TV_Audio_Description_Mix_Down",
        KEYCODE_TV_AUDIO_DESCRIPTION_MIX_DOWN,
          "Audio description mixing volume down");

    send(255 , "TV_Zoom_Mode"           , KEYCODE_TV_ZOOM_MODE          , "Zoom mode");
    send(256 , "TV_Contents_Menu"       , KEYCODE_TV_CONTENTS_MENU      , "Contents menu", note256);
    send(257 , "TV_Media_Context_Menu"  , KEYCODE_TV_MEDIA_CONTEXT_MENU , "Media context menu", note257);
    send(258 , "TV_Timer_Programming"   , KEYCODE_TV_TIMER_PROGRAMMING  , "Timer programming", note258);

    section("General V");
    // ------------------------------------------------------------

    send(259 , "Help"                , KEYCODE_HELP                 , "Help");

    // send(260 , "Navigate_Previous"   , KEYCODE_NAVIGATE_PREVIOUS    , "Navigate to previous");
    // send(261 , "Navigate_Next"       , KEYCODE_NAVIGATE_NEXT        , "Navigate to next");
    // send(262 , "Navigate_In"         , KEYCODE_NAVIGATE_IN          , "Navigate in");
    // send(263 , "Navigate_Out"        , KEYCODE_NAVIGATE_OUT         , "Navigate out");
    // send(264 , "Stem_Primary"        , KEYCODE_STEM_PRIMARY         , "Primary stem for Wear", note264);
    // send(265 , "Stem_1"              , KEYCODE_STEM_1               , "Generic stem 1 for Wear");
    // send(266 , "Stem_2"              , KEYCODE_STEM_2               , "Generic stem 2 for Wear");
    // send(267 , "Stem_3"              , KEYCODE_STEM_3               , "Generic stem 3 for Wear");
    // send(268 , "Dpad_Up_Left"        , KEYCODE_DPAD_UP_LEFT         , "Directional Pad Up-Left");
    // send(269 , "Dpad_Down_Left"      , KEYCODE_DPAD_DOWN_LEFT       , "Directional Pad Down-Left");
    // send(270 , "Dpad_Up_Right"       , KEYCODE_DPAD_UP_RIGHT        , "Directional Pad Up-Right");
    // send(271 , "Dpad_Down_Right"     , KEYCODE_DPAD_DOWN_RIGHT      , "Directional Pad Down-Right");
    // send(272 , "Media_Skip_Forward"  , KEYCODE_MEDIA_SKIP_FORWARD   , "Skip forward media");
    // send(273 , "Media_Skip_Backward" , KEYCODE_MEDIA_SKIP_BACKWARD  , "Skip backward media");
    // send(274 , "Media_Step_Forward"  , KEYCODE_MEDIA_STEP_FORWARD   , "Step forward media");
    // send(275 , "Media_Step_Backward" , KEYCODE_MEDIA_STEP_BACKWARD  , "Step backward media");
    //
    // send(276 ,
    //   "Soft_Sleep",
    //     KEYCODE_SOFT_SLEEP,
    //       "put device to sleep unless a wakelock is held");
    //
    // send(277 , "Cut"                 , KEYCODE_CUT                  , "Cut");
    // send(278 , "Copy"                , KEYCODE_COPY                 , "Copy");
    // send(279 , "Paste"               , KEYCODE_PASTE                , "Paste");
    //
    // send(280,
    //   "System_Navigation_Up",
    //     KEYCODE_SYSTEM_NAVIGATION_UP,
    //       "Consumed by the system for navigation up");
    // send(281,
    //   "System_Navigation_Down",
    //     KEYCODE_SYSTEM_NAVIGATION_DOWN,
    //       "Consumed by the system for navigation down");
    // send(282,
    //   "System_Navigation_Left",
    //     KEYCODE_SYSTEM_NAVIGATION_LEFT,
    //       "Consumed by the system for navigation left");
    // send(283,
    //   "System_Navigation_Right",
    //     KEYCODE_SYSTEM_NAVIGATION_RIGHT,
    //       "Consumed by the system for navigation right");
    //
    // send(284 , "All_Apps"            , KEYCODE_ALL_APPS             , "Show all apps");
  }

  // ************************************************************
  // Attributes
  // ************************************************************

  private Kind mKind;

  private String mName;

  private String mLabel;
  private PKey label (String label) { mLabel = label; return this; }
  public String getLabel () { return mLabel; }

  /** True when key auto-repeats.
   *
   * <p>E.g. cursor keys.</p>
   */
  private boolean mRepeat;
  private PKey repeat () { mRepeat = true; return this; }

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

  public PKey (String name) { mName = name; mLabel = name; }

  private static void section (String name) {
    // TODO
  }

  private static PKey send (String name, int keyCode) {
    PKey key = new PKey(name); key.mKind = Kind.send; key.mKeyCode = keyCode;
    // TODO Register the key
    return key;
  }

  private static PKey send (int nr, String name, int keyCode, String description) {
    PKey key = new PKey(name); key.mKind = Kind.send; key.mKeyCode = keyCode;
    // TODO Register the key
    return key;
  }

  private static PKey send (int nr, String name, int keyCode, String description, String note) {
    PKey key = new PKey(name); key.mKind = Kind.send; key.mKeyCode = keyCode;
    // TODO Register the key
    return key;
  }

  // ------------------------------------------------------------

  private static PKey write (String name) {
    PKey key = new PKey(name); key.mKind = Kind.write; key.mText = name;
    // TODO Register the key
    return key;
  }

  private static PKey special (String name) {
    PKey key = new PKey(name); key.mKind = Kind.special;
    // TODO Register the key
    return key;
  }
}
