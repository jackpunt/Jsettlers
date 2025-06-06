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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;

import javax.swing.SwingUtilities;

import soc.disableDebug.D;
import soc.game.SOCGame;
import soc.game.SOCTradeOffer;
import soc.message.SOCMakeOffer;


/**
 * Interface for a player of Settlers of Catan
 *
 * @author Robert S. Thomas
 */
public class SOCPlayerInterface extends Frame implements ActionListener
{
    /**
     * the board display
     */
    protected SOCBoardPanel boardPanel;

    /**
     * where the player types in text
     */
    protected TextField textInput;

    /**
     * where text is displayed
     */
    // protected SnippingTextArea textDisplay;
    protected TextOut textDisplay;

    /**
     * where chat text is displayed
     */
    protected SnippingTextArea chatDisplay;

    /**
     * interface for building pieces
     */
    protected SOCBuildingPanel buildingPanel;

    /**
     * the display for the players' hands
     */
    protected SOCHandPanel[] hands;
    // (fontSize + 6) * 6 + 10 * (fontSize) /// scalexy == 1 ? 188 : 232
    static int handWidth=(SOCHandPanel.fontSize+6)*6+9*SOCHandPanel.fontSize; // min or fixed width of hand panel

    /**
     * the player colors
     */
    protected Color[] playerColors;

    /**
     * the client that spawned us
     */
    protected SOCPlayerClient client;

    /**
     * the game associated with this interface
     */
    protected SOCGame game;

    /**
     * number of columns in the text output area
     */
    protected int ncols;

    /**
     * width of text output area in pixels
     */
    protected int npix;

    /**
     * the dialog for getting what resources the player wants to discard
     */
    protected SOCDiscardDialog discardDialog;

    /**
     * the dialog for choosing a player from which to steal
     */
    protected SOCChoosePlayerDialog choosePlayerDialog;

    /**
     * the dialog for choosing 2 resources to discover
     */
    protected SOCDiscoveryDialog discoveryDialog;

    /**
     * the dialog for choosing a resource to monopolize
     */
    protected SOCMonopolyDialog monopolyDialog;

    // also: SOCHandPanel.font "Helvetica"
    public static Font monocoFont = new Font("Monoco", Font.PLAIN, SOCHandPanel.fontSize);
    public static Font monocoFont2 = new Font("Monoco", Font.PLAIN, SOCHandPanel.fontSize - 2);
    public static Font genevaFont = new Font("Geneva", Font.PLAIN, SOCHandPanel.fontSize);
    public static Font genevaFont2 = new Font("Geneva", Font.PLAIN, SOCHandPanel.fontSize + 2);

    /**
     * create a new player interface Frame
     *
     * @param title  title for this interface
     * @param cl     the player client that spawned us
     * @param ga     the game associated with this interface
     */
    public SOCPlayerInterface(String title, SOCPlayerClient cl, SOCGame ga)
    {
        super("Settlers of Catan Game: " + title);
        setResizable(true);
        setFont(genevaFont);
        setFont(monocoFont);

        client = cl;
        game = ga;

        /**
         * initialize the player colors (keep these sync'd with .gif images)
         */
        playerColors = new Color[4];
        playerColors[0] = new Color(153, 204, 255); // blue
        playerColors[1] = new Color(255, 153, 255); // pink
        playerColors[2] = new Color(153, 255, 153); // green
        playerColors[3] = new Color(255, 255, 102); // yellow

        /**
         * initialize the font and the forground, and background colors
         */
        setBackground(Color.black);
        setForeground(Color.black);

        /**
         * setup interface elements
         */
        initInterfaceElements();

        /**
         * we're doing our own layout management
         */
        setLayout(null);

        /**
         * more initialization stuff
         */
        setLocation(50, 50);
        if (SOCBoardPanel.scalexy == 1) {
            setSize(754, 600); // (HexMap + 2*HandPanel, BoardPanel+TextPanel+BuildingPanel)
        } else {
            setSize(980, 900); // (HexMap + 2*HandPanel, BoardPanel+TextPanel+BuildingPanel)
        }
        validate();
    }

