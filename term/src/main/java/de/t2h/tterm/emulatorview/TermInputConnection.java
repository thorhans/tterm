package de.t2h.tterm.emulatorview;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;

import java.io.IOException;

// ThH: Cleaned up.
//
class TermInputConnection extends BaseInputConnection {
    // ************************************************************
    // Constants
    // ************************************************************

    private final static String TAG = "TermInputConnection";
    private final static boolean LOG_IME = false;

    // ************************************************************
    // Attributes
    // ************************************************************

    /** Used to handle composing text requests. */
    private int mCursor;
    private int mComposingTextStart;
    private int mComposingTextEnd;
    private int mSelectedTextStart;
    private int mSelectedTextEnd;

    // TODO ThH: Improve encapsulation, `TermInputConnection´ accesses `mEmulatorView´.
    private EmulatorView mEmulatorView;

    // ************************************************************
    // Methods
    // ************************************************************

    public TermInputConnection (EmulatorView mEmulatorView) {
        super(mEmulatorView, true);
        this.mEmulatorView = mEmulatorView;
    }

    private void sendText (CharSequence text) {
        int n = text.length();
        char c;
        try {
            for(int i = 0; i < n; i++) {
                c = text.charAt(i);
                if(Character.isHighSurrogate(c)) {
                    int codePoint;
                    if(++i < n) {
                        codePoint = Character.toCodePoint(c, text.charAt(i));
                    } else {
                        // Unicode Replacement Glyph, aka white question mark in black diamond.
                        codePoint = '\ufffd';
                    }
                    mapAndSend(codePoint);
                } else {
                    mapAndSend(c);
                }
            }
        } catch(IOException e) {
            Log.e(TAG, "error writing ", e);
        }
    }

    private void mapAndSend (int c) throws IOException {
        int result = mEmulatorView.mKeyListener.mapControlChar(c);
        if(result < TermKeyListener.KEYCODE_OFFSET) {
            mEmulatorView.mTermSession.write(result);
        } else {
            mEmulatorView.mKeyListener.handleKeyCode(result - TermKeyListener.KEYCODE_OFFSET, null,
                mEmulatorView.getKeypadApplicationMode());
        }
        mEmulatorView.clearSpecialKeyStatus();
    }

    public boolean beginBatchEdit () {
        if(LOG_IME) Log.w(TAG, "beginBatchEdit");
        mEmulatorView.setImeBuffer("");
        mCursor = 0;
        mComposingTextStart = 0;
        mComposingTextEnd = 0;
        return true;
    }

    public boolean clearMetaKeyStates (int arg0) {
        if(LOG_IME) Log.w(TAG, "clearMetaKeyStates " + arg0);
        return false;
    }

    public boolean commitCompletion (CompletionInfo arg0) {
        if(LOG_IME) Log.w(TAG, "commitCompletion " + arg0);
        return false;
    }

    public boolean endBatchEdit () {
        if(LOG_IME) Log.w(TAG, "endBatchEdit");
        return true;
    }

    public boolean finishComposingText () {
        if(LOG_IME) Log.w(TAG, "finishComposingText");
        sendText(mEmulatorView.mImeBuffer);
        mEmulatorView.setImeBuffer("");
        mComposingTextStart = 0;
        mComposingTextEnd = 0;
        mCursor = 0;
        return true;
    }

    public int getCursorCapsMode (int reqModes) {
        if(LOG_IME) Log.w(TAG, "getCursorCapsMode(" + reqModes + ")");
        int mode = 0;
        if((reqModes & TextUtils.CAP_MODE_CHARACTERS) != 0) {
            mode |= TextUtils.CAP_MODE_CHARACTERS;
        }
        return mode;
    }

    public ExtractedText getExtractedText (ExtractedTextRequest arg0,int arg1) {
        if(LOG_IME) Log.w(TAG, "getExtractedText" + arg0 + "," + arg1);
        return null;
    }

    public CharSequence getTextAfterCursor (int n, int flags) {
        if(LOG_IME) Log.w(TAG, "getTextAfterCursor(" + n + "," + flags + ")");
        int len = Math.min(n, mEmulatorView.mImeBuffer.length() - mCursor);
        if(len <= 0 || mCursor < 0 || mCursor >= mEmulatorView.mImeBuffer.length()) {
            return "";
        }
        return mEmulatorView.mImeBuffer.substring(mCursor, mCursor + len);
    }

    public CharSequence getTextBeforeCursor (int n, int flags) {
        if(LOG_IME) Log.w(TAG, "getTextBeforeCursor(" + n + "," + flags + ")");
        int len = Math.min(n, mCursor);
        if(len <= 0 || mCursor < 0 || mCursor >= mEmulatorView.mImeBuffer.length()) {
            return "";
        }
        return mEmulatorView.mImeBuffer.substring(mCursor - len, mCursor);
    }

    public boolean performContextMenuAction (int arg0) {
        if(LOG_IME) Log.w(TAG, "performContextMenuAction" + arg0);
        return true;
    }

