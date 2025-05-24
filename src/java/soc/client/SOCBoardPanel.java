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

import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;

import soc.game.SOCBoard;
import soc.game.SOCCity;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCRoad;
import soc.game.SOCSettlement;

/**
 * This is a component that can display a Settlers of Catan Board.
 * It can be used in an applet or an application.
 * It loads gifs from a directory named "images" in the same
 * directory as the code.
 */
public class SOCBoardPanel extends Canvas implements MouseListener, MouseMotionListener
{
    private static String IMAGEDIR = "/soc/client/images";

    private static int scalexy = 2; 
    /**
     * size of the whole panel
     */
    public static int panelx = 253 * scalexy;
    public static int panely = 222 * scalexy;
    
    private static double hexrad = 20 * scalexy;  //
    private static double sqrt3 = Math.sqrt(3);
    private static double sqrt3_2 = Math.sqrt(3)/2;
    private static Color edge = new Color(0xFF9966);

    void setScaleXY(int sxy) {
      scalexy = sxy;
      panelx = 253 * scalexy;
      panely = 222 * scalexy;
      hexrad = 20 * scalexy;
      diceFontSize = 14 + (scalexy - 1) * 8;
      portFontSize = 12 + (scalexy - 1) * 8;
    }

    int dxdc() {
      return rnd(hexrad * sqrt3); // half of dxdc
    }
    int dydc() {
      return rnd(1.5 * hexrad);
    }

    /**
     * hex coordinates for drawing (EW topo: dxdc = 36, dydr = 30)
     */
    private static final int[] hexX = 
    {
        54, 90, 126, 162, 36, 72, 108, 144, 180, 18, 54, 90, 126, 162, 198, 0,
        36, 72, 108, 144, 180, 216, 18, 54, 90, 126, 162, 198, 36, 72, 108, 144,
        180, 54, 90, 126, 162
    };
    /** 
     * row = 0..6; dydr = 30
     */
    private static final int[] hexY = 
    {
        0, 0, 0, 0, 30, 30, 30, 30, 30, 60, 60, 60, 60, 60, 60, 90, 90, 90, 90,
        90, 90, 90, 120, 120, 120, 120, 120, 120, 150, 150, 150, 150, 150, 180,
        180, 180, 180
    };
    private int hexX(int ndx) { return hexX[ndx] * scalexy;}
    private int hexY(int ndx) { return hexY[ndx] * scalexy;}

    /**
     * coordinates for drawing the playing pieces
     */
    /***  road looks like "|"  ***/
    private static final int[] vertRoadX = { -2, 3, 3, -2, -2 };
    private static final int[] vertRoadY = { 11, 11, 31, 31, 11 };

    /***  road looks like "/"  ***/
    private static final int[] upRoadX = { -1, 17, 20, 2, -1 };
    private static final int[] upRoadY = { 9, -2, 2, 13, 9 };

    /***  road looks like "\"  ***/
    private static final int[] downRoadX = { -1, 2, 20, 17, -1 };
    private static final int[] downRoadY = { 33, 29, 40, 44, 33 };

    /***  settlement  ***/
    private static final int[] settlementX = { -6, 0, 6, 6, -6, -6, 6 };
    private static final int[] settlementY = { -6, -12, -6, 4, 4, -6, -6 };

    /***  city  ***/
    private static final int[] cityX = 
    {
        -8, -4, 0, 4, 8, 8, -8, -8, 0, 0, 8, 4, -8
    };
    private static final int[] cityY = 
    {
        -6, -12, -6, -6, -2, 4, 4, -6, -6, -2, -2, -6, -6
    };

    /***  robber  ***/
    private static final int[] robberX = 
    {
        6, 4, 4, 6, 10, 12, 12, 10, 12, 12, 4, 4, 6, 10
    };
    private static final int[] robberY = 
    {
        6, 4, 2, 0, 0, 2, 4, 6, 8, 16, 16, 8, 6, 6
    };
    public final static int NONE = 0;
    public final static int PLACE_ROAD = 1;
    public final static int PLACE_SETTLEMENT = 2;
    public final static int PLACE_CITY = 3;
    public final static int PLACE_ROBBER = 4;
    public final static int PLACE_INIT_SETTLEMENT = 5;
    public final static int PLACE_INIT_ROAD = 6;
    public final static int CONSIDER_LM_SETTLEMENT = 7;
    public final static int CONSIDER_LM_ROAD = 8;
    public final static int CONSIDER_LM_CITY = 9;
    public final static int CONSIDER_LT_SETTLEMENT = 10;
    public final static int CONSIDER_LT_ROAD = 11;
    public final static int CONSIDER_LT_CITY = 12;

    /**
     * hex size
     */
    private int HEXWIDTH = 37;
    private int HEXHEIGHT = 42;

    /**
     * translate hex ID to number to get coords
     */
    private int[] hexIDtoNum;

    /**
     * Hex pix
     */
    private static Image[] hexes;
    private static Image[] ports;

    /**
     * number pix
     */
    private static Image[] numbers;

    /**
     * arrow/dice pix
     */
    private static Image arrowR;
    private static Image arrowL;
    private static Image[] dice;
    private static Image[] arrows;

    /**
     * Old pointer coords for interface
     */
    private int ptrOldX;
    private int ptrOldY;

    /**
     * Edge or node being pointed to.
     */
    private int hilight;

    /**
     * Map grid sectors to hex edges
     */
    private int[] edgeMap;

