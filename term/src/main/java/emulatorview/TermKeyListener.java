package emulatorview;

//T-{ ------------------------------------------------------------
//T- import de.t2h.tterm.emulatorview.compat.AndroidCompat;
//T- import de.t2h.tterm.emulatorview.compat.KeyCharacterMapCompat;
//T-} ------------------------------------------------------------

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

//T!{ ------------------------------------------------------------
//T! import static de.t2h.tterm.emulatorview.compat.KeycodeConstants.*;
import static android.view.KeyEvent.*;
//T!} ------------------------------------------------------------

/**
 * An ASCII key listener. Supports control characters and escape. Keeps track of
 * the current state of the alt, shift, fn, and control keys.
 *
 */
class TermKeyListener {
    private final static String TAG = "TermKeyListener";
    private static final boolean LOG_MISC = false;
    private static final boolean LOG_KEYS = false;
    private static final boolean LOG_COMBINING_ACCENT = false;

    /** Disabled for now because it interferes with ALT processing on phones with physical keyboards. */
    private final static boolean SUPPORT_8_BIT_META = false;

    private static final int KEYMOD_ALT   = 0x80000000;
    private static final int KEYMOD_CTRL  = 0x40000000;
    private static final int KEYMOD_SHIFT = 0x20000000;
    /** Means this maps raw scancode */
    private static final int KEYMOD_SCAN  = 0x10000000;

    private static Map<Integer, String> mKeyMap;

    private String[] mKeyCodes = new String[256];
    private String[] mAppKeyCodes = new String[256];

    private void initKeyCodes() {
        mKeyMap = new HashMap<Integer, String>();
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;2D");
        mKeyMap.put(KEYMOD_ALT | KEYCODE_DPAD_LEFT, "\033[1;3D");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;4D");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_DPAD_LEFT, "\033[1;5D");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;6D");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_LEFT, "\033[1;7D");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;8D");

        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;2C");
        mKeyMap.put(KEYMOD_ALT | KEYCODE_DPAD_RIGHT, "\033[1;3C");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;4C");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_DPAD_RIGHT, "\033[1;5C");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;6C");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_RIGHT, "\033[1;7C");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;8C");

        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;2A");
        mKeyMap.put(KEYMOD_ALT | KEYCODE_DPAD_UP, "\033[1;3A");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;4A");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_DPAD_UP, "\033[1;5A");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;6A");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_UP, "\033[1;7A");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;8A");

        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;2B");
        mKeyMap.put(KEYMOD_ALT | KEYCODE_DPAD_DOWN, "\033[1;3B");
        mKeyMap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;4B");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_DPAD_DOWN, "\033[1;5B");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;6B");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_DOWN, "\033[1;7B");
        mKeyMap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;8B");

        //^[[3~
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_FORWARD_DEL, "\033[3;2~");
        mKeyMap.put(KEYMOD_ALT | KEYCODE_FORWARD_DEL, "\033[3;3~");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_FORWARD_DEL, "\033[3;5~");

        //^[[2~
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_INSERT, "\033[2;2~");
        mKeyMap.put(KEYMOD_ALT | KEYCODE_INSERT, "\033[2;3~");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_INSERT, "\033[2;5~");

        mKeyMap.put(KEYMOD_CTRL | KEYCODE_MOVE_HOME, "\033[1;5H");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_MOVE_END, "\033[1;5F");

        mKeyMap.put(KEYMOD_ALT | KEYCODE_ENTER, "\033\r");
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_ENTER, "\n");
        // Duh, so special...
        mKeyMap.put(KEYMOD_CTRL | KEYCODE_SPACE, "\000");

        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F1, "\033[1;2P");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F2, "\033[1;2Q");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F3, "\033[1;2R");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F4, "\033[1;2S");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F5, "\033[15;2~");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F6, "\033[17;2~");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F7, "\033[18;2~");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F8, "\033[19;2~");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F9, "\033[20;2~");
        mKeyMap.put(KEYMOD_SHIFT | KEYCODE_F10, "\033[21;2~");

        mKeyCodes[KEYCODE_DPAD_CENTER] = "\015";
        mKeyCodes[KEYCODE_DPAD_UP] = "\033[A";
        mKeyCodes[KEYCODE_DPAD_DOWN] = "\033[B";
        mKeyCodes[KEYCODE_DPAD_RIGHT] = "\033[C";
        mKeyCodes[KEYCODE_DPAD_LEFT] = "\033[D";
        setFnKeys("vt100");
        mKeyCodes[KEYCODE_SYSRQ] = "\033[32~"; // Sys Request / Print
        // Is this Scroll lock? mKeyCodes[Cancel] = "\033[33~";
        mKeyCodes[KEYCODE_BREAK] = "\033[34~"; // Pause/Break

        mKeyCodes[KEYCODE_TAB] = "\011";
        mKeyCodes[KEYCODE_ENTER] = "\015";
        mKeyCodes[KEYCODE_ESCAPE] = "\033";

        mKeyCodes[KEYCODE_INSERT] = "\033[2~";
        mKeyCodes[KEYCODE_FORWARD_DEL] = "\033[3~";
        // Home/End keys are set by setFnKeys()
        mKeyCodes[KEYCODE_PAGE_UP] = "\033[5~";
        mKeyCodes[KEYCODE_PAGE_DOWN] = "\033[6~";
        mKeyCodes[KEYCODE_DEL]= "\177";
        mKeyCodes[KEYCODE_NUM_LOCK] = "\033OP";
        mKeyCodes[KEYCODE_NUMPAD_DIVIDE] = "/";
        mKeyCodes[KEYCODE_NUMPAD_MULTIPLY] = "*";
        mKeyCodes[KEYCODE_NUMPAD_SUBTRACT] = "-";
        mKeyCodes[KEYCODE_NUMPAD_ADD] = "+";
        mKeyCodes[KEYCODE_NUMPAD_ENTER] = "\015";
        mKeyCodes[KEYCODE_NUMPAD_EQUALS] = "=";
        mKeyCodes[KEYCODE_NUMPAD_COMMA] = ",";
