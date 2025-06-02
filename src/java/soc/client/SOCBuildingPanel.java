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
import java.awt.FontMetrics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCPlayingPiece;


/**
 * This class is a panel that shows how much it costs
 * to build things, and it allows the player to build.
 */
public class SOCBuildingPanel extends Panel implements ActionListener
{
    static final String ROAD = "road";
    static final String STLMT = "stlmt";
    static final String CITY = "city";
    static final String CARD = "card";
    static final String DASH = "----";
    Label title;
    BuildRow city;
    BuildRow card;
    BuildRow setl;
    BuildRow road;

    SOCPlayerInterface pi;

    AButton buildButton(String action) {
        AButton button = new AButton("Cancel"); // max width
        button.setLabel(DASH);  // initial contents
        add(button);
        button.setActionCommand(action);
        button.addActionListener(this);
        return button;
    }
    class BuildRow {
      AButton button;
      Label buildT;
      Label buildV;
      Label buildC;
      ColorSquare[] squares;

      BuildRow(String action, String name, String v, ColorSquare[] res) {
        button = buildButton(action);
        buildC = new Label("Cost: ");
        buildT = new Label(name, Label.LEFT); // "City Upgrade: " or whatever
        buildV = new Label(v, Label.RIGHT);    // "1 VP ", value of purchase
        squares = res;
        add(buildC);
        add(buildT);
        add(buildV);
        for (ColorSquare csq : res) {
          if (csq != null) add(csq);
        }
      }
    }

    /**
     * make a new building panel
     *
     * @param pi  the player interface that this panel is in
     */
    public SOCBuildingPanel(SOCPlayerInterface pi)
    {
        super();
        setLayout(null);

        this.pi = pi;

        setBackground(new Color(156, 179, 94));
        setForeground(Color.black);
        setFont(SOCHandPanel.font); // original panel size

        road = new BuildRow(ROAD, "Road: ", "0 VP  (longest road = 2 VP)", new ColorSquare[] {
          null,
          null,
          null,
          new ColorSquare(ColorSquare.CLAY, 1),
          new ColorSquare(ColorSquare.WOOD, 1),
        });

        setl = new BuildRow(STLMT, "Settlement: ", "1 VP  (can upgrade to City)", new ColorSquare[] {
          null,
          new ColorSquare(ColorSquare.WHEAT, 1),
          new ColorSquare(ColorSquare.SHEEP, 1),
          new ColorSquare(ColorSquare.CLAY, 1),
          new ColorSquare(ColorSquare.WOOD, 1),
        });

        city = new BuildRow(CITY, "City Upgrade: ", "2 VP  (receives 2x rsrc.)", new ColorSquare[] {
          new ColorSquare(ColorSquare.ORE, 3),
          new ColorSquare(ColorSquare.WHEAT, 2),
        });

        card = new BuildRow(CARD, "Dev Card: ", "? VP  (largest army = 2 VP)", new ColorSquare[] {
          new ColorSquare(ColorSquare.ORE, 1),
          new ColorSquare(ColorSquare.WHEAT, 1),
          new ColorSquare(ColorSquare.SHEEP, 1),
        });
    }

    int placeSquares(int curX, int curY, ColorSquare[] squares) {
      for (ColorSquare square : squares) {
          if (square != null) {
              square.setSize(ColorSquare.WIDTH, ColorSquare.HEIGHT);
              square.setLocation(curX, curY);
          }
          curX += (ColorSquare.WIDTH - 1);
      }
      return curX;
    }

    @FunctionalInterface
    interface LineBuilder {
      abstract
        void buildRow(int curY, BuildRow row);
    }

