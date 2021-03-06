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

package de.t2h.tterm;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import de.t2h.tterm.compat.AndroidCompat;
import de.t2h.tterm.emulatorview.EmulatorView;
import de.t2h.tterm.emulatorview.TermSession;
import de.t2h.tterm.emulatorview.UpdateCallback;
import de.t2h.tterm.key.PKey;
import de.t2h.tterm.key.PKeyButton;
import de.t2h.tterm.key.PKeyRow;
import de.t2h.tterm.util.SessionList;
import de.t2h.tterm.util.TermSettings;

import java.io.IOException;
import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** A terminal emulator activity.
 */
public class Term extends Activity
    implements UpdateCallback, SharedPreferences.OnSharedPreferenceChangeListener
{
    // ************************************************************
    // Attributes
    // ************************************************************

    /** The ViewFlipper which holds the collection of EmulatorView widgets. */
    private TermViewFlipper mViewFlipper;
    private EmulatorView getCurrentEmulatorView () {
        return (EmulatorView) mViewFlipper.getCurrentView();
    }

    /** The name of the ViewFlipper in the resources. */
    private static final int VIEW_FLIPPER = R.id.view_flipper;

    // ------------------------------------------------------------
    // { Extra keys
    // ------------------------------------------------------------

    private static int mExtraKeysCount = 0;
    private PKeyButton[] mExtraKeyButtons;
    private PKey[] mExtraKeys;

    private int mExtraKeySize;

    // TODO Move to PKeyButton.
    public static int mExtraKeyDefaultColor;

    private int mExtraKeysShown;
    private LinearLayout mExtraKeysRow;

    // } ------------------------------------------------------------

    private SessionList mTermSessions;
    private TermSession getCurrentTermSession () {
        if(mTermSessions == null) return null;
        return mTermSessions.get(mViewFlipper.getDisplayedChild());
    }

    private TermSettings mSettings;

    // ------------------------------------------------------------
    // { Poppup menu
    // ------------------------------------------------------------

    private final static int SELECT_TEXT_ID = 0;
    private final static int COPY_ALL_ID = 1;
    private final static int PASTE_ID = 2;
    private final static int SEND_CONTROL_KEY_ID = 3;
    private final static int SEND_FN_KEY_ID = 4;

    // } ------------------------------------------------------------

    private boolean mAlreadyStarted = false;
    private boolean mStopServiceOnFinish = false;

    private Intent TermSserviceIntent;

    public static final int REQUEST_CHOOSE_WINDOW = 1;

    // TODO Should this be in `jackpal.androidterm´ or `de.t2h.tterm´?
    //   public static final String EXTRA_WINDOW_ID = "jackpal.androidterm.window_id";
    public static final String EXTRA_WINDOW_ID = "de.t2h.tterm.window_id";

    private int onResumeSelectWindow = -1;
    private ComponentName mPrivateAlias;

    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;

    // ------------------------------------------------------------
    // { Path broadcasts
    // ------------------------------------------------------------
    //
    // Do *not* rename these 4 strings to use `de.t2h.tterm´!

    private static final String ACTION_PATH_BROADCAST = "jackpal.androidterm.broadcast.APPEND_TO_PATH";
    private static final String ACTION_PATH_PREPEND_BROADCAST = "jackpal.androidterm.broadcast.PREPEND_TO_PATH";
    private static final String PERMISSION_PATH_BROADCAST = "jackpal.androidterm.permission.APPEND_TO_PATH";
    private static final String PERMISSION_PATH_PREPEND_BROADCAST = "jackpal.androidterm.permission.PREPEND_TO_PATH";

    private int mPendingPathBroadcasts = 0;
    private BroadcastReceiver mPathReceiver = new BroadcastReceiver() {
        public void onReceive (Context context, Intent intent) {
            String path = makePathFromBundle(getResultExtras(false));
            if(intent.getAction().equals(ACTION_PATH_PREPEND_BROADCAST)) {
                mSettings.setPrependPath(path);
            } else {
                mSettings.setAppendPath(path);
            }
            mPendingPathBroadcasts--;

            if(mPendingPathBroadcasts <= 0 && mTermService != null) {
                populateViewFlipper();
                populateWindowList();
            }
        }
    };

    // } ------------------------------------------------------------

    // ------------------------------------------------------------
    // { Term service
    // ------------------------------------------------------------

    private TermService mTermService;
    private ServiceConnection mTSConnection = new ServiceConnection() {
        public void onServiceConnected (ComponentName className, IBinder service) {
            Log.i(TermDebug.LOG_TAG, "Bound to TermService");
            TermService.TermServiceBinder binder = (TermService.TermServiceBinder) service;
            mTermService = binder.getService();
            if(mPendingPathBroadcasts <= 0) {
                populateViewFlipper();
                populateWindowList();
            }
        }

        public void onServiceDisconnected (ComponentName arg0) {
            mTermService = null;
        }
    };

    // } ------------------------------------------------------------

    private ActionBar mActionBar;

    private int mActionBarMode = TermSettings.ACTION_BAR_MODE_NONE;

    private boolean mHaveFullHwKeyboard = false;

    /** Should we use keyboard shortcuts? */
    private boolean mUseKeyboardShortcuts;

    private Handler mHandler = new Handler();

    // ------------------------------------------------------------
    // { Window list
    // ------------------------------------------------------------

    private WindowListAdapter mWinListAdapter;

    private class WindowListActionBarAdapter extends WindowListAdapter
        implements UpdateCallback
    {
        public WindowListActionBarAdapter (SessionList sessions) {
            super(sessions);
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            TextView label = new TextView(Term.this);
            String title = getSessionTitle(position, getString(R.string.window_title, position + 1));
            label.setText(title);
            label.setTextAppearance(Term.this, android.R.style.TextAppearance_Holo_Widget_ActionBar_Title);
            return label;
        }

        @Override
        public View getDropDownView (int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }

        public void onUpdate () {
            notifyDataSetChanged();
            mActionBar.setSelectedNavigationItem(mViewFlipper.getDisplayedChild());
        }
    }

    private ActionBar.OnNavigationListener mWinListItemSelected = new ActionBar.OnNavigationListener() {
        public boolean onNavigationItemSelected (int position, long id) {
            int oldPosition = mViewFlipper.getDisplayedChild();
            if(position != oldPosition) {
                if(position >= mViewFlipper.getChildCount()) {
                    mViewFlipper.addView(createEmulatorView(mTermSessions.get(position)));
                }
                mViewFlipper.setDisplayedChild(position);
                if(mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                    mActionBar.hide();
                }
            }
            return true;
        }
    };

    // } ------------------------------------------------------------

    // ------------------------------------------------------------
    // { Key listener
    // ------------------------------------------------------------

    /** Intercepts keys before the view/terminal gets it.
     *
     * <p>Handles:</p>
     *
     * <ol>
     *   <li>Back</li>
     *   <li>C-Tab, CS-Tab: next/previous console window</li>
     *   <li>CS-N: new console window</li>
     *   <li>CS-V: paste</li>
     * </ol>
     */
    private View.OnKeyListener mKeyListener = new View.OnKeyListener() {
        public boolean onKey (View v, int keyCode, KeyEvent event) {
            return backkeyInterceptor(keyCode, event) || keyboardShortcuts(keyCode, event);
        }

        /** Handle keyboard shortcuts.
         *
         * <p>See {@link Term#mKeyListener}.</p>
         */
        private boolean keyboardShortcuts (int keyCode, KeyEvent event) {
            if(event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if(! mUseKeyboardShortcuts)                   return false;

            boolean ctrl  = ( event.getMetaState() & KeyEvent.META_CTRL_ON  ) != 0;
            boolean shift = ( event.getMetaState() & KeyEvent.META_SHIFT_ON ) != 0;

            if(keyCode == KeyEvent.KEYCODE_TAB && ctrl && ! shift) { mViewFlipper.showNext();     return true; }
            if(keyCode == KeyEvent.KEYCODE_TAB && ctrl &&   shift) { mViewFlipper.showPrevious(); return true; }
            if(keyCode == KeyEvent.KEYCODE_N   && ctrl &&   shift) { doCreateNewWindow();         return true; }
            if(keyCode == KeyEvent.KEYCODE_V   && ctrl &&   shift) { doPaste();                   return true; }

            return false;
        }

        /** Make sure the back button always leaves the application. */
        private boolean backkeyInterceptor (int keyCode, KeyEvent event) {
            if(keyCode == KeyEvent.KEYCODE_BACK
                && mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES
                && mActionBar != null
                && mActionBar.isShowing()
            ) {
                // We need to intercept the key event before the view sees it, otherwise the view will handle
                // it before we get it.
                onKeyUp(keyCode, event);
                return true;
            } else {
                return false;
            }
        }
    };

    // } ------------------------------------------------------------

    // ************************************************************
    // Inner class EmulatorViewGestureListener
    // ************************************************************

    private class EmulatorViewGestureListener extends SimpleOnGestureListener {
        private EmulatorView view;

        public EmulatorViewGestureListener (EmulatorView view) {
            this.view = view;
        }

        @Override
        public boolean onSingleTapUp (MotionEvent e) {
            // Let the EmulatorView handle taps if mouse tracking is active.
            if(view.isMouseTrackingActive()) return false;

            // Check for link at tap location.
            String link = view.getURLat(e.getX(), e.getY());
            if(link != null) {
                execURL(link);
            } else {
                doUIToggle((int) e.getX(), (int) e.getY(), view.getVisibleWidth(), view.getVisibleHeight());
            }
            return true;
        }

        @Override
        public boolean onFling (MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float absVelocityX = Math.abs(velocityX);
            float absVelocityY = Math.abs(velocityY);
            if(absVelocityX > Math.max(1000.0f, 2.0 * absVelocityY)) {
                // Assume user wanted side to side movement.
                if(velocityX > 0) {
                    // Left to right swipe -- previous window.
                    mViewFlipper.showPrevious();
                } else {
                    // Right to left swipe -- next window
                    mViewFlipper.showNext();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    // ************************************************************
    // Methods
    // ************************************************************

    @Override
    public void onCreate (Bundle icicle) {
        super.onCreate(icicle);

        Log.v(TermDebug.LOG_TAG, "onCreate");

        mPrivateAlias = new ComponentName(this, RemoteInterface.PRIVACT_ACTIVITY_ALIAS);

        if(icicle == null)
            onNewIntent(getIntent());

        final SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSettings = new TermSettings(getResources(), mPrefs);
        mPrefs.registerOnSharedPreferenceChangeListener(this);

        Intent broadcast = new Intent(ACTION_PATH_BROADCAST);
        broadcast.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);

        mPendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, PERMISSION_PATH_BROADCAST, mPathReceiver, null, RESULT_OK, null, null);

        broadcast = new Intent(broadcast);
        broadcast.setAction(ACTION_PATH_PREPEND_BROADCAST);
        mPendingPathBroadcasts++;
        sendOrderedBroadcast(broadcast, PERMISSION_PATH_PREPEND_BROADCAST, mPathReceiver, null, RESULT_OK, null, null);

        TermSserviceIntent = new Intent(this, TermService.class);
        startService(TermSserviceIntent);

        int actionBarMode = mSettings.actionBarMode();
        mActionBarMode = actionBarMode;
        if(AndroidCompat.V11ToV20) {
            switch(actionBarMode) {
            case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
                setTheme(R.style.Theme_Holo);
                break;
            case TermSettings.ACTION_BAR_MODE_HIDES:
                setTheme(R.style.Theme_Holo_ActionBarOverlay);
                break;
            }
        }

        setContentView(R.layout.term_activity);
        mViewFlipper = (TermViewFlipper) findViewById(VIEW_FLIPPER);

        // Extra keys
        // ------------------------------------------------------------

        mExtraKeysRow = (PKeyRow) findViewById(R.id.extra_keys);

        PKeyButton.registerSendOnClick(view -> {
            EmulatorView emv = getCurrentEmulatorView();
            if(emv != null) { emv.sendKey(((PKeyButton) view).getModel().getKeyCode()); }
        });
        PKeyButton.registerWriteOnClick(view -> {
            EmulatorView emv = getCurrentEmulatorView();
            if(emv != null) { emv.getTermSession().write(((PKeyButton) view).getModel().getText()); }
        });
        PKeyButton.registerSpecialOnClick("Control", view -> doSendControlKey());
        PKeyButton.registerSpecialOnClick("Fn1", view -> doSendFnKey());
        PKeyButton.registerSpecialOnClick("CMenu", view -> doSendCMenuKey());

        setExtraKeys(mSettings.getExtraKeys());

        // ------------------------------------------------------------

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TermDebug.LOG_TAG);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        int wifiLockMode = WifiManager.WIFI_MODE_FULL;
        wifiLockMode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
        mWifiLock = wm.createWifiLock(wifiLockMode, TermDebug.LOG_TAG);

        ActionBar actionBar = getActionBar();
        if(actionBar != null) {
            mActionBar = actionBar;
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            if(mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                actionBar.hide();
            }
        }

        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(getResources().getConfiguration());

        updatePrefs();
        mAlreadyStarted = true;
    }

    private String makePathFromBundle (Bundle extras) {
        if(extras == null || extras.size() == 0) {
            return "";
        }

        String[] keys = new String[extras.size()];
        keys = extras.keySet().toArray(keys);
        Collator collator = Collator.getInstance(Locale.US);
        Arrays.sort(keys, collator);

        StringBuilder path = new StringBuilder();
        for(String key : keys) {
            String dir = extras.getString(key);
            if(dir != null && ! dir.equals("")) {
                path.append(dir);
                path.append(":");
            }
        }

        return path.substring(0, path.length() - 1);
    }

    @Override
    protected void onStart () {
        super.onStart();

        if(! bindService(TermSserviceIntent, mTSConnection, BIND_AUTO_CREATE)) {
            throw new IllegalStateException("Failed to bind to TermService!");
        }
    }

    private void populateViewFlipper () {
        if(mTermService != null) {
            mTermSessions = mTermService.getSessions();

            if(mTermSessions.size() == 0) {
                try {
                    mTermSessions.add(createTermSession());
                } catch(IOException e) {
                    Toast.makeText(this, "Failed to start terminal session", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
            }

            mTermSessions.addCallback(this);

            for(TermSession session : mTermSessions) {
                EmulatorView view = createEmulatorView(session);
                mViewFlipper.addView(view);
            }

            updatePrefs();

            if(onResumeSelectWindow >= 0) {
                mViewFlipper.setDisplayedChild(onResumeSelectWindow == Integer.MAX_VALUE
                    ? mViewFlipper.getChildCount() - 1
                    : onResumeSelectWindow);
                onResumeSelectWindow = -1;
            }
            mViewFlipper.onResume();
        }
    }

    private void populateWindowList () {
        if(mActionBar == null)    return;
        if(mTermSessions == null) return;

        int position = mViewFlipper.getDisplayedChild();
        if(mWinListAdapter == null) {
            mWinListAdapter = new WindowListActionBarAdapter(mTermSessions);
            mActionBar.setListNavigationCallbacks(mWinListAdapter, mWinListItemSelected);
        } else {
            mWinListAdapter.setSessions(mTermSessions);
        }
        mViewFlipper.addCallback(mWinListAdapter);

        mActionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onDestroy () {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this);

        if(mStopServiceOnFinish) {
            stopService(TermSserviceIntent);
        }
        mTermService = null;
        mTSConnection = null;
        if(mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if(mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    private void restart () {
        startActivity(getIntent());
        finish();
    }

    protected static TermSession createTermSession (Context context, TermSettings settings,
        String initialCommand
    ) throws IOException {
        GenericTermSession session = new ShellTermSession(settings, initialCommand);
        // XXX We should really be able to fetch this from within TermSession
        session.setProcessExitMessage(context.getString(R.string.process_exit_message));

        return session;
    }

    private TermSession createTermSession () throws IOException {
        TermSession session = createTermSession(this, mSettings, mSettings.getInitialCommand());
        session.setFinishCallback(mTermService);
        return session;
    }

    private TermView createEmulatorView (TermSession session) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        TermView emulatorView = new TermView(this, session, metrics);

        emulatorView.setExtGestureListener(new EmulatorViewGestureListener(emulatorView));
        emulatorView.setOnKeyListener(mKeyListener);
        registerForContextMenu(emulatorView);

        return emulatorView;
    }

    @Override
    public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String s) {
        mSettings.readPrefs(sharedPreferences);
    }

    private void updatePrefs () {
        mUseKeyboardShortcuts = mSettings.getUseKeyboardShortcutsFlag();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        mViewFlipper.updatePrefs(mSettings);

        for(View v : mViewFlipper) {
            ((EmulatorView) v).setDensity(metrics);
            ((TermView) v).updatePrefs(mSettings);
        }

        if(mTermSessions != null) {
            for(TermSession session : mTermSessions) {
                ((GenericTermSession) session).updatePrefs(mSettings);
            }
        }

        {
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            final int FULLSCREEN = WindowManager.LayoutParams.FLAG_FULLSCREEN;
            int desiredFlag = mSettings.showStatusBar() ? 0 : FULLSCREEN;
            if(desiredFlag != (params.flags & FULLSCREEN) || mActionBarMode != mSettings.actionBarMode()) {
                if(mAlreadyStarted) {
                    // Can't switch to/from fullscreen after
                    // starting the activity.
                    restart();
                } else {
                    win.setFlags(desiredFlag, FULLSCREEN);
                    if(mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
                        if(mActionBar != null) {
                            mActionBar.hide();
                        }
                    }
                }
            }
        }

        int orientation = mSettings.getScreenOrientation();
        int o = 0;
        if(orientation == 0) {
            o = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        } else if(orientation == 1) {
            o = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if(orientation == 2) {
            o = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            // Shouldn't happen.
        }
        setRequestedOrientation(o);
    }

    @Override
    public void onPause () {
        super.onPause();

        // Explicitly close the input method.
        //
        // Otherwise, the soft keyboard could cover up whatever activity takes our place
        //
        final IBinder token = mViewFlipper.getWindowToken();
        new Thread(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(token, 0);
        }).start();
    }

    @Override
    protected void onStop () {
        mViewFlipper.onPause();
        if(mTermSessions != null) {
            mTermSessions.removeCallback(this);

            if(mWinListAdapter != null) {
                mTermSessions.removeCallback(mWinListAdapter);
                mTermSessions.removeTitleChangedListener(mWinListAdapter);
                mViewFlipper.removeCallback(mWinListAdapter);
            }
        }

        mViewFlipper.removeAllViews();

        unbindService(mTSConnection);

        super.onStop();
    }

    private boolean checkHaveFullHwKeyboard (Configuration c) {
        return (c.keyboard == Configuration.KEYBOARD_QWERTY) &&
            (c.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);
    }

    @Override
    public void onConfigurationChanged (Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mHaveFullHwKeyboard = checkHaveFullHwKeyboard(newConfig);

        EmulatorView v = (EmulatorView) mViewFlipper.getCurrentView();
        if(v != null) {
            v.updateSize(false);
        }

        if(mWinListAdapter != null) {
            // Force Android to redraw the label in the navigation dropdown
            mWinListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.menu_new_window).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.menu_close_window).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.findItem(R.id.menu_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_preferences) {
            doPreferences();
        } else if(id == R.id.menu_new_window) {
            doCreateNewWindow();
        } else if(id == R.id.menu_close_window) {
            confirmCloseWindow();
        } else if(id == R.id.menu_settings) {
            doPreferences();
        } else if(id == R.id.menu_window_list) {
            startActivityForResult(new Intent(this, WindowList.class), REQUEST_CHOOSE_WINDOW);
        } else if(id == R.id.menu_reset) {
            doResetTerminal();
            Toast toast = Toast.makeText(this, R.string.reset_toast_notification,
                Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        } else if(id == R.id.menu_send_email) {
            doEmailTranscript();
        } else if(id == R.id.menu_special_keys) {
            doDocumentKeys();
        } else if(id == R.id.menu_toggle_soft_keyboard) {
            doToggleSoftKeyboard();
        } else if(id == R.id.menu_toggle_wakelock) {
            doToggleWakeLock();
        } else if(id == R.id.menu_toggle_wifilock) {
            doToggleWifiLock();
        } else if(id == R.id.action_help) {
            Intent helpIntent = new Intent(this, HelpActivity.class);
            startActivity(helpIntent);
        }
        // Hide the action bar if appropriate.
        if(mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES) {
            mActionBar.hide();
        }
        return super.onOptionsItemSelected(item);
    }

    private void doCreateNewWindow () {
        if(mTermSessions == null) {
            Log.w(TermDebug.LOG_TAG, "Couldn't create new window because mTermSessions == null");
            return;
        }

        try {
            TermSession session = createTermSession();

            mTermSessions.add(session);

            TermView view = createEmulatorView(session);
            view.updatePrefs(mSettings);

            mViewFlipper.addView(view);
            mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount() - 1);
        } catch(IOException e) {
            Toast.makeText(this, "Failed to create a session", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmCloseWindow () {
        final AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.confirm_window_close_message);
        final Runnable closeWindow = () -> { doCloseWindow(); };
        b.setPositiveButton(android.R.string.yes,
            (DialogInterface dialog, int id) -> {
                dialog.dismiss();
                mHandler.post(closeWindow);
        });
        b.setNegativeButton(android.R.string.no, null);
        b.show();
    }

    private void doCloseWindow () {
        if(mTermSessions == null) return;

        EmulatorView view = getCurrentEmulatorView();
        if(view == null) return;

        TermSession session = mTermSessions.remove(mViewFlipper.getDisplayedChild());
        view.onPause();
        session.finish();
        mViewFlipper.removeView(view);
        if(mTermSessions.size() != 0) {
            mViewFlipper.showNext();
        }
    }

    @Override
    protected void onActivityResult (int request, int result, Intent data) {
        if(request == REQUEST_CHOOSE_WINDOW) {
            if(result == RESULT_OK && data != null) {
                int position = data.getIntExtra(EXTRA_WINDOW_ID, -2);
                if(position >= 0) {
                    // Switch windows after session list is in sync, not here
                    onResumeSelectWindow = position;
                } else if(position == -1) {
                    doCreateNewWindow();
                    onResumeSelectWindow = mTermSessions.size() - 1;
                }
            } else {
                // Close the activity if user closed all sessions
                // TODO the left path will be invoked when nothing happened, but this Activity was destroyed!
                if(mTermSessions == null || mTermSessions.size() == 0) {
                    mStopServiceOnFinish = true;
                    finish();
                }
            }
        }
    }

    @Override
    protected void onNewIntent (Intent intent) {
        if((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
            // Don't repeat action if intent comes from history
            return;
        }

        String action = intent.getAction();
        if(TextUtils.isEmpty(action) || ! mPrivateAlias.equals(intent.getComponent())) {
            return;
        }

        // huge number simply opens new window
        // TODO: add a way to restrict max number of windows per caller (possibly via reusing BoundSession)
        switch(action) {
        case RemoteInterface.PRIVACT_OPEN_NEW_WINDOW:
            onResumeSelectWindow = Integer.MAX_VALUE;
            // Sometimes Term.mTSConnection.onServiceConnected gets called *before* `onResumeSelectWindow´.
            // As a quick fix, set displayed child again.
            if(mViewFlipper != null && mViewFlipper.getChildCount() > 0) {
                mViewFlipper.setDisplayedChild(mViewFlipper.getChildCount() - 1);
            }
            break;
        case RemoteInterface.PRIVACT_SWITCH_WINDOW:
            int target = intent.getIntExtra(RemoteInterface.PRIVEXTRA_TARGET_WINDOW, -1);
            if(target >= 0) {
                onResumeSelectWindow = target;
            }
            break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        MenuItem wakeLockItem = menu.findItem(R.id.menu_toggle_wakelock);
        MenuItem wifiLockItem = menu.findItem(R.id.menu_toggle_wifilock);
        wakeLockItem.setTitle(mWakeLock.isHeld() ? R.string.disable_wakelock : R.string.enable_wakelock);
        wifiLockItem.setTitle(mWifiLock.isHeld() ? R.string.disable_wifilock : R.string.enable_wifilock);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle(R.string.edit_text);
        menu.add(0, SELECT_TEXT_ID      , 0, R.string.select_text);
        menu.add(0, COPY_ALL_ID         , 0, R.string.copy_all);
        menu.add(0, PASTE_ID            , 0, R.string.paste);
        menu.add(0, SEND_CONTROL_KEY_ID , 0, R.string.send_control_key);
        menu.add(0, SEND_FN_KEY_ID      , 0, R.string.send_fn_key);
        if(! canPaste()) {
            menu.getItem(PASTE_ID).setEnabled(false);
        }
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        switch(item.getItemId()) {
        case SELECT_TEXT_ID:      getCurrentEmulatorView().toggleSelectingText(); return true;
        case COPY_ALL_ID:         doCopyAll();                                    return true;
        case PASTE_ID:            doPaste();                                      return true;
        case SEND_CONTROL_KEY_ID: doSendControlKey();                             return true;
        case SEND_FN_KEY_ID:      doSendFnKey();                                  return true;
        default:                  return super.onContextItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp (int keyCode, KeyEvent event) {
        switch(keyCode) {
        case KeyEvent.KEYCODE_BACK:
            if(mActionBarMode == TermSettings.ACTION_BAR_MODE_HIDES
                && mActionBar != null
                && mActionBar.isShowing()
             ) {
                mActionBar.hide();
                return true;
            }
            switch(mSettings.getBackKeyAction()) {
            case TermSettings.BACK_KEY_STOPS_SERVICE:
                mStopServiceOnFinish = true;
                // Fall through
            case TermSettings.BACK_KEY_CLOSES_ACTIVITY:
                finish();
                return true;
            case TermSettings.BACK_KEY_CLOSES_WINDOW:
                doCloseWindow();
                return true;
            default:
                return false;
            }
        case KeyEvent.KEYCODE_MENU:
            if(mActionBar != null && ! mActionBar.isShowing()) {
                mActionBar.show();
                return true;
            } else {
                return super.onKeyUp(keyCode, event);
            }
        default:
            return super.onKeyUp(keyCode, event);
        }
    }

    // Called when the list of sessions changes
    public void onUpdate () {
        SessionList sessions = mTermSessions;
        if(sessions == null) return;

        if(sessions.size() == 0) {
            mStopServiceOnFinish = true;
            finish();
        } else if(sessions.size() < mViewFlipper.getChildCount()) {
            for(int i = 0; i < mViewFlipper.getChildCount(); ++i) {
                EmulatorView v = (EmulatorView) mViewFlipper.getChildAt(i);
                if(! sessions.contains(v.getTermSession())) {
                    v.onPause();
                    mViewFlipper.removeView(v);
                    --i;
                }
            }
        }
    }

    private boolean canPaste () {
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        return clip.hasText();
    }

    private void doPreferences () {
        startActivity(new Intent(this, TermPreferences.class));
    }

    private void doResetTerminal () {
        TermSession session = getCurrentTermSession();
        if(session != null) {
            session.reset();
        }
    }

    private void doEmailTranscript () {
        TermSession session = getCurrentTermSession();
        if(session == null) return;

        // Don't really want to supply an address, but
        // currently it's required, otherwise nobody
        // wants to handle the intent.
        String addr = "user@example.com";
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + addr));

        String subject = getString(R.string.email_transcript_subject);
        String title = session.getTitle();
        if(title != null) {
            subject = subject + " - " + title;
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, session.getTranscriptText().trim());
        try {
            startActivity(Intent.createChooser(intent,
                getString(R.string.email_transcript_chooser_title)));
        } catch(ActivityNotFoundException e) {
            Toast.makeText(this,
                R.string.email_transcript_no_email_activity_found,
                Toast.LENGTH_LONG).show();
        }
    }

    private void doCopyAll () {
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clip.setText(getCurrentTermSession().getTranscriptText().trim());
    }

    private void doPaste () {
        if(! canPaste()) {
            return;
        }
        ClipboardManager clip = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        CharSequence paste = clip.getText();
        getCurrentTermSession().write(paste.toString());
    }

    private void doSendCMenuKey () {
        EmulatorView emv = getCurrentEmulatorView();
        if(emv != null) { emv.showContextMenu(); }
    }

    private void doSendControlKey () {
        EmulatorView emv = getCurrentEmulatorView();
        if(emv != null) { emv.sendControlKey(); }
    }

    private void doSendFnKey () {
        EmulatorView emv = getCurrentEmulatorView();
        if(emv != null) { emv.sendFnKey(); }
    }

    private void doDocumentKeys () {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        Resources r = getResources();
        dialog.setTitle(r.getString(R.string.control_key_dialog_title));
       StringBuffer msg = new StringBuffer();

        msg.append(r.getString(R.string.control_key_dialog_control_text))
            .append("\n");
        formatMessage(msg, R.string.control_key_dialog_control_mapping,
            "CTRLKEY", mSettings.getControlKeyId(), TermSettings.CONTROL_KEY_ID_NONE);

        msg.append(r.getString(R.string.control_key_dialog_fn_text))
            .append("\n");
        formatMessage(msg, R.string.control_key_dialog_fn_mapping,
            "FNKEY", mSettings.getFnKeyId(), TermSettings.FN_KEY_ID_NONE);

        dialog.setMessage(msg.toString());
        dialog.show();
    }

    /** Document mapping of Ctrl or Fn.
     *
     * <p>If Ctrl or Fn is mapped to a key, add a sentence to @code{msg}, e.g.:</p>
     *
     * <blockquote>Ctrl is also mapped to Vol-Down.</blockquote>
     */
    private void formatMessage (
        StringBuffer msg, int textId,
        String regex, int keyId, int disabledKeyId
    ) {
        Resources r = getResources();
        if(keyId != disabledKeyId) {
            String keyname = r.getStringArray(R.array.entries_controlkey_preference)[keyId];
            msg.append(r.getString(textId).replaceAll(regex, keyname))
                .append("\n");
        }
    }

    private void doToggleSoftKeyboard () {
        // ------------------------------------------------------------
        // Also toggle the extra keys
        // ------------------------------------------------------------
        //
        // This method handles toggling the soft keyboard from the menu and by tapping on the screen.
        //
        // TODO ThH: This method does not handle switching off the soft keyboard with the "hide IME" button
        // that Android 4.4 showns in the navigation bar instead of the "back button".
        //
        // Even though I toggle the soft keyboard before the extra keys, my Nexus 7 toggles the extra keys
        // first. When toggling off, it hides the extra keys first, then the soft keyboard. When toggling on,
        // it adds the extra keys at the bottom of the screen, then it adds the soft keyboard. I don't know
        // how to toggle them at the same time yet.

        // (1) If the soft keyboard is being shown at the beginning of this method, we have to toggle it off,
        // and vice versa.
        boolean show = ! isSoftKeyboardShown();

        // (2) Toggle the soft keyboard.

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        setExtraKeysShown(mExtraKeysShown, show);
    }

    private void doToggleWakeLock () {
        if(mWakeLock.isHeld()) { mWakeLock.release(); } else { mWakeLock.acquire(); }
        invalidateOptionsMenu();
    }

    private void doToggleWifiLock () {
        if(mWifiLock.isHeld()) { mWifiLock.release(); } else { mWifiLock.acquire(); }
        invalidateOptionsMenu();
    }

    private void doToggleActionBar () {
        ActionBar bar = mActionBar;
        if(bar == null) return;
        if(bar.isShowing()) { bar.hide(); } else { bar.show(); }
    }

    private void doUIToggle (int x, int y, int width, int height) {
        switch(mActionBarMode) {
        case TermSettings.ACTION_BAR_MODE_NONE:
            if(mHaveFullHwKeyboard || y < height / 2) {
                openOptionsMenu();
                return;
            } else {
                doToggleSoftKeyboard();
            }
            break;
        case TermSettings.ACTION_BAR_MODE_ALWAYS_VISIBLE:
            if(! mHaveFullHwKeyboard) {
                doToggleSoftKeyboard();
            }
            break;
        case TermSettings.ACTION_BAR_MODE_HIDES:
            if(mHaveFullHwKeyboard || y < height / 2) {
                doToggleActionBar();
                return;
            } else {
                doToggleSoftKeyboard();
            }
            break;
        }
        getCurrentEmulatorView().requestFocus();
    }

    /** Send a URL up to Android to be handled by a browser.
     *
     * @param link The URL to be opened.
     */
    private void execURL (String link) {
        Uri webLink = Uri.parse(link);
        Intent openLink = new Intent(Intent.ACTION_VIEW, webLink);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> handlers = pm.queryIntentActivities(openLink, 0);
        if(handlers.size() > 0)
            startActivity(openLink);
    }

    // ------------------------------------------------------------
    // Extra keys
    // ------------------------------------------------------------

    public void setExtraKeys (String keyString) {
        mExtraKeys = PKey.parse(keyString);
        mExtraKeysCount = mExtraKeys.length;
        mExtraKeyButtons = new PKeyButton[mExtraKeysCount];

        mExtraKeysRow.removeAllViews();

        LayoutInflater infl = getLayoutInflater();

        for(int i = 0; i < mExtraKeysCount; ++i) {
            PKeyButton button = (PKeyButton) infl.inflate(R.layout.extra_key, mExtraKeysRow,
                false); // Pass `false´ so that `inflater´ returns the button, not the row.
            button.setModel(mExtraKeys[i]);

            mExtraKeysRow.addView(button);
            mExtraKeyButtons[i] = button;
        }

        mExtraKeyDefaultColor = mExtraKeyButtons[0].getTextColors().getDefaultColor();
    }

    public void setExtraKeysShown (int when) {
      setExtraKeysShown(when, isSoftKeyboardShown());
    }

    public void setExtraKeysShown (int when, boolean softKeyboardShown) {
      mExtraKeysShown = when;
      if(mExtraKeysShown == 0) {
        mExtraKeysRow.setVisibility(View.GONE);
      } else if(mExtraKeysShown == 1) {
        mExtraKeysRow.setVisibility(View.VISIBLE);
      } else {
        mExtraKeysRow.setVisibility(softKeyboardShown ? View.VISIBLE : View.GONE);
      }
    }

    public void setExtraKeySize (int size) {
      mExtraKeySize = size;

      mExtraKeyButtons[0].setTextSize(size);

      // Due to a bug in Android we can't use android:layout_height="wrap_content" here, since the buttons
      // don't shrink when we reduce the text size. So calculate height and width by hand.
      //
      // We can't use `Button.setHeight´ because the parent LinearLayout recalculates that.
      //
      // TODO ThH: Since I don't understand layout metrics fully yet, the formulas are just a guess.
      float density = getResources().getDisplayMetrics().density;
      int height = (int) (3* mExtraKeyButtons[0].getTextSize() + 10*density);
      int width = (int) (3* mExtraKeyButtons[0].getTextSize() + 15*density);
      LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(width, height);

      mExtraKeyButtons[0].setLayoutParams(layoutparams);

      for(int i = 1; i < mExtraKeysCount; ++i) {
        mExtraKeyButtons[i].setTextSize(size);
        mExtraKeyButtons[i].setLayoutParams(layoutparams);
      }
    }

    /** Return the height off the keys row. */
    public int getKeysHeight () {
      if(mExtraKeysRow == null || mExtraKeySize == 0 || mExtraKeysRow.getVisibility() == View.GONE) return 0;
      return mExtraKeysRow.getHeight();
    }

    // Android wont't tell you if the soft keyboard is being shown, so try to guess based on the height
    // of `top_view´ (which is the root in the XML layout file, but not the root view returned by
    // `View.getRootView´.
    public boolean isSoftKeyboardShown () {
      // The height of the `top_view´ changes when you toggle the soft keyboard, on my Nexus 7 in portrait
      // mode between 1058 and 1662.
      View topView = findViewById(R.id.top_view);
      if(topView == null) return true;
      float topHeight = topView.getHeight();
      // The height of the root view does not change when you toggle the IME, on my Nexus 7 in portrait mode
      // it's 1824.
      float windowHeight = mViewFlipper.getRootView().getHeight();
      boolean res = topHeight / windowHeight < 0.75;
      return res;
    }

    // ------------------------------------------------------------
}