/*
        mKeyCodes[KEYCODE_NUMPAD_DOT] = ".";
        mKeyCodes[KEYCODE_NUMPAD_0] = "0";
        mKeyCodes[KEYCODE_NUMPAD_1] = "1";
        mKeyCodes[KEYCODE_NUMPAD_2] = "2";
        mKeyCodes[KEYCODE_NUMPAD_3] = "3";
        mKeyCodes[KEYCODE_NUMPAD_4] = "4";
        mKeyCodes[KEYCODE_NUMPAD_5] = "5";
        mKeyCodes[KEYCODE_NUMPAD_6] = "6";
        mKeyCodes[KEYCODE_NUMPAD_7] = "7";
        mKeyCodes[KEYCODE_NUMPAD_8] = "8";
        mKeyCodes[KEYCODE_NUMPAD_9] = "9";
*/
        // Keypad is used for cursor/func keys
        mKeyCodes[KEYCODE_NUMPAD_DOT] = mKeyCodes[KEYCODE_FORWARD_DEL];
        mKeyCodes[KEYCODE_NUMPAD_0] = mKeyCodes[KEYCODE_INSERT];
        mKeyCodes[KEYCODE_NUMPAD_1] = mKeyCodes[KEYCODE_MOVE_END];
        mKeyCodes[KEYCODE_NUMPAD_2] = mKeyCodes[KEYCODE_DPAD_DOWN];
        mKeyCodes[KEYCODE_NUMPAD_3] = mKeyCodes[KEYCODE_PAGE_DOWN];
        mKeyCodes[KEYCODE_NUMPAD_4] = mKeyCodes[KEYCODE_DPAD_LEFT];
        mKeyCodes[KEYCODE_NUMPAD_5] = "5";
        mKeyCodes[KEYCODE_NUMPAD_6] = mKeyCodes[KEYCODE_DPAD_RIGHT];
        mKeyCodes[KEYCODE_NUMPAD_7] = mKeyCodes[KEYCODE_MOVE_HOME];
        mKeyCodes[KEYCODE_NUMPAD_8] = mKeyCodes[KEYCODE_DPAD_UP];
        mKeyCodes[KEYCODE_NUMPAD_9] = mKeyCodes[KEYCODE_PAGE_UP];