    /**
     * DOCUMENT ME!
     */
    public void doLayout()
    {
        Dimension dim = getSize();
        FontMetrics fm = this.getFontMetrics(this.getFont());
        int lineH = ColorSquare.HEIGHT;
        int rowSpaceH = (dim.height - (8 * lineH)) / 3;

        int costW = fm.stringWidth(new String("Cost: "));
        int longT = fm.stringWidth(city.buildT.getText()); // longest name "City Upgrade:"
        int longV = fm.stringWidth(road.buildV.getText()); // longest value "0 VP ..."
        int butW = road.button.getWidth(); // all the same width: "Cancel"
        int margin = 2;
        int tab1 = longT + 5;
        int tab2 = tab1 + butW + 5;
        int tab3 = dim.width - (longV + margin);

        LineBuilder aLine = (int lineY, BuildRow row) -> {
          // buildT: button Cost: squares buildV
            int twidth = fm.stringWidth(row.buildT.getText());
            row.buildT.setSize(twidth, lineH);
            row.buildC.setSize(costW, lineH);        // "Cost: "
            row.buildV.setSize(fm.stringWidth(row.buildV.getText()), lineH);

            row.buildT.setLocation(tab1 - twidth, lineY);
            row.button.setLocation(tab1, lineY);
            row.buildC.setLocation(tab2, lineY);
            placeSquares(tab2 + costW + 3, lineY, row.squares);
            row.buildV.setLocation(tab3, lineY);
        };

        int curY = lineH / 2;
        aLine.buildRow(curY, city);
        curY += lineH + lineH + rowSpaceH;
        aLine.buildRow(curY, card);
        curY += lineH + lineH + rowSpaceH;
        aLine.buildRow(curY, setl);
        curY += lineH + lineH + rowSpaceH;
        aLine.buildRow(curY, road);
        curY += lineH + lineH + rowSpaceH;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e)
    {
        String target = e.getActionCommand();
        SOCGame game = pi.getGame();
        SOCPlayerClient client = pi.getClient();
        SOCPlayer ourPlayerData = game.getPlayer(client.getNickname());

        if (ourPlayerData != null)
        {
            if (game.getCurrentPlayerNumber() == ourPlayerData.getPlayerNumber())
            {
                if (target == ROAD)
                {
                    if ((game.getGameState() == SOCGame.PLAY1) && (road.button.getLabel().equals("Buy")))
                    {
                        client.buildRequest(game, SOCPlayingPiece.ROAD);
                    }
                    else if (road.button.getLabel().equals("Cancel"))
                    {
                        client.cancelBuildRequest(game, SOCPlayingPiece.ROAD);
                    }
                }
                else if (target == STLMT)
                {
                    if ((game.getGameState() == SOCGame.PLAY1) && (setl.button.getLabel().equals("Buy")))
                    {
                        client.buildRequest(game, SOCPlayingPiece.SETTLEMENT);
                    }
                    else if (setl.button.getLabel().equals("Cancel"))
                    {
                        client.cancelBuildRequest(game, SOCPlayingPiece.SETTLEMENT);
                    }
                }
                else if (target == CITY)
                {
                    if ((game.getGameState() == SOCGame.PLAY1) && (city.button.getLabel().equals("Buy")))
                    {
                        client.buildRequest(game, SOCPlayingPiece.CITY);
                    }
                    else if (city.button.getLabel().equals("Cancel"))
                    {
                        client.cancelBuildRequest(game, SOCPlayingPiece.CITY);
                    }
                }
                else if (target == CARD)
                {
                    if ((game.getGameState() == SOCGame.PLAY1) && (card.button.getLabel().equals("Buy")))
                    {
                        client.buyDevCard(game);
                    }
                }
            }
        }
    }

    /**
     * update the status of the buttons
     */
    public void updateButtonStatus()
    {
        SOCGame game = pi.getGame();
        SOCPlayer player = game.getPlayer(pi.getClient().getNickname());

        if (player != null)
        {
            if ((game.getCurrentPlayerNumber() == player.getPlayerNumber()) && (game.getGameState() == SOCGame.PLACING_ROAD))
            {
                road.button.setLabel("Cancel");
            }
            else if (game.couldBuildRoad(player.getPlayerNumber()))
            {
                road.button.setLabel("Buy");
            }
            else
            {
                road.button.setLabel(DASH);
            }

            if ((game.getCurrentPlayerNumber() == player.getPlayerNumber()) && (game.getGameState() == SOCGame.PLACING_SETTLEMENT))
            {
                setl.button.setLabel("Cancel");
            }
            else if (game.couldBuildSettlement(player.getPlayerNumber()))
            {
                setl.button.setLabel("Buy");
            }
            else
            {
                setl.button.setLabel(DASH);
            }

            if ((game.getCurrentPlayerNumber() == player.getPlayerNumber()) && (game.getGameState() == SOCGame.PLACING_CITY))
            {
                city.button.setLabel("Cancel");
            }
            else if (game.couldBuildCity(player.getPlayerNumber()))
            {
                city.button.setLabel("Buy");
            }
            else
            {
                city.button.setLabel(DASH);
            }

            if (game.couldBuyDevCard(player.getPlayerNumber()))
            {
                card.button.setLabel("Buy");
            }
            else
            {
                card.button.setLabel(DASH);
            }
        }
    }
}
