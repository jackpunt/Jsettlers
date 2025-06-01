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

// import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;


class SOCDiscoveryDialog extends SOCDialog implements ActionListener
{
    int space = 5;       // margins (insets are (20, 0, 0, 0): the OS supplied framing)
    int buttonW = 50;    // set from fm.stringWidth("Clear")
    int buttonH = 25;    // height of buttons (fontHeight + 3) geneva-16

    String msgText = "Please pick two resources.";
    int msgWidth = 180;  // tweaked below by fontMetrics
    int msgHeight = 20;

    Label msg;
    ColorSquare[] rsrc;
    AButton clearBut;
    AButton doneBut;

    SOCPlayerInterface pi;

    /**
     * Creates a new SOCDiscoveryDialog object.
     *
     * @param pi DOCUMENT ME!
     */
    public SOCDiscoveryDialog(SOCPlayerInterface pi)
    {
        super(pi, "Discovery", true);

        this.pi = pi;

        // set rsrc *before* doing anything that might provoke doLayout()
        rsrc = new ColorSquare[5];
        for (int i = 0; i < 5; i++)
        {
	          rsrc[i] = new ColorSquare(ColorSquare.BOUNDED_INC, true, ColorSquare.RES_COLORS[i], 2, 0);
            add(rsrc[i]);
        }

        setBackground(new Color(255, 230, 162));
        setForeground(Color.black);
        Font font = SOCPlayerInterface.genevaFont2;
        Font font2 = SOCPlayerInterface.genevaFont2;
        setFont(font);

        FontMetrics fm = getFontMetrics(font);

        clearBut = new AButton("Clear", null, font);
        doneBut = new AButton("Done", null, font);

        buttonH = clearBut.getHeight();
        buttonW = clearBut.getWidth();

        if (doneBut.getSize().width > buttonW) { // which is TRUE!
          buttonW = doneBut.getWidth();
          clearBut.setSize(buttonW, buttonH);
        }

        msgWidth = fm.stringWidth(msgText);
        msgHeight = fm.getHeight();
        msg = new Label(msgText, Label.CENTER);
        msg.setSize(new Dimension(msgWidth, msgHeight));
        add(msg);

        add(doneBut);
        doneBut.addActionListener(this);

        add(clearBut);
        clearBut.addActionListener(this);

        setLayout(null);
        addNotify();
        setSize(Math.max(280, msgWidth + 2 * space), 190);

    }

    /**
     * DOCUMENT ME!
     *
     * @param b DOCUMENT ME!
     */
    public void setVisible(boolean b)
    {
        super.setVisible(b);

        if (b)
        {
            doneBut.requestFocus();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {
        // int x = getInsets().left;
        int top = 0; //getInsets().top;
        int width = getSize().width - getInsets().left - getInsets().right;
        int height = getSize().height;

        int sqwidth = ColorSquare.WIDTH;
        int sqspace = (int) Math.min((width - (5 * sqwidth)) / 5, sqwidth * 1.5);

        int msgY = space + buttonH;

        int buttonX = (width - ((2 * buttonW) + space)) / 2;
        int buttonY = top + height - buttonH - 2 * space;
        int rsrcY;

        /* put the dialog in the center of the game window */
        centerInBounds();

        if (msg != null)
        {
            msg.setLocation((width - msgWidth) / 2, msgY);
        }

        if (clearBut != null)
        {
            clearBut.setLocation(buttonX, buttonY);
        }

        if (doneBut != null)
        {
            doneBut.setLocation(buttonX + buttonW + space, buttonY);
        }

        try
        {
            rsrcY = top + (height) / 2 ;
            int rsrcW = (4 * sqspace) + (5 * sqwidth);
            int rsrcLeft = space + (width - rsrcW) / 2;

            for (int i = 0; i < 5; i++)
            {
                rsrc[i].setSize(sqwidth, sqwidth);
                rsrc[i].setLocation((i * (sqspace + sqwidth) + rsrcLeft), rsrcY);
            }
        }
        catch (NullPointerException e) {
          System.err.print(e); // somehow this.rsrc == null
          System.err.println(" -- rsrc is null");
          // e.printStackTrace(System.err);
      }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e)
    {
        Object target = e.getSource();

        if (target == doneBut)
        {
            int[] rsrcCnt = new int[5];
            int i;
            int sum = 0;

            for (i = 0; i < 5; i++)
            {
                rsrcCnt[i] = rsrc[i].getIntValue();
                sum += rsrcCnt[i];
            }

            if (sum == 2)
            {
		            int m1 = -SOCResourceConstants.MIN;
                SOCResourceSet resources = new SOCResourceSet(rsrcCnt[m1+SOCResourceConstants.CLAY],
							      rsrcCnt[m1+SOCResourceConstants.ORE],
							      rsrcCnt[m1+SOCResourceConstants.SHEEP],
							      rsrcCnt[m1+SOCResourceConstants.WHEAT],
							      rsrcCnt[m1+SOCResourceConstants.WOOD], 0);
                pi.getClient().discoveryPick(pi.getGame(), resources);
                dispose();
            }
        }
        else if (target == clearBut)
        {
            for (int i = 0; i < 5; i++)
            {
                rsrc[i].setIntValue(0);
            }
        }
    }
}