    /**
     * Setup the interface elements
     */
    protected void initInterfaceElements()
    {
        /**
         * initialize the player hand displays and add them to the interface
         */
        hands = new SOCHandPanel[SOCGame.MAXPLAYERS];

        for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
        {
            hands[i] = new SOCHandPanel(this, game.getPlayer(i));
            hands[i].setSize(handWidth, 180); // height is not relevant?
            add(hands[i]);
        }

        /**
         * initialize the building interface and add it to the main interface
         */
        buildingPanel = new SOCBuildingPanel(this);
        buildingPanel.setSize(SOCBoardPanel.getPanelX(), 8 * ColorSquare.HEIGHT + 5); // 160 ~ 11 * fontHeight
        add(buildingPanel);

        /**
         * initialize the game board display and add it to the interface
         */
        boardPanel = new SOCBoardPanel(this);
        boardPanel.setBackground(new Color(112, 45, 10)); // Brown?
        boardPanel.setForeground(Color.black);
        boardPanel.setSize(SOCBoardPanel.getPanelX(), SOCBoardPanel.getPanelY());
        add(boardPanel);

        /**
         * initialize the text input and display and add them to the interface
         */
        // textDisplay = new SnippingTextArea("", 40, 80, TextArea.SCROLLBARS_VERTICAL_ONLY, 80);
        textDisplay = new TextOut(40, 80, 180);
        textDisplay.setFont(monocoFont);
        textDisplay.setBackground(new Color(255, 230, 162));
        textDisplay.setForeground(Color.black);
        textDisplay.setEditable(false);
        add(textDisplay);

        chatDisplay = new SnippingTextArea("", 40, 80, TextArea.SCROLLBARS_VERTICAL_ONLY, 100);
        chatDisplay.setFont(monocoFont);
        chatDisplay.setBackground(new Color(255, 230, 162));
        chatDisplay.setForeground(Color.black);
        chatDisplay.setEditable(false);
        add(chatDisplay);

        textInput = new TextField();
        textInput.setFont(monocoFont);

        FontMetrics fm = this.getFontMetrics(textInput.getFont());
        textInput.setSize(SOCBoardPanel.getPanelX(), fm.getHeight() + 4);
        textInput.setBackground(new Color(255, 230, 162));
        textInput.setForeground(Color.black);
        textInput.setEditable(false);
        textInput.setText("Please wait...");
        add(textInput);
        textInput.addActionListener(this);

        addWindowListener(new MyWindowAdapter());
    }

    /**
     * Overriden so the peer isn't painted, which clears background. Don't call
     * this directly, use {@link #repaint()} instead.
     */
    public void update(Graphics g)
    {
        paint(g);
    }

    /**
     * @return the client that spawned us
     */
    public SOCPlayerClient getClient()
    {
        return client;
    }

    /**
     * @return the game associated with this interface
     */
    public SOCGame getGame()
    {
        return game;
    }

    /**
     * @return the color of a player
     * @param pn  the player number
     */
    public Color getPlayerColor(int pn)
    {
        return playerColors[pn];
    }

    /**
     * @return a player's hand panel
     *
     * @param pn  the player's seat number
     */
    public SOCHandPanel getPlayerHandPanel(int pn)
    {
        return hands[pn];
    }

    /**
     * @return the board panel
     */
    public SOCBoardPanel getBoardPanel()
    {
        return boardPanel;
    }

    /**
     * @return the building panel
     */
    public SOCBuildingPanel getBuildingPanel()
    {
        return buildingPanel;
    }

