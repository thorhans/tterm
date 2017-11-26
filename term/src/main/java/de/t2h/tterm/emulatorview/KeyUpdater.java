package de.t2h.tterm.emulatorview;

//T+{ ************************************************************
/**
 * Created by thh on 2017-07-26.
 */
// TODO ThH: Change `int state´ to an `enum´. It's one of
// - `TextRenderer.MODE_OFF´
// - `TextRenderer.MODE_ON´
// - `TextRenderer.MODE_LOCKED´
public interface KeyUpdater {
    public void updateControl(int state);
    public void updateFn(int state);
}
//T+} ************************************************************
