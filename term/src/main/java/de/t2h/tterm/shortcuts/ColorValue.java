// From the desk of Frank P. Westlake; public domain.

package de.t2h.tterm.shortcuts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.EditText;

import de.t2h.tterm.R;

import static android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;

public class ColorValue
    implements CompoundButton.OnCheckedChangeListener
{
    // ************************************************************
    // Attributes
    // ************************************************************

    private final Context mContext;

    private EditText mValueEditText;

    private final int[] mColor = { 0xFF, 0, 0, 0 };

    private boolean mStarted = false;

    private AlertDialog.Builder mBuilder;

    private boolean mBarLock = false;

    private final boolean[] mLocks = {false, false, false, false};

    private final ImageView mImgView;

    private final String mResult[];

    private String imgtext = "";

    // ************************************************************
    // Methods
    // ************************************************************

    public ColorValue (Context context, final ImageView imgview, final String result[]) {
        mContext = context;
        imgtext = result[0];
        mImgView = imgview;
        mResult = result;
        ColorValue2();
    }

    public void ColorValue2 () {
        final int arraySizes = 4;
        mBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_DARK);

        LinearLayout vertical = new LinearLayout(mContext);
        vertical.setOrientation(LinearLayout.VERTICAL);

        String lab[] = {
            mContext.getString(R.string.colorvalue_letter_alpha) + " ",
            mContext.getString(R.string.colorvalue_letter_red)   + " ",
            mContext.getString(R.string.colorvalue_letter_green) + " ",
            mContext.getString(R.string.colorvalue_letter_blue)  + " "
        };
        int clr[] = {0xFFFFFFFF, 0xFFFF0000, 0xFF00FF00, 0xFF0000FF};
        for(int i = 0, n = (Integer) mImgView.getTag(); i < arraySizes; i++) {
          mColor[i] = (n >> (24 - i*8)) & 0xFF;
        }

        mValueEditText = new EditText(mContext);
        mValueEditText.setText(imgtext);
        mValueEditText.setSingleLine(false);
        mValueEditText.setGravity(Gravity.CENTER);
        mValueEditText.setTextColor((Integer) mImgView.getTag());
        mValueEditText.setBackgroundColor((0xFF<<24)|0x007799);

        LinearLayout horizontal = new LinearLayout(mContext);
        horizontal.setOrientation(LinearLayout.HORIZONTAL);
        horizontal.setGravity(Gravity.CENTER_HORIZONTAL);
        horizontal.addView(mValueEditText);
        mValueEditText.setHint(mContext.getString(R.string.colorvalue_icon_text_entry_hint)); //"Enter icon text"
        vertical.addView(horizontal);

        TextView lockTextView = new TextView(mContext);
        lockTextView.setText(mContext.getString(R.string.colorvalue_label_lock_button_column)); // "LOCK"
        lockTextView.setPadding(lockTextView.getPaddingLeft(), lockTextView.getPaddingTop(),
            5, lockTextView.getPaddingBottom());
        lockTextView.setGravity(Gravity.RIGHT);
        vertical.addView(lockTextView);

        final SeekBar  seekBars[]   = new SeekBar[arraySizes+1];
        final CheckBox lk[]         = new CheckBox[arraySizes];
        final TextView hexWindows[] = new TextView[arraySizes];
        for(int i = 0; i < arraySizes; i++) {
            LinearLayout linear = new LinearLayout(mContext);
            linear.setGravity(Gravity.CENTER_VERTICAL);

            final TextView textView = new TextView(mContext);
            textView.setTypeface(Typeface.MONOSPACE);
            textView.setText(lab[i]);
            textView.setTextColor(clr[i]);

            seekBars[i] = new SeekBar(mContext);
            seekBars[i].setMax(0xFF);
            seekBars[i].setProgress(mColor[i]);
            seekBars[i].setSecondaryProgress(mColor[i]);
            seekBars[i].setTag(i);
            seekBars[i].setBackgroundColor(0xFF << 24 | (mColor[i] << (24 - i*8)));
            seekBars[i].setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 1));
            seekBars[i].setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {

                    public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
                        doProgressChanged(seekBar, progress, fromUser);
                    }

                    private void doProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
                        if(fromUser && mStarted) {
                            int me = (Integer) seekBar.getTag();
                            int k = (mColor[0] << 24) | (mColor[1] <<16) | (mColor[2] << 8) | mColor[3];
                            mValueEditText.setTextColor(k);
                            int start, end;
                            if(mBarLock && mLocks[me]) {
                              start = 0;
                              end = arraySizes-1;
                            } else {
                              start = end = (Integer) seekBar.getTag();
                            }
                            for(int i = start; i <= end; i++) {
                                if(i == me || (mBarLock && mLocks[i])) {
                                    mColor[i] = progress;
                                    toHexWindow(hexWindows[i], mColor[i]);
                                    seekBars[i].setBackgroundColor(0xFF << 24 | (progress << (24 - i*8)));
                                    seekBars[i].setProgress(progress);
                    }   }   }   }

                    public void onStartTrackingTouch (SeekBar seekBar) {
                        doProgressChanged(seekBar, seekBar.getProgress(), true);
                    }

                    public void onStopTrackingTouch (SeekBar seekBar) {
                        doProgressChanged(seekBar, seekBar.getProgress(), true);
                    }
                }
            );

            lk[i] = new CheckBox(mContext);
            lk[i].setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, 0));
            lk[i].setOnCheckedChangeListener(this);
            lk[i].setTag(i);

            linear.addView(textView);
            linear.addView(seekBars[i]);
            linear.addView(lk[i]);
            vertical.addView(linear, MATCH_PARENT, WRAP_CONTENT);
        }

        {   // Evaluating hex windows.

            LinearLayout linear = new LinearLayout(mContext);
            linear.setGravity(Gravity.CENTER);
            for(int i = 0; i < arraySizes; i++) {
                hexWindows[i] = new TextView(mContext);
                toHexWindow(hexWindows[i], mColor[i]);
                linear.addView(hexWindows[i]);
            }
            vertical.addView(linear);
        }

        ScrollView scrollView = new ScrollView(mContext);
        scrollView.addView(vertical);
        mBuilder.setView(scrollView);
        DialogInterface.OnClickListener onClickListener =
            (DialogInterface dialog, int which) ->
                    buttonHit(which, (mColor[0] << 24) | (mColor[1] << 16) | (mColor[2] << 8) | mColor[3]);

        String Title = mContext.getString(R.string.addshortcut_make_text_icon);
        mBuilder.setTitle(Title);
        mBuilder.setPositiveButton(android.R.string.yes,    onClickListener);
        mBuilder.setNegativeButton(android.R.string.cancel, onClickListener);
        mBuilder.show();

        mStarted = true;
    }

    public void toHexWindow (TextView tv, int k) {
        String HEX = "0123456789ABCDEF";
        String s = "";
        int n = 8;
        k &= (1L << 8) - 1L;
        for(n -= 4; n >= 0; n -= 4) s += HEX.charAt((k >> n) & 0xF);
        tv.setText(s);
    }

    public void onCheckedChanged (CompoundButton buttonView, boolean isChecked) {
        int view = (Integer)buttonView.getTag();
        mLocks[view] = isChecked;
        mBarLock = false;
        for(int i = 0; i < mLocks.length; i++) {
          if(mLocks[i]) mBarLock = true;
        }
    }

    private void buttonHit (int hit, int color) {
        switch(hit) {
        case AlertDialog.BUTTON_NEGATIVE: // CANCEL
            return;
        case AlertDialog.BUTTON_POSITIVE: // OK == set
            imgtext = mValueEditText.getText().toString();
            mResult[1] = imgtext;
            mImgView.setTag(color);
            if(! imgtext.equals("")) {
                mImgView.setImageBitmap(
                    TextIcon.getTextIcon(imgtext, color, 96, 96));
            }
            return;
        }
    }
}