    /**
     * send the message that was just typed in
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == textInput)
        {
            String s = textInput.getText().trim();

            if (s.length() > 100)
            {
                s = s.substring(0, 100);
            }
            else if (s.length() == 0)
            {
                return;
            }

            textInput.setText("");
            processText(s);
        }
    }

    Frame getFrame(Component comp) {
        return (Frame) SwingUtilities.getWindowAncestor(comp);
    }

    void processText(String s) {
        if (s.startsWith(".op.")) {
            String offerStr = "My Game,1,true,false,true,true,0,0,0,0,0,0,0,1,1,1";
            SOCMakeOffer moffer = SOCMakeOffer.parseDataStr(offerStr);
            int fromPlayer = 1;
            SOCTradeOffer offer = moffer.getOffer();
            getGame().getPlayer(fromPlayer).setCurrentOffer(offer);
            SOCHandPanel hp = getPlayerHandPanel(fromPlayer);
            hp.setVisible(true);
            hp.inPlay = true;
            getFrame(hp).pack(); // may will update size?
            System.out.println("Frame is a "+getFrame(hp).getClass().getName());
            try { Thread.sleep(10); }
            catch (InterruptedException ex) {
              Thread.currentThread().interrupt();
            };
            // hp.offer.setBounds(20, 80, 220, 120);
            // hp.offer.offerPanel.setSize(220, 120);
            hp.updateCurrentOffer();
        } else
        if (s.startsWith(".cp.")) {
            choosePlayer(4, new int[]{ 0, 1, 2, 3 });
        } else
        if (s.startsWith(".disc.")) {
            showDiscoveryDialog();
        } else
        if (s.startsWith(".mono.")) {
            showMonopolyDialog();
        } else
        if (s.startsWith(".dcard.")) {
            showDiscardDialog(0);
        } else
        client.sendText(game, s + "\n");
    }

    /**
     * leave this game
     */
    public void leaveGame()
    {
        client.leaveGame(game);
        dispose();
    }

    /**
     * print text in the text window
     *
     * @param s  the text
     */
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
                textDisplay.append(row + "\n");
                row = tk;