    /**
     * Map grid sectors to hex nodes
     */
    private int[] nodeMap;

    /**
     * Map grid sectors to hexes
     */
    private int[] hexMap;

    /**
     * The game which this board is a part of
     */
    private SOCGame game;

    /**
     * The board in the game
     */
    private SOCBoard board;

    /**
     * The player that is using this interface
     */
    private SOCPlayer player;

    /**
     * When in "consider" mode, this is the player
     * we're talking to
     */
    private SOCPlayer otherPlayer;

    /**
     * offscreen buffer
     */
    private Image buffer;

    /**
     * modes of interaction
     */
    private int mode;

    /**
     * This holds the coord of the last stlmt
     * placed in the initial phase.
     */
    private int initstlmt;

    /**
     * the player interface that this board is a part of
     */
    private SOCPlayerInterface playerInterface;

    /**
     * create a new board panel in an applet
     *
     * @param pi  the player interface that spawned us
     */
    public SOCBoardPanel(SOCPlayerInterface pi)
    {
        super();

        game = pi.getGame();
        playerInterface = pi;
        player = null;
        board = game.getBoard();

        int i;

        // init coord holders
        ptrOldX = 0;
        ptrOldY = 0;

        hilight = 0;

        // init edge map
        edgeMap = new int[345];

        for (i = 0; i < 345; i++)
        {
            edgeMap[i] = 0;
        }

        initEdgeMapAux(4, 3, 9, 6, 0x37);
        initEdgeMapAux(3, 6, 10, 9, 0x35);
        initEdgeMapAux(2, 9, 11, 12, 0x33);
        initEdgeMapAux(3, 12, 10, 15, 0x53);
        initEdgeMapAux(4, 15, 9, 18, 0x73);

        // init node map
        nodeMap = new int[345];

        for (i = 0; i < 345; i++)
        {
            nodeMap[i] = 0;
        }

        initNodeMapAux(4, 3, 10, 7, 0x37);
        initNodeMapAux(3, 6, 11, 10, 0x35);
        initNodeMapAux(2, 9, 12, 13, 0x33);
        initNodeMapAux(3, 12, 11, 16, 0x53);
        initNodeMapAux(4, 15, 10, 19, 0x73);

        // init hex map
        hexMap = new int[345];

        for (i = 0; i < 345; i++)
        {
            hexMap[i] = 0;
        }

        initHexMapAux(4, 4, 9, 5, 0x37);
        initHexMapAux(3, 7, 10, 8, 0x35);
        initHexMapAux(2, 10, 11, 11, 0x33);
        initHexMapAux(3, 13, 10, 14, 0x53);
        initHexMapAux(4, 16, 9, 17, 0x73);

        hexIDtoNum = new int[0xDE];

        for (i = 0; i < 0xDE; i++)
        {
            hexIDtoNum[i] = 0;
        }

        initHexIDtoNumAux(0x17, 0x7D, 0);
        initHexIDtoNumAux(0x15, 0x9D, 4);
        initHexIDtoNumAux(0x13, 0xBD, 9);
        initHexIDtoNumAux(0x11, 0xDD, 15);
        initHexIDtoNumAux(0x31, 0xDB, 22);
        initHexIDtoNumAux(0x51, 0xD9, 28);
        initHexIDtoNumAux(0x71, 0xD7, 33);

        // set mode of interaction
        mode = NONE;

        // Set up mouse listeners
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        // load the static images
        loadImages(this);
    }

    private final void initEdgeMapAux(int x1, int y1, int x2, int y2, int startHex)
    {
        int x;
        int y;
        int facing = 0;
        int count = 0;
        int hexNum;
        int edgeNum = 0;

        for (y = y1; y <= y2; y++)
        {
            hexNum = startHex;

            switch (count)
            {
            case 0:
                facing = 6;
                edgeNum = hexNum - 0x10;

                break;

            case 1:
                facing = 5;
                edgeNum = hexNum - 0x11;

                break;

            case 2:
                facing = 5;
                edgeNum = hexNum - 0x11;

                break;

            case 3:
                facing = 4;
                edgeNum = hexNum - 0x01;

                break;

            default:
                System.out.println("initEdgeMap error");

                return;
            }

            for (x = x1; x <= x2; x++)
            {
                edgeMap[x + (y * 15)] = edgeNum;

                switch (facing)
                {
                case 1:
                    facing = 6;
                    hexNum += 0x22;
                    edgeNum = hexNum - 0x10;

                    break;

                case 2:
                    facing = 5;
                    hexNum += 0x22;
                    edgeNum = hexNum - 0x11;

                    break;

                case 3:
                    facing = 4;
                    hexNum += 0x22;
                    edgeNum = hexNum - 0x01;

                    break;

                case 4:
                    facing = 3;
                    edgeNum = hexNum + 0x10;

                    break;

                case 5:
                    facing = 2;
                    edgeNum = hexNum + 0x11;

                    break;

                case 6:
                    facing = 1;
                    edgeNum = hexNum + 0x01;

                    break;

                default:
                    System.out.println("initEdgeMap error");

                    return;
                }
            }

            count++;
        }
    }

    private final void initHexMapAux(int x1, int y1, int x2, int y2, int startHex)
    {
        int x;
        int y;
        int hexNum;
        int count = 0;

        for (y = y1; y <= y2; y++)
        {
            hexNum = startHex;

            for (x = x1; x <= x2; x++)
            {
                hexMap[x + (y * 15)] = hexNum;

                if ((count % 2) != 0)
                {
                    hexNum += 0x22;
                }

                count++;
            }
        }
    }

