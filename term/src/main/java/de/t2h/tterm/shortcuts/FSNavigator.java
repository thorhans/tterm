// From the desk of Frank P. Westlake; public domain.

package de.t2h.tterm.shortcuts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import de.t2h.tterm.R;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class FSNavigator
    extends android.app.Activity
{
    // ************************************************************
    // Constants
    // ************************************************************

    public static final int
        MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT,
        WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT,
        VG_MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT,
        VG_WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    private final int ACTION_THEME_SWAP = 0x00000100;
    private final int BUTTON_SIZE = 150;

    // ************************************************************
    // Attributes
    // ************************************************************

    // LATER Why this?
    private Context mContext = this;

    private float mTextLg = 24;
    private int mTheme = android.R.style.Theme;
    private SharedPreferences mPrefs;
    private File mCd;
    private File mExtSdCardFile;
    private String mExtSdCard;
    private HashMap<Integer, LinearLayout> mCachedFileView;
    private HashMap<Integer, LinearLayout> mCachedDirectoryView;
    private HashMap<Integer, TextView> mCachedDividerView;
    private int mCountFileView;
    private int mCountDirectoryView;
    private int mCountDividerView;
    private LinearLayout mContentView;
    private LinearLayout mTitleView;
    private LinearLayout mPathEntryView;

    Comparator<String> mStringSortComparator =
        (String a, String b) -> a.toLowerCase().compareTo(b.toLowerCase());

    View.OnClickListener mFileListener =
        (View view) -> {
            String path = (String) view.getTag();
            if(path != null) {
                setResult(RESULT_OK, getIntent().setData(Uri.fromFile(new File(mCd, path))));
                finish();
            }
        };

      View.OnClickListener mDirectoryListener =
        (View view) -> {
            String path = (String) view.getTag();
            if(path == null) return;
            File file = new File(path);
            if(file.isFile()) {
                setResult(RESULT_OK, getIntent().setData(Uri.fromFile(file)));
                finish();
            } else {
                chdir(file);
            }
            makeView();
        };

    // ************************************************************
    // Methods
    // ************************************************************

    public void onCreate (android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default title. The title may be set from the intent later in this method.
        setTitle(getString(R.string.fsnavigator_title)); // "File Selector"

        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mTheme = mPrefs.getInt("theme", mTheme);
        setTheme(mTheme);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mExtSdCardFile = Environment.getExternalStorageDirectory();
        mExtSdCard = getCanonicalPath(mExtSdCardFile);

        Intent intent = getIntent();
        Uri uri = intent.getData();
        String path = uri == null ? null : uri.getPath();
        if(path == null || chdir(path) == null) chdir(mExtSdCard);

        if(intent.hasExtra("title")) setTitle(intent.getStringExtra("title"));

        mCachedDirectoryView = new HashMap<Integer, LinearLayout>();
        mCachedFileView      = new HashMap<Integer, LinearLayout>();
        mCachedDividerView   = new HashMap<Integer, TextView>();

        mTitleView = directoryEntry("..");
        mPathEntryView = fileEntry(null);
        mContentView = makeContentView();
    }

    public void onPause () {
        super.onPause();
        mPrefs.edit().putString("lastDirectory", getCanonicalPath(mCd)).commit();
    }

    public void onResume () {
        super.onResume();
        makeView();
    }

    public boolean onKeyUp (int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK)) {
          finish();
          return true;
        } else {
          return super.onKeyUp(keyCode, event);
        }
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, ACTION_THEME_SWAP, 0, getString(R.string.fsnavigator_change_theme)); // "Change theme"
        return true;
    }

    public boolean onOptionsItemSelected (MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == ACTION_THEME_SWAP) {
            swapTheme();
            return true;
        }
        return false;
    }

    private void swapTheme () {
        switch(mTheme) {
            case android.R.style.Theme:       mTheme = android.R.style.Theme_Light; break;
            case android.R.style.Theme_Light: mTheme = android.R.style.Theme;       break;
            default: return;
        }
        mPrefs.edit().putInt("theme", mTheme).commit();
        startActivityForResult(getIntent().addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT), -1);
        finish();
    }

    // ------------------------------------------------------------
    // File operations
    // ------------------------------------------------------------

    private String ifAvailable (String goTo) {
        if(goTo.startsWith(mExtSdCard)) {
            String s = Environment.getExternalStorageState();
            if(    s.equals(Environment.MEDIA_MOUNTED)
                || s.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
            ) {
                return goTo;
            }
            toast(getString(R.string.fsnavigator_no_external_storage), 1); // "External storage not available"
            return mExtSdCard;
        }
        return goTo;
    }

    private File chdir (File file) {
        String path = ifAvailable(getCanonicalPath(file));
        System.setProperty("user.dir", path);
        return (mCd = new File(path));
    }

    private File chdir (String path) {
        return chdir(new File(path));
    }
    
    String getCanonicalPath (String path) {
        return getCanonicalPath(new File(path));
    }

    String getCanonicalPath (File file) {
        try{
            return file.getCanonicalPath();
        } catch(IOException e){
            return file.getPath();
        }
    }

    // ------------------------------------------------------------
    // Main views
    // ------------------------------------------------------------

    private LinearLayout makeContentView () {
        final LinearLayout mainView = new LinearLayout(mContext);
        mainView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, /*weight*/ 1));
        mainView.setId(R.id.mainview);
        mainView.setOrientation(LinearLayout.VERTICAL);
        mainView.setGravity(android.view.Gravity.FILL);

        final ScrollView scrollView = new ScrollView(mContext);
        scrollView.setId(R.id.scrollview);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, /*weight*/ 1));
        scrollView.addView(mainView);

        final LinearLayout contentView = new LinearLayout(mContext);
        contentView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, /*weight*/ 1));
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(android.view.Gravity.FILL);

        contentView.setTag(mainView); // LATER Why this?

        contentView.addView(mTitleView, VG_MATCH_PARENT, VG_WRAP_CONTENT);
        contentView.addView(scrollView);
        contentView.addView(mPathEntryView, VG_MATCH_PARENT, VG_WRAP_CONTENT);
        return contentView;
    }

    private void makeView () {
        mCountDirectoryView = mCountFileView = 0;
        ScrollView scrollView = (ScrollView) mContentView.findViewById(R.id.scrollview);
        LinearLayout mainView = (LinearLayout) scrollView.findViewById(R.id.mainview);
        mainView.removeAllViews();
        if(mCd == null) chdir("/");
        String path = getCanonicalPath(mCd);

        if(path.equals("")) chdir(path = "/");
        if(path.equals("/")) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setVisibility(View.VISIBLE);
            mTitleView.requestLayout();
            ((TextView) mTitleView.findViewById(R.id.textview)).setText("[" + mCd.getPath() + "]");
        }

        // Add directories to main view.

        String dirs[] = mCd.list(
            (File dir, String name) -> new File(dir, name).isDirectory());
        if(dirs != null) {
            Arrays.sort(dirs, 0, dirs.length, mStringSortComparator);
            for(int i = 0, n = dirs.length; i < n; i++) {
                if(dirs[i].equals(".")) continue;
                mainView.addView(directoryEntry(dirs[i]));
                mainView.addView(entryDivider());
            }
        }

        // Add files to main view.

        String files[] = mCd.list(
            (File dir, String name) -> ! (new File(dir, name).isDirectory()));
        if(files != null) {
            Arrays.sort(files, 0, files.length, mStringSortComparator);
            for(int i = 0, n = files.length; i < n; i++) {
                mainView.addView(fileEntry(files[i]));
                mainView.addView(entryDivider());
            }
        }

        ((TextView) mPathEntryView.findViewById(R.id.textview)).setText("");
        scrollView.scrollTo(0, 0);
        // mTitleView.setSelected(true);
        setContentView(mContentView);
    }

    // ------------------------------------------------------------
    // Divider view
    // ------------------------------------------------------------

    private TextView entryDivider() {
        TextView tv;
        if(mCountDividerView < mCachedDividerView.size()) {
            tv = mCachedDividerView.get(mCountDividerView);
        } else {
            tv = new TextView(mContext);
            tv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, 1, /*weight*/ 1));
            mCachedDividerView.put(mCountDividerView, tv);
        }
        ++mCountDividerView;
        return tv;
    }

    // ------------------------------------------------------------
    // Views for files
    // ------------------------------------------------------------

    private LinearLayout fileEntry (final String entry) {
        LinearLayout ll;
        if(entry == null) {
            ll = fileView(entry == null);
        } else {
            if(mCountFileView < mCachedFileView.size()) {
                ll = mCachedFileView.get(mCountFileView);
            } else {
              mCachedFileView.put(mCountFileView, ll = fileView(entry == null));
            }
            ++mCountFileView;
        }

        TextView tv = (TextView)ll.findViewById(R.id.textview);
        tv.setText(entry == null ? "" : entry);
        tv.setTag( entry == null ? "" : entry);

        return ll;
    }

    private LinearLayout fileView (boolean entryWindow) {
        LinearLayout ll = new LinearLayout(mContext);
        ll.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, /*weight*/ 1));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(android.view.Gravity.FILL);

        final TextView tv;
        if(entryWindow) {
            tv = new EditText(mContext);
            tv.setHint(getString(R.string.fsnavigator_optional_enter_path));
            tv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, /*weight*/ 2));
            tv.setOnKeyListener(
                (View v, int keyCode, KeyEvent event) -> {
                    if(keyCode == KeyEvent.KEYCODE_ENTER) {
                        String path = tv.getText().toString();
                        File file = new File(getCanonicalPath(path));
                        chdir(file.getParentFile() == null?file:file.getParentFile());
                        if(file.isFile()) {
                            setResult(RESULT_OK, getIntent().setData(Uri.fromFile(file)));
                            finish();
                        } else {
                            chdir(file);
                            makeView();
                        }
                        return true;
                    }
                    return false;
                }
            );
            ll.addView(tv);
        } else {
            tv = new TextView(mContext);
            tv.setClickable(true);
            tv.setLongClickable(true);
            tv.setOnClickListener(mFileListener);
            tv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, /*weight*/ 1));

            HorizontalScrollView hv = new HorizontalScrollView(mContext);
            hv.setFillViewport(true);
            hv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, BUTTON_SIZE, /*weight*/ 7));
            hv.addView(tv);
            ll.addView(hv);
        }

        tv.setFocusable(true);
        tv.setSingleLine();
        tv.setTextSize(mTextLg);
        tv.setTypeface(Typeface.SERIF, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        tv.setPadding(10, 5, 10, 5);
        tv.setId(R.id.textview);

        return ll;
    }

    // ------------------------------------------------------------
    // Views for directories
    // ------------------------------------------------------------

    private LinearLayout directoryEntry (final String name) {
        boolean up = name.equals("..");
        LinearLayout ll;
        if(up) {
          ll = directoryView(up);
        } else {
            if(mCountDirectoryView < mCachedDirectoryView.size()) {
              ll = mCachedDirectoryView.get(mCountDirectoryView);
            } else {
              ll = directoryView(up);
              mCachedDirectoryView.put(mCountDirectoryView, ll);
            }
            ++mCountDirectoryView;
        }

        TextView tv = ((TextView) ll.findViewById(R.id.textview));
        tv.setTag(name);
        tv.setText(up ? "[" + mCd.getPath() + "]" : name);

        ll.findViewById(R.id.imageview).setTag(name);
        return ll;
    }

    private LinearLayout directoryView (boolean up) {
        ImageView b1 = imageViewFolder(up);

        TextView tv = new TextView(mContext);
        tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        tv.setClickable(true);
        tv.setLongClickable(true);
        tv.setFocusable(true);
        tv.setOnClickListener(mDirectoryListener);
        tv.setMaxLines(1);
        tv.setTextSize(mTextLg);
        tv.setPadding(10, 5, 10, 5);
        tv.setId(R.id.textview);
        tv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, BUTTON_SIZE, /*weight*/ 1));

        HorizontalScrollView hv = new HorizontalScrollView(mContext);
        hv.addView(tv);
        hv.setFillViewport(true);
        hv.setFocusable(true);
        hv.setOnClickListener(mDirectoryListener);
        hv.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, BUTTON_SIZE, /*weight*/ 7));

        LinearLayout ll = new LinearLayout(mContext);
        ll.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, BUTTON_SIZE, /*weight*/ 2));
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setGravity(android.view.Gravity.FILL);
        ll.setOnClickListener(mDirectoryListener);
        ll.addView(b1);
        ll.addView(hv);

        return ll;
    }

    private ImageView imageViewFolder (boolean up) {
        ImageView b1 = new ImageView(mContext);
        b1.setClickable(true);
        b1.setFocusable(true);
        b1.setId(R.id.imageview);
        b1.setLayoutParams(new LinearLayout.LayoutParams(120, 120, 1));
        b1.setImageResource(up ? R.drawable.ic_folderup : R.drawable.ic_folder);
        b1.setOnClickListener(mDirectoryListener);
        b1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return b1;
    }

    // ------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------

    private void toast (final String message, final int duration) {
        runOnUiThread(() -> {
            Toast.makeText(mContext, message,
                duration == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG
            ).show();
        });
    }
}
