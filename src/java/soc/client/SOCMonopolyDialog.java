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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import soc.game.SOCResourceConstants;


class SOCMonopolyDialog extends SOCDialog implements ActionListener
{
    AButton[] rsrcBut;
    Label msg;
    int msgWidth;
    int msgHeight;
    int buttonW;
    int buttonH;

    SOCPlayerInterface pi;

    /**
     * Creates a new SOCMonopolyDialog object.
     *
     * @param pi DOCUMENT ME!
     */
    public SOCMonopolyDialog(SOCPlayerInterface pi)
    {
        super(pi, "Monopoly", true);

        this.pi = pi;
        Font font = SOCPlayerInterface.genevaFont2;
        setBackground(new Color(255, 230, 162));
        setForeground(Color.black);
        setFont(font);
        setLayout(null);
        addNotify();

        FontMetrics fm = getFontMetrics(font);
        String msgText = "Pick a resource to monopolize.";
        msg = new Label(msgText, Label.CENTER);
        msgWidth = fm.stringWidth(msgText);
        msgHeight = fm.getHeight() + 1;
        add(msg);

        setSize(msgWidth + 20, 170);        // setSize(280, 160);

        // make button to determine preferred size:
        AButton max = new AButton(SOCResourceConstants.names[SOCResourceConstants.WHEAT], ColorSquare.WHEAT);
        max.setFont(font);
        Dimension buttonD = max.getPreferredSize();
        buttonW = buttonD.width;
        buttonH = buttonD.height;

        rsrcBut = new AButton[5];

      	// five buttons for five resource names:
        for (int i = 0; i < 5; i++)
        {
            // color button to match resource
            Color color = ColorSquare.RES_COLORS[i];
            AButton button = new AButton(SOCResourceConstants.names[SOCResourceConstants.MIN + i], color);
            button.setFont(font);
            button.setSize(buttonD);
            add(button);
            button.addActionListener(this);
            rsrcBut[i] = button;
        }
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
            rsrcBut[0].requestFocus();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {
        int width = getSize().width - getInsets().left - getInsets().right;
        int height = getSize().height - getInsets().top - getInsets().bottom;
        int space = 5; // horizontal gap between buttons

        int button2X = (width - ((2 * buttonW) + space)) / 2;       // 2 in top row
        int button3X = (width - ((3 * buttonW) + (2 * space))) / 2; // 3 in bottom row

        /* put the dialog in the center of the game window */
        centerInBounds();

        try
        {
            msg.setBounds((width - msgWidth) / 2, getInsets().top + space, msgWidth, msgHeight);
            int DX = (space + buttonW);
            int Y1 = (height) - 2 * (buttonH + space); // was height + gi().bottom
            int Y2 = (height) - 1 * (buttonH + space);
            rsrcBut[0].setLocation(button2X + 0 * DX, Y1);
            rsrcBut[1].setLocation(button2X + 1 * DX, Y1);
            rsrcBut[2].setLocation(button3X + 0 * DX, Y2);
            rsrcBut[3].setLocation(button3X + 1 * DX, Y2);
            rsrcBut[4].setLocation(button3X + 2 * DX, Y2);
        }
        catch (NullPointerException e) {}
    }

    /**
     * Convert button action to selected SOCResourceConstant
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e)
    {
        Object target = e.getSource();

        for (int i = 0; i < 5; i++)
        {
            if (target == rsrcBut[i])
            {
                pi.getClient().monopolyPick(pi.getGame(), i + SOCResourceConstants.MIN);
                dispose();

                break;
            }
        }
    }
}
