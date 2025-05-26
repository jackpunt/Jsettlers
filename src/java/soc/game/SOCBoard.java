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
package soc.game;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;


/**
 * This is a representation of the board in Settlers of Catan.
 *
 * @author Robert S Thomas
 */
public class SOCBoard implements Serializable, Cloneable
{
    public static final int DESERT_HEX = 0;
    // these need to sync with SOCResourceConstants!
    // so we synch them:
    public static final int  CLAY_HEX = SOCResourceConstants.CLAY; 
    public static final int   ORE_HEX = SOCResourceConstants.ORE;
    public static final int SHEEP_HEX = SOCResourceConstants.SHEEP;
    public static final int WHEAT_HEX = SOCResourceConstants.WHEAT;
    public static final int  WOOD_HEX = SOCResourceConstants.WOOD; // 5
    public static final int  LAND_HEX = SOCResourceConstants.MAX;
    public static final int WATER_HEX = 6;

    public static final int  MISC_PORT_HEX =  7; // 3-for-1 port
    // per getHexTypeFromNumber, these constants/values are never actually used
    public static final int  CLAY_PORT_HEX =  CLAY_HEX+MISC_PORT_HEX;
    public static final int   ORE_PORT_HEX =   ORE_HEX+MISC_PORT_HEX;
    public static final int SHEEP_PORT_HEX = SHEEP_HEX+MISC_PORT_HEX;
    public static final int WHEAT_PORT_HEX = WHEAT_HEX+MISC_PORT_HEX;
    public static final int  WOOD_PORT_HEX =  WOOD_HEX+MISC_PORT_HEX;

    // these used only on end-points:
    public static final int MISC_PORT = 0;// include 3:1 ports
    public static final int MIN_PORT = 1; // start with 2:1 resource ports
//      public static final int CLAY_PORT = 1;
//      public static final int ORE_PORT = 2;
//      public static final int SHEEP_PORT = 3;
//      public static final int WHEAT_PORT = 4;
//      public static final int WOOD_PORT = 5;
    public static final int MAX_PORT = 5;

    /**
     * largest value for a hex
     */
    public static final int MAXHEX = 0xDD;

    /**
     * smallest value for a hex
     */
    public static final int MINHEX = 0x11;

    /**
     * largest value for an edge
     */
    public static final int MAXEDGE = 0xCC;

    /**
     * smallest value for an edge
     */
    public static final int MINEDGE = 0x22;

    /**
     * largest value for a node
     */
    public static final int MAXNODE = 0xDC;

    /**
     * smallest value for a node
     */
    public static final int MINNODE = 0x23;

    /**
     * largest value for a node plus one
     */
    public static final int MAXNODEPLUSONE = MAXNODE + 1;

    /***************************************
       Key to the hexes[] :
       0 : desert
       1 : clay
       2 : ore
       3 : sheep
       4 : wheat
       5 : wood
       6 : water
       7 : misc port facing 1
       8 : misc port facing 2
       9 : misc port facing 3
       10 : misc port facing 4
       11 : misc port facing 5
       12 : misc port facing 6
        ports are represented in binary like this:
        (port facing)           (kind of port)
              \--> [0 0 0][0 0 0 0] <--/
        kind of port:
        1 : clay
        2 : ore
        3 : sheep
        4 : wheat
        5 : wood
        port facing:
        6___    ___1
            \/\/
            /  \
       5___|    |___2
           |    |
            \  /
        4___/\/\___3
     **************************************************/

    /*
       private int hexLayout[] = {
           51, 6, 10, 6,
          6, 5, 3, 4, 68,
         8, 1, 2, 1, 3, 6,
       6, 0, 5, 4, 5, 4, 85,
         8, 1, 3, 3, 2,  6,
           6, 2, 4, 5, 12,
            18, 6, 97, 6 };
     */
    private int[] hexLayout = 
    {
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
        6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6
    };

