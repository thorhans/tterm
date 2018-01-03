package de.t2h.tterm.compat;

public class AndroidCompat {
    public final static int SDK = android.os.Build.VERSION.SDK_INT;

    /** The era of Holo Design. */
    public final static boolean V11ToV20;

    static {
        V11ToV20 = (11 <= SDK) && (SDK <= 20);
    }
}
