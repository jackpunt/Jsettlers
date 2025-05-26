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
import java.awt.Font;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
    Button discardBut;
    ColorSquare[] keep;
    ColorSquare[] disc;
    Label msg;
    Label youHave;
    Label discThese;
    SOCPlayerInterface playerInterface;
    int numDiscards;

    void centerInBounds(Rectangle pb) {
        setLocation(pb.x + pb.width / 2 - (getWidth() / 2), pb.y + pb.height / 2 - (getHeight() / 2));
    }

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
        setFont(new Font("Geneva", Font.PLAIN, SOCHandPanel.fontSize + 2));

        discardBut = new Button("Discard");

        setLayout(null);
        
        setSize(280, 190+getInsets().top+getInsets().bottom);

        msg = new Label("Please discard " + Integer.toString(numDiscards) + " resources.", Label.CENTER);
        add(msg);
        youHave = new Label("You have:", Label.LEFT);
        add(youHave);
        discThese = new Label("Discard these:", Label.LEFT);
        add(discThese);

        add(discardBut);
        discardBut.addActionListener(this);

	int m1 = -SOCResourceConstants.MIN;
        keep = new ColorSquare[5];
        keep[m1+SOCResourceConstants.CLAY] = new ColorSquare(ColorSquare.BOUNDED_DEC, false, ColorSquare.CLAY);
        keep[m1+SOCResourceConstants.ORE] = new ColorSquare(ColorSquare.BOUNDED_DEC, false, ColorSquare.ORE);
        keep[m1+SOCResourceConstants.SHEEP] = new ColorSquare(ColorSquare.BOUNDED_DEC, false, ColorSquare.SHEEP);
        keep[m1+SOCResourceConstants.WHEAT] = new ColorSquare(ColorSquare.BOUNDED_DEC, false, ColorSquare.WHEAT);
        keep[m1+SOCResourceConstants.WOOD] = new ColorSquare(ColorSquare.BOUNDED_DEC, false, ColorSquare.WOOD);

        disc = new ColorSquare[5];
        disc[m1+SOCResourceConstants.CLAY] = new ColorSquare(ColorSquare.BOUNDED_INC, false, ColorSquare.CLAY);
        disc[m1+SOCResourceConstants.ORE] = new ColorSquare(ColorSquare.BOUNDED_INC, false, ColorSquare.ORE);
        disc[m1+SOCResourceConstants.SHEEP] = new ColorSquare(ColorSquare.BOUNDED_INC, false, ColorSquare.SHEEP);
        disc[m1+SOCResourceConstants.WHEAT] = new ColorSquare(ColorSquare.BOUNDED_INC, false, ColorSquare.WHEAT);
        disc[m1+SOCResourceConstants.WOOD] = new ColorSquare(ColorSquare.BOUNDED_INC, false, ColorSquare.WOOD);

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
            keep[m1+SOCResourceConstants.CLAY].setIntValue(resources.getAmount(SOCResourceConstants.CLAY));
            keep[m1+SOCResourceConstants.ORE].setIntValue(resources.getAmount(SOCResourceConstants.ORE));
            keep[m1+SOCResourceConstants.SHEEP].setIntValue(resources.getAmount(SOCResourceConstants.SHEEP));
            keep[m1+SOCResourceConstants.WHEAT].setIntValue(resources.getAmount(SOCResourceConstants.WHEAT));
            keep[m1+SOCResourceConstants.WOOD].setIntValue(resources.getAmount(SOCResourceConstants.WOOD));

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
        int width = getSize().width - getInsets().left - getInsets().right;
        int height = getSize().height - getInsets().top - getInsets().bottom;
        int space = 5;			// leading between rows
        int vhite = 20; // height of text

        // int cfx = playerInterface.getInsets().left;
        // int cfy = playerInterface.getInsets().top;
        // int cfwidth = playerInterface.getSize().width - playerInterface.getInsets().left - playerInterface.getInsets().right;
        // int cfheight = playerInterface.getSize().height - playerInterface.getInsets().top - playerInterface.getInsets().bottom;

        int sqwidth = ColorSquare.WIDTH;
        int sqspace = (width - (5 * sqwidth)) / 5;

        int keepY;
        int discY;

        /* put the dialog in the center of the game window */
        //setLocation(cfx + ((cfwidth - width) / 2), cfy + ((cfheight - height) / 2));
        centerInBounds();

        try
        {
            msg.setBounds((width - 188) / 2, getInsets().top, 180, vhite);
            discardBut.setBounds((width - 88) / 2, (getInsets().top + height) - (vhite+5+space), 80, vhite+5);
            youHave.setBounds(getInsets().left, getInsets().top + vhite + space, 70, vhite);
            discThese.setBounds(getInsets().left, getInsets().top + vhite + space + vhite + space + sqwidth + space, 100, vhite);
        }
        catch (NullPointerException e) {}

        keepY = getInsets().top + vhite + space + vhite + space;
        discY = keepY + sqwidth + space + vhite + space;

        try
        {
            for (int i = 0; i < 5; i++)
            {
                keep[i].setSize(sqwidth, sqwidth);
                keep[i].setLocation((i * sqspace) + ((width - ((3 * sqspace) + (4 * sqwidth))) / 2), keepY);
                disc[i].setSize(sqwidth, sqwidth);
                disc[i].setLocation((i * sqspace) + ((width - ((3 * sqspace) + (4 * sqwidth))) / 2), discY);
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