    public boolean performPrivateCommand (String arg0, Bundle arg1) {
        if(LOG_IME) Log.w(TAG, "performPrivateCommand" + arg0 + "," + arg1);
        return true;
    }

    public boolean reportFullscreenMode (boolean arg0) {
        if(LOG_IME) Log.w(TAG, "reportFullscreenMode" + arg0);
        return true;
    }

    public boolean commitCorrection (CorrectionInfo correctionInfo) {
        if(LOG_IME) Log.w(TAG, "commitCorrection");
        return true;
    }

    public boolean commitText (CharSequence text, int newCursorPosition) {
        if(LOG_IME) Log.w(TAG, "commitText(\"" + text + "\", " + newCursorPosition + ")");
        clearComposingText();
        sendText(text);
        mEmulatorView.setImeBuffer("");
        mCursor = 0;
        return true;
    }

    private void clearComposingText () {
        int len = mEmulatorView.mImeBuffer.length();
        if(mComposingTextStart > len || mComposingTextEnd > len) {
            mComposingTextEnd = mComposingTextStart = 0;
            return;
        }
        mEmulatorView.setImeBuffer(mEmulatorView.mImeBuffer.substring(0, mComposingTextStart) +
            mEmulatorView.mImeBuffer.substring(mComposingTextEnd));
        if(mCursor < mComposingTextStart) {
            // do nothing
        } else if(mCursor < mComposingTextEnd) {
            mCursor = mComposingTextStart;
        } else {
            mCursor -= mComposingTextEnd - mComposingTextStart;
        }
        mComposingTextEnd = mComposingTextStart = 0;
    }

    public boolean deleteSurroundingText (int leftLength, int rightLength) {
        if(LOG_IME) Log.w(TAG, "deleteSurroundingText(" + leftLength + "," + rightLength + ")");
        if(leftLength > 0) {
            for(int i = 0; i < leftLength; i++) {
                sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            }
        } else if((leftLength == 0) && (rightLength == 0)) {
            // Delete key held down / repeating
            sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        }
        // TODO: handle forward deletes.
        return true;
    }

    public boolean performEditorAction (int actionCode) {
        if(LOG_IME) Log.w(TAG, "performEditorAction(" + actionCode + ")");
        if(actionCode == EditorInfo.IME_ACTION_UNSPECIFIED) {
            // Press Enter to close finished session for keyboard Swype.
            //
            // For keyboard GBoard, see `TermKeyListener.handleKeyCode´.
            //
            if(mEmulatorView.mTermSession.mIsExiting) {
                mEmulatorView.mTermSession.finish();
                return true;
            }

            // The "return" key has been pressed on the IME.
            sendText("\r");
        }
        return true;
    }

    public boolean sendKeyEvent (KeyEvent event) {
        if(LOG_IME) Log.w(TAG, "sendKeyEvent(" + event + ")");
        // Some keys are sent here rather than to commitText. In particular, del and the digit keys are sent
        // here. (And I have reports that the HTC Magic also sends Return here.) As a bit of defensive
        // programming, handle every key.
        mEmulatorView.dispatchKeyEvent(event);
        return true;
    }

    public boolean setComposingText (CharSequence text, int newCursorPosition) {
        if(LOG_IME) Log.w(TAG, "setComposingText(\"" + text + "\", " + newCursorPosition + ")");
        int len = mEmulatorView.mImeBuffer.length();
        if(mComposingTextStart > len || mComposingTextEnd > len) return false;

        mEmulatorView.setImeBuffer(mEmulatorView.mImeBuffer.substring(0, mComposingTextStart)
            + text + mEmulatorView.mImeBuffer.substring(mComposingTextEnd));
        mComposingTextEnd = mComposingTextStart + text.length();
        mCursor = newCursorPosition > 0 ? mComposingTextEnd + newCursorPosition - 1
            : mComposingTextStart - newCursorPosition;
        return true;
    }

    public boolean setSelection (int start, int end) {
        if(LOG_IME) Log.w(TAG, "setSelection" + start + "," + end);
        int length = mEmulatorView.mImeBuffer.length();
        if(start == end && start > 0 && start < length) {
            mSelectedTextStart = mSelectedTextEnd = 0;
            mCursor = start;
        } else if(start < end && start > 0 && end < length) {
            mSelectedTextStart = start;
            mSelectedTextEnd = end;
            mCursor = start;
        }
        return true;
    }

    public boolean setComposingRegion (int start, int end) {
        if(LOG_IME) Log.w(TAG, "setComposingRegion " + start + "," + end);
        if(start < end && start > 0 && end < mEmulatorView.mImeBuffer.length()) {
            clearComposingText();
            mComposingTextStart = start;
            mComposingTextEnd = end;
        }
        return true;
    }

    public CharSequence getSelectedText (int flags) {
        if(LOG_IME) Log.w(TAG, "getSelectedText " + flags);
        int len = mEmulatorView.mImeBuffer.length();
        if(mSelectedTextEnd >= len || mSelectedTextStart > mSelectedTextEnd) {
            return "";
        }
        return mEmulatorView.mImeBuffer.substring(mSelectedTextStart, mSelectedTextEnd + 1);
    }
}
