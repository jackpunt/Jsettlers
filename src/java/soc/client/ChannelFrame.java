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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import java.util.Vector;


/** The chat channel window
 *  @version 2.0 (no GridbagLayout) with textwrapping and customized window
 *  @author <A HREF="http://www.nada.kth.se/~cristi">Cristian Bogdan</A>
 */
public class ChannelFrame extends Frame
{
    public SnippingTextArea ta;
    public TextField tf;
    public java.awt.List lst;
    public Canvas cnvs;
    public int ncols;
    public int npix = 1;
    SOCPlayerClient cc;
    String cname;
    Vector history = new Vector();
    int historyCounter = 1;
    boolean down = false;

    /** build a frame with the given title, belonging to the given applet*/
    public ChannelFrame(String t, SOCPlayerClient ccp)
    {
        super("Channel: " + t);
        setBackground(ccp.getBackground());
        setForeground(ccp.getForeground());

        ta = new SnippingTextArea("", 100);
        tf = new TextField("Please wait...");
        lst = new java.awt.List(0, false);
        cc = ccp;
        cname = t;
        ta.setEditable(false);
        tf.setEditable(false);
        cnvs = new Canvas();
        cnvs.setBackground(Color.lightGray);
        cnvs.setSize(5, 200);
        lst.setSize(180, 200);
        setFont(new Font("Helvetica", Font.PLAIN, SOCHandPanel.fontSize + 2));
        add(ta);
        add(cnvs);
        add(lst);
        add(tf);

        setLayout(null);

        setSize(640, 480);
        setLocation(200, 200);
        history.addElement("");

        tf.addActionListener(new InputActionListener());
        tf.addKeyListener(new InputKeyListener());
        cnvs.addMouseListener(new DividerMouseListener());
        addWindowListener(new MyWindowListener());
    }

    /** add some text*/
    public void print(String s)
    {
        StringTokenizer st = new StringTokenizer(s, " \n", true);
        String row = "";

        while (st.hasMoreElements())
        {
            String tk = st.nextToken();

            if (tk.equals("\n"))
            {
                continue;
            }

            if ((row.length() + tk.length()) > ncols)
            {
                ta.append(row + "\n");
                row = tk;

                continue;
            }

            row += tk;
        }

        if (row.trim().length() > 0)
        {
            ta.append(row + "\n");
        }
    }

    /** an error occured, stop editing */
    public void over(String s)
    {
        tf.setEditable(false);
        tf.setText(s);
    }

    /** start */
    public void began()
    {
        tf.setEditable(true);
        tf.setText("");
    }

    /** add a member to the group */
    public void addMember(String s)
    {
        synchronized(lst.getTreeLock())
        {
            int i;
            
            for (i = lst.getItemCount() - 1; i >= 0; i--)
            {
                if (lst.getItem(i).compareTo(s) < 0)
                {
                    break;
                }
            }

            lst.add(s, i + 1);
        }
    }

    /** delete a member from the channel */
    public void deleteMember(String s)
    {
        synchronized(lst.getTreeLock())
        {
            for (int i = lst.getItemCount() - 1; i >= 0; i--)
            {
                if (lst.getItem(i).equals(s))
                {
                    lst.remove(i);
                    
                    break;
                }
            }
        }
    }

    /** send the message that was just typed in, or start editing a private
     * message */
    private class InputActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            String s = tf.getText().trim();

            if (s.length() > 0)
            {
                tf.setText("");
                cc.chSend(cname, s + "\n");

                history.setElementAt(s, history.size() - 1);
                history.addElement("");
                historyCounter = 1;
            }
        }
    }

    private class DividerMouseListener extends MouseAdapter
    {
        public void mouseEntered(MouseEvent e)
        {
            if (!down)
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            }
        }
        public void mouseExited(MouseEvent e)
        {
            if (!down)
            {
                setCursor(Cursor.getDefaultCursor());
            }
        }
        public void mousePressed(MouseEvent e)
        {
            down = true;
        }
        public void mouseReleased(MouseEvent e)
        {
            if (! cnvs.contains(e.getPoint()))
            {
                setCursor(Cursor.getDefaultCursor());
            }

            Dimension d = ta.getSize();
            Point p = cnvs.getLocation();
            // e.getX() is in cnvs coords, and make sure nothing dissappears
            int diff = (p.x + e.getX() - 7) - d.width;
            diff = Math.max(diff, 30 - d.width);
            diff = Math.min(diff, (getSize().width - 30) - d.width);
            d.width += diff;
            ta.setSize(d);
            ncols = (int) ((((float) d.width) * 100.0) / ((float) npix)) - 2;

            d = lst.getSize();
            d.width -= diff;
            lst.setSize(d);

            p.x += diff;
            cnvs.setLocation(p);

            p = lst.getLocation();
            p.x += diff;
            lst.setLocation(p);

            down = false;
        }
    }

    private class InputKeyListener extends KeyAdapter
    {
        public void keyPressed(KeyEvent e)
        {
            int hs = history.size();
            int key = e.getKeyCode();

            if ((key == KeyEvent.VK_UP) && (hs > historyCounter))
            {
                if (historyCounter == 1)
                {
                    history.setElementAt(tf.getText(), hs - 1);
                }

                historyCounter++;
                tf.setText((String) history.elementAt(hs - historyCounter));
            }
            else if ((key == KeyEvent.VK_DOWN) && (historyCounter > 1))
            {
                historyCounter--;
                tf.setText((String) history.elementAt(hs - historyCounter));
            }
        }
    }

    /** when the window is destroyed, tell the applet to leave the group */
    private class MyWindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e)
        {
            cc.leaveChannel(cname);
            dispose();
        }
        public void windowOpened(WindowEvent e)
        {
            tf.requestFocus();
        }
    }
    
    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {
        Insets i = getInsets();
        Dimension dim = getSize();
        dim.width -= (i.left + i.right);
        dim.height -= (i.top + i.bottom);

        int tfheight = tf.getPreferredSize().height;

        int h = dim.height - tfheight;
        int lw = lst.getSize().width;
        int cw = cnvs.getSize().width;
        int w = dim.width - lw - cw;

        tf.setSize(dim.width, tfheight);
        tf.setLocation(i.left, i.top + h);

        ta.setSize(w, h);
        ta.setLocation(i.left, i.top);

        cnvs.setSize(cw, h);
        cnvs.setLocation(i.left + w, i.top);

        lst.setSize(lw, h);
        lst.setLocation(w + cw + i.left, i.top);

        npix = ta.getPreferredSize(100, 100).width;
        ncols = (int) ((((float) w) * 100.0) / ((float) npix)) - 2;
    }
}