    private final void initNodeMapAux(int x1, int y1, int x2, int y2, int startHex)
    {
        int x;
        int y;
        int facing = 0;
        int count = 0;
        int hexNum;
        int edgeNum = 0;

        for (y = y1; y <= y2; y++)
        {
            hexNum = startHex;

            switch (count)
            {
            case 0:
                facing = -1;
                edgeNum = 0;

                break;

            case 1:
                facing = 6;
                edgeNum = hexNum - 0x10;

                break;

            case 2:
                facing = -7;
                edgeNum = 0;

                break;

            case 3:
                facing = 5;
                edgeNum = hexNum - 0x01;

                break;

            case 4:
                facing = -4;
                edgeNum = 0;

                break;

            default:
                System.out.println("initNodeMap error");

                return;
            }

            for (x = x1; x <= x2; x++)
            {
                nodeMap[x + (y * 15)] = edgeNum;

                switch (facing)
                {
                case 1:
                    facing = -1;
                    hexNum += 0x22;
                    edgeNum = 0;

                    break;

                case -1:
                    facing = 1;
                    edgeNum = hexNum + 0x01;

                    break;

                case 2:
                    facing = -2;
                    hexNum += 0x22;
                    edgeNum = 0;

                    break;

                case -2:
                    facing = 2;
                    edgeNum = hexNum + 0x12;

                    break;

                case 6:
                    facing = -2;
                    edgeNum = 0;

                    break;

                case -7:
                    edgeNum = 0;

                    break;

                case 5:
                    facing = -3;
                    edgeNum = 0;

                    break;

                case 3:
                    facing = -3;
                    hexNum += 0x22;
                    edgeNum = 0;

                    break;

                case -3:
                    facing = 3;
                    edgeNum = hexNum + 0x21;

                    break;

                case 4:
                    facing = -4;
                    hexNum += 0x22;
                    edgeNum = 0;

                    break;

                case -4:
                    facing = 4;
                    edgeNum = hexNum + 0x10;

                    break;

                default:
                    System.out.println("initNodeMap error");

                    return;
                }
            }

            count++;
        }
    }

