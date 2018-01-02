// From the desk of Frank P. Westlake; public domain.

package de.t2h.tterm.shortcuts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.EditText;

import de.t2h.tterm.R;
import de.t2h.tterm.RemoteInterface;
import de.t2h.tterm.RunShortcut;
import de.t2h.tterm.TermDebug;
import de.t2h.tterm.util.ShortcutEncryption;

import java.io.File;
import java.security.GeneralSecurityException;

// ThH: Cleaned up.
//
public class AddShortcut extends Activity {
    // ************************************************************
    // Constants
    // ************************************************************

    private final int OP_MAKE_SHORTCUT = 1;

    private final int PATH = 0, ARGS = 1, NAME = 2;

    // ************************************************************
    // Attributes
    // ************************************************************

    // LATER Why this?
    private final Context context = this;

    private SharedPreferences mPrefs;

    private final EditText mEditTexts[] = new EditText[5];

    private String mPath;

    private String mName = "";

    private String mIconText[] = { "", null };

    // ************************************************************
    // Methods
    // ************************************************************

    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String action = getIntent().getAction();
        if(action != null && action.equals("android.intent.action.CREATE_SHORTCUT")) makeShortcut();
        else finish();
    }

    void makeShortcut () {
        if(mPath == null) mPath = "";
        final AlertDialog.Builder alert = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);

        LinearLayout lv = new LinearLayout(context);
        lv.setOrientation(LinearLayout.VERTICAL);
        for(int i = 0, n = mEditTexts.length; i<n; i++) {
            mEditTexts[i] = new EditText(context);
            mEditTexts[i].setSingleLine(true);
        }
        if(! mPath.equals("")) mEditTexts[0].setText(mPath);
        mEditTexts[PATH].setHint(getString(R.string.addshortcut_command_hint)); // `command´
        mEditTexts[NAME].setText(mName);
        mEditTexts[ARGS].setHint(getString(R.string.addshortcut_example_hint)); //`--example = "a"´
        mEditTexts[ARGS].setOnFocusChangeListener((View view, boolean focus) -> {
            if(! focus) {
                String s;
                if(mEditTexts[NAME].getText().toString().equals("")
                    && ! (s = mEditTexts[ARGS].getText().toString()).equals("")
                ) {
                    mEditTexts[NAME].setText(s.split("\\s")[0]);
        } } } );

        Button  btn_path = new Button(context);
        btn_path.setText(getString(R.string.addshortcut_button_find_command));//"Find command");
        btn_path.setOnClickListener((View p1) -> {
            String lastPath = mPrefs.getString("lastPath", null);
            File get = (lastPath == null)
                ? Environment.getExternalStorageDirectory()
                : new File(lastPath).getParentFile();
            Intent pickerIntent = new Intent();
            if(mPrefs.getBoolean("useInternalScriptFinder", false)) {
                pickerIntent.setClass(getApplicationContext(), de.t2h.tterm.shortcuts.FSNavigator.class)
                    .setData(Uri.fromFile(get))
                    .putExtra("title", getString(R.string.addshortcut_navigator_title));
            } else {
                pickerIntent
                    .putExtra("CONTENT_TYPE", "*/*")
                    .setAction(Intent.ACTION_PICK);
            }
            startActivityForResult(pickerIntent, OP_MAKE_SHORTCUT);
        });
        lv.addView(layoutTextViewH(getString(R.string.addshortcut_command_window_instructions), null, false));
        lv.addView(layoutViewViewH(btn_path, mEditTexts[PATH]));
        lv.addView(layoutTextViewH(getString(R.string.addshortcut_arguments_label), mEditTexts[ARGS]));
        lv.addView(layoutTextViewH(getString(R.string.addshortcut_shortcut_label),  mEditTexts[NAME]));

        final ImageView img = new ImageView(context);
        img.setImageResource(R.drawable.ic_launcher);
        img.setMaxHeight(100);
        img.setTag(0xFFFFFFFF);
        img.setMaxWidth(100);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        final Button btn_color = new Button(context);
        btn_color.setText(getString(R.string.addshortcut_button_text_icon));
        btn_color.setOnClickListener((View p1) -> { new ColorValue(context, img, mIconText); });

        lv.addView(layoutTextViewH(
            getString(R.string.addshortcut_text_icon_instructions), //"Optionally create a text icon:"
            null, false));
        lv.addView(layoutViewViewH(btn_color, img));

        final ScrollView sv = new ScrollView(context);
        sv.setFillViewport(true);
        sv.addView(lv);

        alert.setView(sv);
        alert.setTitle(getString(R.string.addshortcut_title));
        alert.setPositiveButton(android.R.string.yes,
            (DialogInterface dialog, int which) -> {
                buildShortcut(mPath, mEditTexts[ARGS].getText().toString(),
                    mEditTexts[NAME].getText().toString(), mIconText[1], (Integer) img.getTag());
        });
        alert.setNegativeButton(android.R.string.cancel,
            (DialogInterface dialog, int which) -> { finish(); });
        alert.show();
    }

    LinearLayout layoutTextViewH(String text, View vw)
    {
      return(layoutTextViewH(text, vw, true));
    }

    LinearLayout layoutTextViewH(String text, View vw, boolean attributes) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            /*weight*/ 1);

        TextView tv = new TextView(context);
        tv.setText(text);
        if(attributes) {
          tv.setTypeface(Typeface.DEFAULT_BOLD);
          tv.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        }
        tv.setPadding(10, tv.getPaddingTop(), 10, tv.getPaddingBottom());

        LinearLayout lh = new LinearLayout(context);
        lh.setOrientation(LinearLayout.HORIZONTAL);
        lh.addView(tv, lp);

        if(vw != null) lh.addView(vw, lp);
        return lh;
    }

    LinearLayout layoutViewViewH(View vw1, View vw2) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            /*weight*/ 1);

        LinearLayout lh = new LinearLayout(context);
        lh.setOrientation(LinearLayout.HORIZONTAL);
        lh.addView(vw1, lp);

        if(vw2 != null) lh.addView(vw2, lp);
        return(lh);
    }

    void buildShortcut(String path, String arguments, String shortcutName, String shortcutText,
        int shortcutColor)
    {
        ShortcutEncryption.Keys keys = ShortcutEncryption.getKeys(context);
        if(keys == null) {
            try {
                keys = ShortcutEncryption.generateKeys();
            }
            catch(GeneralSecurityException e){
                Log.e(TermDebug.LOG_TAG, "Generating shortcut encryption keys failed: " + e.toString());
                throw new RuntimeException(e);
            }
            ShortcutEncryption.saveKeys(context, keys);
        }
        StringBuilder cmd = new StringBuilder();
        if(path != null && ! path.equals("")) cmd.append(RemoteInterface.quoteForBash(path));
        if(arguments != null && ! arguments.equals("")) cmd.append(" " + arguments);
        String cmdStr = cmd.toString();
        String cmdEnc = null;
        try {
            cmdEnc = ShortcutEncryption.encrypt(cmdStr, keys);
        } catch(GeneralSecurityException e) {
            Log.e(TermDebug.LOG_TAG, "Shortcut encryption failed: " + e.toString());
            throw new RuntimeException(e);
        }

        Intent target = new Intent().setClass(context, RunShortcut.class);
        target.setAction(RunShortcut.ACTION_RUN_SHORTCUT);
        target.putExtra(RunShortcut.EXTRA_SHORTCUT_COMMAND, cmdEnc);
        target.putExtra(RunShortcut.EXTRA_WINDOW_HANDLE, shortcutName);
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent wrapper = new Intent();
        wrapper.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        wrapper.putExtra(Intent.EXTRA_SHORTCUT_INTENT, target);
        if(shortcutName != null && !shortcutName.equals("")) {
            wrapper.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName);
        }
        if(shortcutText != null && !shortcutText.equals("")) {
            wrapper.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                TextIcon.getTextIcon(shortcutText, shortcutColor, 96, 96));
        } else {
          wrapper.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
              Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher));
        }
        setResult(RESULT_OK, wrapper);
        finish();
      }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = null;
        mPath = null;
        if(requestCode == OP_MAKE_SHORTCUT) {
            if(    data != null
                && (uri = data.getData()) != null
                && (mPath = uri.getPath()) != null
            ) {
                mPrefs.edit().putString("lastPath", mPath).commit();
                mEditTexts[PATH].setText(mPath);
                mName = mPath.replaceAll(".*/", "");
                if(mEditTexts[NAME].getText().toString().equals("")) mEditTexts[NAME].setText(mName);
                if(mIconText[0] != null && mIconText[0].equals("")) mIconText[0] = mName;
            } else {
                finish();
            }
        }
    }
}
