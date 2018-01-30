/*
 * Copyright (C) 2007 The Android Open Source Project
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

import java.io.*;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import de.t2h.tterm.emulatorview.ColorScheme;
import de.t2h.tterm.emulatorview.TermSession;
import de.t2h.tterm.emulatorview.UpdateCallback;
import de.t2h.util.text.Text;

/** A terminal session.
 *
 * <p>Consists of a TerminalEmulator, a TranscriptScreen, and the I/O streams used to talk to the process.</p>
 */
class GenericTermSession extends TermSession {
    // ************************************************************
    // Attributes
    // ************************************************************

    /** Set to true to force into 80 x 24 for testing with vttest. */
    private static final boolean VTTEST_MODE = false;

    private final long createdAt;

    /** A cookie which uniquely identifies this session. */
    private String mHandle;
    public String getHandle () {
        return mHandle;
    }
    public void setHandle (String handle) {
        if(mHandle != null) throw new IllegalStateException("Cannot change handle once set.");
        mHandle = handle;
    }

    final ParcelFileDescriptor mTermFd;

    TermSettings mSettings;

    private String mProcessExitMessage;
    // TODO We should really get this ourselves from the resource bundle, but we cannot hold a context.
    public void setProcessExitMessage (String message) {
        mProcessExitMessage = message;
    }

    private UpdateCallback mUTF8ModeNotify = () -> { setPtyUTF8Mode(getUTF8Mode()); };

    // ************************************************************
    // Methods
    // ************************************************************

    GenericTermSession (ParcelFileDescriptor mTermFd, TermSettings settings, boolean exitOnEOF) {
        super(exitOnEOF);

        this.mTermFd = mTermFd;
        this.createdAt = System.currentTimeMillis();

        updatePrefs(settings);
    }

    public void updatePrefs (TermSettings settings) {
        mSettings = settings;
        setColorScheme(new ColorScheme(settings.getColorScheme()));
        setDefaultUTF8Mode(settings.defaultToUTF8Mode());
    }

    @Override
    public void initializeEmulator (int columns, int rows) {
        if(VTTEST_MODE) {
            columns = 80;
            rows = 24;
        }
        super.initializeEmulator(columns, rows);

        setPtyUTF8Mode(getUTF8Mode());
        setUTF8ModeUpdateCallback(mUTF8ModeNotify);
    }

    @Override
    public void updateSize (int columns, int rows) {
        if(VTTEST_MODE) {
            columns = 80;
            rows = 24;
        }
        // Inform the attached pty of our new size.
        setPtyWindowSize(rows, columns, 0, 0);
        super.updateSize(columns, rows);
    }

    @Override
    protected void onProcessExit () {
        if(mSettings.closeOnProcessExit()) {
            finish();
        } else if(mProcessExitMessage != null) {
            try {
                // Press Enter to close finished session.
                mIsExiting = true;

                byte[] msg = ("\r\n[" + mProcessExitMessage + "]").getBytes("UTF-8");
                appendToEmulator(msg, 0, msg.length);
                notifyUpdate();
            } catch(UnsupportedEncodingException e) {
                // Never happens
            }
        }
    }

    @Override
    public void finish () {
        try {
            mTermFd.close();
        } catch(IOException e) {
            // ok
        }

        super.finish();
    }

    /** Gets the terminal session's title.
     *
     * <p>Unlike the superclass's getTitle(), if the title is null or an empty string, the provided default
     * title will be returned instead.</p>
     *
     * @param defaultTitle The default title to use if this session's title is unset or an empty string.
     */
    public String getTitle (String defaultTitle) {
        String title = getTitle();
        if(Text.isSet(title)) {
            return title;
        } else {
            return defaultTitle;
        }
    }

    @Override
    public String toString () {
        return getClass().getSimpleName() + '(' + createdAt + ',' + mHandle + ')';
    }

    /** Set the window size for a given PTY.
     *
     * <p>Allows programs connected to the PTY to learn how large their screen is.</p>
     */
    void setPtyWindowSize (int row, int col, int xpixel, int ypixel) {
        // If the TTY goes away too quickly, this may get called after it's descriptor is closed
        if(! mTermFd.getFileDescriptor().valid())
            return;

        try {
            Exec.setPtyWindowSizeInternal(getIntFd(mTermFd), row, col, xpixel, ypixel);
        } catch(IOException e) {
            Log.e("TTerm.exec", "Failed to set window size: " + e.getMessage());

            if(isFailFast())
                throw new IllegalStateException(e);
        }
    }

    /** Set or clear UTF-8 mode for a given PTY.
     *
     * <p>Used by the terminal driver to implement correct erase behavior in cooked mode (Linux >= 2.6.4).</p>
     */
    void setPtyUTF8Mode (boolean utf8Mode) {
        // If the TTY goes away too quickly, this may get called after it's descriptor is closed.
        if(! mTermFd.getFileDescriptor().valid())
            return;

        try {
            Exec.setPtyUTF8ModeInternal(getIntFd(mTermFd), utf8Mode);
        } catch(IOException e) {
            Log.e("TTerm.exec", "Failed to set UTF mode: " + e.getMessage());

            if(isFailFast())
                throw new IllegalStateException(e);
        }
    }

    /**
     * <p>Overridden by {@link BoundSession}.</p>
     *
     * @return true, if failing to operate on file descriptor deserves an exception (never the case for ATE
     * own shell).
     */
    boolean isFailFast () {
        return false;
    }

    private static int getIntFd (ParcelFileDescriptor parcelFd) throws IOException {
        return parcelFd.getFd();
    }
}
