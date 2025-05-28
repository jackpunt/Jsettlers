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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

import javax.swing.JButton;

import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;
import soc.game.SOCTradeOffer;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.3 $
 */
public class TradeOfferPanel extends Panel
{
    public static final Color TRANSP = new Color(0,0,0,0); // transparent
    public static final String OFFER_MODE = "offer";
    public static final String MESSAGE_MODE = "message";
    
    protected static final int[] zero = { 0, 0, 0, 0, 0 };
    static final String OFFER = "counter";
    static final String ACCEPT = "accept";
    static final String REJECT = "reject";
    static final String SEND = "send";
    static final String CLEAR = "clear";
    static final String CANCEL = "cancel";
    static final Color insideBGColor = new Color(255, 230, 162);
    final int inset = 10;

    int from;
    SOCHandPanel hp;
    SOCPlayerInterface pi;

    String mode;
    CardLayout cardLayout;
    MessagePanel messagePanel;
    OfferPanel offerPanel;

    /**
     * Creates a new TradeOfferPanel object.
     */
    public TradeOfferPanel(SOCHandPanel hp, int from)
    {
        this.hp = hp;
        this.from = from;
        pi = hp.getPlayerInterface();

        setBackground(pi.getPlayerColor(from));
        // setBackground(TRANSP); // setOpaque(false);
        setForeground(Color.black);

        messagePanel = new MessagePanel();
        offerPanel = new OfferPanel();
        
        cardLayout = new CardLayout();
        setLayout(cardLayout);

	      // first added = first shown (ie, at back of stack)
        add(messagePanel, MESSAGE_MODE);// SpeechBalloon to display message
        add(offerPanel, OFFER_MODE); // ShadowBox to compose counter offer
        mode = MESSAGE_MODE;
    }
    
    private class MessagePanel extends Panel
    {
        SpeechBalloon balloon;
        Label msg;

        /**
         * Creates a new TradeOfferPanel object.
         */
        public MessagePanel()
        {
            setLayout(null);
            setFont(new Font("Helvetica", Font.PLAIN, SOCHandPanel.fontSize + 2));
        
            msg = new Label(" ", Label.CENTER);
            msg.setBackground(insideBGColor);
            add(msg);
        
            balloon = new SpeechBalloon(pi.getPlayerColor(from));
            add(balloon);
        }
        
        /**
         * @param message message to display
         */
        public void update(String message)
        {
            msg.setText(message);
        }
        
        /**
         * Just for the message panel
         */
        public void doLayout()
        {
            // FontMetrics fm = this.getFontMetrics(this.getFont());
            Dimension dim = getSize();// from parent TradeOfferPanel/CardLayout
            int fontW = SOCHandPanel.fontSize;
            int w = Math.max(135+4*fontW, dim.width);
            int h = Math.min(124, dim.height);

            msg.setBounds(inset, ((h - 18) / 2), w - (2 * inset), 18);
            balloon.setBounds(0, 0, w, h);
        }
    }

    private class OfferPanel extends Panel implements ActionListener
    {
        SpeechBalloon balloon;
        Label toWhom1;
        Label toWhom2;
        Label giveLab;
        Label getLab;

        SquaresPanel squares;
        ShadowedBox offerBox;
        SquaresPanel offerSquares;
        Label giveLab2;
        Label getLab2;
        boolean offered;
        SOCResourceSet give;
        SOCResourceSet get;
        int[] giveInt = new int[5];
        int[] getInt = new int[5];
	      boolean acceptable = false;
        boolean counterOfferMode = false;

        /** ACCEPT, REJECT, OFFER */
        JButton[] offeredButtons;
        String[] offeredNames = {
            ACCEPT, REJECT, OFFER,
        };
        /** SEND, CLEAR, CANCEL */
        JButton[] counterButtons;
        String[] counterNames = {
            SEND, CLEAR, CANCEL,
        };