//        mAppKeyCodes[KEYCODE_DPAD_UP] = "\033OA";
//        mAppKeyCodes[KEYCODE_DPAD_DOWN] = "\033OB";
//        mAppKeyCodes[KEYCODE_DPAD_RIGHT] = "\033OC";
//        mAppKeyCodes[KEYCODE_DPAD_LEFT] = "\033OD";
        mAppKeyCodes[KEYCODE_NUMPAD_DIVIDE] = "\033Oo";
        mAppKeyCodes[KEYCODE_NUMPAD_MULTIPLY] = "\033Oj";
        mAppKeyCodes[KEYCODE_NUMPAD_SUBTRACT] = "\033Om";
        mAppKeyCodes[KEYCODE_NUMPAD_ADD] = "\033Ok";
        mAppKeyCodes[KEYCODE_NUMPAD_ENTER] = "\033OM";
        mAppKeyCodes[KEYCODE_NUMPAD_EQUALS] = "\033OX";
        mAppKeyCodes[KEYCODE_NUMPAD_DOT] = "\033On";
        mAppKeyCodes[KEYCODE_NUMPAD_COMMA] = "\033Ol";
        mAppKeyCodes[KEYCODE_NUMPAD_0] = "\033Op";
        mAppKeyCodes[KEYCODE_NUMPAD_1] = "\033Oq";
        mAppKeyCodes[KEYCODE_NUMPAD_2] = "\033Or";
        mAppKeyCodes[KEYCODE_NUMPAD_3] = "\033Os";
        mAppKeyCodes[KEYCODE_NUMPAD_4] = "\033Ot";
        mAppKeyCodes[KEYCODE_NUMPAD_5] = "\033Ou";
        mAppKeyCodes[KEYCODE_NUMPAD_6] = "\033Ov";
        mAppKeyCodes[KEYCODE_NUMPAD_7] = "\033Ow";
        mAppKeyCodes[KEYCODE_NUMPAD_8] = "\033Ox";
        mAppKeyCodes[KEYCODE_NUMPAD_9] = "\033Oy";
    }

    public void setCursorKeysApplicationMode(boolean val) {
        if (LOG_MISC) {
            Log.d(EmulatorDebug.LOG_TAG, "CursorKeysApplicationMode=" + val);
        }
        if (val) {
            mKeyCodes[KEYCODE_NUMPAD_8] = mKeyCodes[KEYCODE_DPAD_UP] = "\033OA";
            mKeyCodes[KEYCODE_NUMPAD_2] = mKeyCodes[KEYCODE_DPAD_DOWN] = "\033OB";
            mKeyCodes[KEYCODE_NUMPAD_6] = mKeyCodes[KEYCODE_DPAD_RIGHT] = "\033OC";
            mKeyCodes[KEYCODE_NUMPAD_4] = mKeyCodes[KEYCODE_DPAD_LEFT] = "\033OD";
        } else {
            mKeyCodes[KEYCODE_NUMPAD_8] = mKeyCodes[KEYCODE_DPAD_UP] = "\033[A";
            mKeyCodes[KEYCODE_NUMPAD_2] = mKeyCodes[KEYCODE_DPAD_DOWN] = "\033[B";
            mKeyCodes[KEYCODE_NUMPAD_6] = mKeyCodes[KEYCODE_DPAD_RIGHT] = "\033[C";
            mKeyCodes[KEYCODE_NUMPAD_4] = mKeyCodes[KEYCODE_DPAD_LEFT] = "\033[D";
        }
    }

    /**
     * The state engine for a modifier key. Can be pressed, released, locked,
     * and so on.
     *
     */
    private class ModifierKey {

        private int mState;

        private static final int UNPRESSED = 0;

        private static final int PRESSED = 1;

        private static final int RELEASED = 2;

        private static final int USED = 3;

        private static final int LOCKED = 4;

        /**
         * Construct a modifier key. UNPRESSED by default.
         *
         */
        public ModifierKey() {
            mState = UNPRESSED;
        }

        public void onPress() {
            switch (mState) {
            case PRESSED:
                // This is a repeat before use
                break;
            case RELEASED:
                mState = LOCKED;
                break;
            case USED:
                // This is a repeat after use
                break;
            case LOCKED:
                mState = UNPRESSED;
                break;
            //T+{ ------------------------------------------------------------
            // UNPRESSED
            //T+} ------------------------------------------------------------
            default:
                mState = PRESSED;
                break;
            }
        }

        public void onRelease() {
            switch (mState) {
            case USED:
                mState = UNPRESSED;
                break;
            case PRESSED:
                mState = RELEASED;
                break;
            default:
                // Leave state alone
                break;
            }
        }

        public void adjustAfterKeypress() {
            switch (mState) {
            case PRESSED:
                mState = USED;
                break;
            case RELEASED:
                mState = UNPRESSED;
                break;
            default:
                // Leave state alone
                break;
            }
        }

        public boolean isActive() {
            return mState != UNPRESSED;
        }

        public int getUIMode() {
            switch (mState) {
            default:
            case UNPRESSED:
                return TextRenderer.MODE_OFF;
            case PRESSED:
            case RELEASED:
            case USED:
                return TextRenderer.MODE_ON;
            case LOCKED:
                return TextRenderer.MODE_LOCKED;
            }
        }
    }

    private ModifierKey mAltKey = new ModifierKey();

    private ModifierKey mCapKey = new ModifierKey();

    private ModifierKey mControlKey = new ModifierKey();

    private ModifierKey mFnKey = new ModifierKey();

    private int mCursorMode;

    private boolean mHardwareControlKey;

    private TermSession mTermSession;

    private int mBackKeyCode;
    private boolean mAltSendsEsc;

    private int mCombiningAccent;

    // Map keycodes out of (above) the Unicode code point space.
    static public final int KEYCODE_OFFSET = 0xA00000;

    //T+{ ------------------------------------------------------------
    KeyUpdater mKeyUpdater;
    public void attachKeyUpdater(KeyUpdater updater) {
        mKeyUpdater = updater;
    }
    //T+} ------------------------------------------------------------

    /**
     * Construct a term key listener.
     *
     */
    public TermKeyListener(TermSession termSession) {
        mTermSession = termSession;
        initKeyCodes();
        updateCursorMode();
    }

    public void setBackKeyCharacter(int code) {
        mBackKeyCode = code;
    }

    public void setAltSendsEsc(boolean flag) {
        mAltSendsEsc = flag;
    }

    public void handleHardwareControlKey(boolean down) {
        mHardwareControlKey = down;
    }

    public void onPause() {
        // Ensure we don't have any left-over modifier state when switching
        // views.
        mHardwareControlKey = false;
    }

    public void onResume() {
        // Nothing special.
    }

    //T+{ ------------------------------------------------------------
    // TODO ThH: Similar to `handleFnKey´, merge them.
    //T+} ------------------------------------------------------------
    public void handleControlKey(boolean down) {
        if (down) {
            mControlKey.onPress();
        } else {
            mControlKey.onRelease();
        }
        updateCursorMode();
    }

    //T+{ ------------------------------------------------------------
    // TODO ThH: Similar to `handleControlKey´, merge them.
    public void handleFnKey(boolean down) {
    //T+} ------------------------------------------------------------
        if (down) {
            mFnKey.onPress();
        } else {
            mFnKey.onRelease();
        }
        updateCursorMode();
    }

    public void setTermType(String termType) {
        setFnKeys(termType);
    }

    private void setFnKeys(String termType) {
        // These key assignments taken from the debian squeeze terminfo database.
        if (termType.equals("xterm")) {
            mKeyCodes[KEYCODE_NUMPAD_7] = mKeyCodes[KEYCODE_MOVE_HOME] = "\033OH";
            mKeyCodes[KEYCODE_NUMPAD_1] = mKeyCodes[KEYCODE_MOVE_END] = "\033OF";
        } else {
            mKeyCodes[KEYCODE_NUMPAD_7] = mKeyCodes[KEYCODE_MOVE_HOME] = "\033[1~";
            mKeyCodes[KEYCODE_NUMPAD_1] = mKeyCodes[KEYCODE_MOVE_END] = "\033[4~";
        }
        if (termType.equals("vt100")) {
            mKeyCodes[KEYCODE_F1] = "\033OP"; // VT100 PF1
            mKeyCodes[KEYCODE_F2] = "\033OQ"; // VT100 PF2
            mKeyCodes[KEYCODE_F3] = "\033OR"; // VT100 PF3
            mKeyCodes[KEYCODE_F4] = "\033OS"; // VT100 PF4
            // the following keys are in the database, but aren't on a real vt100.
            mKeyCodes[KEYCODE_F5] = "\033Ot";
            mKeyCodes[KEYCODE_F6] = "\033Ou";
            mKeyCodes[KEYCODE_F7] = "\033Ov";
            mKeyCodes[KEYCODE_F8] = "\033Ol";
            mKeyCodes[KEYCODE_F9] = "\033Ow";
            mKeyCodes[KEYCODE_F10] = "\033Ox";
            // The following keys are not in database.
            mKeyCodes[KEYCODE_F11] = "\033[23~";
            mKeyCodes[KEYCODE_F12] = "\033[24~";
        } else if (termType.startsWith("linux")) {
            mKeyCodes[KEYCODE_F1] = "\033[[A";
            mKeyCodes[KEYCODE_F2] = "\033[[B";
            mKeyCodes[KEYCODE_F3] = "\033[[C";
            mKeyCodes[KEYCODE_F4] = "\033[[D";
            mKeyCodes[KEYCODE_F5] = "\033[[E";
            mKeyCodes[KEYCODE_F6] = "\033[17~";
            mKeyCodes[KEYCODE_F7] = "\033[18~";
            mKeyCodes[KEYCODE_F8] = "\033[19~";
            mKeyCodes[KEYCODE_F9] = "\033[20~";
            mKeyCodes[KEYCODE_F10] = "\033[21~";
            mKeyCodes[KEYCODE_F11] = "\033[23~";
            mKeyCodes[KEYCODE_F12] = "\033[24~";
        } else {
            // default
            // screen, screen-256colors, xterm, anything new
            mKeyCodes[KEYCODE_F1] = "\033OP"; // VT100 PF1
            mKeyCodes[KEYCODE_F2] = "\033OQ"; // VT100 PF2
            mKeyCodes[KEYCODE_F3] = "\033OR"; // VT100 PF3
            mKeyCodes[KEYCODE_F4] = "\033OS"; // VT100 PF4
            mKeyCodes[KEYCODE_F5] = "\033[15~";
            mKeyCodes[KEYCODE_F6] = "\033[17~";
            mKeyCodes[KEYCODE_F7] = "\033[18~";
            mKeyCodes[KEYCODE_F8] = "\033[19~";
            mKeyCodes[KEYCODE_F9] = "\033[20~";
            mKeyCodes[KEYCODE_F10] = "\033[21~";
            mKeyCodes[KEYCODE_F11] = "\033[23~";
            mKeyCodes[KEYCODE_F12] = "\033[24~";
        }
    }

    public int mapControlChar(int ch) {
        return mapControlChar(mHardwareControlKey || mControlKey.isActive(), mFnKey.isActive(), ch);
    }

    //T+{ ------------------------------------------------------------
    // TODO ThH: Rename, it also maps `Fn´.
    //T+} ------------------------------------------------------------
    public int mapControlChar(boolean control, boolean fn, int ch) {
        int result = ch;
        if (control) {
            //T!{ ------------------------------------------------------------
            //T! // Search is the control key.
            //T! if (result >= 'a' && result <= 'z') {
            //T!     result = (char) (result - 'a' + '\001');
            //T! } else if (result >= 'A' && result <= 'Z') {
            //T!     result = (char) (result - 'A' + '\001');
            //T! } else if (result == ' ' || result == '2') {
            //T!     result = 0;
            //T! } else if (result == '[' || result == '3') {
            //T!     result = 27; // ^[ (Esc)
            //T! } else if (result == '\\' || result == '4') {
            //T!     result = 28;
            //T! } else if (result == ']' || result == '5') {
            //T!     result = 29;
            //T! } else if (result == '^' || result == '6') {
            //T!     result = 30; // control-^
            //T! } else if (result == '_' || result == '7') {
            //T!     result = 31;
            //T! } else if (result == '8') {
            //T!     result = 127; // DEL
            //T! } else if (result == '9') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_F11;
            //T! } else if (result == '0') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_F12;
            //T! }

            // If you test `Ctrl-0´..`Ctrl-9´ in Bash, press `Ctrl-V´ first, so that you see which char
            // reaches Bash without Readline processing it.
            // 
            if        ('a' <= result  && result <= 'z') { result = (char) (result - 'a' + 1);
            } else if ('A' <= result  && result <= 'Z') { result = (char) (result - 'A' + 1);
            } else if (result == ' '  || result == '@') { result = 0;
            } else if (result == '['  || result == '1') { result = 27;  // `C-[´ (`Esc´)
            } else if (result == '\\' || result == '2') { result = 28;  // `C-\´
            } else if (result == ']'  || result == '3') { result = 29;  // `C-]´
            } else if (result == '^'  || result == '4') { result = 30;  // `C-^´
            } else if (result == '_'  || result == '5') { result = 31;  // `C-_´
            } else if (                  result == '0') { result = 127; // `Del´, also on `Backspace´
            }
            //T!} ------------------------------------------------------------
        } else if (fn) {
            //T!{ ------------------------------------------------------------
            //T! if (result == 'w' || result == 'W') {
            //T!     result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_UP;
            //T! } else if (result == 'a' || result == 'A') {
            //T!     result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_LEFT;
            //T! } else if (result == 's' || result == 'S') {
            //T!     result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_DOWN;
            //T! } else if (result == 'd' || result == 'D') {
            //T!     result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_RIGHT;
            //T! } else if (result == 'p' || result == 'P') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_PAGE_UP;
            //T! } else if (result == 'n' || result == 'N') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_PAGE_DOWN;
            //T! } else if (result == 't' || result == 'T') {
            //T!     result = KEYCODE_OFFSET + KeyEvent.KEYCODE_TAB;
            //T! } else if (result == 'l' || result == 'L') {
            //T!     result = '|';
            //T! } else if (result == 'u' || result == 'U') {
            //T!     result = '_';
            //T! } else if (result == 'e' || result == 'E') {
            //T!     result = 27; // ^[ (Esc)
            //T! } else if (result == '.') {
            //T!     result = 28; // ^\
            //T! } else if (result > '0' && result <= '9') {
            //T!     // F1-F9
            //T!     result = (char)(result + KEYCODE_OFFSET + KEYCODE_F1 - 1);
            //T! } else if (result == '0') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_F10;
            //T! } else if (result == 'i' || result == 'I') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_INSERT;
            //T! } else if (result == 'x' || result == 'X') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_FORWARD_DEL;
            //T! } else if (result == 'h' || result == 'H') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_MOVE_HOME;
            //T! } else if (result == 'f' || result == 'F') {
            //T!     result = KEYCODE_OFFSET + KEYCODE_MOVE_END;
            //T! }

            // Cursor movement
            if        (result == 'q' || result == 'Q') { result = KEYCODE_OFFSET + KEYCODE_MOVE_HOME;
            } else if (result == 'w' || result == 'W') { result = KEYCODE_OFFSET + KEYCODE_DPAD_UP;
            } else if (result == 'e' || result == 'E') { result = KEYCODE_OFFSET + KEYCODE_MOVE_END;
            } else if (result == 'r' || result == 'R') { result = KEYCODE_OFFSET + KEYCODE_PAGE_UP;
            } else if (result == 'a' || result == 'A') { result = KEYCODE_OFFSET + KEYCODE_DPAD_LEFT;
            } else if (result == 's' || result == 'S') { result = KEYCODE_OFFSET + KEYCODE_DPAD_DOWN;
            } else if (result == 'd' || result == 'D') { result = KEYCODE_OFFSET + KEYCODE_DPAD_RIGHT;
            } else if (result == 'f' || result == 'F') { result = KEYCODE_OFFSET + KEYCODE_PAGE_DOWN;

            } else if (result == 't' || result == 'T') { result = KEYCODE_OFFSET + KEYCODE_TAB;
            } else if (result == 'i' || result == 'I') { result = KEYCODE_OFFSET + KEYCODE_INSERT;
            } else if (result == 'x' || result == 'X') { result = KEYCODE_OFFSET + KEYCODE_FORWARD_DEL;

            // Function keys
            } else if ('1' <= result && result <= '9') { result = (char)(result + KEYCODE_OFFSET + KEYCODE_F1 - 1); // F1-F9
            } else if (result == '0')                  { result = KEYCODE_OFFSET + KEYCODE_F10;
            } else if (result == 'o' || result == 'O') { result = KEYCODE_OFFSET + KEYCODE_F10;
            } else if (result == 'p' || result == 'P') { result = KEYCODE_OFFSET + KEYCODE_F10;
            }
        }

        if (result > -1) {
            mAltKey.adjustAfterKeypress();
            mCapKey.adjustAfterKeypress();
            mControlKey.adjustAfterKeypress();
            mFnKey.adjustAfterKeypress();
            updateCursorMode();
        }

        return result;
    }

    /**
     * Handle a keyDown event.
     *
     * T+ On Ten7 with Swype, called for `Tab´ and `C-a´, but not for `a´, which is handled by
     * T+ code below `EmulatorView.onCreateInputConnection´.
     *
     * @param keyCode the keycode of the keyDown event
     *
     */
    public void keyDown(int keyCode, KeyEvent event, boolean appMode,
            boolean allowToggle) throws IOException {
        if (LOG_KEYS) {
            Log.i(TAG, "keyDown(" + keyCode + "," + event + "," + appMode + "," + allowToggle + ")");
        }
        if (handleKeyCode(keyCode, event, appMode)) {
            return;
        }
        int result = -1;
        boolean chordedCtrl = false;
        boolean setHighBit = false;
        switch (keyCode) {
        case KeyEvent.KEYCODE_ALT_RIGHT:
        case KeyEvent.KEYCODE_ALT_LEFT:
            if (allowToggle) {
                mAltKey.onPress();
                updateCursorMode();
            }
            break;

        case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_SHIFT_RIGHT:
            if (allowToggle) {
                mCapKey.onPress();
                updateCursorMode();
            }
            break;

        case KEYCODE_CTRL_LEFT:
        case KEYCODE_CTRL_RIGHT:
            // Ignore the control key.
            return;

        case KEYCODE_CAPS_LOCK:
            // Ignore the capslock key.
            return;

        case KEYCODE_FUNCTION:
            // Ignore the function key.
            return;

        case KeyEvent.KEYCODE_BACK:
            result = mBackKeyCode;
            break;

        default: {
            int metaState = event.getMetaState();
            chordedCtrl = ((META_CTRL_ON & metaState) != 0);
            boolean effectiveCaps = allowToggle &&
                    (mCapKey.isActive());
            boolean effectiveAlt = allowToggle && mAltKey.isActive();
            int effectiveMetaState = metaState & (~META_CTRL_MASK);
            if (effectiveCaps) {
                effectiveMetaState |= KeyEvent.META_SHIFT_ON;
            }
            if (!allowToggle && (effectiveMetaState & META_ALT_ON) != 0) {
                effectiveAlt = true;
            }
            if (effectiveAlt) {
                if (mAltSendsEsc) {
                    mTermSession.write(new byte[]{0x1b},0,1);
                    effectiveMetaState &= ~KeyEvent.META_ALT_MASK;
                } else if (SUPPORT_8_BIT_META) {
                    setHighBit = true;
                    effectiveMetaState &= ~KeyEvent.META_ALT_MASK;
                } else {
                    // Legacy behavior: Pass Alt through to allow composing characters.
                    effectiveMetaState |= KeyEvent.META_ALT_ON;
                }
            }

            // Note: The Hacker keyboard IME key labeled Alt actually sends Meta.


            if ((metaState & KeyEvent.META_META_ON) != 0) {
                if (mAltSendsEsc) {
                    mTermSession.write(new byte[]{0x1b},0,1);
                    effectiveMetaState &= ~KeyEvent.META_META_MASK;
                } else {
                    if (SUPPORT_8_BIT_META) {
                        setHighBit = true;
                        effectiveMetaState &= ~KeyEvent.META_META_MASK;
                    }
                }
            }
            result = event.getUnicodeChar(effectiveMetaState);

            if ((result & KeyCharacterMap.COMBINING_ACCENT) != 0) {
                if (LOG_COMBINING_ACCENT) {
                    Log.i(TAG, "Got combining accent " + result);
                }
                mCombiningAccent = result & KeyCharacterMap.COMBINING_ACCENT_MASK;
                return;
            }
            if (mCombiningAccent != 0) {
                int unaccentedChar = result;
                result = KeyCharacterMap.getDeadChar(mCombiningAccent, unaccentedChar);
                if (LOG_COMBINING_ACCENT) {
                    Log.i(TAG, "getDeadChar(" + mCombiningAccent + ", " + unaccentedChar + ") -> " + result);
                }
                mCombiningAccent = 0;
            }

            break;
            }
        }

        //T!{ ------------------------------------------------------------
        // This is the relevant place for *locked* `Ctrl´ and `Fn´. I'm currently testing what happens when I
        // ignore `allowToggle´ here for all keyboards.
        //
        // TTerm's buttons `Ctrl´ and `Fn´ should always toggle and lock, even when the keyboard reports that
        // it does not "toggle", only "chord". For example, when using Google's keyboard "GBoard",
        // `KeyCharacterMap.getModifierBehavior´ will return `MODIFIER_BEHAVIOR_CHORDED´, not
        // `MODIFIER_BEHAVIOR_CHORDED_OR_TOGGLED´ as the keyboards "Swype" or "Hacker's Keyboard" do, so
        // the call to `isEventFromToggleDevice´ will set `allowToggle´ to `false´.
        //T! boolean effectiveControl = chordedCtrl || mHardwareControlKey || (allowToggle && mControlKey.isActive());
        //T! boolean effectiveFn = allowToggle && mFnKey.isActive();
        boolean effectiveControl = chordedCtrl || mHardwareControlKey || (/*allowToggle &&*/ mControlKey.isActive());
        boolean effectiveFn = /*allowToggle &&*/ mFnKey.isActive();
        // T!} ------------------------------------------------------------

        result = mapControlChar(effectiveControl, effectiveFn, result);

        if (result >= KEYCODE_OFFSET) {
            handleKeyCode(result - KEYCODE_OFFSET, null, appMode);
        } else if (result >= 0) {
            if (setHighBit) {
                result |= 0x80;
            }
            mTermSession.write(result);
        }
    }

    public int getCombiningAccent() {
        return mCombiningAccent;
    }

    public int getCursorMode() {
        return mCursorMode;
    }

    private void updateCursorMode() {
        mCursorMode = getCursorModeHelper(mCapKey, TextRenderer.MODE_SHIFT_SHIFT)
                | getCursorModeHelper(mAltKey, TextRenderer.MODE_ALT_SHIFT)
                | getCursorModeHelper(mControlKey, TextRenderer.MODE_CTRL_SHIFT)
                | getCursorModeHelper(mFnKey, TextRenderer.MODE_FN_SHIFT);
        //T+{ ------------------------------------------------------------
        if(mKeyUpdater != null) {
            mKeyUpdater.updateControl(mControlKey.getUIMode());
            mKeyUpdater.updateFn(mFnKey.getUIMode());
        }
        //T+} ------------------------------------------------------------
    }

    private static int getCursorModeHelper(ModifierKey key, int shift) {
        return key.getUIMode() << shift;
    }

    static boolean isEventFromToggleDevice(KeyEvent event) {
        //T!{ ------------------------------------------------------------
        //T! if (AndroidCompat.SDK < 11) {
        //T!     return true;
        //T! }
        //T! KeyCharacterMapCompat kcm = KeyCharacterMapCompat.wrap(
        //T!         KeyCharacterMap.load(event.getDeviceId()));
        //T! return kcm.getModifierBehaviour() ==
        //T!         KeyCharacterMapCompat.MODIFIER_BEHAVIOR_CHORDED_OR_TOGGLED;
        KeyCharacterMap kcm = KeyCharacterMap.load(event.getDeviceId());
        return kcm.getModifierBehavior() == KeyCharacterMap.MODIFIER_BEHAVIOR_CHORDED_OR_TOGGLED;
        //T!} ------------------------------------------------------------
    }

    public boolean handleKeyCode(int keyCode, KeyEvent event, boolean appMode) throws IOException {
        String code = null;
        if (event != null) {
            int keyMod = 0;
            // META_CTRL_ON was added only in API 11, so don't use it,
            // use our own tracking of Ctrl key instead.
            // (event.getMetaState() & META_CTRL_ON) != 0
            if (mHardwareControlKey || mControlKey.isActive()) {
                keyMod |= KEYMOD_CTRL;
            }
            if ((event.getMetaState() & META_ALT_ON) != 0) {
                keyMod |= KEYMOD_ALT;
            }
            if ((event.getMetaState() & META_SHIFT_ON) != 0) {
                keyMod |= KEYMOD_SHIFT;
            }
            // First try to map scancode
            code = mKeyMap.get(event.getScanCode() | KEYMOD_SCAN | keyMod);
            if (code == null) {
                code = mKeyMap.get(keyCode | keyMod);
            }
        }

        if (code == null && keyCode >= 0 && keyCode < mKeyCodes.length) {
            if (appMode) {
                code = mAppKeyCodes[keyCode];
            }
            if (code == null) {
                code = mKeyCodes[keyCode];
            }
        }

        if (code != null) {
            if (EmulatorDebug.LOG_CHARACTERS_FLAG) {
                byte[] bytes = code.getBytes();
                Log.d(EmulatorDebug.LOG_TAG, "Out: '" + EmulatorDebug.bytesToString(bytes, 0, bytes.length) + "'");
            }
            //T+{ ------------------------------------------------------------
            // Press Enter to close finished session for keyboard GBoard.
            //
            // For keyboard Swype, see `TermKeyListener.handleKeyCode´.
            //
            if(mTermSession.mIsExiting && code.equals("\r")) {
                mTermSession.finish();
                return true;
            }
            //T+} ------------------------------------------------------------

            mTermSession.write(code);
            return true;
        }
        return false;
    }

    /**
     * Handle a keyUp event.
     *
     * T+ On Ten7 with Swype, called for `Tab´ and `C-a´, but not for `a´, which is handled by
     * T+ code below `EmulatorView.onCreateInputConnection´.
     *
     * @param keyCode the keyCode of the keyUp event
     */
    public void keyUp(int keyCode, KeyEvent event) {
        boolean allowToggle = isEventFromToggleDevice(event);
        switch (keyCode) {
        case KeyEvent.KEYCODE_ALT_LEFT:
        case KeyEvent.KEYCODE_ALT_RIGHT:
            if (allowToggle) {
                mAltKey.onRelease();
                updateCursorMode();
            }
            break;
        case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_SHIFT_RIGHT:
            if (allowToggle) {
                mCapKey.onRelease();
                updateCursorMode();
            }
            break;

        case KEYCODE_CTRL_LEFT:
        case KEYCODE_CTRL_RIGHT:
            // ignore control keys.
            break;

        default:
            // Ignore other keyUps
            break;
        }
    }

    public boolean getAltSendsEsc() {
        return mAltSendsEsc;
    }

    public boolean isAltActive() {
        return mAltKey.isActive();
    }

    public boolean isCtrlActive() {
        return mControlKey.isActive();
    }
}
