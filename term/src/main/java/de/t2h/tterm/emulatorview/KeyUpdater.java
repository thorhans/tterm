package de.t2h.tterm.emulatorview;

/**
 */
// TODO ThH: Change `int state´ to an `enum´. It's one of
// - `TextRenderer.MODE_OFF´
// - `TextRenderer.MODE_ON´
// - `TextRenderer.MODE_LOCKED´
// ThH: Cleaned up.
//
public interface KeyUpdater {
    void updateControl(int state);
    void updateFn(int state);
}
