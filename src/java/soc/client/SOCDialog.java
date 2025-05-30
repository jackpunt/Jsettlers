package soc.client;

import java.awt.Dialog;

/** common routines for Dialog panels. */
public class SOCDialog extends Dialog {
    protected SOCPlayerInterface pi;

    public SOCDialog(SOCPlayerInterface pi, String str, boolean b) {
        super(pi, str, b);
        this.pi = pi;
    }

    public void centerInBounds() {
        pi.centerInBounds(this);  // delegate to SOCPlayerInterface
        // setLocation(pb.x + pb.width / 2 - (getWidth() / 2), pb.y + pb.height / 2 - (getHeight() / 2));
    }
}
