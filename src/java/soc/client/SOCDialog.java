package soc.client;

import java.awt.Dialog;
import java.awt.Rectangle;
import java.awt.Frame;

/** common routines for Dialog panels. */
public class SOCDialog extends Dialog {
    protected SOCPlayerInterface pi;

    public SOCDialog(SOCPlayerInterface pi, String str, boolean b) {
        super(pi, str, b);
        this.pi = pi;
    }

    public void centerInBounds() {
        Rectangle pb = pi.getBounds();
        setLocation(pb.x + pb.width / 2 - (getWidth() / 2), pb.y + pb.height / 2 - (getHeight() / 2));
    }
}
