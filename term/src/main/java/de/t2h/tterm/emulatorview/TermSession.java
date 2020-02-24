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

package de.t2h.tterm.emulatorview;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/** A terminal session, consisting of a VT100 terminal emulator and its input and output streams.
 *
 * <p> You need to supply an {@link InputStream} and {@link OutputStream} to provide input and output to the
 * terminal. For a locally running program, these would typically point to a tty; for a telnet program they
 * might point to a network socket. Reader and writer threads will be spawned to do I/O to these streams. All
 * other operations, including processing of input and output in {@link #processInput processInput} and {@link
 * #write(byte[], int, int) write}, will be performed on the main thread.</p>
 *
 * <p>Call {@link #setTermIn} and {@link #setTermOut} to connect the input and output streams to the emulator.
 * When all of your initialization is complete, your initial screen size is known, and you're ready to start
 * VT100 emulation, call {@link #initializeEmulator} or {@link #updateSize} with the number of rows and
 * columns the terminal should initially have. (If you attach the session to an {@link EmulatorView}, the view
 * will take care of setting the screen size and initializing the emulator for you.)</p>
 *
 * <p>When you're done with the session, you should call {@link #finish} on it. This frees emulator data from
 * memory, stops the reader and writer threads, and closes the attached I/O streams.</p>
 */
public class TermSession {
    // ************************************************************
    // Constants
    // ************************************************************

    // Number of rows in the transcript
    private static final int TRANSCRIPT_ROWS = 10000;

    private static final int NEW_INPUT = 1;
    private static final int NEW_OUTPUT = 2;
    private static final int FINISH = 3;
    private static final int EOF = 4;

    // ************************************************************
    // Attributes
    // ************************************************************

    // ------------------------------------------------------------
    // Color scheme
    // ------------------------------------------------------------
  
    private ColorScheme mColorScheme = BaseTextRenderer.defaultColorScheme;
    /** Set the terminal emulator's color scheme (default colors).
     *
     * @param scheme The {@link ColorScheme} to be used (use null for the default scheme).
     */
    public void setColorScheme (ColorScheme scheme) {
        if(scheme == null) {
            scheme = BaseTextRenderer.defaultColorScheme;
        }
        mColorScheme = scheme;
        if(mEmulator == null) return;

        mEmulator.setColorScheme(scheme);
    }

    // ------------------------------------------------------------
    // Emulator and its UTF-8 mode
    // ------------------------------------------------------------

    private TerminalEmulator mEmulator;
    TerminalEmulator getEmulator () { return mEmulator; }

    private boolean mDefaultUTF8Mode;
    /** Set whether the terminal emulator should be in UTF-8 mode by default.
     *
     * <p>In UTF-8 mode, the terminal will handle UTF-8 sequences, allowing the display of text in most of the
     * world's languages, but applications must encode C1 control characters and graphics drawing characters
     * as the corresponding UTF-8 sequences.</p>
     */
    public void setDefaultUTF8Mode (boolean utf8ByDefault) {
        mDefaultUTF8Mode = utf8ByDefault;
        if(mEmulator == null) {
            return;
        }
        mEmulator.setDefaultUTF8Mode(utf8ByDefault);
    }

    /** Get whether the terminal emulator is currently in UTF-8 mode. */
    public boolean getUTF8Mode () {
        if(mEmulator == null) {
            return mDefaultUTF8Mode;
        } else {
            return mEmulator.getUTF8Mode();
        }
    }

    /** Set an {@link UpdateCallback} to be invoked when the terminal emulator goes into or out of UTF-8
     *  mode.
     */
    public void setUTF8ModeUpdateCallback (UpdateCallback utf8ModeNotify) {
        if(mEmulator != null) {
            mEmulator.setUTF8ModeUpdateCallback(utf8ModeNotify);
        }
    }

    // ------------------------------------------------------------
    // IO
    // ------------------------------------------------------------
  
    private OutputStream mTermOut;
    /** Get the {@link OutputStream} associated with this session. */
    public OutputStream getTermOut () { return mTermOut; }
    /** Set the {@link OutputStream} associated with this session. */
    public void setTermOut (OutputStream termOut) { mTermOut = termOut; }

    private InputStream mTermIn;
    /** Get the {@link InputStream} associated with this session. */
    public InputStream getTermIn () { return mTermIn; }

    /** Set the {@link InputStream} associated with this session. */
    public void setTermIn (InputStream termIn) { mTermIn = termIn; }

