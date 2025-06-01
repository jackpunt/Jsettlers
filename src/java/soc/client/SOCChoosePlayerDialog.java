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
import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import soc.game.SOCGame;


/**
 * This is the dialog to ask a player from whom she wants to steal.
 *
 * @author  Robert S. Thomas
 */
class SOCChoosePlayerDialog extends SOCDialog implements ActionListener
{
    AButton[] buttons;
    int[] players;
    int number;
    Label msg;
    SOCPlayerInterface pi;

    /**
     * Creates a new SOCChoosePlayerDialog object.
     *
     * @param plInt this SOCPlayerInterface
     * @param num useful length of p[]
     * @param p array of player numbers from which to choose
     */
    public SOCChoosePlayerDialog(SOCPlayerInterface plInt, int num, int[] p)
    {
        super(plInt, "Choose Player", true);

        pi = plInt;
        number = num;
        players = p;
        setBackground(new Color(255, 230, 162));
        setForeground(Color.black);
        setFont(SOCPlayerInterface.genevaFont2);
        setLayout(null);

        FontMetrics fm = getFontMetrics(getFont());
        msg = new Label("Please choose a player to steal from:", Label.CENTER);
        msg.setSize(fm.stringWidth(msg.getText()) + 2, fm.getHeight() + 2);
        add(msg);

        buttons = new AButton[number];

        SOCGame ga = pi.getGame();
        int bwidth = 0;
        int bheight = 0;

        for (int i = 0; i < number; i++)
        {
            Color playerColor = pi.getPlayerColor(players[i]);
            AButton button = new AButton(ga.getPlayer(players[i]).getName(), playerColor);
            bwidth = Math.max(bwidth, button.getSize().width); // find max width
            bheight = Math.max(bheight, button.getSize().height); // find max height
            button.addActionListener(this);
            buttons[i] = button;
            add(button);
        }
        for (int i = 0; i< number; i++) {
          buttons[i].setSize(bwidth, bheight); // make sizes equal
        }

        int space = 10; // between buttons
        int vspace = 10; // vertical whitespace above & below & between?
        //  twidth = [space/2, bwidth, space, ..., bwidth, space/2]
        int twidth = buttons.length * (buttons[0].getWidth() + space);
        // Insets not yet instantiated, use 28 for top;
        int theight = 28 + 4 * vspace + msg.getHeight() + bheight ;
        setSize(Math.max(msg.getWidth() + 2 * space, twidth), theight);
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
            buttons[0].requestFocus();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {

        int space = 10;
        int vspace = 10;
        int bwidth = buttons[0].getWidth();
        int bheight = buttons[0].getHeight();
        int twidth = buttons.length * (buttons[0].getWidth() + space);

        int theight = getInsets().top + 2 * vspace + msg.getHeight() + bheight + getInsets().bottom;
        setSize(Math.max(msg.getWidth() + 2 * space, twidth), theight);

        int width = getSize().width - getInsets().left - getInsets().right;
        int height = getSize().height - getInsets().top - getInsets().bottom;
        int x = getInsets().left + (width - msg.getWidth()) / 2; // in case msgWidth < twidth
        int y = getInsets().top + vspace;

        /* put the dialog in the center of the game window */
        centerInBounds();

        int bx0 = (width - twidth + space) / 2;
        int bby = (getInsets().top + height) - (bheight + vspace);
        try
        {
            msg.setLocation(x, y);

            for (int i = 0; i < number; i++)
            {
                buttons[i].setLocation(bx0 + i * (bwidth + space), bby);
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

        for (int i = 0; i < number; i++)
        {
            if (target == buttons[i])
            {
                pi.getClient().choosePlayer(pi.getGame(), players[i]);
                dispose();

                break;
            }
        }
    }
}