    /* For -one- placement of robber:
       private int numberLayout[] = {
              0,  0,  0,  0,
            0, 11, 12,  9,  0,
          0,  4,  3,  6, 10,  0,
        0,  8, 11,  0,  5,  8,  0,
          0,  10,  9,  4,  3,  0,
            0,  5,  2,  6,  0,
              0,  0,  0,  0 };
     */
    private int[] numberLayout = 
    {
    	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };				// 37 elements
    private int[] numToHexID = 
    {
              0x17, 0x39, 0x5B, 0x7D, 		// 0-4
        
	         0x15, 0x37, 0x59, 0x7B, 0x9D,	// 5-9
        
        0x13, 0x35, 0x57, 0x79, 0x9B, 0xBD, 	// 10-15
        
     0x11, 0x33, 0x55, 0x77, 0x99, 0xBB, 0xDD, 	// 16-22
        
        0x31, 0x53, 0x75, 0x97, 0xB9, 0xDB, 	// 23-28
        
           0x51, 0x73, 0x95, 0xB7, 0xD9, 	// 29-33
        
              0x71, 0x93, 0xB5, 0xD7 		// 34-37
    };

    /**
     * translate hex ID to an array index
     */
    private int[] hexIDtoNum;

    /**
     * add to hex coord to get all node coords
     */
    private int[] hexNodes = { 0x01, 0x12, 0x21, 0x10, -0x01, -0x10 };

    /**
     *  all hexes adjacent to a node
     */
    private int[] nodeToHex = { -0x21, 0x01, -0x01, -0x10, 0x10, -0x12 };

    /**
     * the hex that the robber is in
     */
    private int robberHex;

    /**
     * where the ports are: elts={node1_ID, node2_ID}
     */
    private Vector<Integer>[] ports;

    /**
     * pieces on the board
     */
    private Vector<SOCPlayingPiece> pieces;

    /**
     * roads on the board
     */
    private Vector<SOCPlayingPiece> roads;

    /**
     * settlements on the board
     */
    private Vector<SOCPlayingPiece> settlements;

    /**
     * cities on the board
     */
    private Vector<SOCPlayingPiece> cities;

    /**
     * random number generator
     */
    private Random rand = new Random();

    /**
     * a list of nodes on the board
     */
    protected boolean[] nodesOnBoard;

    /**
     * Create a new Settlers of Catan Board
     */
    public SOCBoard()
    {
        robberHex = -1;

        /**
         * generic counter
         */
        int i;

        /**
         * initialize the pieces vectors
         */
        pieces = new Vector(96);
        roads = new Vector(60);
        settlements = new Vector(20);
        cities = new Vector(16);

        /**plus
         * initialize the port Vectors; holds Vector of coords for each port tile
         */
        ports = new Vector[6];
        ports[MISC_PORT] = new Vector(8); // 8 elements (2 coords per 4 misc port-hex)

        for (i = MIN_PORT; i <= MAX_PORT; i++)
        {
            ports[i] = new Vector(2); // 2 elements (2 coords per resource port-hex)
        }

        /**
         * initialize the hexIDtoNum array
         */
        hexIDtoNum = new int[0xEE];
        nodesOnBoard = new boolean[0xEE];

        for (i = 0; i < 0xEE; i++)
        {
            hexIDtoNum[i] = 0;
            nodesOnBoard[i] = false;
        }

        // insert index to Hex within numToHexID:
        for (i = 0; i < numToHexID.length; i++) {
            hexIDtoNum[numToHexID[i]] = i;
        }

        /**
         * initialize the list of nodes on the board
         */
        for (i = 0x27; i <= 0x8D; i += 0x11) {nodesOnBoard[i]=true;}
        for (i = 0x25; i <= 0xAD; i += 0x11) {nodesOnBoard[i]=true;}
        for (i = 0x23; i <= 0xCD; i += 0x11) {nodesOnBoard[i]=true;}
        for (i = 0x32; i <= 0xDC; i += 0x11) {nodesOnBoard[i]=true;}
        for (i = 0x52; i <= 0xDA; i += 0x11) {nodesOnBoard[i]=true;}
        for (i = 0x72; i <= 0xD8; i += 0x11) {nodesOnBoard[i]=true;}
    }

    /** permute given array in place. */
    <T> void permute (T[] ary) {
        int len = ary.length - 1;
        for (int i = len; i > 0; i--) {
          int ndx = rand.nextInt(i + 1);
          T tmp = ary[i];
          ary[i] = ary[ndx];
          ary[ndx] = tmp;
        }
    }
    void permuteInt (int[] ary) {
        int len = ary.length - 1;
        for (int i = len; i > 0; i--) {
          int ndx = rand.nextInt(i + 1);
          int tmp = ary[i];
          ary[i] = ary[ndx];
          ary[ndx] = tmp;
        }
    }