    private Thread mReaderThread;
    private ByteQueue mByteQueue;
    private byte[] mReceiveBuffer;

    private Thread mWriterThread;
    private ByteQueue mWriteQueue;
    private Handler mWriterHandler;

    private CharBuffer mWriteCharBuffer;
    private ByteBuffer mWriteByteBuffer;
    private CharsetEncoder mUTF8Encoder;

    // ------------------------------------------------------------
    // Keys
    // ------------------------------------------------------------

    private TermKeyListener mKeyListener;
    public void setKeyListener (TermKeyListener l) { mKeyListener = l; }

    // ------------------------------------------------------------
    // Notify changes to terminal emulator's screen
    // ------------------------------------------------------------

    private UpdateCallback mNotify;
    /** Set an {@link UpdateCallback} to be invoked when the terminal emulator's screen is changed. */
    public void setUpdateCallback (UpdateCallback notify) { mNotify = notify; }
    /** Notify the {@link UpdateCallback} registered by {@link #setUpdateCallback setUpdateCallback} that the
     *  screen has changed.
     */
    protected void notifyUpdate () { if(mNotify != null) { mNotify.onUpdate(); } }

    // ------------------------------------------------------------
    // Running & finishing
    // ------------------------------------------------------------

    /** Callback to be invoked when a {@link TermSession} finishes.
     *
     * @see TermSession#setUpdateCallback
     */
    public interface FinishCallback {
        /** Callback function to be invoked when a {@link TermSession} finishes.
         *
         * @param session  the ‘TermSession’ which has finished
         */
        void onSessionFinish (TermSession session);
    }

    private FinishCallback mFinishCallback;
    /** Set a {@link FinishCallback} to be invoked once this terminal session is finished. */
    public void setFinishCallback (FinishCallback callback) { mFinishCallback = callback; }

    private boolean mIsRunning = false;
    public boolean isRunning () { return mIsRunning; }

    /** Needed to support pressing Enter to close finished session. */
    // TODO Merge with ‘mIsRunning’?
    public boolean mIsExiting = false;

    // ------------------------------------------------------------
    // Title and listener for it
    // ------------------------------------------------------------

    private String mTitle;
    /** Get the terminal session's title (may be null). */
    public String getTitle () { return mTitle; }
    /** Set the terminal session's title. */
    public void setTitle (String title) { mTitle = title; notifyTitleChanged(); }

    private UpdateCallback mTitleChangedListener;
    /** Set an {@link UpdateCallback} to be invoked when the terminal emulator's title is changed. */
    public void setTitleChangedListener (UpdateCallback listener) { mTitleChangedListener = listener; }
    /** Notify the UpdateCallback registered for title changes, the terminal session's title has changed. */
    protected void notifyTitleChanged () {
        UpdateCallback listener = mTitleChangedListener;
        if(listener != null) listener.onUpdate();
    }

    // ------------------------------------------------------------
    // Transcript (screen and scrollback buffer)
    // ------------------------------------------------------------

    private TranscriptScreen mTranscriptScreen;
    TranscriptScreen getTranscriptScreen () { return mTranscriptScreen; }
    /** Retrieve the terminal's screen and scrollback buffer.
     *
     * @return A {@link String} containing the contents of the screen and scrollback buffer.
     */
    public String getTranscriptText () { return mTranscriptScreen.getTranscriptText(); }

    // ------------------------------------------------------------
    // TODO What is this?
    // ------------------------------------------------------------

    private Handler mMsgHandler = new Handler() {
        @Override
        public void handleMessage (Message msg) {
            if(! mIsRunning) return;

            if(msg.what == NEW_INPUT) {
                readFromProcess();
            } else if(msg.what == EOF) {
                new Handler(Looper.getMainLooper()).post(
                    () -> { onProcessExit(); }
                );
            }
        }
    };

    // ************************************************************
    // Methods
    // ************************************************************

    public TermSession () {
        this(false);
    }