                continue;
            }

            row += tk;
        }

        if (row.trim().length() > 0)
        {
            textDisplay.append(row + "\n");
        }
    }

    /**
     * print text in the chat window
     *
     * @param s  the text
     */
    public void chatPrint(String s)
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
                chatDisplay.append(row + "\n");
                row = tk;

                continue;
            }

            row += tk;
        }

        if (row.trim().length() > 0)
        {
            chatDisplay.append(row + "\n");
        }
    }

    /**
     * an error occured, stop editing
     *
     * @param s  an error message
     */
    public void over(String s)
    {
        textInput.setEditable(false);
        textInput.setText(s);
    }

    /**
     * start
     */
    public void began()
    {
        textInput.setEditable(true);
        textInput.setText("");
        textInput.requestFocus();

        if ((game.getGameState() == SOCGame.NEW) || (game.getGameState() == SOCGame.READY))
        {
            for (int i = 0; i < 4; i++)
            {
                hands[i].addSitButton();
            }
        }
    }

    /**
     * a player has sat down to play
     *
     * @param n   the name of the player
     * @param pn  the seat number of the player
     */
    public void addPlayer(String n, int pn)
    {
        hands[pn].addPlayer(n);

        if (n.equals(client.getNickname()))
        {
            for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
            {
                D.ebugPrintln("game.getPlayer(" + i + ").isRobot() = " + game.getPlayer(i).isRobot());

                if (game.getPlayer(i).isRobot())
                {
                    hands[i].addSeatLockBut();
                }
            }
        }
    }

    /**
     * remove a player from the game
     *
     * @param pn the number of the player
     */
    public void removePlayer(int pn)
    {
        hands[pn].removePlayer();

        if (game.getGameState() <= SOCGame.READY)
        {
            boolean match = false;

            for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
            {
                if ((game.getPlayer(i).getName() != null) && (!game.isSeatVacant(i)) && (game.getPlayer(i).getName().equals(client.getNickname())))
                {
                    match = true;

                    break;
                }
            }

            if (!match)
            {
                hands[pn].addSitButton();
            }
        }
    }

    /**
     * remove the start buttons
     */
    public void startGame()
    {
        for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
        {
            hands[i].removeStartBut();
        }
    }

    /**
     * Place given Dialog where user can interact with it.
     * @param dialog one of the discard, choose, discovery, monopoly Dialog boxes.
     */
    public void centerInBounds(Dialog dialog) {
        Rectangle p0 = getBounds();
        Rectangle pb = this.chatDisplay.getBounds();
        dialog.setLocation(p0.x + pb.x + (pb.width - dialog.getWidth()) / 2, p0.y + pb.y);
    }

    /**
     * show the discard dialog
     *
     * @param nd  the number of discards
     */
    public void showDiscardDialog(int nd)
    {
        discardDialog = new SOCDiscardDialog(this, nd);
        discardDialog.setVisible(true);
    }

    /**
     * show the choose player dialog box
     *
     * @param count   the number of players to choose from
     * @param pnums   the player ids of those players
     */
    public void choosePlayer(int count, int[] pnums)
    {
        choosePlayerDialog = new SOCChoosePlayerDialog(this, count, pnums);
        choosePlayerDialog.setVisible(true);
    }

    /**
     * show the Discovery dialog box
     */
    public void showDiscoveryDialog()
    {
        discoveryDialog = new SOCDiscoveryDialog(this);
        discoveryDialog.setVisible(true);
    }

    /**
     * show the Monopoly dialog box
     */
    public void showMonopolyDialog()
    {
        monopolyDialog = new SOCMonopolyDialog(this);
        monopolyDialog.setVisible(true);
    }

    /**
     * set the face icon for a player
     *
     * @param pn  the number of the player
     * @param id  the id of the face image
     */
    public void changeFace(int pn, int id)
    {
        hands[pn].changeFace(id);
    }

    /**
     * do the layout
     */
    public void doLayout()
    {
        Insets i = getInsets();
        Dimension dim = getSize();
        dim.width -= (i.left + i.right);
        dim.height -= (i.top + i.bottom); 	// minus insets/boarders

        int hw = handWidth;
        int hh = (dim.height - 12) / 2;		// hh = 'hand' panel height

        int bw = dim.width - ( 2*hw + 16) ;
        int bh = SOCBoardPanel.getPanelY(); 	// bh = board/map height
//         int hw = (dim.width - bw - 16) / 2;
//         int hh = (dim.height - 12) / 2;		// hh = 'hand' panel height
        int kw = bw;
        int kh = buildingPanel.getSize().height; //kh = build panel
        int tfh = textInput.getSize().height; 	// tfh = fontsize+4
        int tah = dim.height - bh - kh - tfh - 16; // preallocated - borders

        boardPanel.setBounds(   i.left + hw + 8, i.top + tah + tfh + 8,       bw, bh);
        buildingPanel.setBounds(i.left + hw + 8, i.top + tah + tfh + bh + 12, kw, kh);

        hands[0].setBounds(i.left + 4, i.top + 4, hw, hh);

        if (SOCGame.MAXPLAYERS > 1)
        {
            hands[1].setBounds(i.left + hw + bw + 12, i.top + 4,      hw, hh);
            hands[2].setBounds(i.left + hw + bw + 12, i.top + hh + 8, hw, hh);
            hands[3].setBounds(i.left + 4,            i.top + hh + 8, hw, hh);
        }

        int cdh = tah / 4;	// could be 2, 3, 4
        int tdh = tah - cdh;	// bias for more text, especially when one human!
        textDisplay.setBounds(i.left + hw + 8, i.top + 4,       bw, tdh);
        chatDisplay.setBounds(i.left + hw + 8, i.top + 4 + tdh, bw, cdh);
        textInput.setBounds(  i.left + hw + 8, i.top + 4 + tah, bw, tfh);

        npix = textDisplay.getPreferredSize().width;
        ncols = (int) ((((float) bw) * 100.0) / ((float) npix)) - 2;

        FontMetrics fm = this.getFontMetrics(textDisplay.getFont());
        int nrows = (tdh / fm.getHeight()) - 1;

        //textDisplay.setMaximumLines(nrows);
        nrows = (cdh / fm.getHeight()) - 1;

        //chatDisplay.setMaximumLines(nrows);
        boardPanel.doLayout();
    }

    private class MyWindowAdapter extends WindowAdapter
    {
        /**
         * Leave the game when the window closes.
         */
        public void windowClosing(WindowEvent e)
        {
            leaveGame();
        }
    }
}
