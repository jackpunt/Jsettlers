package soc.client;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Button using JButton, can be colored.\
 * 
 * because Mac L&F makes everything grey,
 * and other L&F makes everything same/inherit.
 * 
 * want a clean way to say: make this button colored vs inherited.
 */
public class AButton extends JButton {
  static Color defColor = new Color(0xEEEEEE);
  boolean isJButton;
  boolean isAwtButton;

  public AButton(String label) {
    this(label, null);
  }

  /**
   * color will be either:
   * 
   * A: grey button (from L&F or set it). 
   * 
   * B: color button to resource/player color.
   * 
   * @param label
   * @param color - null -> grey, color -> specific color
   * @param force
   */
  public AButton(String label, Color color) {
    this(label, color, null);
  }

  public AButton(String label, Color color, Font font) {
    super(label);
    isJButton = this.getClass().getSuperclass() == JButton.class;
    isAwtButton = this.getClass().getSuperclass() == Button.class;
    if (font != null) setFont(font);

    Color aColor = (color == null) ? defColor : color;

    String laf = UIManager.getLookAndFeel().getName();

    if (!laf.equals("Mac OS X")) {
      // can paint button background! but use soft bevel:
      int pad = 6;  // space before/after text, for CDE/Motif
      setBorder(BorderFactory.createRaisedSoftBevelBorder());
      setMargin(new Insets(2, 2, 2, 2)); // smallest that work
      setSize(getExpectedSize(pad));
      // assert the given color, or a silver/grey for legacy 'match'
      setBackground(aColor);
    } else 
    if (color != null)
    {
      setBackground(color);
      if (isJButton) {
          // to get color, we need a non-Mac border:
          setBorder(BorderFactory.createRaisedSoftBevelBorder()); // or new EmptyBorder(2, 2, 2, 2);
          setBorderPainted(false);
          setOpaque(true); // per StackOverflow - Aqua needs this
       }
    }
    // else: no color applied, use system coloration (Aqua -> grey)
  }

  Dimension getExpectedSize(int xpad) {
    if (isAwtButton) {
        Dimension ps = getPreferredSize();
        if (ps.width > 0) {
            return ps;
        } else {
            int border_padding = 5;
            FontMetrics fm = getFontMetrics(getFont());
            String label = getLabel();
            int w = fm.stringWidth(label) + border_padding;
            int h = fm.stringWidth(label) + border_padding;
            return new Dimension(w, h);
        }
    } else {
      Dimension ps = getPreferredSize();
      return new Dimension(ps.width + xpad, ps.height);
    }
  }

  /* some L&F (MacOS Aqua) assert their own color on buttons, 
   * some allow Color (but inherit from parent panel, so must supply GREY)
   */
  boolean lafAllowsColor() {
    String laf = UIManager.getLookAndFeel().getName();
    return !laf.equals("Mac OS X");
  }
}
