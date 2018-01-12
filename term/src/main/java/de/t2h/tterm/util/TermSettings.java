/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * T+ With modifications (C) 2014 Thorbjørn Hansen.
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

package de.t2h.tterm.util;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.StringRes;

import de.t2h.tterm.R;

import static android.view.KeyEvent.*;

/** Terminal emulator settings.
 */
public class TermSettings {

   private SharedPreferences mPrefs;

    // ------------------------------------------------------------

    // Section "Screen"

    private int mStatusBar;             public boolean showStatusBar ()        { return (mStatusBar != 0); }
    private int mActionBarMode;         public int actionBarMode ()            { return mActionBarMode; }
    private int mOrientation;           public int getScreenOrientation ()     { return mOrientation; }

    // Section "Text"

    private int mFontSize;              public int getFontSize ()              { return mFontSize; }
    private int mColorId;               public int[] getColorScheme ()         { return COLOR_SCHEMES[mColorId]; }
    private boolean mUTF8ByDefault;     public boolean defaultToUTF8Mode ()    { return mUTF8ByDefault; }

    // Section "Extra ekys"

    private String mExtraKeys;          public String getExtraKeys()           { return mExtraKeys; }
    private int mExtraKeySize;          public int getExtraKeySize ()          { return mExtraKeySize; }
    private int mExtraKeysShown;        public int getExtraKeysShown ()        { return mExtraKeysShown; }

    // Section "Keyboard"

    private int mBackKeyAction;         public int getBackKeyAction ()         { return mBackKeyAction; }
                                        public int getBackKeyCharacter ()      {
                                          switch (mBackKeyAction) {
                                            case BACK_KEY_SENDS_ESC:             return 27;
                                            case BACK_KEY_SENDS_TAB:             return 9;
                                            default:                             return 0;
                                          }
                                        }
    private int mControlKeyId;          public int getControlKeyId ()          { return mControlKeyId; }
                                        public int getControlKeyCode ()        { return CONTROL_KEY_SCHEMES[
                                                                                            mControlKeyId]; }
    private int mFnKeyId;               public int getFnKeyId ()               { return mFnKeyId; }
                                        public int getFnKeyCode ()             { return FN_KEY_SCHEMES[mFnKeyId]; }
    private int mUseCookedIME;          public boolean useCookedIME ()         { return (mUseCookedIME != 0); }
    private boolean mAltSendsEsc;       public boolean getAltSendsEsc()        { return mAltSendsEsc; }

    private boolean mUseKeyboardShortcuts;
    public boolean getUseKeyboardShortcutsFlag () { return mUseKeyboardShortcuts; }

    // Section "Shell"

    private String mFailsafeShell;      public String getFailsafeShell ()      { return mFailsafeShell; }
    private String mShell;              public String getShell ()              { return mShell; }
    private String mInitialCommand;     public String getInitialCommand ()     { return mInitialCommand; }
    private String mTermType;           public String getTermType ()           { return mTermType; }
    private boolean mMouseTracking;     public boolean getMouseTrackingFlag () { return mMouseTracking; }
    private boolean mCloseOnExit;       public boolean closeOnProcessExit ()   { return mCloseOnExit; }
    private boolean mVerifyPath;        public boolean verifyPath ()           { return mVerifyPath; }
    private boolean mDoPathExtensions;  public boolean doPathExtensions ()     { return mDoPathExtensions; }
    private boolean mAllowPathPrepend;  public boolean allowPathPrepend ()     { return mAllowPathPrepend; }
    private String mHomePath;           public String getHomePath ()           { return mHomePath; }

    private String mPrependPath = null;
    public String getPrependPath() { return mPrependPath; }
    public void setPrependPath(String prependPath) { mPrependPath = prependPath; }

    private String mAppendPath = null;
    public String getAppendPath() { return mAppendPath; }
    public void setAppendPath(String appendPath) { mAppendPath = appendPath; }

    // ------------------------------------------------------------
    //
    // TODO These keys must correspond with `res/xml/preferences.xml´. Is there a way to guarantee it?

    private static final String

        // Section "Screen"

        STATUSBAR_KEY          = "statusbar",
        ACTIONBAR_KEY          = "actionbar",
        ORIENTATION_KEY        = "orientation",

        // Section "Text"

        FONTSIZE_KEY           = "fontsize",
        COLOR_KEY              = "color",
        UTF8_KEY               = "utf8_by_default",

        // Section "Extra keys"

        EXTRAKEYS_STRING_KEY   = "extrakeys_string", // We can't use `extrakeys´ here, up to 2017-12-28 I used
                                                     // that for `extrakeys_show´.
        EXTRAKEY_SIZE_KEY      = "extrakeys_size",
        EXTRAKEYS_SHOWN_KEY    = "extrakeys_shown",

        // Section "Keyboard"

        BACKACTION_KEY         = "backaction",
        CONTROLKEY_KEY         = "controlkey",
        FNKEY_KEY              = "fnkey",
        IME_KEY                = "ime",
        ALT_SENDS_ESC          = "alt_sends_esc",
        USE_KEYBOARD_SHORTCUTS = "use_keyboard_shortcuts",