    public TermSession (final boolean exitOnEOF) {
        mWriteCharBuffer = CharBuffer.allocate(2);
        mWriteByteBuffer = ByteBuffer.allocate(4);
        mUTF8Encoder = Charset.forName("UTF-8").newEncoder();
        mUTF8Encoder.onMalformedInput(CodingErrorAction.REPLACE);
        mUTF8Encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        mReceiveBuffer = new byte[4 * 1024];
        mByteQueue = new ByteQueue(4 * 1024);
        mReaderThread = new Thread() {
            private byte[] mBuffer = new byte[4096];

            @Override
            public void run () {
                try {
                    while(true) {
                        int read = mTermIn.read(mBuffer);
                        if(read == -1) {
                            // EOF -- process exited.
                            break;
                        }
                        int offset = 0;
                        while(read > 0) {
                            int written = mByteQueue.write(mBuffer, offset, read);
                            offset += written;
                            read -= written;
                            mMsgHandler.sendMessage(mMsgHandler.obtainMessage(NEW_INPUT));
                        }
                    }
                } catch(IOException e) {
                } catch(InterruptedException e) {
                }

                if(exitOnEOF) mMsgHandler.sendMessage(mMsgHandler.obtainMessage(EOF));
            }
        };
        mReaderThread.setName("TermSession input reader");

        mWriteQueue = new ByteQueue(4096);
        mWriterThread = new Thread() {
            private byte[] mBuffer = new byte[4096];

            @Override
            public void run () {
                Looper.prepare();

                mWriterHandler = new Handler() {
                    @Override
                    public void handleMessage (Message msg) {
                        if(msg.what == NEW_OUTPUT) {
                            writeToOutput();
                        } else if(msg.what == FINISH) {
                            Looper.myLooper().quit();
                        }
                    }
                };

                // Drain anything in the queue from before we started
                writeToOutput();

                Looper.loop();
            }

            private void writeToOutput () {
                ByteQueue writeQueue = mWriteQueue;
                byte[] buffer = mBuffer;
                OutputStream termOut = mTermOut;

                int bytesAvailable = writeQueue.getBytesAvailable();
                int bytesToWrite = Math.min(bytesAvailable, buffer.length);

                if(bytesToWrite == 0) return;

                try {
                    writeQueue.read(buffer, 0, bytesToWrite);
                    termOut.write(buffer, 0, bytesToWrite);
                    termOut.flush();
                } catch(IOException e) {
                    // Ignore exception. We don't really care if the receiver isn't listening. We just make a
                    // best effort to answer the query.
                    e.printStackTrace();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        mWriterThread.setName("TermSession output writer");
    }

    protected void onProcessExit () {
        finish();
    }

    /** Set the terminal emulator's window size and start terminal emulation.
     *
     * @param columns  the number of columns in the terminal window
     * @param rows     the number of rows in the terminal window
     */
    public void initializeEmulator (int columns, int rows) {
        mTranscriptScreen = new TranscriptScreen(columns, TRANSCRIPT_ROWS, rows);
        mEmulator = new TerminalEmulator(this, mTranscriptScreen, columns, rows, mColorScheme);
        mEmulator.setDefaultUTF8Mode(mDefaultUTF8Mode);
        mEmulator.setKeyListener(mKeyListener);

        mIsRunning = true;
        mReaderThread.start();
        mWriterThread.start();
    }

    /** Write data to the terminal output.
     *
     * <p>The written data will be consumed by the emulation client as input.</p>
     *
     * <p>‘write’ itself runs on the main thread. The default implementation writes the data into a circular
     * buffer and signals the writer thread to copy it from there to the {@link OutputStream}.</p>
     * 
     * <p>Subclasses may override this method to modify the output before writing it to the stream, but
     * implementations in derived classes should call through to this method to do the actual writing.</p>
     *
     * @param data    an array of bytes to write to the terminal
     * @param offset  the offset into the array at which the data starts
     * @param count   the number of bytes to be written
     */
    public void write (byte[] data, int offset, int count) {
        try {
            while(count > 0) {
                int written = mWriteQueue.write(data, offset, count);
                offset += written;
                count -= written;
                notifyNewOutput();
            }
        } catch(InterruptedException e) {}
    }

    /** Write the UTF-8 representation of a String to the terminal output.
     *
     * <p>The written data will be consumed by the emulation client as input.</p>
     * 
     * <p>This implementation encodes the String and then calls {@link #write(byte[], int, int)} to do the
     * actual writing. It should therefore usually be unnecessary to override this method; override {@link
     * #write(byte[], int, int)} instead.</p>
     *
     * @param data  the String to write to the terminal
     */
    public void write (String data) {
        try {
            byte[] bytes = data.getBytes("UTF-8");
            write(bytes, 0, bytes.length);
        } catch(UnsupportedEncodingException e) {}
    }

    /** Write the UTF-8 representation of a single Unicode code point to the terminal output.
     *
     * <p>The written data will be consumed by the emulation client as input.</p>
     * 
     * <p>This implementation encodes the code point and then calls {@link #write(byte[], int, int)} to do the
     * actual writing. It should therefore usually be unnecessary to override this method; override {@link
     * #write(byte[], int, int)} instead.</p>
     *
     * @param codePoint  the Unicode code point to write to the terminal
     */
    public void write (int codePoint) {
        ByteBuffer byteBuf = mWriteByteBuffer;
        if(codePoint < 128) {
            // Fast path for ASCII characters.
            byte[] buf = byteBuf.array();
            buf[0] = (byte) codePoint;
            write(buf, 0, 1);
            return;
        }

        CharBuffer charBuf = mWriteCharBuffer;
        CharsetEncoder encoder = mUTF8Encoder;

        charBuf.clear();
        byteBuf.clear();
        Character.toChars(codePoint, charBuf.array(), 0);
        encoder.reset();
        encoder.encode(charBuf, byteBuf, true);
        encoder.flush(byteBuf);
        write(byteBuf.array(), 0, byteBuf.position()-1);
    }

    /* Notify the writer thread that there's new output waiting. */
    private void notifyNewOutput () {
        Handler writerHandler = mWriterHandler;
        if(writerHandler == null) {
           // Writer thread isn't started -- will pick up data once it does.
           return;
        }
        writerHandler.sendEmptyMessage(NEW_OUTPUT);
    }

    /** Change the terminal's window size.
     *
     * <p>Will call {@link #initializeEmulator} if the emulator is not yet running.</p>
     * 
     * <p>You should override this method if your application needs to be notified when the screen size
     * changes (for example, if you need to issue <code>TIOCSWINSZ</code> to a TTY to adjust the window size).
     * <em>If you do override this method, you must call through to the superclass implementation.</em></p>
     *
     * @param columns  the number of columns in the terminal window
     * @param rows     the number of rows in the terminal window
     */
    public void updateSize (int columns, int rows) {
        if(mEmulator == null) {
            initializeEmulator(columns, rows);
        } else {
            mEmulator.updateSize(columns, rows);
        }
    }

    /** Look for new input from the PTTY, send it to the terminal emulator. */
    private void readFromProcess () {
        int bytesAvailable = mByteQueue.getBytesAvailable();
        int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
        int bytesRead = 0;
        try {
            bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
        } catch(InterruptedException e) {
            return;
        }

        // Give subclasses a chance to process the read data
        processInput(mReceiveBuffer, 0, bytesRead);
        notifyUpdate();
    }

    /** Process input and send it to the terminal emulator.
     *
     * <p>This method is invoked on the main thread whenever new data is read from the InputStream.</p>
     *
     * <p>The default implementation sends the data straight to the terminal emulator without modifying it in
     * any way. Subclasses can override it to modify the data before giving it to the terminal.</p>
     *
     * @param data    a byte array containing the data read
     * @param offset  the offset into the buffer where the read data begins
     * @param count   the number of bytes read
     */
    protected void processInput (byte[] data, int offset, int count) {
        mEmulator.append(data, offset, count);
    }

    /** Write something directly to the terminal emulator input, bypassing everything.
     *
     * <p>Bypasses the emulation client, the session's {@link InputStream}, and any processing being done by
     * {@link #processInput processInput}.</p>
     *
     * @param data    the data to be written to the terminal
     * @param offset  the starting offset into the buffer of the data
     * @param count   the length of the data to be written
     */
    protected final void appendToEmulator (byte[] data, int offset, int count) {
        mEmulator.append(data, offset, count);
    }

    /** Reset the terminal emulator's state. */
    public void reset () {
        mEmulator.reset();
        notifyUpdate();
    }

    /** Finish this terminal session.
     * 
     * <p>Frees resources used by the terminal emulator and closes the attached ‘InputStream’ and
     * ‘OutputStream’.</p>
     */
    public void finish () {
        mIsRunning = false;
        mEmulator.finish();
        if(mTranscriptScreen != null) {
            mTranscriptScreen.finish();
        }

        // Stop the reader and writer threads, and close the I/O streams.
        if(mWriterHandler != null) {
            mWriterHandler.sendEmptyMessage(FINISH);
        }
        try {
            mTermIn.close();
            mTermOut.close();
        } catch(IOException e) {
            // We don't care if this fails.
        } catch(NullPointerException e) {
        }

        if(mFinishCallback != null) {
            mFinishCallback.onSessionFinish(this);
        }
    }
}