    private final void initHexIDtoNumAux(int begin, int end, int num)
    {
        int i;

        for (i = begin; i <= end; i += 0x22)
        {
            hexIDtoNum[i] = num;
            num++;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getPreferedSize()
    {
        return new Dimension(panelx, panely);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Dimension getMinimumSize()
    {
        return new Dimension(panelx, panely);
    }

    /**
     * Redraw the board using double buffering. Don't call this directly, use
     * {@link Component#repaint()} instead.
     */
    public void paint(Graphics g)
    {
        if (buffer == null ||
	    buffer.getWidth(null) < getWidth() ||
	    buffer.getHeight(null) < getHeight())
        {
            buffer = this.createImage(getWidth(), getHeight());	// panelx, panely
	    //D.ebugPrintln("paint: buffer - ["+getWidth()+", "+getHeight()+"]");
	}
        drawBoard(buffer.getGraphics());
        buffer.flush();
        g.drawImage(buffer, 0, 0, this);
    }

    /**
     * Overriden so the peer isn't painted, which clears background. Don't call
     * this directly, use {@link Component#repaint()} instead.
     */
    public void update(Graphics g)
    {
        paint(g);
    }

    Color[] colorForDiceNum = 
    {
      new Color(0x445566), // 0 not used
      new Color(0x445566), // 1 not used
      new Color(0xFFFF00), // 2
      new Color(0xFFE000), // 3
      new Color(0xFFA000), // 4
      new Color(0xFF7000), // 5
      new Color(0xFF0000), // 6
      new Color(0xAAAAAA), // 7 (grey)
      new Color(0xFF0000), // 8
      new Color(0xFF7000), // 9
      new Color(0xFFA000), // 10
      new Color(0xFFE000), // 11
      new Color(0xFFFF00), // 12
    };

    static int diceFontSize = scalexy == 1 ? 14 : 26;
    static String fontFamily = "SF Compact Rounded";
    static Font diceNumFont = new Font(fontFamily, Font.PLAIN, diceFontSize);

    void drawDiceNum(int diceNum, double lx, double ty, Graphics g) 
    {
      Color color = colorForDiceNum[diceNum];
      double cx = (lx + hexrad * sqrt3_2);
      double cy = (ty + hexrad);
      double r1 = hexrad;
      String num = Integer.toString(diceNum);
      g.setFont(diceNumFont);
      Rectangle2D sb = g.getFontMetrics().getStringBounds(num, g);
      g.setColor(color);
      g.fillOval(rnd(cx-r1/2), rnd(cy-r1/2), rnd(r1), rnd(r1));
      g.setColor(Color.BLACK);
      g.drawString(Integer.toString(diceNum), rnd(cx-sb.getCenterX()), rnd(cy-sb.getCenterY()));
    }

    int rnd(double v) { return (int) Math.round(v); }

    /**
     * Add hex (hexrad) to Graphics, given top-left corner filled with color.
     * @param color fill Color
     * @param lx top-x
     * @param ty left-y
     * @param g Graphics
     */
    void drawHex(int type, double lx, double ty, Graphics g) 
    {
      Color color = colorOfHexType(type);
      double cx = (lx + hexrad * sqrt3_2);
      double cy = (ty + hexrad);
      double d2 = (hexrad * sqrt3_2 + .75); // half of dxdc
      double r2 =  hexrad/2;  // half of hexrad
      double r1 =  hexrad;
      int[] xPoints = { rnd(cx+ 0), rnd(cx+d2), rnd(cx+d2), rnd(cx+ 0), rnd(cx-d2), rnd(cx-d2), };
      int[] yPoints = { rnd(cy-r1), rnd(cy-r2), rnd(cy+r2), rnd(cy+r1), rnd(cy+r2), rnd(cy-r2), };
      g.setColor(color);
      g.fillPolygon(xPoints, yPoints, 6);
      g.setColor(edge);
      g.drawPolygon(xPoints, yPoints, 6);
    }

    static int portFontSize = scalexy == 1 ? 12 : 20;
    static Font threePortFont = new Font(fontFamily, Font.PLAIN, portFontSize);
    static Color threePortColor = new Color(0xffffb4); // light cream

    /**
     * todo: draw port to back of hex
     * @param type 1-5 (resource type)
     * @param port     (orientation: )
     * @param lx
     * @param ty
     * @param g0
     */
    void drawPort(int type, int port, double lx, double ty, Graphics g0) 
    {
      // g.drawImage(ports[port], rnd(lx), rnd(ty), this);
      double cx = (lx + hexrad * sqrt3_2);
      double cy = (ty + hexrad);
      double r1 = hexrad;
      double r2 =  hexrad/2;  // half of hexrad
      double dy0 = r2*.25;
      double dy1 = r2;
      double ex0 = cx + r1 * .0;
      double ex = cx + r1 * sqrt3_2;;
      int[] orient = { -60, 0, 60, 120, 180, 240, };
      double theta = orient[port-1] / (180 / Math.PI);
      Graphics2D g = (Graphics2D) g0.create();

      g.rotate(theta, cx, cy);
      g.setColor(Color.WHITE);
      g.setStroke(new BasicStroke(1 * scalexy));
      g.drawLine(rnd(ex0), rnd(cy-dy0), rnd(ex), rnd(cy + dy1));
      g.drawLine(rnd(ex0), rnd(cy+dy0), rnd(ex), rnd(cy - dy1));
      int cr = 8; // todo: draw arc
      g.fillOval(rnd(ex-cr/2), rnd(cy+dy1-cr/2), cr, cr);
      g.fillOval(rnd(ex-cr/2), rnd(cy-dy1-cr/2), cr, cr);

      g.setColor(colorOfHexType(type > 6 ? 7 : type));
      g.fillOval(rnd(cx-r2), rnd(cy-r2), rnd(r1), rnd(r1));

      g.dispose();

      if (type > 6) {
        g0.setColor(Color.BLACK);
        g0.setFont(threePortFont);
        String num = "3:1";
        Rectangle2D sb = g0.getFontMetrics().getStringBounds(num, g);
        g0.drawString(num, rnd(cx-sb.getCenterX()), rnd(cy-sb.getCenterY()));
      }
    }

    /**
     * 
     * @param type
     * @return
     */
    Color colorOfHexType(int type) 
    {
      // aligned with SOCBoard
      Color[] colors = {
        ColorSquare.DESERT, // index 0, presumably not used
        ColorSquare.CLAY, // 1
        ColorSquare.ORE,  // 2
        ColorSquare.SHEEP,// 3
        ColorSquare.WHEAT,// 4
        ColorSquare.WOOD, // 5
        ColorSquare.WATER,// 6
        ColorSquare.PORT, // 7
      };
      return type < colors.length ? colors[type] : ColorSquare.WATER; 
    }

    /**
     * draw a board tile (new: paint graphics)
     */
    private final void drawHex2(Graphics g, int hexNum)
    {
        int[] hexLayout = board.getHexLayout();
        int[] numberLayout = board.getNumberLayout();
        int hexType = hexLayout[hexNum];

        int wx = (getWidth() - panelx) / 2 + hexX(hexNum);
        int wy = hexY(hexNum);

        // this could be cleaner; encode 3:1 as 7 + dir << 4;
        int type = hexType & 0xF; // get only the last 4 bits; (7-12 implies the 3:1 port direction)
        int port = hexType >> 4; // get the facing of the 2-port

        this.drawHex(hexType <= 6 ? type : 6, wx, wy, g);

        if (type > 6) {
          drawPort(type, type-6, wx, wy, g); // generic 3:1 port
        } else if (port > 0) {
          drawPort(type, port, wx, wy, g); // 2:1 port
        }

        if (numberLayout[hexNum] >= 2)
        {
            // overlay with dice number graphic:
            this.drawDiceNum(numberLayout[hexNum], wx, wy, g);
        }
    }

    /**
     * draw the robber
     */
    private final void drawRobber(Graphics g, int hexID)
    {
        int[] tmpX = new int[14];
        int[] tmpY = new int[14];
        int hexNum = hexIDtoNum[hexID];
        int wx = (getWidth() - panelx) / 2;

        for (int i = 0; i < 14; i++)
        {
            tmpX[i] = robberX[i] + hexX(hexNum) + 18 + wx;
            tmpY[i] = robberY[i] + hexY(hexNum) + 12;
        }

        g.setColor(Color.lightGray);
        g.fillPolygon(tmpX, tmpY, 13);
        g.setColor(Color.black);
        g.drawPolygon(tmpX, tmpY, 14);
    }

    /**
     * draw a road
     */
    private final void drawRoad(Graphics g, int edgeNum, int pn)
    {
        // Draw a road
        int i;
        int[] tmpX = new int[5];
        int[] tmpY = new int[5];
        int hexNum;
        int wx = (getWidth() - panelx) / 2;

        if ((((edgeNum & 0x0F) + (edgeNum >> 4)) % 2) == 0)
        { // If first and second digit 
            hexNum = hexIDtoNum[edgeNum + 0x11]; // are even, then it is '|'.

            for (i = 0; i < 5; i++)
            {
                tmpX[i] = vertRoadX[i] + hexX(hexNum) + wx;
                tmpY[i] = vertRoadY[i] + hexY(hexNum);
            }
        }
        else if (((edgeNum >> 4) % 2) == 0)
        { // If first digit is even,
            hexNum = hexIDtoNum[edgeNum + 0x10]; // then it is '/'.
            //hexNum = hexIDtoNum[edgeNum + 0x10];

            for (i = 0; i < 5; i++)
            {
                tmpX[i] = upRoadX[i] + hexX(hexNum) + wx;
                tmpY[i] = upRoadY[i] + hexY(hexNum);
            }
        }
        else
        { // Otherwise it is '\'.
            hexNum = hexIDtoNum[edgeNum + 0x01];

            for (i = 0; i < 5; i++)
            {
                tmpX[i] = downRoadX[i] + hexX(hexNum) + wx;
                tmpY[i] = downRoadY[i] + hexY(hexNum);
            }
        }

        g.setColor(playerInterface.getPlayerColor(pn));

        g.fillPolygon(tmpX, tmpY, 5);
        g.setColor(Color.black);
        g.drawPolygon(tmpX, tmpY, 5);
    }

    /**
     * draw a settlement
     */
    private final void drawSettlement(Graphics g, int nodeNum, int pn)
    {
        int i;
        int[] tmpX = new int[7];
        int[] tmpY = new int[7];
        int hexNum;
        int wx = (getWidth() - panelx) / 2;

        if (((nodeNum >> 4) % 2) == 0)
        { // If first digit is even,
            hexNum = hexIDtoNum[nodeNum + 0x10]; // then it is a 'Y' node

            for (i = 0; i < 7; i++)
            {
                tmpX[i] = settlementX[i] + hexX(hexNum) + wx;
                tmpY[i] = settlementY[i] + hexY(hexNum) + 11;
            }
        }
        else
        { // otherwise it is an 'A' node
            hexNum = hexIDtoNum[nodeNum - 0x01];

            for (i = 0; i < 7; i++)
            {
                tmpX[i] = settlementX[i] + hexX(hexNum) + wx + 18;
                tmpY[i] = settlementY[i] + hexY(hexNum) + 2;
            }
        }

        // System.out.println("NODEID = "+Integer.toHexString(nodeNum)+" | HEXNUM = "+hexNum);
        g.setColor(playerInterface.getPlayerColor(pn));
        g.fillPolygon(tmpX, tmpY, 6);
        g.setColor(Color.black);
        g.drawPolygon(tmpX, tmpY, 7);
    }

    /**
     * draw a city
     */
    private final void drawCity(Graphics g, int nodeNum, int pn)
    {
        int i;
        int[] tmpX = new int[13];
        int[] tmpY = new int[13];
        int hexNum;
        int wx = (getWidth() - panelx) / 2;

        if (((nodeNum >> 4) % 2) == 0)
        { // If first digit is even,
            hexNum = hexIDtoNum[nodeNum + 0x10]; // then it is a 'Y' node

            for (i = 0; i < 13; i++)
            {
                tmpX[i] = cityX[i] + hexX(hexNum) + wx;
                tmpY[i] = cityY[i] + hexY(hexNum) + 11;
            }
        }
        else
        { // otherwise it is an 'A' node
            hexNum = hexIDtoNum[nodeNum - 0x01];

            for (i = 0; i < 13; i++)
            {
                tmpX[i] = cityX[i] + hexX(hexNum) + wx + 18;
                tmpY[i] = cityY[i] + hexY(hexNum) + 2;
            }
        }

        g.setColor(playerInterface.getPlayerColor(pn));

        g.fillPolygon(tmpX, tmpY, 8);
        g.setColor(Color.black);
        g.drawPolygon(tmpX, tmpY, 13);
    }

    /**
     * draw the arrow that shows whos turn it is
     */
    private final void drawArrow(Graphics g, int pnum, int diceResult) {
        if (pnum < 0) return;
        // {blue, pink, green, yellow}
        // {top-left, top-right, bottom-right, bottom-left} 253, 222
        int wx = getWidth(); // >= panelx
        int hy = getHeight(); // >= panely; mostly =panely
        int[] px = { +3, wx - 40, wx - 40, +3 };
        int[] py = { +5, +5, hy - HEXHEIGHT, hy - HEXHEIGHT };
        int[] pxd = { 13, wx - 40, wx - 40, 13 };
        int[] pyd = { 10, 10, hy - HEXWIDTH, hy - HEXWIDTH };
        g.drawImage(arrows[pnum], px[pnum], py[pnum], this);
        if ((diceResult >= 2) && (game.getGameState() != SOCGame.PLAY)) {
            g.drawImage(dice[diceResult], pxd[pnum], pyd[pnum], this);
        }
    }

    /**
     * draw the whole board
     */
    private void drawBoard(Graphics g)
    {
        g.setPaintMode();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        // D.ebugPrintln("drawBoard - ["+getWidth()+", "+getHeight()+"]");

        for (int i = 0; i < 37; i++)
        {
            drawHex2(g, i);
        }

        if ((mode != PLACE_ROBBER) && (board.getRobberHex() != -1))
        {
            drawRobber(g, board.getRobberHex());
        }

        int pn;
        int idx;
        int max;

        int gameState = game.getGameState();

        if (gameState != SOCGame.NEW)
        {
            drawArrow(g, game.getCurrentPlayerNumber(), game.getCurrentDice());
        }

        /**
         * draw the roads
         */
        Enumeration roads = board.getRoads().elements();

        while (roads.hasMoreElements())
        {
            SOCRoad r = (SOCRoad) roads.nextElement();
            drawRoad(g, r.getCoordinates(), r.getPlayer().getPlayerNumber());
        }

        /**
         * draw the settlements
         */
        Enumeration settlements = board.getSettlements().elements();

        while (settlements.hasMoreElements())
        {
            SOCSettlement s = (SOCSettlement) settlements.nextElement();
            drawSettlement(g, s.getCoordinates(), s.getPlayer().getPlayerNumber());
        }

        /**
         * draw the cities
         */
        Enumeration cities = board.getCities().elements();

        while (cities.hasMoreElements())
        {
            SOCCity c = (SOCCity) cities.nextElement();
            drawCity(g, c.getCoordinates(), c.getPlayer().getPlayerNumber());
        }

        /**
         * Draw the hilight when in interactive mode
         */
        switch (mode)
        {
        case PLACE_ROAD:
        case PLACE_INIT_ROAD:

            if (hilight > 0)
            {
                drawRoad(g, hilight, player.getPlayerNumber());
            }

            break;

        case PLACE_SETTLEMENT:
        case PLACE_INIT_SETTLEMENT:

            if (hilight > 0)
            {
                drawSettlement(g, hilight, player.getPlayerNumber());
            }

            break;

        case PLACE_CITY:

            if (hilight > 0)
            {
                drawCity(g, hilight, player.getPlayerNumber());
            }

            break;

        case CONSIDER_LM_SETTLEMENT:
        case CONSIDER_LT_SETTLEMENT:

            if (hilight > 0)
            {
                drawSettlement(g, hilight, otherPlayer.getPlayerNumber());
            }

            break;

        case CONSIDER_LM_ROAD:
        case CONSIDER_LT_ROAD:

            if (hilight > 0)
            {
                drawRoad(g, hilight, otherPlayer.getPlayerNumber());
            }

            break;

        case CONSIDER_LM_CITY:
        case CONSIDER_LT_CITY:

            if (hilight > 0)
            {
                drawCity(g, hilight, otherPlayer.getPlayerNumber());
            }

            break;

        case PLACE_ROBBER:

            if (hilight > 0)
            {
                drawRobber(g, hilight);
            }

            break;
        }
    }

    /**
     * update the type of interaction mode
     */
    public void updateMode()
    {
        if (player != null)
        {
            if (game.getCurrentPlayerNumber() == player.getPlayerNumber())
            {
                switch (game.getGameState())
                {
                case SOCGame.START1A:
                case SOCGame.START2A:
                    mode = PLACE_INIT_SETTLEMENT;

                    break;

                case SOCGame.START1B:
                case SOCGame.START2B:
                    mode = PLACE_INIT_ROAD;

                    break;

                case SOCGame.PLACING_ROAD:
                case SOCGame.PLACING_FREE_ROAD1:
                case SOCGame.PLACING_FREE_ROAD2:
                    mode = PLACE_ROAD;

                    break;

                case SOCGame.PLACING_SETTLEMENT:
                    mode = PLACE_SETTLEMENT;

                    break;

                case SOCGame.PLACING_CITY:
                    mode = PLACE_CITY;

                    break;

                case SOCGame.PLACING_ROBBER:
                    mode = PLACE_ROBBER;

                    break;

                default:
                    mode = NONE;

                    break;
                }
            }
            else
            {
                mode = NONE;
            }
        }
        else
        {
            mode = NONE;
        }
    }

    /**
     * set the player that is using this board panel
     */
    public void setPlayer()
    {
        player = game.getPlayer(playerInterface.getClient().getNickname());
    }

    /**
     * set the other player
     *
     * @param op  the other player
     */
    public void setOtherPlayer(SOCPlayer op)
    {
        otherPlayer = op;
    }

    /*********************************
     * Handle Events
     *********************************/
    public void mouseEntered(MouseEvent e)
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
    public void mouseDragged(MouseEvent e)
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
        if (mode != NONE)
        {
            hilight = 0;
            repaint();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void mouseMoved(MouseEvent e)
    {
        int x = e.getX() - (getWidth()-panelx)/2;
        int y = e.getY();

        int edgeNum;
        int nodeNum;
        int hexNum;

        switch (mode)
        {
        case NONE:
            break;

        case PLACE_INIT_ROAD:

            /**** Code for finding an edge ********/
            edgeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                edgeNum = findEdge(x, y);

                // Figure out if this is a legal road
                // It must be attached to the last stlmt
                if (!((player.isPotentialRoad(edgeNum)) && ((edgeNum == initstlmt) || (edgeNum == (initstlmt - 0x11)) || (edgeNum == (initstlmt - 0x01)) || (edgeNum == (initstlmt - 0x10)))))
                {
                    edgeNum = 0;
                }

                if (hilight != edgeNum)
                {
                    hilight = edgeNum;
                    repaint();
                }
            }

            break;

        case PLACE_ROAD:

            /**** Code for finding an edge ********/
            edgeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                edgeNum = findEdge(x, y);

                if (!player.isPotentialRoad(edgeNum))
                {
                    edgeNum = 0;
                }

                if (hilight != edgeNum)
                {
                    hilight = edgeNum;
                    repaint();
                }
            }

            break;

        case PLACE_SETTLEMENT:
        case PLACE_INIT_SETTLEMENT:

            /**** Code for finding a node *********/
            nodeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                nodeNum = findNode(x, y);

                if (!player.isPotentialSettlement(nodeNum))
                {
                    nodeNum = 0;
                }

                if (hilight != nodeNum)
                {
                    hilight = nodeNum;
                    repaint();
                }
            }

            break;

        case PLACE_CITY:

            /**** Code for finding a node *********/
            nodeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                nodeNum = findNode(x, y);

                if (!player.isPotentialCity(nodeNum))
                {
                    nodeNum = 0;
                }

                if (hilight != nodeNum)
                {
                    hilight = nodeNum;
                    repaint();
                }
            }

            break;

        case PLACE_ROBBER:

            /**** Code for finding a hex *********/
            hexNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                hexNum = findHex(x, y);

                if (hexNum == board.getRobberHex())
                {
                    hexNum = 0;
                }

                if (hilight != hexNum)
                {
                    hilight = hexNum;
                    repaint();
                }
            }

            break;

        case CONSIDER_LM_SETTLEMENT:
        case CONSIDER_LT_SETTLEMENT:

            /**** Code for finding a node *********/
            nodeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                nodeNum = findNode(x, y);

                //if (!otherPlayer.isPotentialSettlement(nodeNum))
                //  nodeNum = 0;
                if (hilight != nodeNum)
                {
                    hilight = nodeNum;
                    repaint();
                }
            }