    /**
     * Shuffle the hex tiles and layout a board
     */
    public void makeNewBoard()
    {
      	// there are 8 water, 8 port, 19 land tiles: 37 total
        // 19 land tiles:
      	int[] landHex = { DESERT_HEX, CLAY_HEX, CLAY_HEX, CLAY_HEX, ORE_HEX, ORE_HEX, ORE_HEX,
			  SHEEP_HEX, SHEEP_HEX, SHEEP_HEX, SHEEP_HEX, 
			  WHEAT_HEX, WHEAT_HEX, WHEAT_HEX, WHEAT_HEX, 
			  WOOD_HEX, WOOD_HEX, WOOD_HEX, WOOD_HEX, };
	      // 8 port tiles: { 0,0,0,0,1,2,3,4,5}
        int[] portHex = { MISC_PORT, MISC_PORT, MISC_PORT, MISC_PORT,
			  CLAY_HEX, ORE_HEX, SHEEP_HEX, WHEAT_HEX, WOOD_HEX };
	
        // A,B,C,...Q; indicating which die roll activates this resource tile:
        // 0-9 -> [2-6,8-12]  (also -1 for the desert)
        // int[] number = { 3, 0, 4, 1, 5, 7, 6, 9, 8, 2, 5, 7, 6, 2, 3, 4, 1, 8 };
        // use direct dice numbers:
        int[] number = { 5, 2, 6, 3, 8,10, 9,12,11, 4, 8,10, 9, 4, 5, 6, 3,11 };
      	// place shuffled stack on hex map in this order: (these are the grid numbers)
        int[] numPath = { 29, 30, 31, 26, 20, 13, 7, 6, 5, 10, 16, 23, 24, 25, 19, 12, 11, 17, 18 };
        int i;
        int j;
        int idx;
        int tmp;

        permuteInt(landHex);

        int cnt = 0;

        for (i = 0; i < landHex.length; i++)
        {
            // place the land hexes
            hexLayout[numPath[i]] = landHex[i];

            // place the robber on desert
            if (landHex[i] == 0)
            {
                robberHex = numToHexID[numPath[i]];
                numberLayout[numPath[i]] = 0; // no number[] for robber
            }
            else
            {
                // place the numbers
                numberLayout[numPath[i]] = number[cnt];
                cnt++;
            }
        }

        // shuffle the ports
        permuteInt(portHex);

        // set ports in place and orientation: the ports
        placePort(portHex[0],  0, 3);
        placePort(portHex[1],  2, 4);
        placePort(portHex[2],  8, 4);
        placePort(portHex[3],  9, 2);
        placePort(portHex[4], 21, 5);
        placePort(portHex[5], 22, 2);
        placePort(portHex[6], 32, 6);
        placePort(portHex[7], 33, 1);
	      placePort(portHex[8], 35, 6);

        // fill out the ports[] vectors (or call setHexLayout(hexLayout)!)
      	setHexLayout(hexLayout); // just to set the port-node vectors
    }

    /* ***************************************
        ports are represented in binary like this:
        (port facing)           (kind of port)
              \--> [0 0 0][0 0 0 0] <--/
        kind of port:
        1 : clay
        2 : ore
        3 : sheep
        4 : wheat
        5 : wood
        port facing:
        6___    ___1
            \/\/
            /  \
       5___|    |___2
           |    |
            \  /
        4___/\/\___3
    *****************************************/

    /**
     * Auxillary method for placing the port hexes
     */
    private final void placePort(int port, int hex, int face)
    {
        hexLayout[hex] = (port == 0) 
          ? MISC_PORT_HEX + face-1 // add face to MISC_PORT_HEX
          : (face << 4) + port;    // else code face in high bits!
  	      // port value is: [0..8] (3, make it 4 bits)
    }

    /**
     * @return the hex layout
     */
    public int[] getHexLayout()
    {
        return hexLayout;
    }

    /**
     * @return the number layout
     */
    public int[] getNumberLayout()
    {
        return numberLayout;
    }

    /**
     * @return where the robber is
     */
    public int getRobberHex()
    {
        return robberHex;
    }

