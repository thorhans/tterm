package de.t2h.tterm;

import android.os.*;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

/** Utility methods for creating and managing a subprocess.
 *
 * <p>This class differs from {@link java.lang.ProcessBuilder} in that a PTY is used to communicate with the
 * subprocess.</p>
 *
 * <p>Pseudo-terminals are a powerful Unix feature that allows programs to interact with other programs they
 * start in slightly more human-like way. For example, a PTY owner can send `^C´ (aka SIGINT) to attached
 * shell, even if said shell runs under a different user ID.</p>
 */
public class TermExec {
    // ************************************************************
    // Initialization
    // ************************************************************

    // Warning: bump the library revision, when an incompatible change happens
    static {
        System.loadLibrary("de-t2h-tterm-exec2");
    }

    // ************************************************************
    // Constants
    // ************************************************************

    // public static final String SERVICE_ACTION_V1 = "jackpal.androidterm.action.START_TERM.v1";
    public static final String SERVICE_ACTION_V1 = "de.t2h.tterm.action.START_TERM.v1";

    // ************************************************************
    // Attributes
    // ************************************************************

    private static Field descriptorField;

    private final List<String> command;
    public @NonNull List<String> getCommand () { return command; }
    public @NonNull TermExec setCommand (@NonNull String... command) {
        return setCommand(new ArrayList<>(Arrays.asList(command)));
    }

    public @NonNull TermExec setCommand (List<String> command) {
        command.clear();
        command.addAll(command);
        return this;
    }

    private final Map<String, String> environment;
    public @NonNull Map<String, String> getEnvironment () { return environment; }

    // ************************************************************
    // Methods
    // ************************************************************

    public TermExec (@NonNull String... command) {
        this(new ArrayList<>(Arrays.asList(command)));
    }

    public TermExec (@NonNull List<String> command) {
        this.command = command;
        this.environment = new Hashtable<>(System.getenv());
    }

    /** Start the process and attach it to the PTY corresponding to given file descriptor.
     *
     * <p>You have to obtain this file descriptor yourself by calling
     * {@link android.os.ParcelFileDescriptor#open} on a special terminal multiplexer device (located at
     * `/dev/ptmx´).</p>
     *
     * <p>Callers are responsible for closing the descriptor.</p>
     *
     * @return the PID of the started process.
     */
    public int start (@NonNull ParcelFileDescriptor ptmxFd) throws IOException {
        if(Looper.getMainLooper() == Looper.myLooper())
            throw new IllegalStateException("This method must not be called from the main thread!");

        if(command.size() == 0)
            throw new IllegalStateException("Empty command!");

        final String cmd = command.remove(0);
        final String[] cmdArray = command.toArray(new String[command.size()]);
        final String[] envArray = new String[environment.size()];
        int i = 0;
        for(Map.Entry<String, String> entry : environment.entrySet()) {
            envArray[i++] = entry.getKey() + "=" + entry.getValue();
        }

        return createSubprocess(ptmxFd, cmd, cmdArray, envArray);
    }

    /** Causes the calling thread to wait for the process associated with the receiver to finish executing.
     *
     * @return The exit value of the Process being waited on
     */
    public static native int waitFor (int processId);

    /** Send signal via the "kill" system call.
     *
     * <p>Android {@link android.os.Process#sendSignal} does not allow negative numbers (denoting process
     * groups) to be used.</p>
     */
    public static native void sendSignal (int processId, int signal);

    static int createSubprocess (ParcelFileDescriptor masterFd, String cmd, String[] args, String[] envVars)
        throws IOException
    {
        int integerFd = masterFd.getFd();
        return createSubprocessInternal(cmd, args, envVars, integerFd);
    }

    private static native int createSubprocessInternal (
        String cmd, String[] args, String[] envVars, int masterFd);
}