            break;

        case CONSIDER_LM_ROAD:
        case CONSIDER_LT_ROAD:

            /**** Code for finding an edge ********/
            edgeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                edgeNum = findEdge(x, y);

                if (!otherPlayer.isPotentialRoad(edgeNum))
                {
                    edgeNum = 0;
                }

                if (hilight != edgeNum)
                {
                    hilight = edgeNum;
                    repaint();
                }
            }

            break;

        case CONSIDER_LM_CITY:
        case CONSIDER_LT_CITY:

            /**** Code for finding a node *********/
            nodeNum = 0;

            if ((ptrOldX != x) || (ptrOldY != y))
            {
                ptrOldX = x;
                ptrOldY = y;
                nodeNum = findNode(x, y);

                if (!otherPlayer.isPotentialCity(nodeNum))
                {
                    nodeNum = 0;
                }

                if (hilight != nodeNum)
                {
                    hilight = nodeNum;
                    repaint();
                }
            }

            break;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param evt DOCUMENT ME!
     */
    public void mousePressed(MouseEvent evt)
    {
        int x = evt.getX() - (getWidth()-panelx)/2;
        int y = evt.getY();

        if (hilight > 0)
        {
            SOCPlayerClient client = playerInterface.getClient();

            switch (mode)
            {
            case NONE:
                break;

            case PLACE_INIT_ROAD:
            case PLACE_ROAD:

                if (player.isPotentialRoad(hilight))
                {
                    client.putPiece(game, new SOCRoad(player, hilight));
                }

                break;

            case PLACE_INIT_SETTLEMENT:
                initstlmt = hilight;

                if (player.isPotentialSettlement(hilight))
                {
                    client.putPiece(game, new SOCSettlement(player, hilight));
                }

                break;

            case PLACE_SETTLEMENT:

                if (player.isPotentialSettlement(hilight))
                {
                    client.putPiece(game, new SOCSettlement(player, hilight));
                }

                break;

            case PLACE_CITY:

                if (player.isPotentialCity(hilight))
                {
                    client.putPiece(game, new SOCCity(player, hilight));
                }

                break;

            case PLACE_ROBBER:

                if (hilight != board.getRobberHex())
                {
                    client.moveRobber(game, player, hilight);
                }

                break;

            case CONSIDER_LM_SETTLEMENT:

                if (otherPlayer.isPotentialSettlement(hilight))
                {
                    client.considerMove(game, otherPlayer.getName(), new SOCSettlement(otherPlayer, hilight));
                }

                break;

            case CONSIDER_LM_ROAD:

                if (otherPlayer.isPotentialRoad(hilight))
                {
                    client.considerMove(game, otherPlayer.getName(), new SOCRoad(otherPlayer, hilight));
                }

                break;

            case CONSIDER_LM_CITY:

                if (otherPlayer.isPotentialCity(hilight))
                {
                    client.considerMove(game, otherPlayer.getName(), new SOCCity(otherPlayer, hilight));
                }

                break;

            case CONSIDER_LT_SETTLEMENT:

                if (otherPlayer.isPotentialSettlement(hilight))
                {
                    client.considerTarget(game, otherPlayer.getName(), new SOCSettlement(otherPlayer, hilight));
                }

                break;

            case CONSIDER_LT_ROAD:

                if (otherPlayer.isPotentialRoad(hilight))
                {
                    client.considerTarget(game, otherPlayer.getName(), new SOCRoad(otherPlayer, hilight));
                }

                break;

            case CONSIDER_LT_CITY:

                if (otherPlayer.isPotentialCity(hilight))
                {
                    client.considerTarget(game, otherPlayer.getName(), new SOCCity(otherPlayer, hilight));
                }

                break;
            }

            mode = NONE;
            hilight = 0;
        }
    }

    /**
     * given a pixel on the board, find the edge that contains it
     *
     * @param x  x coordinate
     * @param y  y coordinate
     * @return the coordinates of the edge
     */
    private final int findEdge(int x, int y)
    {
        // find which grid section the pointer is in 
        // ( 31 is the y-distance between the centers of two hexes )
        int sector = (x / 18) + ((y / 10) * 15);
	if (sector < 0 || sector >= edgeMap.length) return 0;

        // System.out.println("SECTOR = "+sector+" | EDGE = "+edgeMap[sector]);
        return edgeMap[sector];
    }

    /**
     * given a pixel on the board, find the node that contains it
     *
     * @param x  x coordinate
     * @param y  y coordinate
     * @return the coordinates of the node
     */
    private final int findNode(int x, int y)
    {
        // find which grid section the pointer is in 
        // ( 31 is the y-distance between the centers of two hexes )
        int sector = ((x + 9) / 18) + (((y + 5) / 10) * 15);
        if (sector < 0 || sector >= nodeMap.length) return 0;

        // System.out.println("SECTOR = "+sector+" | NODE = "+nodeMap[sector]);
        return nodeMap[sector];
    }

    /**
     * given a pixel on the board, find the hex that contains it
     *
     * @param x  x coordinate
     * @param y  y coordinate
     * @return the coordinates of the hex
     */
    private final int findHex(int x, int y)
    {
        // find which grid section the pointer is in 
        // ( 31 is the y-distance between the centers of two hexes )
        int sector = (x / 18) + ((y / 10) * 15);
        if (sector < 0 || sector >= hexMap.length) return 0;

        // System.out.println("SECTOR = "+sector+" | HEX = "+hexMap[sector]);
        return hexMap[sector];
    }

    /**
     * set the interaction mode
     *
     * @param m  mode
     */
    public void setMode(int m)
    {
        mode = m;
    }

    /**
     * get the interaction mode
     *
     * @return the mode
     */
    public int getMode()
    {
        return mode;
    }
    private static Image getImage(Toolkit tk, Class clazz, MediaTracker tracker, String name)
    {
	Image img=tk.getImage(clazz.getResource(IMAGEDIR + "/"+name+".gif"));
	tracker.addImage(img,0);
	return img;
    }

    /**
     * load the images for the board
     * we need to know if this board is in an applet
     * or an application
     */
    private static synchronized void loadImages(Component c)
    {
        if (hexes != null) { return; }

        MediaTracker tracker = new MediaTracker(c);
        Toolkit tk = c.getToolkit();
        Class clazz = c.getClass();

        String[] numberNames = { "null", "null", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "eleven", "twelve" };
        // String[] numberNames = { "two", "three", "four", "five", "six", "eight",
        // "nine", "ten", "eleven", "twelve" };

        hexes = new Image[13];
        numbers = new Image[numberNames.length];
        ports = new Image[7];

	    dice = new Image[14];
        arrows = new Image[4];
	
        hexes[SOCBoard.DESERT_HEX] = getImage(tk, clazz, tracker, "desertHex");
        hexes[SOCBoard.CLAY_HEX] = getImage(tk, clazz, tracker, "clayHex");
        hexes[SOCBoard.ORE_HEX] = getImage(tk, clazz, tracker, "oreHex");
        hexes[SOCBoard.SHEEP_HEX] = getImage(tk, clazz, tracker, "sheepHex");
        hexes[SOCBoard.WHEAT_HEX] = getImage(tk, clazz, tracker, "wheatHex");
        hexes[SOCBoard.WOOD_HEX] = getImage(tk, clazz, tracker, "woodHex");
        hexes[SOCBoard.WATER_HEX] = getImage(tk, clazz, tracker, "waterHex");

        // generic 3:1 Misc Port tile, on per facing:
        for (int i = 0; i < 6; i++) {
            hexes[i + 7] = getImage(tk, clazz, tracker, "miscPort" + i);
        }

        // untyped resource port tile, one per facing:
        for (int i = 0; i < 6; i++) {
            ports[i + 1] = getImage(tk, clazz, tracker, "port" + i);
        }

        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = getImage(tk, clazz, tracker, numberNames[i]);
        }

        arrowL = getImage(tk, clazz, tracker, "arrowL");
        arrowR = getImage(tk, clazz, tracker, "arrowR");

        for (int i = 0; i < 4; i++) {
            arrows[i] = getImage(tk, clazz, tracker, "arrow" + i);
        }

        for (int i = 2; i < 13; i++) {
            dice[i] = getImage(tk, clazz, tracker, "dice" + i);
        }

        try {
            tracker.waitForID(0);
        } catch (InterruptedException e) {
        }

        if (tracker.isErrorID(0)) {
            System.out.println("Error loading board images");
        }
    }

    /**
     * @return panelx
     */
    public static int getPanelX()
    {
        return panelx;
    }

    /**
     * @return panely
     */
    public static int getPanelY()
    {
        return panely;
    }
}
