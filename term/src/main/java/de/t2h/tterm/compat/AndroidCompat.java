package de.t2h.tterm.compat;

//T-{ ------------------------------------------------------------
//T+ // I've removed all uses of this technique.
//T- /**
//T-  * The classes in this package take advantage of the fact that the VM does
//T-  * not attempt to load a class until it's accessed, and the verifier
//T-  * does not run until a class is loaded.  By keeping the methods which
//T-  * are unavailable on older platforms in subclasses which are only ever
//T-  * accessed on platforms where they are available, we can preserve
//T-  * compatibility with older platforms without resorting to reflection.
//T-  *
//T-  * See http://developer.android.com/resources/articles/backward-compatibility.html
//T-  * and http://android-developers.blogspot.com/2010/07/how-to-have-your-cupcake-and-eat-it-too.html
//T-  * for further discussion of this technique.
//T-  */
//T-} ------------------------------------------------------------

public class AndroidCompat {
    //T!{ ------------------------------------------------------------
    //T! public final static int SDK = getSDK();
    public final static int SDK = android.os.Build.VERSION.SDK_INT;
    //T!} ------------------------------------------------------------

    // The era of Holo Design
    public final static boolean V11ToV20;

    static {
        V11ToV20 = (SDK >= 11) && (SDK <= 20);
    }

    //T-{ ------------------------------------------------------------
    //T- private final static int getSDK() {
    //T-     int result;
    //T-     try {
    //T-         result = AndroidLevel4PlusCompat.getSDKInt();
    //T-     } catch (VerifyError e) {
    //T-         // We must be at an SDK level less than 4.
    //T-         try {
    //T-             result = Integer.valueOf(android.os.Build.VERSION.SDK);
    //T-         } catch (NumberFormatException e2) {
    //T-             // Couldn't parse string, assume the worst.
    //T-             result = 1;
    //T-         }
    //T-     }
    //T-     return result;
    //T- }
    //T-} ------------------------------------------------------------
}

//T-{ ------------------------------------------------------------
//T- class AndroidLevel4PlusCompat {
//T-     static int getSDKInt() { return android.os.Build.VERSION.SDK_INT; }
//T- }
//T-} ------------------------------------------------------------