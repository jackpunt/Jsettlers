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
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import soc.game.SOCGame;


/**
 * This is the dialog to ask a player from whom she wants to steal.
 *
 * @author  Robert S. Thomas
 */
class SOCChoosePlayerDialog extends SOCDialog implements ActionListener
{
    JButton[] buttons;
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
        setFont(new Font("Geneva", Font.PLAIN, SOCHandPanel.fontSize + 2));
        setLayout(null);
        setSize(350, 100+getInsets().top+getInsets().bottom);

        msg = new Label("Please choose a player to steal from:", Label.CENTER);
        add(msg);

        buttons = new JButton[number];

        SOCGame ga = pi.getGame();

        for (int i = 0; i < number; i++)
        {
            Color playerColor = pi.getPlayerColor(players[i]);
            JButton button = new JButton(ga.getPlayer(players[i]).getName());
            button.setOpaque(true);          // MacOS paints the background of panel?
            button.setBorderPainted(false);  // Stack Overflow says: use JButton & do this.
            button.setBackground(playerColor);
            button.addActionListener(this);
            buttons[i] = button;
            add(button);
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
            buttons[0].requestFocus();
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {
        int x = getInsets().left;
        int y = getInsets().top;
        int width = getSize().width - getInsets().left - getInsets().right;
        int height = getSize().height - getInsets().top - getInsets().bottom;
        int space = 10;
        int vhite = 20;

        // int piX = pi.getInsets().left;
        // int piY = pi.getInsets().top;
        // int piWidth = pi.getSize().width - pi.getInsets().left - pi.getInsets().right;
        // int piHeight = pi.getSize().height - pi.getInsets().top - pi.getInsets().bottom;

        int bwidth = (width - ((number - 1 + 2) * space)) / number;

        /* put the dialog in the center of the game window */
        //setLocation(piX + ((piWidth - width) / 2), piY + ((piHeight - height) / 2));
        centerInBounds();

        try
        {
            msg.setBounds(x, y, width, vhite);

            for (int i = 0; i < number; i++)
            {
                buttons[i].setBounds(x + space + (i * (bwidth + space)), (getInsets().top + height) - (vhite + space), bwidth, vhite);
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