    /**
     * set the hexLayout
     *
     * @param hl  the hex layout
     */
    public void setHexLayout(int[] hl)
    {
        hexLayout = hl;

        if (hl[0] == 6)
        {
            /**
             * this is a blank board
             */
            return;
        }

        /**
         * fill in the port node information
         */
        ports[getPortTypeFromHex(hexLayout[0])].addElement((0x27));
        ports[getPortTypeFromHex(hexLayout[0])].addElement((0x38));

        ports[getPortTypeFromHex(hexLayout[2])].addElement((0x5A));
        ports[getPortTypeFromHex(hexLayout[2])].addElement((0x6B));

        ports[getPortTypeFromHex(hexLayout[8])].addElement((0x9C));
        ports[getPortTypeFromHex(hexLayout[8])].addElement((0xAD));

        ports[getPortTypeFromHex(hexLayout[9])].addElement((0x25));
        ports[getPortTypeFromHex(hexLayout[9])].addElement((0x34));

        ports[getPortTypeFromHex(hexLayout[21])].addElement((0xCD));
        ports[getPortTypeFromHex(hexLayout[21])].addElement((0xDC));

        ports[getPortTypeFromHex(hexLayout[22])].addElement((0x43));
        ports[getPortTypeFromHex(hexLayout[22])].addElement((0x52));

        ports[getPortTypeFromHex(hexLayout[32])].addElement((0xC9));
        ports[getPortTypeFromHex(hexLayout[32])].addElement((0xDA));

        ports[getPortTypeFromHex(hexLayout[33])].addElement((0x72));
        ports[getPortTypeFromHex(hexLayout[33])].addElement((0x83));

        ports[getPortTypeFromHex(hexLayout[35])].addElement((0xA5));
        ports[getPortTypeFromHex(hexLayout[35])].addElement((0xB6));
    }

    /**
     * @return the type of port given a hex type
     * @param hex  the hex type
     */
    public int getPortTypeFromHex(int hex) // private...?
    {
        int portType = hex & 0xF;

        if (portType >= 7)
        {
            portType = 0;	// MISC_PORT_HEX (7-12) -> MISC_PORT (0)
        }

        return portType;
    }

    /**
     * set the number layout
     *
     * @param nl  the number layout
     */
    public void setNumberLayout(int[] nl)
    {
        numberLayout = nl;
    }

    /**
     * set where the robber is
     *
     * @param rh  the robber hex
     */
    public void setRobberHex(int rh)
    {
        robberHex = rh;
    }

    /**
     * @return the list of (node) coordinates for a type of port 
     *
     * @param portType  the type of port
     */
    public Vector getPortCoordinates(int portType)
    {
        return ports[portType];
    }

    /**
     * Given a hex coordinate, return the number on that hex
     *
     * @param hex  the coordinates for a hex
     *
     * @return the number on that hex
     */
    public int getNumberOnHexFromCoord(int hex)
    {
        return getNumberOnHexFromNumber(hexIDtoNum[hex]);
    }

    /**
     * Given a hex number, return the number on that hex [or zero]
     *
     * @param hex  the number of a hex
     *
     * @return the number on that hex (or zero, if not a resource)
     */
    public int getNumberOnHexFromNumber(int ndx)
    {
	return Math.max(0,numberLayout[ndx]);
    }

    /**
     * Given a hex coordinate, return the type of hex
     *
     * @param hex  the coordinates for a hex
     *
     * @return the type of hex
     */
    public int getHexTypeFromCoord(int hex)
    {
        return getHexTypeFromNumber(hexIDtoNum[hex]);
    }

    /**
     * Given a hex number, return the type of hex.
     * In practice, only care if is a resource versus port/water hex.
     * (or desert, when placing robber).
     * the higher values (MAX_PORT_HEX and greater) are not used.
     *
     * @param hex  the number of a hex
     *
     * @return the type of hex
     */
    public int getHexTypeFromNumber(int hex)
    {
        int hexType = hexLayout[hex];

        if (hexType < MISC_PORT_HEX) {
            return hexType;	// DESERT, ORE, ..., WATER
        } else if ((hexType >= MISC_PORT_HEX) && (hexType < MISC_PORT_HEX+6)) {
            return MISC_PORT_HEX; // remove MISC_PORT 'facing' info:
        }
	int rv = (hexType & 7);	// extract 3 low-bits (port type?)
	// remove resource port facing info:
	return ((rv >= 1) && (rv <= 5)) ? MISC_PORT_HEX + rv : -1;
	// there are no "water" ports...
    }

    /**
     * put a piece on the board
     */
    public void putPiece(SOCPlayingPiece pp)
    {
        pieces.addElement(pp);

        switch (pp.getType())
        {
        case SOCPlayingPiece.ROAD:
            roads.addElement(pp);

            break;

        case SOCPlayingPiece.SETTLEMENT:
            settlements.addElement(pp);

            break;

        case SOCPlayingPiece.CITY:
            cities.addElement(pp);

            break;
        }
    }

