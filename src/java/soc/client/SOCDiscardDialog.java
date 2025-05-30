/**
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * Copyright (C) 2003  Robert S. Thomas
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * The author of this program can be reached at thomas@infolab.northwestern.edu
 **/
package soc.client;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;

import soc.game.SOCPlayer;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;


/**
 * This is the dialog to ask players what resources they want
 * to discard.
 *
 * @author  Robert S. Thomas
 */
class SOCDiscardDialog extends SOCDialog implements ActionListener, MouseListener
{
    JButton discardBut;
    ColorSquare[] keep;
    ColorSquare[] disc;
    Label msg;
    Label youHave;
    Label discThese;
    SOCPlayerInterface playerInterface;
    int numDiscards;
    String msgFmt = "Please discard %s resources.";

    // void centerInBounds(Rectangle pb) {
    //     setLocation(pb.x + pb.width / 2 - (getWidth() / 2), pb.y + pb.height / 2 - (getHeight() / 2));
    // }

    /**
     * Creates a new SOCDiscardDialog object.
     *
     * @param pi DOCUMENT ME!
     * @param rnum DOCUMENT ME!
     */
    public SOCDiscardDialog(SOCPlayerInterface pi, int rnum)
    {
        super(pi, "Discard", true);

        playerInterface = pi;
        numDiscards = rnum;
        setBackground(new Color(255, 230, 162));
        setForeground(Color.black);
        Font font = SOCPlayerInterface.genevaFont2;
        setFont(font);

        discardBut = new JButton("Discard");

        setLayout(null);
        
        // 280 X 190+insets @ fontSize=12; ~9 * squareWidth
        FontMetrics fm = getFontMetrics(getFont());
        int innerWidth = fm.stringWidth(msgFmt) + 5 * ColorSquare.WIDTH;
        Insets insets = getInsets();
        setSize(innerWidth + insets.left + insets.right, 220 + insets.top + insets.bottom);

        msg = new Label(String.format(msgFmt, numDiscards), Label.CENTER);
        add(msg);
        youHave = new Label("You have:", Label.LEFT);
        add(youHave);
        discThese = new Label("Discard these:", Label.LEFT);
        add(discThese);

        add(discardBut);
        discardBut.addActionListener(this);

	      int m1 = -SOCResourceConstants.MIN;
        keep = new ColorSquare[5];
        for (int rs : SOCResourceConstants.EACH) {
            keep[rs + m1] = new ColorSquare(ColorSquare.BOUNDED_DEC, false, ColorSquare.RES_COLORS[rs-1]);
        }

        disc = new ColorSquare[5];
        for (int rs : SOCResourceConstants.EACH) {
            disc[rs + m1] = new ColorSquare(ColorSquare.BOUNDED_INC, false, ColorSquare.RES_COLORS[rs-1]);
        }

        for (int i = 0; i < 5; i++)
        {
            add(keep[i]);
            add(disc[i]);
            keep[i].addMouseListener(this);
            disc[i].addMouseListener(this);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setVisible(boolean b)
    {
        if (b)
        {
            /**
             * set initial values
             */
            SOCPlayer player = playerInterface.getGame().getPlayer(playerInterface.getClient().getNickname());
            SOCResourceSet resources = player.getResources();
	          int m1 = -SOCResourceConstants.MIN;
            for (int rs : SOCResourceConstants.EACH) {
                keep[rs+m1].setIntValue(resources.getAmount(rs));
            }

            discardBut.requestFocus();
        }

        super.setVisible(b);
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {
        // int x = getInsets().left;
        // int y = getInsets().top;
        FontMetrics fm = getFontMetrics(getFont());
        int msgWidth = fm.stringWidth(msgFmt);
        int yhWidth = fm.stringWidth(youHave.getText()) + 2;
        int dtWidth = fm.stringWidth(discThese.getText()) + 2;
        Insets insets = getInsets();
        int top = 5 + insets.top;
        int left = 10 + insets.left;

        Dimension dcDim = discardBut.getPreferredSize();
        int dcWidth = dcDim.width;
        int dcHeight = dcDim.height;

        int width = getSize().width - getInsets().left - getInsets().right;
        int height = getSize().height - getInsets().top - getInsets().bottom;
        int space = 5;			// leading between rows
        int vhite = fm.getHeight() + 4;

        int sqwidth = ColorSquare.WIDTH;
        int sqspace = (int) Math.min((width - (5 * sqwidth)) / 5, sqwidth * 1.5);

        int keepY;
        int discY;

        /* put the dialog in the center of the game window */
        centerInBounds();

        try
        { // WTF? insets={0, 0, 0, 20} bot, left, right, top; width = 320
            msg.setBounds((width - msgWidth) / 2, top, msgWidth, vhite); // 280 X 190
            discardBut.setBounds((width - dcWidth) / 2, (top + height) - (vhite+5+space), dcWidth, dcHeight);
            youHave.setBounds(left, top + vhite + space, yhWidth, vhite);
            discThese.setBounds(left, top + vhite + space + vhite + space + sqwidth + space, dtWidth, vhite);
        }
        catch (NullPointerException e) {}

        keepY = top + vhite + space + vhite + space; // below 2 lines of text
        discY = keepY + sqwidth + space + vhite + space;

        try
        {
            int centerX = ((width - ((3 * sqspace) + (4 * sqwidth))) / 2);
            for (int i : SOCResourceConstants.EACH)
            {
                keep[i-1].setSize(sqwidth, sqwidth);
                keep[i-1].setLocation((i * sqspace) + centerX, keepY);
                disc[i-1].setSize(sqwidth, sqwidth);
                disc[i-1].setLocation((i * sqspace) + centerX, discY);
            }
        }
        catch (NullPointerException e) {}
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e)
    {
        Object target = e.getSource();

        if (target == discardBut)
        {
	    int m1 = -SOCResourceConstants.MIN;
            SOCResourceSet rsrcs = new SOCResourceSet(disc[m1+SOCResourceConstants.CLAY].getIntValue(),
						      disc[m1+SOCResourceConstants.ORE].getIntValue(),
						      disc[m1+SOCResourceConstants.SHEEP].getIntValue(),
						      disc[m1+SOCResourceConstants.WHEAT].getIntValue(),
						      disc[m1+SOCResourceConstants.WOOD].getIntValue(), 0);

            if (rsrcs.getTotal() == numDiscards)
            {
                playerInterface.getClient().discard(playerInterface.getGame(), rsrcs);
                dispose();
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseEntered(MouseEvent e)
    {
        ;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseExited(MouseEvent e)
    {
        ;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseClicked(MouseEvent e)
    {
        ;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseReleased(MouseEvent e)
    {
        ;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mousePressed(MouseEvent e)
    {
        Object target = e.getSource();

        for (int i = 0; i < 5; i++)
        {
            if ((target == keep[i]) && (disc[i].getIntValue() > 0))
            {
                keep[i].addValue(1);
                disc[i].subtractValue(1);
            }
            else if ((target == disc[i]) && (keep[i].getIntValue() > 0))
            {
                keep[i].subtractValue(1);
                disc[i].addValue(1);
            }
        }
    }
}
