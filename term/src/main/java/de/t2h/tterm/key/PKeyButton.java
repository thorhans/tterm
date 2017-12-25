package de.t2h.tterm.key;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.function.Consumer;

import de.t2h.tterm.emulatorview.EmulatorView;

/** A programmable key's view.
 */
public class PKeyButton extends Button {
  protected PKey mModel;
  public void setModel(PKey model) {
    mModel = model;
    setText(model.getLabel());
  }

  public PKeyButton(Context context) { super(context); }
  public PKeyButton(Context context, AttributeSet attrs) { super(context, attrs); }
  public PKeyButton(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
  public PKeyButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

  // TODO Why can't we use `Consumer<View>´ before API 24, but `OnClickListener´ works?
  public void setCallback(OnClickListener callback) {
    if(mModel.mRepeat) {
      setOnTouchListener(new View.OnTouchListener() {

        private View mButton;
        private Handler mHandler;

        Runnable mAction = new Runnable() {
          @Override
          public void run() {
            callback.onClick(mButton);
            mHandler.postDelayed(this, 50);
          }
        };

        public boolean onTouch(View v, MotionEvent event) {
          mButton = v;
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              if (mHandler != null) return true;
              mHandler = new Handler();
              callback.onClick(mButton);
              mHandler.postDelayed(mAction, 200);
              break;
            case MotionEvent.ACTION_UP:
              if (mHandler == null) return true;
              mHandler.removeCallbacks(mAction);
              mHandler = null;
              break;
          }
          return false;
        }
      });
    } else {
      setOnClickListener((View v) -> callback.onClick(v));
    }
  }
}