    /**
     * remove a piece from the board
     */
    public void removePiece(SOCPlayingPiece piece)
    {
        Enumeration<SOCPlayingPiece> pEnum = pieces.elements();

        while (pEnum.hasMoreElements())
        {
            SOCPlayingPiece p = (SOCPlayingPiece) pEnum.nextElement();

            if ((piece.getType() == p.getType()) && (piece.getCoordinates() == p.getCoordinates()))
            {
                pieces.removeElement(p);

                switch (piece.getType())
                {
                case SOCPlayingPiece.ROAD:
                    roads.removeElement(p);

                    break;

                case SOCPlayingPiece.SETTLEMENT:
                    settlements.removeElement(p);

                    break;

                case SOCPlayingPiece.CITY:
                    cities.removeElement(p);

                    break;
                }

                break;
            }
        }
    }

    /**
     * get the list of pieces on the board
     */
    public Vector<SOCPlayingPiece> getPieces()
    {
        return pieces;
    }

    /**
     * get the list of roads
     */
    public Vector<SOCPlayingPiece> getRoads()
    {
        return roads;
    }

    /**
     * get the list of settlements
     */
    public Vector<SOCPlayingPiece> getSettlements()
    {
        return settlements;
    }

    /**
     * get the list of cities
     */
    public Vector<SOCPlayingPiece> getCities()
    {
        return cities;
    }

    /**
     * @return the nodes that touch this edge
     */
    public static Vector<Integer> getAdjacentNodesToEdge(int coord)
    {
        Vector<Integer> nodes = new Vector<Integer>(2);
        int tmp;

        /**
         * if the coords are (even, even), then
         * the road is '|'.
         */
        if ((((coord & 0x0F) + (coord >> 4)) % 2) == 0)
        {
            tmp = coord + 0x01;

            if ((tmp >= MINNODE) && (tmp <= MAXNODE))
            {
                nodes.addElement((tmp));
            }

            tmp = coord + 0x10;

            if ((tmp >= MINNODE) && (tmp <= MAXNODE))
            {
                nodes.addElement((tmp));
            }
        }
        else
        {
            /* otherwise the road is either '/' or '\' */
            tmp = coord;

            if ((tmp >= MINNODE) && (tmp <= MAXNODE))
            {
                nodes.addElement((tmp));
            }

            tmp = coord + 0x11;

            if ((tmp >= MINNODE) && (tmp <= MAXNODE))
            {
                nodes.addElement((tmp));
            }
        }

        return nodes;
    }

    /**
     * @return the adjacent edges to this edge
     */
    public static Vector<Integer> getAdjacentEdgesToEdge(int coord)
    {
        Vector<Integer> edges = new Vector<Integer>(4);
        int tmp;

        /**
         * if the coords are (even, even), then
         * the road is '|'.
         */
        if ((((coord & 0x0F) + (coord >> 4)) % 2) == 0)
        {
            tmp = coord - 0x10;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord + 0x01;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord + 0x10;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord - 0x01;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }
        }

