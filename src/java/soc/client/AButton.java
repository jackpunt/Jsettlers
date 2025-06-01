package soc.client;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;

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
  public AButton(String label) {
    this(label, null);
    isJButton = this.getClass().getSuperclass() == JButton.class;
    isAwtButton = this.getClass().getSuperclass() == Button.class;
  }
  boolean isJButton;
  boolean isAwtButton;

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
    super(label);

    setSize(getExpectedSize());

    if (lafAllowsColor()) {
      // assert the given color, of a silver/grey for legacy 'match'
      this.setBackground(color == null ? new Color(0xBBBBBB) : color);
    } else 
    if (color != null)
    {
      setBackground(color);
      if (isJButton) {
          Border padding = new EmptyBorder(2, 4, 2, 4);
          setBorder(padding);
          setBorderPainted(false);
          setOpaque(true); // per StackOverflow - Aqua needs this
       }
    }
    // else: no color applied, use system coloration (Aqua -> grey)
  }

  Dimension getExpectedSize() {
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
      return getPreferredSize();
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
