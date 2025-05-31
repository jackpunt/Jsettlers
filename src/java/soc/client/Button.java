package soc.client;

import java.awt.Color;

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
public class Button extends JButton {
  public Button(String label) {
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
  public Button(String label, Color color) {
    super(label);

    JButton button = this;
    if (allowsColor()) {
      // assert the given color, of a silver/grey for legacy 'match'
      button.setBackground(color == null ? ColorSquare.GREY : color);
    } else 
    if (color != null)
    {
      Border padding = new EmptyBorder(2, 4, 2, 4);
      button.setBorder(padding);
      button.setBackground(color);
      button.setBorderPainted(false);
      button.setOpaque(true); // per StackOverflow - Aqua needs this
    }
    // else: no color applied, use system coloration (Aqua -> grey)
  }

  /* some L&F (MacOS Aqua) assert their own color on buttons, 
   * some allow Color (but inherit from parent panel, so must supply GREY)
   */
  boolean allowsColor() {
    String laf = UIManager.getLookAndFeel().getName();
    return !laf.equals("Mac OS X");
  }
}