        // Section "Shell"

        SHELL_KEY              = "shell",
        INITIALCOMMAND_KEY     = "initialcommand",
        TERMTYPE_KEY           = "termtype",
        MOUSE_TRACKING         = "mouse_tracking",
        CLOSEONEXIT_KEY        = "close_window_on_process_exit",
        VERIFYPATH_KEY         = "verify_path",
        PATHEXTENSIONS_KEY     = "do_path_extensions",
        PATHPREPEND_KEY        = "allow_prepend_path",
        HOMEPATH_KEY           = "home_path";

    // ------------------------------------------------------------

    public static final int
        WHITE               = 0xffffffff,
        BLACK               = 0xff000000,
        BLUE                = 0xff344ebd,
        GREEN               = 0xff00ff00,
        AMBER               = 0xffffb651,
        RED                 = 0xffff0113,
        HOLO_BLUE           = 0xff33b5e5,
        SOLARIZED_FG        = 0xff657b83,
        SOLARIZED_BG        = 0xfffdf6e3,
        SOLARIZED_DARK_FG   = 0xff839496,
        SOLARIZED_DARK_BG   = 0xff002b36,
        LINUX_CONSOLE_WHITE = 0xffaaaaaa;

    // foreground color, background color
    public static final int[][] COLOR_SCHEMES = {
        { BLACK,               WHITE             },
        { WHITE,               BLACK             },
        { WHITE,               BLUE              },
        { GREEN,               BLACK             },
        { AMBER,               BLACK             },
        { RED,                 BLACK             },
        { HOLO_BLUE,           BLACK             },
        { SOLARIZED_FG,        SOLARIZED_BG      },
        { SOLARIZED_DARK_FG,   SOLARIZED_DARK_BG },
        { LINUX_CONSOLE_WHITE, BLACK             }
    };

    // Section "Screen"

    public  static final int
        ACTION_BAR_MODE_NONE           = 0,
        ACTION_BAR_MODE_ALWAYS_VISIBLE = 1,
        ACTION_BAR_MODE_HIDES          = 2,
        ACTION_BAR_MODE_MAX            = 2,

        ORIENTATION_UNSPECIFIED        = 0,
        ORIENTATION_LANDSCAPE          = 1,
        ORIENTATION_PORTRAIT           = 2;

    // Section "Text"

    /** An integer not in the range of real key codes. */
    public static final int my_KEYCODE_NONE = -1;

    public static final int CONTROL_KEY_ID_NONE = 7;

    public static final int[] CONTROL_KEY_SCHEMES = {
        KEYCODE_DPAD_CENTER,
        KEYCODE_AT,
        KEYCODE_ALT_LEFT,
        KEYCODE_ALT_RIGHT,
        KEYCODE_VOLUME_UP,
        KEYCODE_VOLUME_DOWN,
        KEYCODE_CAMERA,
        my_KEYCODE_NONE
    };

    public static final int FN_KEY_ID_NONE = 7;

    public static final int[] FN_KEY_SCHEMES = {
        KEYCODE_DPAD_CENTER,
        KEYCODE_AT,
        KEYCODE_ALT_LEFT,
        KEYCODE_ALT_RIGHT,
        KEYCODE_VOLUME_UP,
        KEYCODE_VOLUME_DOWN,
        KEYCODE_CAMERA,
        my_KEYCODE_NONE
    };

    public static final int
        BACK_KEY_STOPS_SERVICE   = 0,
        BACK_KEY_CLOSES_WINDOW   = 1,
        BACK_KEY_CLOSES_ACTIVITY = 2,
        BACK_KEY_SENDS_ESC       = 3,
        BACK_KEY_SENDS_TAB       = 4,
        BACK_KEY_MAX             = 4;

    // ************************************************************
    // Methods
    // ************************************************************

    public TermSettings(Resources res, SharedPreferences prefs) {
        readDefaultPrefs(res);
        readPrefs(prefs);
    }

    // ------------------------------------------------------------