        JButton addButton(String name, boolean visible) 
        {
          FontMetrics fm = getFontMetrics(getFont());
          String cname = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
          JButton button = new JButton(cname); 
          button.setSize(new Dimension(fm.stringWidth(name)+2, fm.getHeight()));
          button.setOpaque(true);          // MacOS paints the background of panel? (Stack Overflow)
          button.setVisible(visible);
          button.setActionCommand(name);
          button.addActionListener(this);
          add(button);
          return button;
        }

        JButton[] addButtons(String[] names, boolean vis) {
          Stack<JButton> stk = new Stack<JButton>();
          for (String name : names) {
            stk.push(addButton(name, vis));
          }
          return stk.toArray(new JButton[stk.size()]);
        }

        /**
         * Creates a new OfferPanel object.
         */
        public OfferPanel()
        {
            setLayout(null);
            setFont(new Font("Helvetica", Font.PLAIN, SOCHandPanel.fontSize));

            toWhom1 = new Label();
            toWhom1.setBackground(insideBGColor);
            add(toWhom1);

            toWhom2 = new Label();
            toWhom2.setBackground(insideBGColor);
            add(toWhom2);

            squares = new SquaresPanel(false); // incoming offer
            add(squares);

            giveLab = new Label("I Give: ");
            giveLab.setBackground(insideBGColor);
            add(giveLab);

            getLab = new Label("I Get: ");
            getLab.setBackground(insideBGColor);
            add(getLab);

            giveInt = new int[5];
            getInt = new int[5];

            offeredButtons = addButtons(offeredNames, true);
            counterButtons = addButtons(counterNames, false);

            offerSquares = new SquaresPanel(true); // counter offer
            offerSquares.setVisible(false);
            add(offerSquares);

            giveLab2 = new Label("I Give: ");
            giveLab2.setVisible(false);
            add(giveLab2);

            getLab2 = new Label("I Get: ");
            getLab2.setVisible(false);
            add(getLab2);

            // correct the interior when we can get our player color
            offerBox = new ShadowedBox(pi.getPlayerColor(from), Color.white);
            offerBox.setVisible(false);
            add(offerBox);

            balloon = new SpeechBalloon(pi.getPlayerColor(from));
            add(balloon);
        }

        /**
         * update OfferPanel.
         * 
         * @param  give  the set of resources being given
         * @param  get   the set of resources being asked for
         * @param  to    a boolean array where 'true' means that the offer
         *               is being made to the player with the same number as
         *               the index of the 'true'
         */
        public void update(SOCTradeOffer offer)
        {

            this.give = offer.getGiveSet();
            this.get = offer.getGetSet();
            boolean[] offerList = offer.getTo();
            SOCGame ga = hp.getGame();
        
	          // get player if this client is playing, else null:
            SOCPlayer player = ga.getPlayer(hp.getClient().getNickname());

	          offered = (player != null) && offerList[player.getPlayerNumber()]; // is offered to this player-client

            if (player != null)
            {
		            acceptable = SOCResourceSet.gte(player.getResources(), get); // only used as: (offered && acceptable)

                Color ourPlayerColor = pi.getPlayerColor(player.getPlayerNumber());
                giveLab2.setBackground(ourPlayerColor);
                getLab2.setBackground(ourPlayerColor);
                offerBox.setInterior(ourPlayerColor);
            }
        
            FontMetrics fm = this.getFontMetrics(this.getFont());
            String names1 = "Offered to: ";  // first line of names
            String names2 = null;            // second line

            for (int cnt = 0; cnt < SOCGame.MAXPLAYERS; cnt++) {
                if (offerList[cnt]) {
                    String name = ga.getPlayer(cnt).getName();
		                int eol = getWidth() - 2*inset;
                    
                    if (fm.stringWidth(names1+", "+name) < eol) { // getWidth()-2*inset
                        if (names1.endsWith(" ")) {
                            names1 += name; // append first name
                        } else {
                            names1 += ", " + name;
                        }
                    } else {
                        if (names2 == null) {
                            names1 += ",";
                            names2 = name;
                        } else {
                            names2 += ", " + name;
                        }
                    }
                }
            }
            toWhom1.setText(names1);
            toWhom2.setText(names2);

            /**
             * Note: this only works if SOCResourceConstants.MIN == 1, MAX==6
             */
            for (int i = 0; i < 5; i++)
            {
                giveInt[i] = give.getAmount(i + 1); // i+MIN
                getInt[i] = get.getAmount(i + 1); // i+MIN
            }
            squares.setValues(giveInt, getInt);

            // enables accept,reject,offer Buttons if 'offered' is true
            setCounterOfferVisible(false);
            validate();
        }

