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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.stream.IntStream;


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
    public static final int[] RES_PORTS = { 1,2,3,4,5};
    public static final int[] ANY_PORTS = { 0,1,2,3,4,5};

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
     * Type of hex to place in each of 37 coords.
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
        port facing: { -, NE, E, SE, SW, W, NW }
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
    /** direction port tile is facing; index into portNodes */
    private int[] portFaces = 
    {    3, 3, 4, 4, 
       3, 0, 0, 0, 4, 
      2, 0, 0, 0, 0, 5,
     2, 0, 0, 0, 0, 0, 5, 
      2, 0, 0, 0, 0, 5, 
        1, 0, 0, 0, 6,
          1, 1, 6, 6,
    };

    // the spiral path: (not used when permute)   
    //  int[] numPath = { 29, 30, 31, 26, 20, 13, 7, 6, 5, 10, 16, 23, 24, 25, 19, 12, 11, 17, 18 };
    /** standard hex numbers of hexes holding 9 port tiles */
    int[] portLocs = { 0, 2, 8, 9, 21, 22, 32, 33, 35 };

    /* For -one- placement of robber:
       private int numberLayout[] = {
              0,  0,  0,  0,       // 0-3
            0, 11, 12,  9,  0,     // 4-8
          0,  4,  3,  6, 10,  0,   // 9-14
        0,  8, 11,  0,  5,  8,  0, // 15-21
          0,  10,  9,  4,  3,  0,  // 22-27
            0,  5,  2,  6,  0,     // 28-32
              0,  0,  0,  0 };     // 33-36 (19 + 18 = 37 hexes)
     */
    private int[] numberLayout = 
    {
    	0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
    };				// 37 elements

    // 0, 2, 8, 9, 21, 22, 32, 33, 35
    // 17 -> 27, 38  S, ES
    // 5B -> 5A, 6B
    // 9D -> 9C, AD
    // 13 -> 25, 34
    // DD -> CD, DC
    // 31 -> 43, 52
    // D9 -> C9, DA
    // 71 -> 72, 83  N, EN
    // B5 -> A5, B6 WN,  N
    //               N:0  EN:1  ES:2   S:3   WS:4   WN:5
    // hexNodes = { 0x01, 0x12, 0x21, 0x10, -0x01, -0x10 }; // Add to coord
    /** Two nodes on hex facing given face
     * @param coord location of hex
     * @param face direction port is facing [1..6]
     */
    int[] nodesOfHexFacing(int coord, int face) { 
      return new int[] { coord + hexNodes[face-1], coord + hexNodes[face % 6] }; 
    }
    
    /** the coords of each of 37 hex locations */
    private int[] numToHexID = 
    {
              0x17, 0x39, 0x5B, 0x7D, 		// 0-3
        
	         0x15, 0x37, 0x59, 0x7B, 0x9D,	// 4-8
        
        0x13, 0x35, 0x57, 0x79, 0x9B, 0xBD, 	// 9-14
        
     0x11, 0x33, 0x55, 0x77, 0x99, 0xBB, 0xDD, 	// 15-21
        
        0x31, 0x53, 0x75, 0x97, 0xB9, 0xDB, 	// 22-27
        
           0x51, 0x73, 0x95, 0xB7, 0xD9, 	// 28-32
        
              0x71, 0x93, 0xB5, 0xD7 		// 33-36
    };

    /** Coordinates: [WN-WS index: 1,3,5,7,9,B,D][WS-EN index: 1,3,5,7,9,B,D]
     * 
     * is adjacent if both indexes are within 2
     */
    boolean isAdjacent(int num0, int num1) {
      int hexID0 = numToHexID[num0];
      int hexID1 = numToHexID[num1];
      int h0 = hexID0 >> 4; int l0 = hexID0 & 0xF; 
      int h1 = hexID1 >> 4; int l1 = hexID1 & 0xF; 
      int dh = Math.abs(h1-h0); int dl = Math.abs(l1-l0);
      return dh <= 2 && dl <= 2;
    }

    /** @return true if one isAdjacent to any of others */
    boolean findAdjacent(Integer one, Stack<Integer> others) {
      for (Integer other : others) {
        if (isAdjacent(one, other)) return true;
      }
      return false;
    }

    /** @returns true if any one of coords isAdjacent to any of the others. */
    boolean anyAdjacent(Stack<Integer> coords) {
      if (coords.empty()) return false;
      Integer one = coords.pop();
      return (one != null) && findAdjacent(one, coords) ? true : anyAdjacent(coords);
    }
    // print array in hex
    void printCoords(String v, int[] coords) {
      System.out.format("%s = [", v);
      for (int coord : coords) {
        System.out.format("%02x, ", numToHexID[coord]);
      }
      System.out.format("]\n");
    }

    int[] mapToIntArray(Integer[] intArray) {
      return Arrays.stream(intArray).mapToInt(i -> i != null ? i : -1).toArray();
    }

    boolean six_eightAdjacent(int[] number, int[] numPath) {
      // printCoords("numPath", numPath);
      Stack<Integer> six_eight = new Stack<Integer>();
      for ( int ndx = 0; ndx < number.length; ndx++ ) {
        if ((number[ndx] == 6) || (number[ndx] == 8)) {
          six_eight.push(numPath[ndx]); // record locations of 6s and 8s
        }
      }
      // int[] given = mapToIntArray(six_eight.toArray(new Integer[0]));
      boolean rv = anyAdjacent(six_eight); // pops from six_eight
      // six_eight is subset of numPath
      // System.out.format("adj = %b; ", rv);
      // printCoords("six_eight", given);

      return rv;
    }

    /**
     * translate hex ID to an array index
     */
    private int[] hexIDtoNum;

    /**
     * add to hex coord to get all node coords (node = 'vertex')
     */
    private int[] hexNodes = { 0x01, 0x12, 0x21, 0x10, -0x01, -0x10 };
    //                          N:0  EN:1  ES:2   S:3   WS:4   WN:5

    /**
     *  all hexes adjacent to a node (add to move up/down & over)
     */
    private int[] nodeToHex = { -0x21, 0x01, -0x01, -0x10, 0x10, -0x12 };

    /**
     * the hex that the robber is in
     */
    private int robberHex;

    /**
     * nodes where the ports are: elts={node1_ID, node2_ID}
     */
    private Vector<Integer>[] ports;

    /**
     * pieces on the board (settlement, road, city) 
     */
    private Vector<SOCPlayingPiece> pieces;

    /**
     * roads on the board
     */
    private Vector<SOCRoad> roads;

    /**
     * settlements on the board
     */
    private Vector<SOCSettlement> settlements;

    /**
     * cities on the board
     */
    private Vector<SOCCity> cities;

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
         * initialize the pieces vectors
         */
        pieces = new Vector<SOCPlayingPiece>(96);
        roads = new Vector<SOCRoad>(60);
        settlements = new Vector<SOCSettlement>(20);
        cities = new Vector<SOCCity>(16);

        /**plus
         * initialize the port Vectors; holds Vector of coords for each port tile
         * 
         * [misc:[8], clay:[2], ore:[2], sheep:[2], wheat:[2], wood:[2]]
         */
        ports = (Vector<Integer>[]) new Vector[6];
        ports[MISC_PORT] = new Vector<Integer>(8); // 8 elements (2 coords per 4 misc port-hex)

        for (int i : RES_PORTS)
        {
            ports[i] = new Vector<Integer>(2); // 2 elements (2 coords per resource port-hex)
        }

        /**
         * initialize the hexIDtoNum array
         */
        hexIDtoNum = new int[0xEE]; // 256 elements
        nodesOnBoard = new boolean[0xEE];

        for (int i = 0; i < 0xEE; i++)
        {
            hexIDtoNum[i] = 0;
            nodesOnBoard[i] = false;
        }

        // insert index to Hex within numToHexID:
        for (int i = 0; i < numToHexID.length; i++) {
            hexIDtoNum[numToHexID[i]] = i;
        }

        /**
         * initialize the list of nodes on the board
         */
        for (int i = 0x27; i <= 0x8D; i += 0x11) {nodesOnBoard[i]=true;}
        for (int i = 0x25; i <= 0xAD; i += 0x11) {nodesOnBoard[i]=true;}
        for (int i = 0x23; i <= 0xCD; i += 0x11) {nodesOnBoard[i]=true;}
        for (int i = 0x32; i <= 0xDC; i += 0x11) {nodesOnBoard[i]=true;}
        for (int i = 0x52; i <= 0xDA; i += 0x11) {nodesOnBoard[i]=true;}
        for (int i = 0x72; i <= 0xD8; i += 0x11) {nodesOnBoard[i]=true;}
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
    /** permute given array in place */
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
     * Insert a '7' somewhere in dieNumbers
     * @returns new array with the extra element
     */
    int[] insertDesert(int[] dieNumbers) {
        int dLoc = rand.nextInt(dieNumbers.length);
        int[] numPath2 = new int[dieNumbers.length + 1];
        System.arraycopy(dieNumbers, 0, numPath2, 0, dLoc);
        numPath2[dLoc] = 7;  // the desert & robber goes here
        System.arraycopy(dieNumbers, dLoc, numPath2, dLoc + 1, dieNumbers.length-dLoc);
        return numPath2;
    }

    /**
     * Shuffle the hex tiles and layout a board
     */
    public void makeNewBoard(SOCGame game)
    {
      	// there are 8 water, 8 port, 19 land tiles: 37 total
        // 19 land tiles: (withholding: DESERT_HEX )
      	int[] landHex = { CLAY_HEX, CLAY_HEX, CLAY_HEX, ORE_HEX, ORE_HEX, ORE_HEX,
			  SHEEP_HEX, SHEEP_HEX, SHEEP_HEX, SHEEP_HEX, 
			  WHEAT_HEX, WHEAT_HEX, WHEAT_HEX, WHEAT_HEX, 
			  WOOD_HEX, WOOD_HEX, WOOD_HEX, WOOD_HEX, };

        // A,B,C,...Q; indicating which die roll activates this resource tile:
        // 0-9 -> [2-6,8-12]  (also -1 for the desert)
        // int[] number = { 3, 0, 4, 1, 5, 7, 6, 9, 8, 2, 5, 7, 6, 2, 3, 4, 1, 8 };
        // use direct dice numbers:
        /** 18 dieNumbers to place at coord indicated by numPath, '7' is added later. */
        int[] dieNumbers = { 5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11 };
      	// place shuffled stack on hex map in this order: (these are the grid numbers)
        int[] numPath = { 29, 30, 31, 26, 20, 13, 7, 6, 5, 10, 16, 23, 24, 25, 19, 12, 11, 17, 18 };

        /* 19 grid numbers, place dieNumber (or robber) on each  */
        int[] numPath0 = { 5, 6, 7, 
                      10, 11, 12, 13, 
                    16, 17, 18, 19, 20, 
                      23, 24, 25, 26,
                         29, 30, 31,
                    };
        permuteInt(landHex); // randomize the resource tiles

        System.out.format("name: %s SD=%b SP=%b\n", game.getName(), game.standardDice, game.standardPorts);
        if (!game.standardDice) {
          permuteInt(numPath); // randomize placement of dieNumber
        }

        // must specify location of DESERT_HEX before permuting numPath
        dieNumbers = insertDesert(dieNumbers);

        // assert: !(standardDice && six_eightAdjacent())
        if (game.standardDice && six_eightAdjacent(dieNumbers, numPath)) {
          System.err.println("WTF? inserting robber made 6/8 adjacent!");
        }
        while (!game.standardDice && six_eightAdjacent(dieNumbers, numPath)) { 
          permuteInt(numPath);
        }
        // printCoords("final:", numPath);
        // printCoords("6/8=", new int[]{numPath[0], numPath[1], numPath[2], numPath[3]});

        // landHex: 36 non-desert tile
        // numPath: 37 hex coords for number OR robber&desert
        // dieNumbers: 37 die numbers
        int cnt = 0; // ndx into landHex

        // put dieNumber & landHex or Robber & DESERT_HEX on each coord:
        for (int i = 0; i < numPath.length; i++)
        {
            int gridNumber = numPath[i];
            int dieNumber = dieNumbers[i];
            // DESERT_HEX set aside; 36 shuffled are interchangeable 
            int tile = (dieNumber == 7) ? DESERT_HEX : landHex[cnt++];
            // set hexLayout at coords with indicated type of hex tile
            hexLayout[gridNumber] = tile;

            // place the robber on desert
            if (tile == DESERT_HEX) {
                robberHex = numToHexID[gridNumber];
                numberLayout[gridNumber] = 0; // no number[] for robber
            }
            else
            {
                // place the numbers
                numberLayout[gridNumber] = dieNumber;
            }
        }
        // put ports on outer ring (portLocs)
        setPortLayout(game.standardPorts);

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
     * @param port one of the 9 port tiles [0..8]
     * @param hex which of the (18 of 37) slots to put it in [0..36]
     */
    private final void placePort(int port, int hex)
    {
        int face = portFaces[hex];
        hexLayout[hex] = (port == 0) 
          ? MISC_PORT_HEX + face-1 // add face to MISC_PORT_HEX
          : (face << 4) + port;    // else code face in high bits!
  	      // port value is: [1..6] (3 bit, but use 4 for %02x)
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

    }

    void setPortLayout(boolean standardPorts) 
    {
        /** edgeLocs are suitable for placing ports */
        int[] edgeLocs = IntStream.range(0, portFaces.length)
            .filter(i -> portFaces[i] != 0).toArray(); 

        // System.out.println("edgelocs=" +Arrays.toString(edgeLocs));
        // 9 port tiles: { 0,0,0,0,1,2,3,4,5} leaving 9 water tiles
        int[] portHex = { MISC_PORT, MISC_PORT, MISC_PORT, MISC_PORT,
			                    CLAY_HEX, ORE_HEX, SHEEP_HEX, WHEAT_HEX, WOOD_HEX };
	
        // shuffle the ports resources/types
        permuteInt(portHex);

        // pick 9 edgeLocs for placing ports:
        if (!standardPorts) {
            permuteInt(edgeLocs);   // put ports in ANY edgeLoc
            portLocs = Arrays.stream(edgeLocs).limit(9).toArray(); 
            // System.out.println("portLocs=" +Arrays.toString(portLocs));
        }
        
        // set ports in place and orientation:
        for (int k = 0; k < portLocs.length; k++) {
          placePort(portHex[k], portLocs[k]);
        }

        /**
         * fill in the port node information
         */
        for (int loc : portLocs) {
          int hex = hexLayout[loc]; // type of tile at coord
          int coord = numToHexID[loc];
          int face = portFaces[loc];
          int[] nodes = nodesOfHexFacing(coord, face);
          for (int node : nodes) {
            int portType = getPortTypeFromHex(hex);
            ports[portType].addElement(node);
          }
        }
    }

    /**
     * @return the type of port given a hex type [MISC, ORE, WHEAT...]
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
    public Vector<Integer> getPortCoordinates(int portType)
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
            roads.addElement((SOCRoad)pp);

            break;

        case SOCPlayingPiece.SETTLEMENT:
            settlements.addElement((SOCSettlement)pp);

            break;

        case SOCPlayingPiece.CITY:
            cities.addElement((SOCCity)pp);

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
    public Vector<SOCRoad> getRoads()
    {
        return roads;
    }

    /**
     * get the list of settlements
     */
    public Vector<SOCSettlement> getSettlements()
    {
        return settlements;
    }

    /**
     * get the list of cities
     */
    public Vector<SOCCity> getCities()
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