    private void readDefaultPrefs(Resources res) {
        // Section "Screen"

        mStatusBar            = str2int(res,    R.string.pref_statusbar_default                  );
        mActionBarMode        = res.getInteger( R.integer.pref_actionbar_default                 );
        mOrientation          = res.getInteger( R.integer.pref_orientation_default               );

        // Section "Text"

        mFontSize             = str2int(res,    R.string.pref_fontsize_default                   );
        mColorId              = str2int(res,    R.string.pref_color_default                      );
        mUTF8ByDefault        = res.getBoolean( R.bool.pref_utf8_by_default_default              );

        // Section "Extra keys"

        mExtraKeys            = res.getString(  R.string.pref_extrakeys_string_default           );
        mExtraKeySize         = str2int(res,    R.string.pref_extrakeys_size_default             );
        mExtraKeysShown       = str2int(res,    R.string.pref_extrakeys_shown_default            );

        // Section "Keyboard"

        mBackKeyAction        = str2int(res,    R.string.pref_backaction_default                 );
        mControlKeyId         = str2int(res,    R.string.pref_controlkey_default                 );
        mFnKeyId              = str2int(res,    R.string.pref_fnkey_default                      );
        mUseCookedIME         = str2int(res,    R.string.pref_ime_default                        );
        mAltSendsEsc          = res.getBoolean( R.bool.pref_alt_sends_esc_default                );
        mUseKeyboardShortcuts = res.getBoolean( R.bool.pref_use_keyboard_shortcuts_default       );

        // Section "Shell"

        mFailsafeShell        = res.getString(  R.string.pref_shell_default                      );
        mShell                = mFailsafeShell;
        mInitialCommand       = res.getString(  R.string.pref_initialcommand_default             );
        mTermType             = res.getString(  R.string.pref_termtype_default                   );
        mMouseTracking        = res.getBoolean( R.bool.pref_mouse_tracking_default               );
        mCloseOnExit          = res.getBoolean( R.bool.pref_close_window_on_process_exit_default );
        mVerifyPath           = res.getBoolean( R.bool.pref_verify_path_default                  );
        mDoPathExtensions     = res.getBoolean( R.bool.pref_do_path_extensions_default           );
        mAllowPathPrepend     = res.getBoolean( R.bool.pref_allow_prepend_path_default           );

        // The mHomePath default is set dynamically in `readPrefs´.
    }

    private int str2int(Resources res, @StringRes int id) { return Integer.parseInt(res.getString(id)); }

    // ------------------------------------------------------------

    public void readPrefs(SharedPreferences prefs) {
        mPrefs = prefs;

        // Section "Screen"

        mStatusBar            = anInt( STATUSBAR_KEY            , mStatusBar      , 1                              );
        mActionBarMode        = anInt( ACTIONBAR_KEY            , mActionBarMode  , ACTION_BAR_MODE_MAX            );
        mOrientation          = anInt( ORIENTATION_KEY          , mOrientation    , 2                              );

        // Section "Text"

        mFontSize             = anInt(   FONTSIZE_KEY           , mFontSize       , 288                            );
        mColorId              = anInt(   COLOR_KEY              , mColorId        , COLOR_SCHEMES.length - 1       );
        mUTF8ByDefault        = aBool(   UTF8_KEY               , mUTF8ByDefault                                   );

        // Section "Extra keys"

        mExtraKeys            = aString( EXTRAKEYS_STRING_KEY   , mExtraKeys                                       );
        mExtraKeySize         = anInt(   EXTRAKEY_SIZE_KEY      , mExtraKeySize   , 36                             );
        mExtraKeysShown       = anInt(   EXTRAKEYS_SHOWN_KEY    , mExtraKeysShown , 2                              );

        // Section "Keyboard"

        mBackKeyAction        = anInt(   BACKACTION_KEY         , mBackKeyAction  , BACK_KEY_MAX                   );
        mControlKeyId         = anInt(   CONTROLKEY_KEY         , mControlKeyId   , CONTROL_KEY_SCHEMES.length - 1 );
        mFnKeyId              = anInt(   FNKEY_KEY              , mFnKeyId        , FN_KEY_SCHEMES.length - 1      );
        mUseCookedIME         = anInt(   IME_KEY                , mUseCookedIME   , 1                              );
        mAltSendsEsc          = aBool(   ALT_SENDS_ESC          , mAltSendsEsc                                     );
        mUseKeyboardShortcuts = aBool(   USE_KEYBOARD_SHORTCUTS , mUseKeyboardShortcuts                            );

        mShell                = aString( SHELL_KEY              , mShell                                           );
        mInitialCommand       = aString( INITIALCOMMAND_KEY     , mInitialCommand                                  );
        mTermType             = aString( TERMTYPE_KEY           , mTermType                                        );
        mMouseTracking        = aBool(   MOUSE_TRACKING         , mMouseTracking                                   );
        mCloseOnExit          = aBool(   CLOSEONEXIT_KEY        , mCloseOnExit                                     );
        mVerifyPath           = aBool(   VERIFYPATH_KEY         , mVerifyPath                                      );
        mDoPathExtensions     = aBool(   PATHEXTENSIONS_KEY     , mDoPathExtensions                                );
        mAllowPathPrepend     = aBool(   PATHPREPEND_KEY        , mAllowPathPrepend                                );
        mHomePath             = aString( HOMEPATH_KEY           , mHomePath                                        );

        mPrefs                = null;  // we leak a Context if we hold on to this
    }

    private boolean aBool (String key, boolean defaultValue) { return mPrefs.getBoolean(key, defaultValue); }

    private int anInt(String key, int defaultValue, int maxValue) {
        int val;
        try {
            val = Integer.parseInt(
                mPrefs.getString(key, Integer.toString(defaultValue)));
        } catch (NumberFormatException e) {
            val = defaultValue;
        }
        val = Math.max(0, Math.min(val, maxValue));
        return val;
    }

    private String aString (String key, String defaultValue) { return mPrefs.getString(key, defaultValue); }
}