        /**
         * Layout OfferPanel
         */
        public void doLayout()
        {
            // make parent panel just big enough;
            // outerHeight = cancelBut.bottom + space + shadow:
            // 10 + 12 + 62 + 18 + 16 + 5 = 123 !
            // TradeOfferPanel.this.setSize(w, top + 12 + (2 * squaresHeight) + buttonH + lineH + 2 + 5);

            FontMetrics fm = this.getFontMetrics(this.getFont());
            Dimension dim = getSize(); // CardLayout so this matches parent!

            int squaresHeight = squares.getBounds().height; // 31
            int vspace = 3;
            int tang = SpeechBalloon.TANG;
            int dsh = 5;	// drop shadow height

            int liney = tang + vspace;	// start at top and paint down:
                
            int giveW = fm.stringWidth("I Give: ") + 2;
            int fontH = fm.getHeight() + 1;   // 10 -> 14
            int lineH = fontH + vspace;	// could derive from fm? (16)
            int buttonW = fm.stringWidth("Counter")+3; // (48)
            int buttonH = lineH + vspace;	// lineH + 2 (18)
            // inset:buttonW<ibs>buttonW<ibs>buttonW:inset
            int use = (3 * buttonW + 2 * inset + 2);
            int w = Math.min(use+2, dim.width);
            int h = Math.min(124, dim.height); // (top + 32 + squaresHeight + 2 + 5) or 92 if counterOfferMode
            int ibs = (w - use) / 2; // inter-button horiz space

            // System.out.println("TradeOffer: ibs = "+ibs);

            toWhom1.setBounds(inset, liney, w - 2*inset, fontH); liney += fontH;
            if (toWhom2.getText() != null) {
                toWhom2.setBounds(inset, liney, w - 2*inset, fontH); liney += fontH;
            } else {
                toWhom2.setBounds(inset, liney, 0, 0);
            }
            liney += vspace;
            squares.setLocation(inset + giveW, liney);
            squares.doLayout();
            giveLab.setBounds(inset, liney, giveW, lineH); liney += lineH;
            getLab.setBounds( inset, liney, giveW, lineH); liney += lineH;
            liney += vspace;

            if (counterOfferMode) {
                balloon.setBounds(0, 0, w, liney + dsh); // was min(h,liney);
                // show the counter offer:
                liney += vspace;
                int boxTop = liney;
                liney += 2*vspace;
                offerSquares.setLocation(inset + giveW, liney);
                offerSquares.doLayout();
                giveLab2.setBounds(inset, liney, giveW, lineH); liney += lineH;
                getLab2.setBounds( inset, liney, giveW, lineH); liney += lineH;

                liney += vspace;
                int buttonX = inset;
                for (JButton button : counterButtons) {
                    button.setBounds(buttonX, liney, buttonW, buttonH); 
                    buttonX += buttonW + ibs;
                }
                liney += lineH + 2*vspace;
                offerBox.setBounds(0, boxTop, w, liney + dsh - boxTop);
            } else {
                if (offered) {
                  int buttonX = inset;
                  for (JButton button : offeredButtons) {
                      button.setBounds(buttonX, liney, buttonW, buttonH); 
                      buttonX += buttonW + ibs;
                  }
                }
                liney += lineH + 2*vspace;	// leave space even if buttons not shown...
                balloon.setBounds(0, 0, w, liney + dsh);
            }
          }
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void actionPerformed(ActionEvent e)
        {
            String target = e.getActionCommand();

            if (target == OFFER)
            {
                setCounterOfferVisible(true);
            }
            else if (target == CLEAR)
            {
                offerSquares.setValues(zero, zero);
            }
            else if (target == SEND)
            {
                SOCGame game = hp.getGame();
                SOCPlayer player = game.getPlayer(pi.getClient().getNickname());

                if (game.getGameState() == SOCGame.PLAY1)
                {
                    // slot for each resource, plus one for 'unknown' (remains 0)
                    int[] give = new int[5];
                    int[] get = new int[5];
                    int giveSum = 0;
                    int getSum = 0;
                    offerSquares.getValues(give, get);
                    
                    for (int i = 0; i < 5; i++)
                    {
                        giveSum += give[i];
                        getSum += get[i];
                    }

                int m1 = - SOCResourceConstants.MIN;
                SOCResourceSet giveSet = new SOCResourceSet(give[m1+SOCResourceConstants.CLAY],
                    give[m1+SOCResourceConstants.ORE],
                    give[m1+SOCResourceConstants.SHEEP],
                    give[m1+SOCResourceConstants.WHEAT],
                    give[m1+SOCResourceConstants.WOOD], 0);
		            SOCResourceSet getSet = new SOCResourceSet(get[m1+SOCResourceConstants.CLAY],
							       get[m1+SOCResourceConstants.ORE],
							       get[m1+SOCResourceConstants.SHEEP],
							       get[m1+SOCResourceConstants.WHEAT],
							       get[m1+SOCResourceConstants.WOOD], 0);
                    
                    if (!player.getResources().contains(giveSet))
                    {
                        pi.print("*** You can't offer what you don't have.");
                    }
                    else if ((giveSum == 0) || (getSum == 0))
                    {
                        pi.print("*** A trade must contain at least one resource card from each player.");
                    }
                    else
                    {
                        // arrays of bools are initially false
                        boolean[] to = new boolean[SOCGame.MAXPLAYERS];
                        // offer to the player that made the original offer
                        to[from] = true;

                        SOCTradeOffer tradeOffer =
                            new SOCTradeOffer (game.getName(),
                                               player.getPlayerNumber(),
                                               to, giveSet, getSet);
                        hp.getClient().offerTrade(game, tradeOffer);
                        
                        setCounterOfferVisible(true);
                    }
                }
            }

            if (target == CANCEL)
            {
                setCounterOfferVisible(false);
            }

            if (target == REJECT)
            {
                hp.getClient().rejectOffer(hp.getGame());

                counterButtons[1].setVisible(false);
            }

            if (target == ACCEPT)
            {
                //int[] tempGive = new int[5];
                //int[] tempGet = new int[5];
                //squares.getValues(tempGive, tempGet);
                hp.getClient().acceptOffer(hp.getGame(), from);
            }
        }
        
