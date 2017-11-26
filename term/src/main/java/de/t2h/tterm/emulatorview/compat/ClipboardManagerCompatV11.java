package de.t2h.tterm.emulatorview.compat;

//T-{ ------------------------------------------------------------
//T- import android.annotation.SuppressLint;
//T-} ------------------------------------------------------------
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.ClipboardManager;

//T!{ ------------------------------------------------------------
//T! @SuppressLint("NewApi")
//T! public class ClipboardManagerCompatV11 implements ClipboardManagerCompat {
public class ClipboardManagerCompatV11 {
//T!} ------------------------------------------------------------

    private final ClipboardManager clip;

    //T+{ ------------------------------------------------------------
    //T+ // Originally in `ClipboardManagerCompatFactory´.
    public static ClipboardManagerCompatV11 getManager(Context context) {
        return new ClipboardManagerCompatV11(context);
    }
    //T+} ------------------------------------------------------------

    //T!{ ------------------------------------------------------------
    //T! public ClipboardManagerCompatV11(Context context) {
    private ClipboardManagerCompatV11(Context context) {
    //T!} ------------------------------------------------------------
        clip = (ClipboardManager) context.getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
    }

    //T-{ ------------------------------------------------------------
    //T!- @Override
    //T-} ------------------------------------------------------------
    public CharSequence getText() {
        ClipData.Item item = clip.getPrimaryClip().getItemAt(0);
        return item.getText();
    }

    //T-{ ------------------------------------------------------------
    //T!- @Override
    //T-} ------------------------------------------------------------
    public boolean hasText() {
        return (clip.hasPrimaryClip() && clip.getPrimaryClipDescription()
                .hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN));
    }

    //T-{ ------------------------------------------------------------
    //T!- @Override
    //T-} ------------------------------------------------------------
    public void setText(CharSequence text) {
        ClipData clipData = ClipData.newPlainText("simple text", text);
        clip.setPrimaryClip(clipData);
    }
}
