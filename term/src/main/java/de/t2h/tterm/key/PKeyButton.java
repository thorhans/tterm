package de.t2h.tterm.key;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;

import de.t2h.tterm.Term;
import de.t2h.tterm.emulatorview.TextRenderer;

/** A programmable key's view.
 *
 * @see PKey
 */
public class PKeyButton extends Button {

  // ************************************************************
  // Attributes
  // ************************************************************

  private static OnClickListener mSendOnClick;
  /** Register `onClick´ handler for `PKey´ with `Kind.send´. */
  public static void registerSendOnClick (OnClickListener onClick) { mSendOnClick = onClick; }

  private static OnClickListener mWriteOnClick;
  /** Register `onClick´ handler for `PKey´ with `Kind.write´. */
  public static void registerWriteOnClick (OnClickListener onClick) { mWriteOnClick = onClick; }

  private static HashMap<String, OnClickListener> mSpecialOnClick = new HashMap<>();
  /** Register `onClick´ handler for `PKey´ with `Kind.special´. */
  public static void registerSpecialOnClick (String name, OnClickListener onClick) {
    mSpecialOnClick.put(name, onClick); }

  private PKey mModel;
  public PKey getModel () { return mModel; }
  public void setModel (PKey model) {
    mModel = model;
    setText(mModel.getLabel());
    if(mModel.getKind() == PKey.Kind.send  && mSendOnClick  != null) setOnClickListener(mSendOnClick);
    if(mModel.getKind() == PKey.Kind.write && mWriteOnClick != null) setOnClickListener(mWriteOnClick);
    if(mModel.getKind() == PKey.Kind.special) setOnClickListener(mSpecialOnClick.get(mModel.getName()));
    model.addButton(this);
  }

  public void setState (int state) {
      switch(state) {
      case TextRenderer.MODE_ON:     setTextColor(Color.GREEN);           break;
      case TextRenderer.MODE_LOCKED: setTextColor(Color.RED);             break;
      case TextRenderer.MODE_OFF:
      default:                       setTextColor(Term.mExtraKeyDefaultColor); break;
      }
  }

  // ************************************************************
  // Methods
  // ************************************************************

  public PKeyButton (Context context) { super(context); }
  public PKeyButton (Context context, AttributeSet attrs) { super(context, attrs); }
  public PKeyButton (Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }
  public PKeyButton (Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) { super(context, attrs, defStyleAttr, defStyleRes); }

  /** Register a callback to be invoked when this button is clicked <b>or auto-repeats</b>.
   *
   * <p>To handle a single key press, we pass the `callback´ directly to {@link Button#setOnClickListener},
   * so a single callback object can handle many buttons.</p>
   * 
   * <p>To handle auto-repeats, we set up an {@link OnTouchListener} object for each auto-repeating key's
   * button. It calls {@link #performClick} for each key-press.</p>
   */
  @Override
  public void setOnClickListener (OnClickListener onClick) {
    super.setOnClickListener(onClick);
    if(mModel.getRepeat()) {
      setOnTouchListener(new PKeyButton.RepeatingOnTouchListener());
    }
  }

  // ************************************************************
  // Member classes
  // ************************************************************

  /** Listener to handle auto-repeating for one button.
   *
   * https://stackoverflow.com/questions/4284224/android-hold-button-to-repeat-action
   *
   * <p>Start repeating after 200 ms, repeating every 50 ms.</p>
   *
   * <p>I have not searched for a way to share this object between buttons yet, since it's only relevant if we
   * have very many auto-repeating keys./p>
   *
   * TODO Move this to `de.t2h.widget´?
   */
  private static class RepeatingOnTouchListener implements View.OnTouchListener {

    // ************************************************************
    // Attributes
    // ************************************************************

    // The view passed in by `onTouch´. Since I haven't found a way to share a `RepeatingOnTouchListener´
    // between two buttons yet, I could make `RepeatingOnTouchListener´ a non-static member class instead.
    private Button mButton;

    private Rect mRect;

    private Handler mHandler;

    Runnable mAction = new Runnable() {
      @Override
      public void run () {
        mButton.performClick();          // an auto-repeated key-press
        mHandler.postDelayed(this, 50);
      }
    };

    // ************************************************************
    // Methods
    // ************************************************************

    /**
     * @return True if the listener has consumed the event, false otherwise.
     *
     * @see View.OnTouchListener
     */
    public boolean onTouch (View v, MotionEvent event) {
      mButton = (Button) v;

      switch(event.getAction()) {

        case MotionEvent.ACTION_DOWN:
          if(mHandler != null) return true;     // Consume the event.

          mButton.performClick();               // the first key-press

          mRect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());

          mHandler = new Handler();
          mHandler.postDelayed(mAction, 200);

          return true;                          // Consume the event, since we called `performClick´.

        case MotionEvent.ACTION_MOVE:           // Has user moved outside the button?
          if(mRect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
            break;
          }
          // break;                             // User moved outside the button, fall through to
                                                // `ACTION_CANCEL´.

        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
          if(mHandler == null) return true;     // Consume the event.

          mHandler.removeCallbacks(mAction);
          mHandler = null;
          break;                                // LATER Maybe we should return true to consume the event.
                                                // But at a repeat of 50 ms it doesn't make a difference.
      }
      return false; // Do *not* consume the event.
    }
  }
}