        private void setCounterOfferVisible(boolean visible)
        {
            giveLab2.setVisible(visible);
            getLab2.setVisible(visible);
            offerSquares.setVisible(visible);

            for (JButton button : counterButtons) {
              button.setVisible(visible);
            }
            offerBox.setVisible(visible);

            // see if this player has the 'give' resources!
            for (int i = 0; i < offeredButtons.length; i++) {
              boolean accept = (i == 0) ? acceptable : true;
              offeredButtons[i].setVisible(offered && ! visible && accept);
            }
            counterOfferMode = visible;
            validate();
        }
    }

    /**
     * Switch to the Message from another player.
     *
     * @param  message  the message message to show
     */
    public void setMessage(String message)
    {
        messagePanel.update(message);
        cardLayout.show(this, mode = MESSAGE_MODE);
        validate();
    }

    /**
     * Update to view the of an offer from another player.
     *
     * @param  currentOffer the trade being proposed
     */
    public void setOffer(SOCTradeOffer currentOffer)
    {
        offerPanel.update(currentOffer);
        cardLayout.show(this, mode = OFFER_MODE);
        validate();
    }

    /**
     * Returns current mode of <code>TradeOfferPanel.OFFER_MODE</code>, or
     * <code>TradeOfferPanel.MESSAGE_MODE</code>, which has been set by using
     * {@link #setOffer} or {@link #setMessage}
     */
    public String getMode() {
        return mode;
    }
}