        /**
         * if the coords are (even, odd), then
         * the road is '/'.
         */
        else if (((coord >> 4) % 2) == 0)
        {
            tmp = coord - 0x11;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord + 0x01;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord + 0x11;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord - 0x01;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }
        }
        else
        {
            /**
             * otherwise the coords are (odd, even),
             * and the road is '\'
             */
            tmp = coord - 0x10;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord + 0x11;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord + 0x10;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord - 0x11;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }
        }

        return edges;
    }

    /**
     * @return the hexes touching this node
     */
    public static Vector<Integer> getAdjacentHexesToNode(int coord)
    {
        Vector<Integer> hexes = new Vector<Integer>(3);
        int tmp;

        /**
         * if the coords are (even, odd), then
         * the node is 'Y'.
         */
        if (((coord >> 4) % 2) == 0)
        {
            tmp = coord - 0x10;

            if ((tmp >= MINHEX) && (tmp <= MAXHEX))
            {
                hexes.addElement((tmp));
            }

            tmp = coord + 0x10;

            if ((tmp >= MINHEX) && (tmp <= MAXHEX))
            {
                hexes.addElement((tmp));
            }

            tmp = coord - 0x12;

            if ((tmp >= MINHEX) && (tmp <= MAXHEX))
            {
                hexes.addElement((tmp));
            }
        }
        else
        {
            /**
             * otherwise the coords are (odd, even),
             * and the node is 'upside down Y'.
             */
            tmp = coord - 0x21;

            if ((tmp >= MINHEX) && (tmp <= MAXHEX))
            {
                hexes.addElement((tmp));
            }

            tmp = coord + 0x01;

            if ((tmp >= MINHEX) && (tmp <= MAXHEX))
            {
                hexes.addElement((tmp));
            }

            tmp = coord - 0x01;

            if ((tmp >= MINHEX) && (tmp <= MAXHEX))
            {
                hexes.addElement((coord - 0x01));
            }
        }

        return hexes;
    }

    /**
     * @return the edges touching this node
     */
    public static Vector<Integer> getAdjacentEdgesToNode(int coord)
    {
        Vector<Integer> edges = new Vector<Integer>(3);
        int tmp;

        /**
         * if the coords are (even, odd), then
         * the node is 'Y'.
         */
        if (((coord >> 4) % 2) == 0)
        {
            tmp = coord - 0x11;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord - 0x01;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }
        }
        else
        {
            /**
             * otherwise the coords are (odd, even),
             * and the EDGE is 'upside down Y'.
             */
            tmp = coord - 0x10;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }

            tmp = coord - 0x11;

            if ((tmp >= MINEDGE) && (tmp <= MAXEDGE))
            {
                edges.addElement((tmp));
            }
        }

        return edges;
    }

    /**
     * @return the EDGEs adjacent to this node
     */
    public static Vector<Integer> getAdjacentNodesToNode(int coord)
    {
        Vector<Integer> nodes = new Vector<Integer>(3);
        int tmp;

        tmp = coord - 0x11;

        if ((tmp >= MINNODE) && (tmp <= MAXNODE))
        {
            nodes.addElement((tmp));
        }

        tmp = coord + 0x11;

        if ((tmp >= MINNODE) && (tmp <= MAXNODE))
        {
            nodes.addElement((tmp));
        }

        /**
         * if the coords are (even, odd), then
         * the node is 'Y'.
         */
        if (((coord >> 4) % 2) == 0)
        {
            tmp = (coord + 0x10) - 0x01;

            if ((tmp >= MINNODE) && (tmp <= MAXNODE))
            {
                nodes.addElement(((coord + 0x10) - 0x01));
            }
        }
        else
        {
            /**
             * otherwise the coords are (odd, even),
             * and the node is 'upside down Y'.
             */
            tmp = coord - 0x10 + 0x01;

            if ((tmp >= MINNODE) && (tmp <= MAXNODE))
            {
                nodes.addElement((coord - 0x10 + 0x01));
            }
        }

        return nodes;
    }

    /**
     * @return true if the node is on the board
     */
    public boolean isNodeOnBoard(int node)
    {
	return nodesOnBoard[node];
    }

    /**
     * "string representation of node coords" (sic)
     * @return a String of 3 numbers from hexes surrounding the given node.
     */
    public String nodeCoordToString(int node)
    {
        String str = "";
	      String sep = "";
        Enumeration<Integer> hexes = getAdjacentHexesToNode(node).elements();

        while (hexes.hasMoreElements())
        {
            int hex = ( hexes.nextElement()).intValue();
            int number = getNumberOnHexFromCoord(hex);
            str += ((number == 0) ? (sep+"-") : (sep+number));
            sep = "/";
	}
        return str;
    }

    /**
     * @return a string representation of an edge coordinate
     */
    public String edgeCoordToString(int edge)
    {
        String str;
        int number1;
        int number2;

        /**
         * if the coords are (even, even), then
         * the road is '|'.
         */
        if ((((edge & 0x0F) + (edge >> 4)) % 2) == 0)
        {
            number1 = getNumberOnHexFromCoord(edge - 0x11);
            number2 = getNumberOnHexFromCoord(edge + 0x11);
        }

        /**
         * if the coords are (even, odd), then
         * the road is '/'.
         */
        else if (((edge >> 4) % 2) == 0)
        {
            number1 = getNumberOnHexFromCoord(edge - 0x10);
            number2 = getNumberOnHexFromCoord(edge + 0x10);
        }
        else
        {
            /**
             * otherwise the coords are (odd, even),
             * and the road is '\'
             */
            number1 = getNumberOnHexFromCoord(edge - 0x01);
            number2 = getNumberOnHexFromCoord(edge + 0x01);
        }

        str = number1 + "/" + number2;

        return str;
    }
}
