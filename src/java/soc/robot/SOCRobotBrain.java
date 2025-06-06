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
package soc.robot;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import soc.disableDebug.D;		// 
import soc.game.SOCBoard;
import soc.game.SOCCity;
import soc.game.SOCDevCardConstants;
import soc.game.SOCDevCardSet;
import soc.game.SOCGame;
import soc.game.SOCPlayer;
import soc.game.SOCPlayerNumbers;
import soc.game.SOCPlayingPiece;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;
import soc.game.SOCRoad;
import soc.game.SOCSettlement;
import soc.game.SOCTradeOffer;
import soc.message.SOCAcceptOffer;
import soc.message.SOCChoosePlayerRequest;
import soc.message.SOCClearOffer;
import soc.message.SOCDevCard;
import soc.message.SOCDevCardCount;
import soc.message.SOCDiceResult;
import soc.message.SOCDiscardRequest;
import soc.message.SOCFirstPlayer;
import soc.message.SOCGameState;
import soc.message.SOCGameTextMsg;
import soc.message.SOCMakeOffer;
import soc.message.SOCMessage;
import soc.message.SOCMoveRobber;
import soc.message.SOCPlayerElement;
import soc.message.SOCPotentialSettlements;
import soc.message.SOCPutPiece;
import soc.message.SOCRejectOffer;
import soc.message.SOCResourceCount;
import soc.message.SOCSetPlayedDevCard;
import soc.message.SOCSetTurn;
import soc.message.SOCTurn;
import soc.server.SOCServer;
import soc.util.CappedQueue;
import soc.util.DebugRecorder;
import soc.util.Queue;
import soc.util.SOCRobotParameters;


/**
 * AI for playing Settlers of Catan
 *
 * @author Robert S Thomas
 */
public class SOCRobotBrain extends Thread
{
    /**
     * The robot parameters
     */
    SOCRobotParameters robotParameters;

    /**
     * Flag for wheather or not we're alive
     */
    protected boolean alive;

    /**
     * Flag for wheather or not it is our turn
     */
    protected boolean ourTurn;

    /**
     * Timer for turn taking
     */
    protected int turnTime;

    /**
     * Our current state
     */
    protected int curState;

    /**
     * Random number generator
     */
    protected Random rand = new Random();

    /**
     * The client we are hooked up to
     */
    protected SOCRobotClient client;

    /**
     * The game we are playing
     */
    protected SOCGame game;

    /**
     * Is this a special no robber game?
     */
    protected boolean noRobber = false;

    /**
     * Our player data
     */
    protected SOCPlayer ourPlayerData;

    /**
     * The queue of game messages
     */
    protected CappedQueue gameEventQ;

    /**
     * A counter used to measure passage of time
     */
    protected int counter;

    /**
     * This is what we want to build
     */
    protected SOCPlayingPiece whatWeWantToBuild;

    /**
     * This is our current building plan
     */
    protected Stack<SOCPossiblePiece> buildingPlan;

    /**
     * these are the two resources that we want
     * when we play a discovery dev card
     */
    protected SOCResourceSet resourceChoices;

    /**
     * this is the resource we want to monopolize
     */
    protected int monopolyChoice;

    /**
     * our player tracker
     */
    protected SOCPlayerTracker ourPlayerTracker;

    /**
     * trackers for all players
     */
    protected HashMap<Integer, SOCPlayerTracker> playerTrackers;

    /**
     * the thing that determines what we want to build next
     */
    protected SOCRobotDM decisionMaker;

    /**
     * the thing that determines how we negotiate
     */
    protected SOCRobotNegotiator negotiator;

    /**
     * true if we're expecting the START1A state
     */
    protected boolean expectSTART1A;

    /**
     * true if we're expecting the START1B state
     */
    protected boolean expectSTART1B;

    /**
     * true if we're expecting the START2A state
     */
    protected boolean expectSTART2A;

    /**
     * true if we're expecting the START2B state
     */
    protected boolean expectSTART2B;

    /**
     * true if we're expecting the PLAY state
     */
    protected boolean expectPLAY;

    /**
     * true if we're expecting the PLAY1 state
     */
    protected boolean expectPLAY1;

    /**
     * true if we're expecting the PLACING_ROAD state
     */
    protected boolean expectPLACING_ROAD;

    /**
     * true if we're expecting the PLACING_SETTLEMENT state
     */
    protected boolean expectPLACING_SETTLEMENT;

    /**
     * true if we're expecting the PLACING_CITY state
     */
    protected boolean expectPLACING_CITY;

    /**
     * true if we're expecting the PLACING_ROBBER state
     */
    protected boolean expectPLACING_ROBBER;

    /**
     * true if we're expecting the PLACING_FREE_ROAD1 state
     */
    protected boolean expectPLACING_FREE_ROAD1;

    /**
     * true if we're expecting the PLACING_FREE_ROAD2 state
     */
    protected boolean expectPLACING_FREE_ROAD2;

    /**
     * true if were expecting a PUTPIECE message after
     * a START1A game state
     */
    protected boolean expectPUTPIECE_FROM_START1A;

    /**
     * true if were expecting a PUTPIECE message after
     * a START1B game state
     */
    protected boolean expectPUTPIECE_FROM_START1B;

    /**
     * true if were expecting a PUTPIECE message after
     * a START1A game state
     */
    protected boolean expectPUTPIECE_FROM_START2A;

    /**
     * true if were expecting a PUTPIECE message after
     * a START1A game state
     */
    protected boolean expectPUTPIECE_FROM_START2B;

    /**
     * true if we're expecting a DICERESULT message
     */
    protected boolean expectDICERESULT;

    /**
     * true if we're expecting a DISCARDREQUEST message
     */
    protected boolean expectDISCARD;

    /**
     * true if we're expecting to have to move the robber
     */
    protected boolean expectMOVEROBBER;	// DEFUNCT... never used

    /**
     * true if we're expecting to pick two resources
     */
    protected boolean expectWAITING_FOR_DISCOVERY;

    /**
     * true if we're expecting to pick a monopoly
     */
    protected boolean expectWAITING_FOR_MONOPOLY;

    /**
     * true if we're waiting for a GAMESTATE message from the server
     */
    protected boolean waitingForGameState;

    /**
     * true if we're waiting for a TURN message from the server
     * when it's our turn
     */
    protected boolean waitingForOurTurn;

    /**
     * true when we're waiting for the results of a trade
     */
    protected boolean waitingForTradeMsg;

    /**
     * true when we're waiting to receive a dev card
     */
    protected boolean waitingForDevCard;

    /**
     * true when the robber will move because a seven was rolled
     */
    protected boolean moveRobberOnSeven;

    /*
     * true if we're waiting for a response to our trade message
     */
    protected boolean waitingForTradeResponse;

    /**
     * true if we're done trading
     */
    protected boolean doneTrading;

    /**
     * true if the player with that player number has rejected our offer
     */
    protected boolean[] offerRejections;

    /**
     * the game state before the current one
     */
    protected int oldGameState;

    /**
     * used to cache resource estiamtes for the board
     */
    protected int[] resourceEstimates;

    /**
     * used in planning where to put our first and second settlements
     */
    protected int firstSettlement;

    /**
     * used in planning where to put our first and second settlements
     */
    protected int secondSettlement;

    /**
     * a thread that sends ping messages to this one
     */
    protected SOCRobotPinger pinger;

    /**
     * an object for recording debug information that can
     * be accessed interactively
     */
    protected DebugRecorder[] dRecorder;

    /**
     * keeps track of which dRecorder is current
     */
    protected int currentDRecorder;

    /**
     * keeps track of the last thing we bought for debugging purposes
     */
    protected SOCPossiblePiece lastMove;

    /**
     * keeps track of the last thing we wanted for debugging purposes
     */
    protected SOCPossiblePiece lastTarget;

    /**
     * Create a robot brain to play a game
     *
     * @param rc  the robot client
     * @param params  the robot parameters
     * @param ga  the game we're playing
     * @param mq  the message queue
     */
    public SOCRobotBrain(SOCRobotClient rc, SOCRobotParameters params, SOCGame ga, CappedQueue mq)
    {
        client = rc;
        robotParameters = params;
        game = ga;
      	noRobber = game.getName().startsWith("NR") || game.getName().startsWith("NTNR");
        gameEventQ = mq;
        alive = true;
        counter = 0;
        expectSTART1A = true;
        expectSTART1B = false;
        expectSTART2A = false;
        expectSTART2B = false;
        expectPLAY = false;
        expectPLAY1 = false;
        expectPLACING_ROAD = false;
        expectPLACING_SETTLEMENT = false;
        expectPLACING_CITY = false;
        expectPLACING_ROBBER = false;
        expectPLACING_FREE_ROAD1 = false;
        expectPLACING_FREE_ROAD2 = false;
        expectPUTPIECE_FROM_START1A = false;
        expectPUTPIECE_FROM_START1B = false;
        expectPUTPIECE_FROM_START2A = false;
        expectPUTPIECE_FROM_START2B = false;
        expectDICERESULT = false;
        expectDISCARD = false;
        expectMOVEROBBER = false;
        expectWAITING_FOR_DISCOVERY = false;
        expectWAITING_FOR_MONOPOLY = false;
        ourTurn = false;
        oldGameState = game.getGameState();
        waitingForGameState = false;
        waitingForOurTurn = false;
        waitingForTradeMsg = false;
        waitingForDevCard = false;
        moveRobberOnSeven = false;
        waitingForTradeResponse = false;
        doneTrading = false;
        offerRejections = new boolean[SOCGame.MAXPLAYERS];

        for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
        {
            offerRejections[i] = false;
        }

        buildingPlan = new Stack<SOCPossiblePiece>();
        resourceChoices = new SOCResourceSet();
        resourceChoices.add(2, SOCResourceConstants.CLAY);
        monopolyChoice = SOCResourceConstants.SHEEP;
        pinger = new SOCRobotPinger(gameEventQ);
        dRecorder = new DebugRecorder[2];
        dRecorder[0] = new DebugRecorder();
        dRecorder[1] = new DebugRecorder();
        currentDRecorder = 0;
    }

    /**
     * @return the robot parameters
     */
    public SOCRobotParameters getRobotParameters()
    {
        return robotParameters;
    }

    /**
     * @return the player client
     */
    public SOCRobotClient getClient()
    {
        return client;
    }

    /**
     * @return the player trackers
     */
    public HashMap<Integer, SOCPlayerTracker> getPlayerTrackers()
    {
        return playerTrackers;
    }

    /**
     * @return our player tracker
     */
    public SOCPlayerTracker getOurPlayerTracker()
    {
        return ourPlayerTracker;
    }

    /**
     * @return the game data
     */
    public SOCGame getGame()
    {
        return game;
    }

    /**
     * @return our player data
     */
    public SOCPlayer getOurPlayerData()
    {
        return ourPlayerData;
    }

    /**
     * @return the building plan
     */
    public Stack<SOCPossiblePiece> getBuildingPlan()
    {
        return buildingPlan;
    }

    /**
     * @return the decision maker
     */
    public SOCRobotDM getDecisionMaker()
    {
        return decisionMaker;
    }

    /**
     * turns the debug recorders on
     */
    public void turnOnDRecorder()
    {
        dRecorder[0].turnOn();
        dRecorder[1].turnOn();
    }

    /**
     * turns the debug recorders off
     */
    public void turnOffDRecorder()
    {
        dRecorder[0].turnOff();
        dRecorder[1].turnOff();
    }

    /**
     * @return the debug recorder
     */
    public DebugRecorder getDRecorder()
    {
        return dRecorder[currentDRecorder];
    }

    /**
     * @return the old debug recorder
     */
    public DebugRecorder getOldDRecorder()
    {
        return dRecorder[(currentDRecorder + 1) % 2];
    }

    /**
     * @return the last move we made
     */
    public SOCPossiblePiece getLastMove()
    {
        return lastMove;
    }

    /**
     * @return our last target piece
     */
    public SOCPossiblePiece getLastTarget()
    {
        return lastTarget;
    }

    /**
     * Find our player data using our nickname
     */
    public void setOurPlayerData()
    {
        ourPlayerData = game.getPlayer(client.getNickname());
        ourPlayerTracker = new SOCPlayerTracker(ourPlayerData, this);
        playerTrackers = new HashMap<Integer, SOCPlayerTracker>();
        playerTrackers.put(ourPlayerData.getPlayerNumber(), ourPlayerTracker);

        for (int pn = 0; pn < SOCGame.MAXPLAYERS; pn++)
        {
            if (pn != ourPlayerData.getPlayerNumber())
            {
                SOCPlayerTracker tracker = new SOCPlayerTracker(game.getPlayer(pn), this);
                playerTrackers.put(pn, tracker);
            }
        }

        decisionMaker = new SOCRobotDM(this);
        negotiator = new SOCRobotNegotiator(this);
    }

    /**
     * Here is the run method.  Just keep receiving game events
     * and deal with each one.
     */
    public void run()
    {
        if (pinger != null)
        {
            pinger.start();

            try
            {
                while (alive)
                {
                    SOCMessage mes;

                    //if (!gameEventQ.empty()) {
                    mes = (SOCMessage) gameEventQ.get();
                    SOCPlayerElement elt = (mes instanceof SOCPlayerElement) ? (SOCPlayerElement) mes : null;

                    //} else {
                    //mes = null;
                    //}
                    int mesType;

                    if (mes != null)
                    {
                        mesType = mes.getType();
		                  	if ((mesType == SOCMessage.GAMETEXTMSG) && (((SOCGameTextMsg) mes).getText().equals("*PING*"))) {} else 
                        D.ebugPrintln("mes - " + mes);
                    }
                    else
                    {
                        mesType = -1;
                    }

                    if (waitingForTradeMsg && (counter > 10))
                    {
                        waitingForTradeMsg = false; // stop waiting for TradeMsg
                        counter = 0;
                    }

                    if (waitingForTradeResponse && (counter > 100))
                    {
                        //D.ebugPrintln("NOT WAITING ANY MORE FOR TRADE RESPONSE");
                        ///
                        /// record which players said no by not saying anything
                        ///
                        SOCTradeOffer ourCurrentOffer = ourPlayerData.getCurrentOffer();

                        if (ourCurrentOffer != null)
                        {
                            boolean[] offeredTo = ourCurrentOffer.getTo();
                            SOCResourceSet getSet = ourCurrentOffer.getGetSet();

                            for (int rsrcType = SOCResourceConstants.MIN;
                                    rsrcType < SOCResourceConstants.MAX;
                                    rsrcType++)
                            {
                                if (getSet.getAmount(rsrcType) > 0)
                                {
                                    for (int pn = 0; pn < SOCGame.MAXPLAYERS;
                                            pn++)
                                    {
                                        if (offeredTo[pn])
                                        {
                                            negotiator.markAsNotSelling(pn, rsrcType);
                                            negotiator.markAsNotWantingAnotherOffer(pn, rsrcType);
                                        }
                                    }
                                }
                            }

                            pause(1500);
                            client.clearOffer(game);
                            pause(500);
                        }

                        counter = 0;
                        waitingForTradeResponse = false;
                    }

                    if (waitingForGameState && (counter > 10000))
                    {
                        //D.ebugPrintln("counter = "+counter);
                        //D.ebugPrintln("RESEND");
                        counter = 0;
                        client.resend(); // hmmm, server must have missed my request, send again?
                    }

                    if (mesType == SOCMessage.GAMESTATE)
                    {
                        waitingForGameState = false;
                        oldGameState = game.getGameState();
                        game.setGameState(((SOCGameState) mes).getState());
                    }

                    else if (mesType == SOCMessage.FIRSTPLAYER)
                    {
                        game.setFirstPlayer(((SOCFirstPlayer) mes).getPlayerNumber());
                    }

                    else if (mesType == SOCMessage.SETTURN)
                    {
                        game.setCurrentPlayerNumber(((SOCSetTurn) mes).getPlayerNumber());
                    }

                    else if (mesType == SOCMessage.TURN)
                    {
                        //
                        // check if this is the first player
                        ///
                        if (game.getFirstPlayer() == -1)
                        {
                            game.setFirstPlayer(((SOCTurn) mes).getPlayerNumber());
                        }

                        game.setCurrentPlayerNumber(((SOCTurn) mes).getPlayerNumber());
                        game.getPlayer(((SOCTurn) mes).getPlayerNumber()).getDevCards().newToOld();

                        //
                        // remove any expected states
                        //
                        expectPLAY = false;
                        expectPLAY1 = false;
                        expectPLACING_ROAD = false;
                        expectPLACING_SETTLEMENT = false;
                        expectPLACING_CITY = false;
                        expectPLACING_ROBBER = false;
                        expectPLACING_FREE_ROAD1 = false;
                        expectPLACING_FREE_ROAD2 = false;
                        expectDICERESULT = false;
                        expectDISCARD = false;
                        expectMOVEROBBER = false;
                        expectWAITING_FOR_DISCOVERY = false;
                        expectWAITING_FOR_MONOPOLY = false;

                        //
                        // reset the selling flags and offers history
                        //
                        if (robotParameters.getTradeFlag() == 1)
                        {
                            doneTrading = false;
                        }
                        else
                        {
                            doneTrading = true;
                        }

                        waitingForTradeMsg = false;
                        waitingForTradeResponse = false;
                        negotiator.resetIsSelling();
                        negotiator.resetOffersMade();

                        //
                        // reset any plans we had
                        //
                        buildingPlan.clear();
                        negotiator.resetTargetPieces();
                    }

                    if (game.getCurrentPlayerNumber() == ourPlayerData.getPlayerNumber())
                    {
                        ourTurn = true;
                    }
                    else
                    {
                        ourTurn = false;
                    }

                    if ((mesType == SOCMessage.TURN) && (ourTurn))
                    {
                        waitingForOurTurn = false;
                    }

                    if (mesType == SOCMessage.PLAYERELEMENT)
                    {
                        SOCPlayer pl = game.getPlayer(elt.getPlayerNumber());

                        switch (elt.getElementType())
                        {
                        case SOCPlayerElement.ROADS:

                            switch (elt.getAction())
                            {
                            case SOCPlayerElement.SET:
                                pl.setNumPieces(SOCPlayingPiece.ROAD, elt.getValue());

                                break;

                            case SOCPlayerElement.GAIN:
                                pl.setNumPieces(SOCPlayingPiece.ROAD, pl.getNumPieces(SOCPlayingPiece.ROAD) + elt.getValue());

                                break;

                            case SOCPlayerElement.LOSE:
                                pl.setNumPieces(SOCPlayingPiece.ROAD, pl.getNumPieces(SOCPlayingPiece.ROAD) - elt.getValue());

                                break;
                            }

                            break;

                        case SOCPlayerElement.SETTLEMENTS:

                            switch (elt.getAction())
                            {
                            case SOCPlayerElement.SET:
                                pl.setNumPieces(SOCPlayingPiece.SETTLEMENT, elt.getValue());

                                break;

                            case SOCPlayerElement.GAIN:
                                pl.setNumPieces(SOCPlayingPiece.SETTLEMENT, pl.getNumPieces(SOCPlayingPiece.SETTLEMENT) + elt.getValue());

                                break;

                            case SOCPlayerElement.LOSE:
                                pl.setNumPieces(SOCPlayingPiece.SETTLEMENT, pl.getNumPieces(SOCPlayingPiece.SETTLEMENT) - elt.getValue());

                                break;
                            }

                            break;

                        case SOCPlayerElement.CITIES:

                            switch (elt.getAction())
                            {
                            case SOCPlayerElement.SET:
                                pl.setNumPieces(SOCPlayingPiece.CITY, elt.getValue());

                                break;

                            case SOCPlayerElement.GAIN:
                                pl.setNumPieces(SOCPlayingPiece.CITY, pl.getNumPieces(SOCPlayingPiece.CITY) + elt.getValue());

                                break;

                            case SOCPlayerElement.LOSE:
                                pl.setNumPieces(SOCPlayingPiece.CITY, pl.getNumPieces(SOCPlayingPiece.CITY) - elt.getValue());

                                break;
                            }

                            break;

                        case SOCPlayerElement.NUMKNIGHTS:

                            switch (elt.getAction())
                            {
                            case SOCPlayerElement.SET:
                                pl.setNumKnights(elt.getValue());

                                break;

                            case SOCPlayerElement.GAIN:
                                pl.setNumKnights(pl.getNumKnights() + elt.getValue());

                                break;

                            case SOCPlayerElement.LOSE:
                                pl.setNumKnights(pl.getNumKnights() - elt.getValue());

                                break;
                            }

                            game.updateLargestArmy();

                            break;

                        case SOCPlayerElement.CLAY:
                        case SOCPlayerElement.ORE:
                        case SOCPlayerElement.SHEEP:
                        case SOCPlayerElement.WHEAT:
                        case SOCPlayerElement.WOOD:
                        case SOCPlayerElement.UNKNOWN:
	                  		    handleResourceElement(pl, elt, elt.getElementType());

                            break;
                        }

                        ///
                        /// if this during the PLAY state, then update the is selling flags
                        ///
                        if (game.getGameState() == SOCGame.PLAY)
                        {
                            negotiator.resetIsSelling();
                        }
                    }

                    else if (mesType == SOCMessage.RESOURCECOUNT)
                    {
                        SOCPlayer pl = game.getPlayer(((SOCResourceCount) mes).getPlayerNumber());

                        if (((SOCResourceCount) mes).getCount() != pl.getResources().getTotal())
                        {
                            SOCResourceSet rsrcs = pl.getResources();

                            if (D.ebugOn)
                            {
                                client.sendText(game, ">>> RESOURCE COUNT ERROR FOR PLAYER " + pl.getPlayerNumber() + ": " + ((SOCResourceCount) mes).getCount() + " != " + rsrcs.getTotal());
                            }

                            //
                            //  fix it: mark them all UNKNOWN!
                            //
                            if (pl.getPlayerNumber() != ourPlayerData.getPlayerNumber())
                            {
                                rsrcs.clear();
                                rsrcs.setAmount(((SOCResourceCount) mes).getCount(), SOCResourceConstants.UNKNOWN);
                            }
                        }
                    }

                    else if (mesType == SOCMessage.DICERESULT)
                    {
                        game.setCurrentDice(((SOCDiceResult) mes).getResult());
                    }

                    else if (mesType == SOCMessage.PUTPIECE)
                    {
                        D.ebugPrintln("*** PUTPIECE for game ***");

                        SOCPlayer pl = game.getPlayer(((SOCPutPiece) mes).getPlayerNumber());

                        switch (((SOCPutPiece) mes).getPieceType())
                        {
                        case SOCPlayingPiece.ROAD:

                            SOCRoad rd = new SOCRoad(pl, ((SOCPutPiece) mes).getCoordinates());
                            game.putPiece(rd);

                            break;

                        case SOCPlayingPiece.SETTLEMENT:

                            SOCSettlement se = new SOCSettlement(pl, ((SOCPutPiece) mes).getCoordinates());
                            game.putPiece(se);

                            break;

                        case SOCPlayingPiece.CITY:

                            SOCCity ci = new SOCCity(pl, ((SOCPutPiece) mes).getCoordinates());
                            game.putPiece(ci);

                            break;
                        }
                    }

                    else if (mesType == SOCMessage.MOVEROBBER)
                    {
                        //
                        // Note: Don't call ga.moveRobber() because that will call the 
                        // functions to do the stealing.  We just want to say where 
                        // the robber moved without seeing if something was stolen.
                        //
                        moveRobberOnSeven = false;
                        game.getBoard().setRobberHex(((SOCMoveRobber) mes).getCoordinates());
                    }

                    else if ((robotParameters.getTradeFlag() == 1) && (mesType == SOCMessage.MAKEOFFER))
                    {
                        SOCTradeOffer offer = ((SOCMakeOffer) mes).getOffer();
                        game.getPlayer(offer.getFrom()).setCurrentOffer(offer);

                        ///
                        /// if another player makes an offer, that's the
                        /// same as a rejection, but still wants to deal
                        ///				
                        if ((offer.getFrom() != ourPlayerData.getPlayerNumber()))
                        {
                            ///
                            /// record that this player wants to sell me the stuff
                            ///
                            SOCResourceSet giveSet = offer.getGiveSet();

                            for (int rsrcType = SOCResourceConstants.MIN;
                                    rsrcType < SOCResourceConstants.MAX;
                                    rsrcType++)
                            {
                                if (giveSet.getAmount(rsrcType) > 0)
                                {
                                    D.ebugPrintln("%%% player " + offer.getFrom() + " wants to sell " + rsrcType);
                                    negotiator.markAsWantsAnotherOffer(offer.getFrom(), rsrcType);
                                }
                            }

                            ///
                            /// record that this player is not selling the resources 
                            /// he is asking for
                            ///
                            SOCResourceSet getSet = offer.getGetSet();

                            for (int rsrcType = SOCResourceConstants.MIN;
                                    rsrcType < SOCResourceConstants.MAX;
                                    rsrcType++)
                            {
                                if (getSet.getAmount(rsrcType) > 0)
                                {
                                    D.ebugPrintln("%%% player " + offer.getFrom() + " wants to buy " + rsrcType + " and therefore does not want to sell it");
                                    negotiator.markAsNotSelling(offer.getFrom(), rsrcType);
                                }
                            }

                            if (waitingForTradeResponse)
                            {
                                offerRejections[offer.getFrom()] = true;

                                boolean everyoneRejected = true;
                                D.ebugPrintln("ourPlayerData.getCurrentOffer() = " + ourPlayerData.getCurrentOffer());

                                if (ourPlayerData.getCurrentOffer() != null)
                                {
                                    boolean[] offeredTo = ourPlayerData.getCurrentOffer().getTo();

                                    for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
                                    {
                                        D.ebugPrintln("offerRejections[" + i + "]=" + offerRejections[i]);

                                        if (offeredTo[i] && !offerRejections[i])
                                        {
                                            everyoneRejected = false;
                                        }
                                    }
                                }

                                D.ebugPrintln("everyoneRejected=" + everyoneRejected);

                                if (everyoneRejected)
                                {
                                    negotiator.addToOffersMade(ourPlayerData.getCurrentOffer());
                                    client.clearOffer(game);
                                    waitingForTradeResponse = false;
                                }
                            }

                            ///
                            /// consider the offer
                            ///
                            int ourResponseToOffer = considerOffer(offer);

                            D.ebugPrintln("%%% ourResponseToOffer = " + ourResponseToOffer);

                            if (ourResponseToOffer >= 0)
                            {
                                int delayLength = 3500 + rand.nextInt(500);
				// pause(delayLength);
                                switch (ourResponseToOffer)
                                {
                                case SOCRobotNegotiator.ACCEPT_OFFER:
				    pause(delayLength + 1000);
                                    client.acceptOffer(game, offer.getFrom());

                                    ///
                                    /// clear our building plan, so that we replan
                                    ///
                                    buildingPlan.clear();
                                    negotiator.setTargetPiece(ourPlayerData.getPlayerNumber(), null);

                                    break;

                                case SOCRobotNegotiator.REJECT_OFFER:
				    pause(delayLength/4);
                                    if (!waitingForTradeResponse)
                                    {
                                        client.rejectOffer(game);
                                    }

                                    break;

                                case SOCRobotNegotiator.COUNTER_OFFER:
				    pause(delayLength/2);
                                    if (!makeCounterOffer(offer))
                                    {
                                        client.rejectOffer(game);
                                    }

                                    break;
                                }
                            }
                        }
                    }

                    else if ((robotParameters.getTradeFlag() == 1) && (mesType == SOCMessage.CLEAROFFER))
                    {
                        game.getPlayer(((SOCClearOffer) mes).getPlayerNumber()).setCurrentOffer(null);
                    }

                    else if ((robotParameters.getTradeFlag() == 1) && (mesType == SOCMessage.ACCEPTOFFER))
                    {
                        if (((((SOCAcceptOffer) mes).getOfferingNumber() == ourPlayerData.getPlayerNumber()) || (((SOCAcceptOffer) mes).getAcceptingNumber() == ourPlayerData.getPlayerNumber())) && waitingForTradeResponse)
                        {
                            waitingForTradeResponse = false;
                        }
                    }

                    else if ((robotParameters.getTradeFlag() == 1) && (mesType == SOCMessage.REJECTOFFER))
                    {
                        ///
                        /// see if everyone has rejected our offer
                        ///
                        int rejector = ((SOCRejectOffer) mes).getPlayerNumber();

                        if ((ourPlayerData.getCurrentOffer() != null) && (waitingForTradeResponse))
                        {
                            D.ebugPrintln("%%%%%%%%% REJECT OFFER %%%%%%%%%%%%%");

                            ///
                            /// record which player said no
                            ///
                            SOCResourceSet getSet = ourPlayerData.getCurrentOffer().getGetSet();

                            for (int rsrcType = SOCResourceConstants.MIN;
                                    rsrcType < SOCResourceConstants.MAX;
                                    rsrcType++)
                            {
                                if ((getSet.getAmount(rsrcType) > 0) && (!negotiator.wantsAnotherOffer(rejector, rsrcType)))
                                {
                                    negotiator.markAsNotSelling(rejector, rsrcType);
                                }
                            }

                            offerRejections[((SOCRejectOffer) mes).getPlayerNumber()] = true;

                            boolean everyoneRejected = true;
                            D.ebugPrintln("ourPlayerData.getCurrentOffer() = " + ourPlayerData.getCurrentOffer());

                            boolean[] offeredTo = ourPlayerData.getCurrentOffer().getTo();

                            for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
                            {
                                D.ebugPrintln("offerRejections[" + i + "]=" + offerRejections[i]);

                                if (offeredTo[i] && !offerRejections[i])
                                {
                                    everyoneRejected = false;
                                }
                            }

                            D.ebugPrintln("everyoneRejected=" + everyoneRejected);

                            if (everyoneRejected)
                            {
                                negotiator.addToOffersMade(ourPlayerData.getCurrentOffer());
                                client.clearOffer(game);
                                waitingForTradeResponse = false;
                            }
                        }
                        else
                        {
                            ///
                            /// we also want to watch rejections of other players' offers
                            ///
                            D.ebugPrintln("%%%% ALT REJECT OFFER %%%%");

                            for (int pn = 0; pn < SOCGame.MAXPLAYERS; pn++)
                            {
                                SOCTradeOffer offer = game.getPlayer(pn).getCurrentOffer();

                                if (offer != null)
                                {
                                    boolean[] offeredTo = offer.getTo();

                                    if (offeredTo[rejector])
                                    {
                                        //
                                        // I think they were rejecting this offer
                                        // mark them as not selling what was asked for
                                        //
                                        SOCResourceSet getSet = offer.getGetSet();

                                        for (int rsrcType = SOCResourceConstants.MIN;
                                                rsrcType < SOCResourceConstants.MAX;
                                                rsrcType++)
                                        {
                                            if ((getSet.getAmount(rsrcType) > 0) && (!negotiator.wantsAnotherOffer(rejector, rsrcType)))
                                            {
                                                negotiator.markAsNotSelling(rejector, rsrcType);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else if (mesType == SOCMessage.DEVCARDCOUNT)
                    {
                        game.setNumDevCards(((SOCDevCardCount) mes).getNumDevCards());
                    }

                    else if (mesType == SOCMessage.DEVCARD)
                    {
                        SOCPlayer player = game.getPlayer(((SOCDevCard) mes).getPlayerNumber());

                        switch (((SOCDevCard) mes).getAction())
                        {
                        case SOCDevCard.DRAW:
                            player.getDevCards().add(1, SOCDevCardSet.NEW, ((SOCDevCard) mes).getCardType());

                            break;

                        case SOCDevCard.PLAY:
                            player.getDevCards().subtract(1, SOCDevCardSet.OLD, ((SOCDevCard) mes).getCardType());

                            break;

                        case SOCDevCard.ADDOLD:
                            player.getDevCards().add(1, SOCDevCardSet.OLD, ((SOCDevCard) mes).getCardType());

                            break;

                        case SOCDevCard.ADDNEW:
                            player.getDevCards().add(1, SOCDevCardSet.NEW, ((SOCDevCard) mes).getCardType());

                            break;
                        }
                    }

                    else if (mesType == SOCMessage.SETPLAYEDDEVCARD)
                    {
                        SOCPlayer player = game.getPlayer(((SOCSetPlayedDevCard) mes).getPlayerNumber());
                        player.setPlayedDevCard(((SOCSetPlayedDevCard) mes).hasPlayedDevCard());
                    }

                    else if (mesType == SOCMessage.POTENTIALSETTLEMENTS)
                    {
                        SOCPlayer player = game.getPlayer(((SOCPotentialSettlements) mes).getPlayerNumber());
                        player.setPotentialSettlements(((SOCPotentialSettlements) mes).getPotentialSettlements());
                    }

                    debugInfo();

                    if ((game.getGameState() == SOCGame.PLAY) && (!waitingForGameState))
                    {
                        expectPLAY = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!expectPLAY1 && !expectDISCARD && !expectPLACING_ROBBER && !(expectDICERESULT && (counter < 4000)))
                            {
                                /**
                                 * if we have a knight card and the robber
                                 * is on one of our numbers, play the knight card
                                 */
                                if ((ourPlayerData.getDevCards().getAmount(SOCDevCardSet.OLD, SOCDevCardConstants.KNIGHT) > 0) && (!(ourPlayerData.getNumbers().getNumberResourcePairsForHex(game.getBoard().getRobberHex())).isEmpty()))
                                {
                                    if (!noRobber) {
                                        expectPLACING_ROBBER = true;
                                        waitingForGameState = true;
                                    }
                                    counter = 0;
                                    client.playDevCard(game, SOCDevCardConstants.KNIGHT);
                                    pause(1500);
                                }
                                else
                                {
                                    expectDICERESULT = true;
                                    counter = 0;

                                    //D.ebugPrintln("!!! ROLLING DICE !!!");
                                    client.rollDice(game);
                                }
                            }
                        }
                        else
                        {
                            /**
                             * not our turn
                             */
                            expectDICERESULT = true;
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLACING_ROBBER) && (!waitingForGameState))
                    {
                        expectPLACING_ROBBER = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!((expectPLAY || expectPLAY1) && (counter < 4000)))
                            {
                                if (moveRobberOnSeven == true)
                                {
                                    moveRobberOnSeven = false;
                                    waitingForGameState = true;
                                    counter = 0;
                                    expectPLAY1 = true;
                                }
                                else
                                {
                                    waitingForGameState = true;
                                    counter = 0;

                                    if (oldGameState == SOCGame.PLAY)
                                    {
                                        expectPLAY = true;
                                    }
                                    else if (oldGameState == SOCGame.PLAY1)
                                    {
                                        expectPLAY1 = true;
                                    }
                                }

                                counter = 0;
                                moveRobber();
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.WAITING_FOR_DISCOVERY) && (!waitingForGameState))
                    {
                        expectWAITING_FOR_DISCOVERY = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPLAY1) && (counter < 4000))
                            {
                                waitingForGameState = true;
                                expectPLAY1 = true;
                                counter = 0;
                                client.discoveryPick(game, resourceChoices);
                                pause(1500);
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.WAITING_FOR_MONOPOLY) && (!waitingForGameState))
                    {
                        expectWAITING_FOR_MONOPOLY = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPLAY1) && (counter < 4000))
                            {
                                waitingForGameState = true;
                                expectPLAY1 = true;
                                counter = 0;
                                client.monopolyPick(game, monopolyChoice);
                                pause(1500);
                            }
                        }
                    }

                    if (waitingForTradeMsg && (mesType == SOCMessage.GAMETEXTMSG) && (((SOCGameTextMsg) mes).getNickname().equals(SOCServer.SERVERNAME)))
                    {
                        //
                        // This might be the trade message we've been waiting for
                        //
                        if (((SOCGameTextMsg) mes).getText().startsWith(client.getNickname() + " traded"))
                        {
                            waitingForTradeMsg = false;
                        }
                    }

                    if (waitingForDevCard && (mesType == SOCMessage.GAMETEXTMSG) && (((SOCGameTextMsg) mes).getNickname().equals(SOCServer.SERVERNAME)))
                    {
                        //
                        // This might be the dev card message we've been waiting for
                        //
                        if (((SOCGameTextMsg) mes).getText().equals(client.getNickname() + " bought a development card."))
                        {
                            waitingForDevCard = false;
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLAY1) && (!waitingForGameState) && (!waitingForTradeMsg) && (!waitingForTradeResponse) && (!waitingForDevCard) && (!expectPLACING_ROAD) && (!expectPLACING_SETTLEMENT) && (!expectPLACING_CITY) && (!expectPLACING_ROBBER) && (!expectPLACING_FREE_ROAD1) && (!expectPLACING_FREE_ROAD2) && (!expectWAITING_FOR_DISCOVERY) && (!expectWAITING_FOR_MONOPOLY))
                    {
                        expectPLAY1 = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPLAY && (counter < 4000)))
                            {
                                counter = 0;

                                //D.ebugPrintln("DOING PLAY1");
                                if (D.ebugOn)
                                {
                                    client.sendText(game, "================================");

                                    for (int i = 0; i < SOCGame.MAXPLAYERS;
                                            i++)
                                    {
                                        SOCResourceSet rsrcs = game.getPlayer(i).getResources();
                                        String resourceMessage = "PLAYER " + i + " RESOURCES: ";
                                        resourceMessage += (rsrcs.getAmount(SOCResourceConstants.CLAY) + " ");
                                        resourceMessage += (rsrcs.getAmount(SOCResourceConstants.ORE) + " ");
                                        resourceMessage += (rsrcs.getAmount(SOCResourceConstants.SHEEP) + " ");
                                        resourceMessage += (rsrcs.getAmount(SOCResourceConstants.WHEAT) + " ");
                                        resourceMessage += (rsrcs.getAmount(SOCResourceConstants.WOOD) + " ");
                                        resourceMessage += (rsrcs.getAmount(SOCResourceConstants.UNKNOWN) + " ");
                                        client.sendText(game, resourceMessage);
                                        D.ebugPrintln(resourceMessage);
                                    }
                                }

                                /**
                                 * if we haven't played a dev card yet,
                                 * and we have a knight, and we can get
                                 * largest army, play the knight
                                 */
                                if (!ourPlayerData.hasPlayedDevCard())
                                {
                                    SOCPlayer laPlayer = game.getPlayerWithLargestArmy();

                                    if (((laPlayer != null) && (laPlayer.getPlayerNumber() != ourPlayerData.getPlayerNumber())) || (laPlayer == null))
                                    {
                                        int larmySize;

                                        if (laPlayer == null)
                                        {
                                            larmySize = 3;
                                        }
                                        else
                                        {
                                            larmySize = laPlayer.getNumKnights() + 1;
                                        }

                                        if (((ourPlayerData.getNumKnights() + ourPlayerData.getDevCards().getAmount(SOCDevCardSet.NEW, SOCDevCardConstants.KNIGHT) + ourPlayerData.getDevCards().getAmount(SOCDevCardSet.OLD, SOCDevCardConstants.KNIGHT)) >= larmySize) && (ourPlayerData.getDevCards().getAmount(SOCDevCardSet.OLD, SOCDevCardConstants.KNIGHT) > 0))
                                        {
                                            /**
                                             * play a knight card
                                             */
                                            if (!noRobber) {
                                                expectPLACING_ROBBER = true;
                                                waitingForGameState = true;
                                            } 
                                            counter = 0;
                                            client.playDevCard(game, SOCDevCardConstants.KNIGHT);
                                            pause(500);
                                        }
                                    }
                                }

                                /**
                                 * make a plan if we don't have one
                                 */
                                if (!expectPLACING_ROBBER && (buildingPlan.empty()) && (ourPlayerData.getResources().getTotal() > 1))
                                {
                                    decisionMaker.planStuff(robotParameters.getStrategyType());

                                    if (!buildingPlan.empty())
                                    {
                                        lastTarget = (SOCPossiblePiece) buildingPlan.peek();
                                        negotiator.setTargetPiece(ourPlayerData.getPlayerNumber(), (SOCPossiblePiece) buildingPlan.peek());
                                    }
                                }

                                //D.ebugPrintln("DONE PLANNING");
                                if (!expectPLACING_ROBBER && !buildingPlan.empty())
                                {
                                    /**
                                     * check to see if this is a Road Building plan
                                     */
                                    boolean roadBuildingPlan = false;

                                    if (!ourPlayerData.hasPlayedDevCard() && (ourPlayerData.getNumPieces(SOCPlayingPiece.ROAD) >= 2) && (ourPlayerData.getDevCards().getAmount(SOCDevCardSet.OLD, SOCDevCardConstants.ROADS) > 0))
                                    {
                                        //D.ebugPrintln("** Checking for Road Building Plan **");
                                        SOCPossiblePiece topPiece = (SOCPossiblePiece) buildingPlan.pop();

                                        //D.ebugPrintln("$ POPPED "+topPiece);
                                        if ((topPiece != null) && (topPiece.getType() == SOCPossiblePiece.ROAD) && (!buildingPlan.empty()))
                                        {
                                            SOCPossiblePiece secondPiece = (SOCPossiblePiece) buildingPlan.peek();

                                            //D.ebugPrintln("secondPiece="+secondPiece);
                                            if ((secondPiece != null) && (secondPiece.getType() == SOCPossiblePiece.ROAD))
                                            {
                                                roadBuildingPlan = true;
                                                whatWeWantToBuild = new SOCRoad(ourPlayerData, topPiece.getCoordinates());
                                                waitingForGameState = true;
                                                counter = 0;
                                                expectPLACING_FREE_ROAD1 = true;

                                                //D.ebugPrintln("!! PLAYING ROAD BUILDING CARD");
                                                client.playDevCard(game, SOCDevCardConstants.ROADS);
                                            }
                                            else
                                            {
                                                //D.ebugPrintln("$ PUSHING "+topPiece);
                                                buildingPlan.push(topPiece);
                                            }
                                        }
                                        else
                                        {
                                            //D.ebugPrintln("$ PUSHING "+topPiece);
                                            buildingPlan.push(topPiece);
                                        }
                                    }

                                    if (!roadBuildingPlan)
                                    {
                                        ///
                                        /// figure out what resources we need
                                        ///
                                        SOCResourceSet targetResources = null;
                                        SOCPossiblePiece targetPiece = (SOCPossiblePiece) buildingPlan.peek();

                                        //D.ebugPrintln("^^^ targetPiece = "+targetPiece);
                                        //D.ebugPrintln("^^^ ourResources = "+ourPlayerData.getResources());
                                        switch (targetPiece.getType())
                                        {
                                        case SOCPossiblePiece.CARD:
                                            targetResources = SOCGame.CARD_SET;

                                            break;

                                        case SOCPossiblePiece.ROAD:
                                            targetResources = SOCGame.ROAD_SET;

                                            break;

                                        case SOCPossiblePiece.SETTLEMENT:
                                            targetResources = SOCGame.SETTLEMENT_SET;

                                            break;

                                        case SOCPossiblePiece.CITY:
                                            targetResources = SOCGame.CITY_SET;

                                            break;
                                        }

                                        negotiator.setTargetPiece(ourPlayerData.getPlayerNumber(), targetPiece);

                                        ///
                                        /// if we have a 2 free resources card and we need
                                        /// at least 2 resources, play the card
                                        ///
                                        if (!ourPlayerData.hasPlayedDevCard() && (ourPlayerData.getDevCards().getAmount(SOCDevCardSet.OLD, SOCDevCardConstants.DISC) > 0))
                                        {
                                            SOCResourceSet ourResources = ourPlayerData.getResources();
                                            int numNeededResources = 0;

                                            for (int resource = SOCResourceConstants.MIN;
                                                    resource < SOCResourceConstants.MAX;
                                                    resource++)
                                            {
                                                int diff = targetResources.getAmount(resource) - ourResources.getAmount(resource);

                                                if (diff > 0)
                                                {
                                                    numNeededResources += diff;
                                                }
                                            }

                                            if (numNeededResources == 2)
                                            {
                                                chooseFreeResources(targetResources);

                                                ///
                                                /// play the card
                                                ///
                                                expectWAITING_FOR_DISCOVERY = true;
                                                waitingForGameState = true;
                                                counter = 0;
                                                client.playDevCard(game, SOCDevCardConstants.DISC);
                                                pause(1500);
                                            }
                                        }

                                        if (!expectWAITING_FOR_DISCOVERY)
                                        {
                                            ///
                                            /// if we have a monopoly card, play it
                                            /// and take what there is most of
                                            ///
                                            if (!ourPlayerData.hasPlayedDevCard() && (ourPlayerData.getDevCards().getAmount(SOCDevCardSet.OLD, SOCDevCardConstants.MONO) > 0) && chooseMonopoly())
                                            {
                                                ///
                                                /// play the card
                                                ///
                                                expectWAITING_FOR_MONOPOLY = true;
                                                waitingForGameState = true;
                                                counter = 0;
                                                client.playDevCard(game, SOCDevCardConstants.MONO);
                                                pause(1500);
                                            }

                                            if (!expectWAITING_FOR_MONOPOLY)
                                            {
                                                if ((!doneTrading) && (!ourPlayerData.getResources().contains(targetResources)))
                                                {
                                                    waitingForTradeResponse = false;

                                                    if (robotParameters.getTradeFlag() == 1)
                                                    {
                                                        makeOffer(targetPiece);
                                                    }
                                                }

                                                if (!waitingForTradeResponse)
                                                {
                                                    /**
                                                     * trade with the bank/ports
                                                     */
                                                    if (tradeToTarget2(targetResources))
                                                    {
                                                        counter = 0;
                                                        waitingForTradeMsg = true;
                                                        pause(1500);
                                                    }
                                                }

                                                ///
                                                /// build if we can
                                                ///
                                                if (!waitingForTradeMsg && !waitingForTradeResponse && ourPlayerData.getResources().contains(targetResources))
                                                {
                                                    buildingPlan.pop();
                                                    D.ebugPrintln("$ POPPED " + targetPiece);
                                                    lastMove = targetPiece;
                                                    currentDRecorder = (currentDRecorder + 1) % 2;
                                                    negotiator.setTargetPiece(ourPlayerData.getPlayerNumber(), targetPiece);

                                                    switch (targetPiece.getType())
                                                    {
                                                    case SOCPossiblePiece.CARD:
                                                        client.buyDevCard(game);
                                                        waitingForDevCard = true;

                                                        break;

                                                    case SOCPossiblePiece.ROAD:
                                                        waitingForGameState = true;
                                                        counter = 0;
                                                        expectPLACING_ROAD = true;
                                                        whatWeWantToBuild = new SOCRoad(ourPlayerData, targetPiece.getCoordinates());
                                                        D.ebugPrintln("!!! BUILD REQUEST FOR A ROAD AT " + Integer.toHexString(targetPiece.getCoordinates()) + " !!!");
                                                        client.buildRequest(game, SOCPlayingPiece.ROAD);

                                                        break;

                                                    case SOCPlayingPiece.SETTLEMENT:
                                                        waitingForGameState = true;
                                                        counter = 0;
                                                        expectPLACING_SETTLEMENT = true;
                                                        whatWeWantToBuild = new SOCSettlement(ourPlayerData, targetPiece.getCoordinates());
                                                        D.ebugPrintln("!!! BUILD REQUEST FOR A SETTLEMENT " + Integer.toHexString(targetPiece.getCoordinates()) + " !!!");
                                                        client.buildRequest(game, SOCPlayingPiece.SETTLEMENT);

                                                        break;

                                                    case SOCPlayingPiece.CITY:
                                                        waitingForGameState = true;
                                                        counter = 0;
                                                        expectPLACING_CITY = true;
                                                        whatWeWantToBuild = new SOCCity(ourPlayerData, targetPiece.getCoordinates());
                                                        D.ebugPrintln("!!! BUILD REQUEST FOR A CITY " + Integer.toHexString(targetPiece.getCoordinates()) + " !!!");
                                                        client.buildRequest(game, SOCPlayingPiece.CITY);

                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                /**
                                 * see if we're done with our turn
                                 */
                                if (!(expectPLACING_SETTLEMENT || expectPLACING_FREE_ROAD1 || expectPLACING_FREE_ROAD2 || expectPLACING_ROAD || expectPLACING_CITY || expectWAITING_FOR_DISCOVERY || expectWAITING_FOR_MONOPOLY || expectPLACING_ROBBER || waitingForTradeMsg || waitingForTradeResponse || waitingForDevCard))
                                {
                                    waitingForGameState = true;
                                    counter = 0;
                                    expectPLAY = true;
                                    waitingForOurTurn = true;

                                    if (robotParameters.getTradeFlag() == 1)
                                    {
                                        doneTrading = false;
                                    }
                                    else
                                    {
                                        doneTrading = true;
                                    }

                                    //D.ebugPrintln("!!! ENDING TURN !!!");
                                    negotiator.resetIsSelling();
                                    negotiator.resetOffersMade();
                                    buildingPlan.clear();
                                    negotiator.resetTargetPieces();
                                    pause(1500);
                                    client.endTurn(game);
                                }
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLACING_SETTLEMENT) && (!waitingForGameState))
                    {
                        if ((ourTurn) && (!waitingForOurTurn) && (expectPLACING_SETTLEMENT))
                        {
                            expectPLACING_SETTLEMENT = false;
                            waitingForGameState = true;
                            counter = 0;
                            expectPLAY1 = true;

                            //D.ebugPrintln("!!! PUTTING PIECE "+whatWeWantToBuild+" !!!");
                            pause(500);
                            client.putPiece(game, whatWeWantToBuild);
                            pause(1000);
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLACING_ROAD) && (!waitingForGameState))
                    {
                        if ((ourTurn) && (!waitingForOurTurn) && (expectPLACING_ROAD))
                        {
                            expectPLACING_ROAD = false;
                            waitingForGameState = true;
                            counter = 0;
                            expectPLAY1 = true;

                            //D.ebugPrintln("!!! PUTTING PIECE "+whatWeWantToBuild+" !!!");
                            pause(500);
                            client.putPiece(game, whatWeWantToBuild);
                            pause(1000);
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLACING_CITY) && (!waitingForGameState))
                    {
                        if ((ourTurn) && (!waitingForOurTurn) && (expectPLACING_CITY))
                        {
                            expectPLACING_CITY = false;
                            waitingForGameState = true;
                            counter = 0;
                            expectPLAY1 = true;

                            //D.ebugPrintln("!!! PUTTING PIECE "+whatWeWantToBuild+" !!!");
                            pause(500);
                            client.putPiece(game, whatWeWantToBuild);
                            pause(1000);
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLACING_FREE_ROAD1) && (!waitingForGameState))
                    {
                        if ((ourTurn) && (!waitingForOurTurn) && (expectPLACING_FREE_ROAD1))
                        {
                            expectPLACING_FREE_ROAD1 = false;
                            waitingForGameState = true;
                            counter = 0;
                            expectPLACING_FREE_ROAD2 = true;
                            D.ebugPrintln("!!! PUTTING PIECE 1 " + whatWeWantToBuild + " !!!");
                            pause(500);
                            client.putPiece(game, whatWeWantToBuild);
                            pause(1000);
                        }
                    }

                    if ((game.getGameState() == SOCGame.PLACING_FREE_ROAD2) && (!waitingForGameState))
                    {
                        if ((ourTurn) && (!waitingForOurTurn) && (expectPLACING_FREE_ROAD2))
                        {
                            expectPLACING_FREE_ROAD2 = false;
                            waitingForGameState = true;
                            counter = 0;
                            expectPLAY1 = true;

                            SOCPossiblePiece posPiece = (SOCPossiblePiece) buildingPlan.pop();

                            if (posPiece.getType() == SOCPossiblePiece.ROAD)
                            {
                                D.ebugPrintln("posPiece = " + posPiece);
                                whatWeWantToBuild = new SOCRoad(ourPlayerData, posPiece.getCoordinates());
                                D.ebugPrintln("$ POPPED OFF");
                                D.ebugPrintln("!!! PUTTING PIECE 2 " + whatWeWantToBuild + " !!!");
                                pause(500);
                                client.putPiece(game, whatWeWantToBuild);
                                pause(1000);
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.START1A) && (!waitingForGameState))
                    {
                        expectSTART1A = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPUTPIECE_FROM_START1A && (counter < 4000)))
                            {
                                expectPUTPIECE_FROM_START1A = true;
                                counter = 0;
                                waitingForGameState = true;
                                planInitialSettlements();
                                placeFirstSettlement();
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.START1B) && (!waitingForGameState))
                    {
                        expectSTART1B = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPUTPIECE_FROM_START1B && (counter < 4000)))
                            {
                                expectPUTPIECE_FROM_START1B = true;
                                counter = 0;
                                waitingForGameState = true;
                                pause(1500);
                                placeInitRoad();
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.START2A) && (!waitingForGameState))
                    {
                        expectSTART2A = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPUTPIECE_FROM_START2A && (counter < 4000)))
                            {
                                expectPUTPIECE_FROM_START2A = true;
                                counter = 0;
                                waitingForGameState = true;
                                planSecondSettlement();
                                placeSecondSettlement();
                            }
                        }
                    }

                    if ((game.getGameState() == SOCGame.START2B) && (!waitingForGameState))
                    {
                        expectSTART2B = false;

                        if ((!waitingForOurTurn) && (ourTurn))
                        {
                            if (!(expectPUTPIECE_FROM_START2B && (counter < 4000)))
                            {
                                expectPUTPIECE_FROM_START2B = true;
                                counter = 0;
                                waitingForGameState = true;
                                pause(1500);
                                placeInitRoad();
                            }
                        }
                    }

                    /*
                       if (game.getGameState() == SOCGame.OVER) {
                       client.leaveGame(game);
                       alive = false;
                       }
                     */
                    if (mesType == SOCMessage.SETTURN)
                    {
                        game.setCurrentPlayerNumber(((SOCSetTurn) mes).getPlayerNumber());
                    }

                    /**
                     * this is for player tracking
                     */
                    if (mesType == SOCMessage.PUTPIECE)
                    {
                        D.ebugPrintln("*** PUTPIECE for playerTrackers ***");

                        switch (((SOCPutPiece) mes).getPieceType())
                        {
                        case SOCPlayingPiece.ROAD:

                            SOCRoad newRoad = new SOCRoad(game.getPlayer(((SOCPutPiece) mes).getPlayerNumber()), ((SOCPutPiece) mes).getCoordinates());
                            Iterator<SOCPlayerTracker> trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = trackersIter.next();
                                tracker.takeMonitor();

                                try
                                {
                                    tracker.addNewRoad(newRoad, playerTrackers);
                                }
                                catch (Exception e)
                                {
                                    tracker.releaseMonitor();
                                    System.out.println("Exception caught - " + e);
                                    e.printStackTrace();
                                }

                                tracker.releaseMonitor();
                            }

                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();
                                tracker.takeMonitor();

                                try
                                {
                                    Iterator<SOCPossibleRoad> posRoadsIter = tracker.getPossibleRoads().values().iterator();

                                    while (posRoadsIter.hasNext())
                                    {
                                        posRoadsIter.next().clearThreats();
                                    }

                                    Iterator<SOCPossibleSettlement> posSetsIter = tracker.getPossibleSettlements().values().iterator();

                                    while (posSetsIter.hasNext())
                                    {
                                        posSetsIter.next().clearThreats();
                                    }
                                }
                                catch (Exception e)
                                {
                                    tracker.releaseMonitor();
                                    System.out.println("Exception caught - " + e);
                                    e.printStackTrace();
                                }

                                tracker.releaseMonitor();
                            }

                            ///
                            /// update LR values and ETA
                            ///
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();
                                tracker.updateThreats(playerTrackers);
                                tracker.takeMonitor();

                                try
                                {
                                    if (tracker.getPlayer().getPlayerNumber() == ((SOCPutPiece) mes).getPlayerNumber())
                                    {
                                        //D.ebugPrintln("$$ updating LR Value for player "+tracker.getPlayer().getPlayerNumber());
                                        //tracker.updateLRValues();
                                    }

                                    //tracker.recalcLongestRoadETA();
                                }
                                catch (Exception e)
                                {
                                    tracker.releaseMonitor();
                                    System.out.println("Exception caught - " + e);
                                    e.printStackTrace();
                                }

                                tracker.releaseMonitor();
                            }

                            break;

                        case SOCPlayingPiece.SETTLEMENT:

                            SOCSettlement newSettlement = new SOCSettlement(game.getPlayer(((SOCPutPiece) mes).getPlayerNumber()), ((SOCPutPiece) mes).getCoordinates());
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();
                                tracker.addNewSettlement(newSettlement, playerTrackers);
                            }

                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();
                                Iterator<SOCPossibleRoad> posRoadsIter = tracker.getPossibleRoads().values().iterator();

                                while (posRoadsIter.hasNext())
                                {
                                    posRoadsIter.next().clearThreats();
                                }

                                Iterator<SOCPossibleSettlement> posSetsIter = tracker.getPossibleSettlements().values().iterator();

                                while (posSetsIter.hasNext())
                                {
                                    posSetsIter.next().clearThreats();
                                }
                            }

                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();
                                tracker.updateThreats(playerTrackers);
                            }

                            ///
                            /// see if this settlement bisected someone elses road
                            ///
                            int[] roadCount = { 0, 0, 0, 0 };
                            Enumeration<Integer>adjEdgeEnum = SOCBoard.getAdjacentEdgesToNode(((SOCPutPiece) mes).getCoordinates()).elements();

                            while (adjEdgeEnum.hasMoreElements())
                            {
                                Integer adjEdge = adjEdgeEnum.nextElement();
                                Enumeration<SOCRoad> roadEnum =  game.getBoard().getRoads().elements();

                                while (roadEnum.hasMoreElements())
                                {
                                    SOCRoad road = (SOCRoad) roadEnum.nextElement();

                                    if (road.getCoordinates() == adjEdge.intValue())
                                    {
                                        roadCount[road.getPlayer().getPlayerNumber()]++;

                                        if (roadCount[road.getPlayer().getPlayerNumber()] == 2)
                                        {
                                            if (road.getPlayer().getPlayerNumber() != ourPlayerData.getPlayerNumber())
                                            {
                                                ///
                                                /// this settlement bisects another players road
                                                ///
                                                trackersIter = playerTrackers.values().iterator();

                                                while (trackersIter.hasNext())
                                                {
                                                    SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();

                                                    if (tracker.getPlayer().getPlayerNumber() == road.getPlayer().getPlayerNumber())
                                                    {
                                                        //D.ebugPrintln("$$ updating LR Value for player "+tracker.getPlayer().getPlayerNumber());
                                                        //tracker.updateLRValues();
                                                    }

                                                    //tracker.recalcLongestRoadETA();
                                                }
                                            }

                                            break;
                                        }
                                    }
                                }
                            }

                            ///
                            /// update the speedups from possible settlements
                            ///
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();

                                if (tracker.getPlayer().getPlayerNumber() == ((SOCPutPiece) mes).getPlayerNumber())
                                {
                                    Iterator<SOCPossibleSettlement> posSetsIter = tracker.getPossibleSettlements().values().iterator();

                                    while (posSetsIter.hasNext())
                                    {
                                        posSetsIter.next().updateSpeedup();
                                    }

                                    break;
                                }
                            }

                            ///
                            /// update the speedups from possible cities
                            ///
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();

                                if (tracker.getPlayer().getPlayerNumber() == ((SOCPutPiece) mes).getPlayerNumber())
                                {
                                    Iterator<SOCPossibleCity> posCitiesIter = tracker.getPossibleCities().values().iterator();

                                    while (posCitiesIter.hasNext())
                                    {
                                        posCitiesIter.next().updateSpeedup();
                                    }

                                    break;
                                }
                            }

                            break;

                        case SOCPlayingPiece.CITY:

                            SOCCity newCity = new SOCCity(game.getPlayer(((SOCPutPiece) mes).getPlayerNumber()), ((SOCPutPiece) mes).getCoordinates());
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();

                                if (tracker.getPlayer().getPlayerNumber() == ((SOCPutPiece) mes).getPlayerNumber())
                                {
                                    tracker.addOurNewCity(newCity);

                                    break;
                                }
                            }

                            ///
                            /// update the speedups from possible settlements
                            ///
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();

                                if (tracker.getPlayer().getPlayerNumber() == ((SOCPutPiece) mes).getPlayerNumber())
                                {
                                    Iterator<SOCPossibleSettlement> posSetsIter = tracker.getPossibleSettlements().values().iterator();

                                    while (posSetsIter.hasNext())
                                    {
                                        posSetsIter.next().updateSpeedup();
                                    }

                                    break;
                                }
                            }

                            ///
                            /// update the speedups from possible cities
                            ///
                            trackersIter = playerTrackers.values().iterator();

                            while (trackersIter.hasNext())
                            {
                                SOCPlayerTracker tracker = (SOCPlayerTracker) trackersIter.next();

                                if (tracker.getPlayer().getPlayerNumber() == ((SOCPutPiece) mes).getPlayerNumber())
                                {
                                    Iterator<SOCPossibleCity> posCitiesIter = tracker.getPossibleCities().values().iterator();

                                    while (posCitiesIter.hasNext())
                                    {
                                        posCitiesIter.next().updateSpeedup();
                                    }

                                    break;
                                }
                            }

                            break;
                        }

                        if (D.ebugOn)
                        {
                            SOCPlayerTracker.playerTrackersDebug(playerTrackers);
                        }
                    }

                    if (expectPUTPIECE_FROM_START1A && (mesType == SOCMessage.PUTPIECE) && (((SOCPutPiece) mes).getPlayerNumber() == ourPlayerData.getPlayerNumber()) && (((SOCPutPiece) mes).getPieceType() == SOCPlayingPiece.SETTLEMENT) && (((SOCPutPiece) mes).getCoordinates() == ourPlayerData.getLastSettlementCoord()))
                    {
                        expectPUTPIECE_FROM_START1A = false;
                        expectSTART1B = true;
                    }

                    if (expectPUTPIECE_FROM_START1B && (mesType == SOCMessage.PUTPIECE) && (((SOCPutPiece) mes).getPlayerNumber() == ourPlayerData.getPlayerNumber()) && (((SOCPutPiece) mes).getPieceType() == SOCPlayingPiece.ROAD) && (((SOCPutPiece) mes).getCoordinates() == ourPlayerData.getLastRoadCoord()))
                    {
                        expectPUTPIECE_FROM_START1B = false;
                        expectSTART2A = true;
                    }

                    if (expectPUTPIECE_FROM_START2A && (mesType == SOCMessage.PUTPIECE) && (((SOCPutPiece) mes).getPlayerNumber() == ourPlayerData.getPlayerNumber()) && (((SOCPutPiece) mes).getPieceType() == SOCPlayingPiece.SETTLEMENT) && (((SOCPutPiece) mes).getCoordinates() == ourPlayerData.getLastSettlementCoord()))
                    {
                        expectPUTPIECE_FROM_START2A = false;
                        expectSTART2B = true;
                    }

                    if (expectPUTPIECE_FROM_START2B && (mesType == SOCMessage.PUTPIECE) && (((SOCPutPiece) mes).getPlayerNumber() == ourPlayerData.getPlayerNumber()) && (((SOCPutPiece) mes).getPieceType() == SOCPlayingPiece.ROAD) && (((SOCPutPiece) mes).getCoordinates() == ourPlayerData.getLastRoadCoord()))
                    {
                        expectPUTPIECE_FROM_START2B = false;
                        expectPLAY = true;
                    }

                    if (expectDICERESULT && (mesType == SOCMessage.DICERESULT))
                    {
                        expectDICERESULT = false;

                        if (((SOCDiceResult) mes).getResult() == 7)
                        {
                            moveRobberOnSeven = true;

                            if (ourPlayerData.getResources().getTotal() > 7)
                            {
                                expectDISCARD = true;
                            }
                            else if (ourTurn)
                            {
                                if (noRobber) {
                                    expectPLAY1 = true;
                                } else {
                                    expectPLACING_ROBBER = true;
                                }
			                      }
                        } else {
                            expectPLAY1 = true;
                        }
                    }

                    if (mesType == SOCMessage.DISCARDREQUEST)
                    {
                        expectDISCARD = false;

                        /**
                         * If we haven't recently discarded...
                         */

                        //	if (!((expectPLACING_ROBBER || expectPLAY1) &&
                        //	      (counter < 4000))) {
                        if ((game.getCurrentDice() == 7) && (ourTurn) && !noRobber)
                        {
	                  		    expectPLACING_ROBBER = true;
                        }
                        else
                        {
                            expectPLAY1 = true;
                        }

                        counter = 0;
                        discard(((SOCDiscardRequest) mes).getNumberOfDiscards());

                        //	}
                    }

                    if (mesType == SOCMessage.CHOOSEPLAYERREQUEST)
                    {
                        chooseRobberVictim(((SOCChoosePlayerRequest) mes).getChoices());
                    }

                    if ((mesType == SOCMessage.ROBOTDISMISS) && (!expectDISCARD) && (!expectPLACING_ROBBER))
                    {
                        client.leaveGame(game);
                        alive = false;
                    }

                    if ((mesType == SOCMessage.GAMETEXTMSG) && (((SOCGameTextMsg) mes).getText().equals("*PING*")))
                    {
                        counter++;
                    }

                    if (counter > 15000)
                    {
                        // We've been waiting too long, commit suicide.
                        client.leaveGame(game);
                        alive = false;
                    }

                    /*
                       if (D.ebugOn) {
                       if (mes != null) {
                       debugInfo();
                       D.ebugPrintln("~~~~~~~~~~~~~~~~");
                       }
                       }
                     */
                    Thread.yield();
                }
            }
            catch (Exception e)
            {
                D.ebugPrintln("*** Caught an exception - " + e);
                System.out.println("*** Caught an exception - " + e);
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("AGG! NO PINGER!");
        }

        //D.ebugPrintln("STOPPING AND DEALLOCATING");
        gameEventQ = null;
        client.addCleanKill();
        client = null;
        game = null;
        ourPlayerData = null;
        whatWeWantToBuild = null;
        resourceChoices = null;
        ourPlayerTracker = null;
        playerTrackers = null;
        pinger.stopPinger();
        pinger = null;
    }

    void handleResourceElement(SOCPlayer pl, SOCPlayerElement mes, int rsc) {
    SOCResourceSet rs = pl.getResources();
    SOCResourceSet ur = pl.getUResources();

        int mv = mes.getValue();

        switch (mes.getAction()) {
            case SOCPlayerElement.SET:
          
          if (D.ebugOn) {
              if (mv != rs.getAmount(rsc)) {
                 client.sendText(game, ">>> RSRC ERROR FOR " + SOCResourceConstants.names[rsc] + ": " + mv + " != " + rs.getAmount(rsc));
              }
          }
          rs.setAmount(mv, rsc);
          
          break;
          
            case SOCPlayerElement.GAIN:
          rs.add(mv, rsc);
          
          break;
          
            case SOCPlayerElement.LOSE:
          if (rsc == SOCResourceConstants.UNKNOWN) {
              // convert known resources to unknown resources:
              for (int rt = SOCResourceConstants.MIN; rt < SOCResourceConstants.MAX; rt++) {
                  int ra = Math.min(rs.getAmount(rt), mv);
                  rs.subtract(ra, rt); 
                  rs.add(ra, SOCResourceConstants.UNKNOWN);
              }
          }

          rs.subtract(mv, rsc); // and reduce UNKNOWN if necessary

          break;
	    }
	
    }
    
    /**
     * kill this brain
     */
    public void kill()
    {
        alive = false;

        try
        {
            gameEventQ.put(null);
        }
        catch (Exception exc) {}
    }

    /**
     * pause for a bit
     *
     * @param msec  number of milliseconds to pause
     */
    public void pause(int msec)
    {
        try
        {
            Thread.yield();
            sleep(msec);
        }
        catch (InterruptedException exc) {}
    }

    protected int addNumbersForHex(SOCBoard board, SOCPlayerNumbers playerNumbers , int node) {
        Enumeration<Integer> hexes = SOCBoard.getAdjacentHexesToNode(node).elements();
        int probTotal = 0;

        while (hexes.hasMoreElements()) {
            int hex = hexes.nextElement().intValue();
            int number = board.getNumberOnHexFromCoord(hex);
            int resource = board.getHexTypeFromCoord(hex);
            playerNumbers.addNumberForResource(number, resource, hex);
            probTotal += SOCNumberProbabilities.INT_VALUES[number]; // integer percents
            D.ebugPrint(number + " ");
        }
        return probTotal;
    }

    protected void markPortType(SOCBoard board, boolean[] ports, int node1, int node2) {
        markPortType(board, ports, node1);
        markPortType(board, ports, node2);
    }

    protected void markPortType(SOCBoard board, boolean[] ports, int node) {
        D.ebugPrint(" ports:");
        Integer nodeInt = node;
        for (int portType = SOCBoard.MISC_PORT; portType <= SOCBoard.MAX_PORT; portType++) {
            ports[portType] = (board.getPortCoordinates(portType).contains(nodeInt));
            D.ebugPrint(" " + ports[portType]);
            // each node is of most one port type:
            // if (ports[portType]) break;
        }
    }

    /**
     * figure out where to place the two settlements.
     * Find the pair of settlements that have the lowest production time
     * (as determined by getEstimates).
     * Then choose the one that has the highest individual production
     */
    protected void planInitialSettlements()
    {
        D.ebugPrintln("--- planInitialSettlements");

        int speed;
        boolean allTheWay;
        firstSettlement = 0;
        secondSettlement = 0;

        int bestSpeed = 4 * SOCBuildingSpeedEstimate.DEFAULT_ROLL_LIMIT;
        SOCBoard board = game.getBoard();
        SOCResourceSet emptySet = new SOCResourceSet();
        SOCPlayerNumbers playerNumbers = new SOCPlayerNumbers();
        int probTotal;
        int bestProbTotal;
        boolean[] ports = new boolean[SOCBoard.MAX_PORT + 1];
        SOCBuildingSpeedEstimate estimate = new SOCBuildingSpeedEstimate();

        bestProbTotal = 0;

        for (int firstNode = 0x23; firstNode < 0xDC; firstNode++)
        {
            if (ourPlayerData.isPotentialSettlement(firstNode))
            {
                Integer firstNodeInt = (firstNode);

                //
                // this is just for testing purposes
                //
                D.ebugPrintln("FIRST NODE -----------");
                D.ebugPrintln("firstNode = " + board.nodeCoordToString(firstNode));
                playerNumbers.clear();
                probTotal = 0;
                D.ebugPrint("numbers:[");
		            probTotal += addNumbersForHex(board, playerNumbers, firstNode);
                D.ebugPrintln("]");
		            markPortType(board, ports, firstNode);
                D.ebugPrintln();
                D.ebugPrintln(" probTotal = " + probTotal);
                estimate.recalculateEstimates(playerNumbers);

		            int testSpeed = 300;
                speed = 0;
		            allTheWay = true;
		
                for (int rst = SOCBuildingSpeedEstimate.MIN; rst < SOCBuildingSpeedEstimate.MAXPLUSONE; rst++) {
                    if (speed > testSpeed) { allTheWay = false; break; }
                    speed += estimate.calculateRollsFast(emptySet, SOCBuildingSpeedEstimate.buildTargets[rst], testSpeed, ports);
                }
                
                // Debug print estimate as of first settlement:
                {
                    int[] rolls = estimate.getEstimatesFromNothingFast(ports, testSpeed);
                    D.ebugPrint(" speed = " + speed + " -- ( ");

                    D.ebugPrint(" road: " + rolls[SOCBuildingSpeedEstimate.ROAD]);
                    D.ebugPrint(" stlmt: " + rolls[SOCBuildingSpeedEstimate.SETTLEMENT]);
                    D.ebugPrint(" city: " + rolls[SOCBuildingSpeedEstimate.CITY]);
                    D.ebugPrint(" card: " + rolls[SOCBuildingSpeedEstimate.CARD]);
                    D.ebugPrintln(" )");
                }
                //
                // end test
                //

                // looking at each combination of 2 playable nodes:
                // order is not a factor at this point.
                for (int secondNode = firstNode + 1; secondNode < 0xDC; secondNode++)
                {
                    if ((ourPlayerData.isPotentialSettlement(secondNode)) &&
		                  	(!SOCBoard.getAdjacentNodesToNode(secondNode).contains(firstNodeInt)))
                    {
                        //D.ebugPrintln("firstNode = " + board.nodeCoordToString(firstNode));
                        //D.ebugPrintln("secondNode = " + board.nodeCoordToString(secondNode));

                        /**
                         * get the numbers for these settlements
                         */
                        D.ebugPrint("numbers:[");
                        playerNumbers.clear();
                        probTotal = 0;
			                  probTotal += addNumbersForHex(board, playerNumbers, firstNode);
                        D.ebugPrint("] [");
		                  	probTotal += addNumbersForHex(board, playerNumbers, secondNode);
                        D.ebugPrintln("]");
		                  	markPortType(board, ports, firstNode, secondNode);
                        D.ebugPrintln();

                        D.ebugPrintln(" probTotal = " + probTotal);

                        /**
                         * estimate the building speed for this pair
                         */
                        estimate.recalculateEstimates(playerNumbers);
                        speed = 0;
                        allTheWay = true;

                        // see if this rolls-to-resources is low for this pair:
                        for (int rst = SOCBuildingSpeedEstimate.MIN; rst < SOCBuildingSpeedEstimate.MAXPLUSONE; rst++) {
                            if (speed > bestSpeed) { allTheWay = false; break; }
                            speed += estimate.calculateRollsFast(emptySet, SOCBuildingSpeedEstimate.buildTargets[rst], bestSpeed, ports);
                        }

                        {
                            int[] rolls = estimate.getEstimatesFromNothingFast(ports, bestSpeed);
                            D.ebugPrintln(" allTheWay = " + allTheWay);
                            D.ebugPrint(" speed = " + speed + " -- ( ");
                            
                            D.ebugPrint(" road: " + rolls[SOCBuildingSpeedEstimate.ROAD]);
                            D.ebugPrint(" stlmt: " + rolls[SOCBuildingSpeedEstimate.SETTLEMENT]);
                            D.ebugPrint(" city: " + rolls[SOCBuildingSpeedEstimate.CITY]);
                            D.ebugPrint(" card: " + rolls[SOCBuildingSpeedEstimate.CARD]);
                            D.ebugPrintln(" )");
                        }
                        /**
                         * keep the settlements with the best speed
                         */
                        if (speed < bestSpeed)
                        {
                            firstSettlement = firstNode;
                            secondSettlement = secondNode;
                            bestSpeed = speed;
                            bestProbTotal = probTotal;
                            D.ebugPrintln("bestSpeed = " + bestSpeed);
                            D.ebugPrintln("bestProbTotal = " + bestProbTotal);
                        }
                        else if ((speed == bestSpeed) && allTheWay && (probTotal > bestProbTotal))
                        {
                            D.ebugPrintln("Equal speed, better prob");
                            firstSettlement = firstNode;
                            secondSettlement = secondNode;
                            bestSpeed = speed;
                            bestProbTotal = probTotal;
                            D.ebugPrintln("firstSettlement = " + Integer.toHexString(firstSettlement));
                            D.ebugPrintln("secondSettlement = " + Integer.toHexString(secondSettlement));
                            D.ebugPrintln("bestSpeed = " + bestSpeed);
                            D.ebugPrintln("bestProbTotal = " + bestProbTotal);
                        }
                    }
                }
            }
        }

        /**
         * choose which settlement to place first
         */
        playerNumbers.clear();
        addNumbersForHex(board, playerNumbers, firstSettlement); // ignore probTotal
        markPortType(board, ports, firstSettlement);
        estimate.recalculateEstimates(playerNumbers);

        int cutoff = 100;
        int firstSpeed = 0;	// roll to complete canonical ResourceSet
        firstSpeed = estimate.getEstimatesFast(null, emptySet, ports, cutoff);

        // now do it for secondSettlement:
        playerNumbers.clear();
        addNumbersForHex(board, playerNumbers, secondSettlement); // ignore probTotal
        markPortType(board, ports, secondSettlement);
        estimate.recalculateEstimates(playerNumbers);

        int secondSpeed = 0;	// get comparable secondSpeed
      	secondSpeed = estimate.getEstimatesFast(null, emptySet, ports, cutoff); 

        if (secondSpeed < firstSpeed)
        {
            int tmp = firstSettlement;
            firstSettlement = secondSettlement;
            secondSettlement = tmp;
        }

        D.ebugPrintln(board.nodeCoordToString(firstSettlement) + ":" + firstSpeed + ", " + board.nodeCoordToString(secondSettlement) + ":" + secondSpeed);
    }

    /**
     * figure out where to place the second settlement
     */
    protected void planSecondSettlement()
    {
        D.ebugPrintln("--- planSecondSettlement");

        int bestSpeed = 4 * SOCBuildingSpeedEstimate.DEFAULT_ROLL_LIMIT;
        SOCBoard board = game.getBoard();
        SOCResourceSet emptySet = new SOCResourceSet();
        SOCPlayerNumbers playerNumbers = new SOCPlayerNumbers();
        boolean[] ports = new boolean[SOCBoard.MAX_PORT + 1];
        SOCBuildingSpeedEstimate estimate = new SOCBuildingSpeedEstimate();
        int probTotal;
        int bestProbTotal;
        // int[] prob = SOCNumberProbabilities.INT_VALUES;
        int firstNode = firstSettlement;
        Integer firstNodeInt = (firstNode);

        bestProbTotal = 0;
        secondSettlement = -1;

        for (int secondNode = 0x23; secondNode < 0xDC; secondNode++)
        {
            // i would expect that our firstSettlement is already on the board 
            // along with the other players' settlements...
            if ((ourPlayerData.isPotentialSettlement(secondNode)) &&
		            (!SOCBoard.getAdjacentNodesToNode(secondNode).contains(firstNodeInt)))
            {
                /**
                 * get the numbers for these settlements
                 */
                playerNumbers.clear();
                probTotal = 0;
                D.ebugPrint("numbers: [");
                probTotal += addNumbersForHex(board, playerNumbers, firstNode);
                D.ebugPrint("] [");
                probTotal += addNumbersForHex(board, playerNumbers, secondNode);
                D.ebugPrint("]");
                markPortType(board, ports, firstNode, secondNode);
                D.ebugPrintln();

                D.ebugPrintln(" probTotal = " + probTotal);

                /**
                 * estimate the building speed for this pair
                 */
                estimate.recalculateEstimates(playerNumbers);

                int speed = 0;	// estimated rolls to produce resources
	            	boolean allTheWay = true; 
		
                // see if rolls-to-resources is low for this pair:
                for (int rst = SOCBuildingSpeedEstimate.MIN; rst < SOCBuildingSpeedEstimate.MAXPLUSONE; rst++) {
                    if (speed > bestSpeed) { allTheWay = false; break; }
                    speed += estimate.calculateRollsFast(emptySet, SOCBuildingSpeedEstimate.buildTargets[rst], bestSpeed, ports);
                }

                D.ebugPrintln(Integer.toHexString(firstNode) + ", " + Integer.toHexString(secondNode) + ":" + speed);

                /**
                 * keep the settlements with the best speed
                 */
                if ((speed < bestSpeed) || (secondSettlement < 0) ||
		               ((speed == bestSpeed) && (probTotal > bestProbTotal)))
                {
                    // firstSettlement = firstNode;
                    secondSettlement = secondNode;
                    bestSpeed = speed;
                    bestProbTotal = probTotal;
                    int number = board.getNumberOnHexFromCoord(secondNode);
                    int resource = board.getHexTypeFromCoord(secondNode);

                    D.ebugPrintln("secondSettlement = " + Integer.toHexString(secondSettlement)
                        + "=" + board.nodeCoordToString(secondSettlement)
                        + " speed=" + bestSpeed);

                    int[] rolls = estimate.getEstimatesFromNothingFast(ports);
                    D.ebugPrint("road: " + rolls[SOCBuildingSpeedEstimate.ROAD]);
                    D.ebugPrint(" stlmt: " + rolls[SOCBuildingSpeedEstimate.SETTLEMENT]);
                    D.ebugPrint(" city: " + rolls[SOCBuildingSpeedEstimate.CITY]);
                    D.ebugPrintln(" card: " + rolls[SOCBuildingSpeedEstimate.CARD]);
                    D.ebugPrintln("bestSpeed = " + bestSpeed);
                }
            }
        }
    }

    /**
     * place planned first settlement
     */
    protected void placeFirstSettlement()
    {
        //D.ebugPrintln("BUILD REQUEST FOR SETTLEMENT AT "+Integer.toHexString(firstSettlement));
        pause(500);
        client.putPiece(game, new SOCSettlement(ourPlayerData, firstSettlement));
        pause(1000);
    }

    /**
     * place planned second settlement
     */
    protected void placeSecondSettlement()
    {
        //D.ebugPrintln("BUILD REQUEST FOR SETTLEMENT AT "+Integer.toHexString(secondSettlement));
        pause(500);
        client.putPiece(game, new SOCSettlement(ourPlayerData, secondSettlement));
        pause(1000);
    }

    /**
     * place a road attached to the last initial settlement
     */
    public void placeInitRoad()
    {
        int settlementNode = ourPlayerData.getLastSettlementCoord();
        Hashtable<Integer, Integer> twoAway = new Hashtable<Integer, Integer>();

        D.ebugPrintln("--- placeInitRoad");

        /**
         * look at all of the nodes that are 2 away from the
         * last settlement and pick the best one
         */
        int tmp;

        tmp = settlementNode - 0x20;

        if ((tmp >= SOCBoard.MINNODE) && (tmp <= SOCBoard.MAXNODE) && (ourPlayerData.isPotentialSettlement(tmp)))
        {
            twoAway.put(tmp, 0);
        }

        tmp = settlementNode + 0x02;

        if ((tmp >= SOCBoard.MINNODE) && (tmp <= SOCBoard.MAXNODE) && (ourPlayerData.isPotentialSettlement(tmp)))
        {
            twoAway.put(tmp, 0);
        }

        tmp = settlementNode + 0x22;

        if ((tmp >= SOCBoard.MINNODE) && (tmp <= SOCBoard.MAXNODE) && (ourPlayerData.isPotentialSettlement(tmp)))
        {
            twoAway.put(tmp, 0);
        }

        tmp = settlementNode + 0x20;

        if ((tmp >= SOCBoard.MINNODE) && (tmp <= SOCBoard.MAXNODE) && (ourPlayerData.isPotentialSettlement(tmp)))
        {
            twoAway.put(tmp, 0);
        }

        tmp = settlementNode - 0x02;

        if ((tmp >= SOCBoard.MINNODE) && (tmp <= SOCBoard.MAXNODE) && (ourPlayerData.isPotentialSettlement(tmp)))
        {
            twoAway.put(tmp, 0);
        }

        tmp = settlementNode - 0x22;

        if ((tmp >= SOCBoard.MINNODE) && (tmp <= SOCBoard.MAXNODE) && (ourPlayerData.isPotentialSettlement(tmp)))
        {
            twoAway.put(tmp, 0);
        }

        scoreNodesForSettlements(twoAway, 3, 5, 10);

        D.ebugPrintln("Init Road for " + client.getNickname());

        /**
         * create a dummy player to calculate possible places to build
         * taking into account where other players will build before
         * we can.
         */
        SOCPlayer dummy = new SOCPlayer(ourPlayerData.getPlayerNumber(), game);

        if (game.getGameState() == SOCGame.START1B)
        {
            /**
             * do a look ahead so we don't build toward a place
             * where someone else will build first.
             */
            int numberOfBuilds = numberOfEnemyBuilds();
            D.ebugPrintln("Other players will build " + numberOfBuilds + " settlements before I get to build again.");

            if (numberOfBuilds > 0)
            {
                /**
                 * rule out where other players are going to build
                 */
                Hashtable<Integer, Integer> allNodes = new Hashtable<Integer, Integer>();

                for (int i = 0x23; i < 0xDC; i++)
                {
                    if (ourPlayerData.isPotentialSettlement(i))
                    {
                        D.ebugPrintln("-- potential settlement at " + Integer.toHexString(i));
                        allNodes.put(i, 0);
                    }
                }

                /**
                 * favor spots with the most high numbers
                 */
                bestSpotForNumbers(allNodes, 100);

                /**
                 * favor spots near good ports
                 */
                /**
                 * check 3:1 ports
                 */
                Vector<Integer> miscPortNodes = game.getBoard().getPortCoordinates(SOCBoard.MISC_PORT);
                bestSpot2AwayFromANodeSet(allNodes, miscPortNodes, 5);

                /**
                 * check out good 2:1 ports
                 */
                for (int portType = SOCBoard.MIN_PORT;
                        portType <= SOCBoard.MAX_PORT; portType++)
                {
                    /**
                     * if the chances of rolling a number on the resource is better than 1/3,
                     * then it's worth looking at the port
                     */
                    if (resourceEstimates[portType] > 33)
                    {
                        Vector<Integer> portNodes = game.getBoard().getPortCoordinates(portType);
                        int portWeight = (resourceEstimates[portType] * 10) / 56;
                        bestSpot2AwayFromANodeSet(allNodes, portNodes, portWeight);
                    }
                }

                /*
                 * create a list of potential settlements that takes into account
                 * where other players will build
                 */
                Vector<Integer> psList = new Vector<Integer>();

                for (int j = 0x23; j <= 0xDC; j++)
                {
                    if (ourPlayerData.isPotentialSettlement(j))
                    {
                        D.ebugPrintln("- potential settlement at " + Integer.toHexString(j));
                        psList.addElement(j);
                    }
                }

                dummy.setPotentialSettlements(psList);

                for (int builds = 0; builds < numberOfBuilds; builds++)
                {
		                //BoardNodeScorePair bestNodePair = findBestScoringNode(allNodes);
                    BoardNodeScorePair bestNodePair = new BoardNodeScorePair(0, 0);
                    Enumeration<Integer> nodesEnum = allNodes.keys();

                    while (nodesEnum.hasMoreElements())
                    {
                        Integer nodeCoord = nodesEnum.nextElement();
                        Integer score = allNodes.get(nodeCoord);
                        D.ebugPrintln("NODE = " + Integer.toHexString(nodeCoord.intValue()) + " SCORE = " + score);

                        if (bestNodePair.getScore() < score.intValue())
                        {
                            bestNodePair.setScore(score.intValue());
                            bestNodePair.setNode(nodeCoord.intValue());
                        }
                    }

                    /**
                     * pretend that someone has built a settlement on the best spot
                     */
                    dummy.updatePotentials(new SOCSettlement(ourPlayerData, bestNodePair.getNode()));

                    /**
                     * remove this spot from the list of best spots
                     */
                    allNodes.remove(bestNodePair.getNode());
                }
            }
        }

        /**
         * Find the best-scoring node
         */
        BoardNodeScorePair bestNodePair = new BoardNodeScorePair(0, 0);
        Enumeration<Integer> enumr = twoAway.keys();

        while (enumr.hasMoreElements())
        {
	          Integer icoord = enumr.nextElement();
            int coord = icoord.intValue();
            int score = twoAway.get(icoord).intValue();

            D.ebugPrintln("Considering " + Integer.toHexString(coord) + " with a score of " + score);

            if (dummy.isPotentialSettlement(coord))
            {
                if (bestNodePair.getScore() < score)
                {
                    bestNodePair.setScore(score);
                    bestNodePair.setNode(coord);
                }
            }
            else
            {
                D.ebugPrintln("Someone is bound to ruin that spot.");
            }
        }

        int roadEdge = 0;
        int destination = bestNodePair.getNode();

        /**
         * if the coords are (even, odd), then
         * the node is 'Y'.
         */
        if (((settlementNode >> 4) % 2) == 0)
        {
            if ((destination == (settlementNode - 0x02)) || (destination == (settlementNode + 0x20)))
            {
                roadEdge = settlementNode - 0x01;
            }
            else if (destination < settlementNode)
            {
                roadEdge = settlementNode - 0x11;
            }
            else
            {
                roadEdge = settlementNode;
            }
        }
        else
        {
            if ((destination == (settlementNode - 0x20)) || (destination == (settlementNode + 0x02)))
            {
                roadEdge = settlementNode - 0x10;
            }
            else if (destination > settlementNode)
            {
                roadEdge = settlementNode;
            }
            else
            {
                roadEdge = settlementNode - 0x11;
            }
        }

        //D.ebugPrintln("!!! PUTTING INIT ROAD !!!");
        pause(500);

        //D.ebugPrintln("Trying to build a road at "+Integer.toHexString(roadEdge));
        client.putPiece(game, new SOCRoad(ourPlayerData, roadEdge));
        pause(1000);

        dummy.destroyPlayer();
    }

    /**
     * estimate the rarity of each resource
     *
     * @return an array of rarity numbers where
     *         estimates[SOCBoard.CLAY_HEX] == the clay rarity
     */
    protected int[] estimateResourceRarity()
    {
        if (resourceEstimates == null)
        {
            SOCBoard board = game.getBoard();
            int[] numberWeights = SOCNumberProbabilities.INT_VALUES;

            resourceEstimates = new int[6];

            resourceEstimates[0] = 0;

            // look at each land hex (ignore ports)
            for (int i = 0; i < 37; i++) // 4..31
            {
                int hexNumber = board.getNumberOnHexFromNumber(i);

                if (hexNumber > 0)
                {
                    resourceEstimates[board.getHexTypeFromNumber(i)] += numberWeights[hexNumber];
                }
            }
        }

        //D.ebugPrint("Resource Estimates = ");
        for (int i = 1; i < 6; i++)
        {
            //D.ebugPrint(i+":"+resourceEstimates[i]+" ");
        }

        //D.ebugPrintln();
        return resourceEstimates;
    }

    /**
     * Takes a table of nodes and adds a weighted score to
     * each node score in the table.  Nodes touching hexes
     * with better numbers get better scores.
     *
     * @param nodes    the table of nodes with scores
     * @param weight   a number that is multiplied by the score
     */
    protected void bestSpotForNumbers(Hashtable<Integer, Integer> nodes, int weight)
    {
        int[] numRating = SOCNumberProbabilities.INT_VALUES;
        SOCBoard board = game.getBoard();
        int oldScore;
        Enumeration<Integer> nodesEnum = nodes.keys();

        while (nodesEnum.hasMoreElements())
        {
            Integer node = nodesEnum.nextElement();

            //D.ebugPrintln("BSN - looking at node "+Integer.toHexString(node.intValue()));
            oldScore = nodes.get(node).intValue();

            int score = 0;
            Enumeration<Integer> hexesEnum = SOCBoard.getAdjacentHexesToNode(node.intValue()).elements();

            while (hexesEnum.hasMoreElements())
            {
                Integer hex = hexesEnum.nextElement();
                score += numRating[board.getNumberOnHexFromCoord(hex.intValue())];

                //D.ebugPrintln(" -- -- Adding "+numRating[board.getNumberOnHexFromCoord(hex.intValue())]);
            }

            /*
             * normalize score and multiply by weight
             * 40 is highest practical score
             * lowest score is 0
             */
            int nScore = ((score * 100) / 40) * weight;
            Integer finalScore = (nScore + oldScore);
            nodes.put(node, finalScore);

            //D.ebugPrintln("BSN -- put node "+Integer.toHexString(node.intValue())+" with old score "+oldScore+" + new score "+nScore);
        }
    }

    /**
     * Takes a table of nodes and adds a weighted score to
     * each node score in the table.  Nodes touching hexes
     * with better numbers get better scores.  Also numbers
     * that the player isn't touching yet are better than ones
     * that the player is already touching.
     *
     * @param nodes    the table of nodes with scores
     * @param player   the player that we are doing the rating for
     * @param weight   a number that is multiplied by the score
     */
    protected void bestSpotForNumbers(Hashtable<Integer, Integer> nodes, SOCPlayer player, int weight)
    {
        int[] numRating = SOCNumberProbabilities.INT_VALUES;
        SOCBoard board = game.getBoard();
        int oldScore;
        Enumeration<Integer> nodesEnum = nodes.keys();

        while (nodesEnum.hasMoreElements())
        {
            Integer node = nodesEnum.nextElement();

            //D.ebugPrintln("BSN - looking at node "+Integer.toHexString(node.intValue()));
            oldScore = nodes.get(node).intValue();

            int score = 0;
            Enumeration<Integer> hexesEnum = SOCBoard.getAdjacentHexesToNode(node.intValue()).elements();

            while (hexesEnum.hasMoreElements())
            {
                int hex = hexesEnum.nextElement().intValue();
                int number = board.getNumberOnHexFromCoord(hex);
                score += numRating[number];

                if ((number != 0) && (!player.getNumbers().hasNumber(number)))
                {
                    /**
                     * add a bonus for numbers that the player doesn't already have
                     */

                    //D.ebugPrintln("ADDING BONUS FOR NOT HAVING "+number);
                    score += numRating[number];
                }

                //D.ebugPrintln(" -- -- Adding "+numRating[board.getNumberOnHexFromCoord(hex)]);
            }

            /*
             * normalize score and multiply by weight
             * 80 is highest practical score
             * lowest score is 0
             */
            int nScore = ((score * 100) / 80) * weight;
            Integer finalScore = (nScore + oldScore);
            nodes.put(node, finalScore);

            //D.ebugPrintln("BSN -- put node "+Integer.toHexString(node.intValue())+" with old score "+oldScore+" + new score "+nScore);
        }
    }

    /**
     * Takes a table of nodes and adds a weighted score to
     * each node score in the table.  A vector of nodes that
     * we want to be near is also taken as an argument.
     * Here are the rules for scoring:
     * If a node is two away from a node in the desired set of nodes it gets 100.
     * Otherwise it gets 0.
     *
     * @param nodesIn   the table of nodes to evaluate
     * @param nodeSet   the set of desired nodes
     * @param weight    the score multiplier
     */
    protected void bestSpot2AwayFromANodeSet(Hashtable<Integer, Integer> nodesIn, Vector<Integer> nodeSet, int weight)
    {
        Enumeration<Integer> nodesInEnum = nodesIn.keys();

        while (nodesInEnum.hasMoreElements())
        {
            Integer nodeCoord = nodesInEnum.nextElement();
            int node = nodeCoord.intValue();
            int score = 0;
            int oldScore = nodesIn.get(nodeCoord).intValue();

            Enumeration<Integer> nodeSetEnum = nodeSet.elements();

            while (nodeSetEnum.hasMoreElements())
            {
                int target = nodeSetEnum.nextElement().intValue();

                if (node == target)
                {
                    break;
                }
                else if (node == (target - 0x20))
                {
                    score = 100;
                }
                else if (node == (target + 0x02))
                {
                    score = 100;
                }
                else if (node == (target + 0x22))
                {
                    score = 100;
                }
                else if (node == (target + 0x20))
                {
                    score = 100;
                }
                else if (node == (target - 0x02))
                {
                    score = 100;
                }
                else if (node == (target - 0x22))
                {
                    score = 100;
                }
            }

            /**
             * multiply by weight
             */
            score *= weight;

            nodesIn.put(nodeCoord, (oldScore + score));

            //D.ebugPrintln("BS2AFANS -- put node "+Integer.toHexString(node)+" with old score "+oldScore+" + new score "+score);
        }
    }

    /**
     * Takes a table of nodes and adds a weighted score to
     * each node score in the table.  A vector of nodes that
     * we want to be on is also taken as an argument.
     * Here are the rules for scoring:
     * If a node is in the desired set of nodes it gets 100.
     * Otherwise it gets 0.
     *
     * @param nodesIn   the table of nodes to evaluate
     * @param nodeSet   the set of desired nodes
     * @param weight    the score multiplier
     */
    protected void bestSpotInANodeSet(Hashtable<Integer, Integer> nodesIn, Vector<Integer> nodeSet, int weight)
    {
        Enumeration<Integer> nodesInEnum = nodesIn.keys();

        while (nodesInEnum.hasMoreElements())
        {
            Integer nodeCoord = (Integer) nodesInEnum.nextElement();
            int node = nodeCoord.intValue();
            int score = 0;
            int oldScore = nodesIn.get(nodeCoord).intValue();

            Enumeration<Integer> nodeSetEnum = nodeSet.elements();

            while (nodeSetEnum.hasMoreElements())
            {
                int target = nodeSetEnum.nextElement().intValue();

                if (node == target)
                {
                    score = 100;

                    break;
                }
            }

            /**
             * multiply by weight
             */
            score *= weight;

            nodesIn.put(nodeCoord, (oldScore + score));

            //D.ebugPrintln("BSIANS -- put node "+Integer.toHexString(node)+" with old score "+oldScore+" + new score "+score);
        }
    }

    /**
     * move the robber
     */
    protected void moveRobber()
    {
        D.ebugPrintln("%%% MOVEROBBER");

        int[] hexes = 
        {
            0x33, 0x35, 0x37, 0x53, 0x55, 0x57, 0x59, 0x73, 0x75, 0x77, 0x79,
            0x7B, 0x95, 0x97, 0x99, 0x9B, 0xB7, 0xB9, 0xBB
        };

        int robberHex = game.getBoard().getRobberHex();

        /**
         * decide which player we want to thwart
         */
        int[] winGameETAs = { 100, 100, 100, 100 };
        Iterator<SOCPlayerTracker> trackersIter = playerTrackers.values().iterator();

        while (trackersIter.hasNext())
        {
            SOCPlayerTracker tracker = trackersIter.next();
            D.ebugPrintln("%%%%%%%%% TRACKER FOR PLAYER " + tracker.getPlayer().getPlayerNumber());

            try
            {
                tracker.recalcWinGameETA();
                winGameETAs[tracker.getPlayer().getPlayerNumber()] = tracker.getWinGameETA();
                D.ebugPrintln("winGameETA = " + tracker.getWinGameETA());
            }
            catch (NullPointerException e)
            {
                D.ebugPrintln("Null Pointer Exception calculating winGameETA");
                winGameETAs[tracker.getPlayer().getPlayerNumber()] = 500;
            }
        }

        int victimNum = -1;

        for (int pnum = 0; pnum < SOCGame.MAXPLAYERS; pnum++)
        {
            if ((victimNum < 0) && (pnum != ourPlayerData.getPlayerNumber()))
            {
                D.ebugPrintln("Picking a robber victim: pnum=" + pnum);
                victimNum = pnum;
            }
            else if ((pnum != ourPlayerData.getPlayerNumber()) && (winGameETAs[pnum] < winGameETAs[victimNum]))
            {
                D.ebugPrintln("Picking a robber victim: pnum=" + pnum);
                victimNum = pnum;
            }
        }

        /**
         * figure out the best way to thwart that player
         */
        SOCPlayer victim = game.getPlayer(victimNum);
        SOCBuildingSpeedEstimate estimate = new SOCBuildingSpeedEstimate();
        int bestHex = robberHex;
        int worstSpeed = 0;

        for (int i = 0; i < 19; i++)
        {
            /**
             * only check hexes that we're not touching,
             * and not the robber hex
             */
            if ((hexes[i] != robberHex) && (ourPlayerData.getNumbers().getNumberResourcePairsForHex(hexes[i]).isEmpty()))
            {
                estimate.recalculateEstimates(victim.getNumbers(), hexes[i]);

                int[] speeds = estimate.getEstimatesFromNothingFast(victim.getPortFlags());
                int totalSpeed = 0;

                for (int j = SOCBuildingSpeedEstimate.MIN;
                        j < SOCBuildingSpeedEstimate.MAXPLUSONE; j++)
                {
                    totalSpeed += speeds[j];
                }

                D.ebugPrintln("total Speed = " + totalSpeed);

                if (totalSpeed > worstSpeed)
                {
                    bestHex = hexes[i];
                    worstSpeed = totalSpeed;
                    D.ebugPrintln("bestHex = " + Integer.toHexString(bestHex));
                    D.ebugPrintln("worstSpeed = " + worstSpeed);
                }
            }
        }

        D.ebugPrintln("%%% bestHex = " + Integer.toHexString(bestHex));

        /**
         * pick a spot at random if we can't decide
         */
        while ((bestHex == robberHex) && (ourPlayerData.getNumbers().getNumberResourcePairsForHex(hexes[bestHex]).isEmpty()))
        {
            bestHex = hexes[Math.abs(rand.nextInt() % hexes.length)];
            D.ebugPrintln("%%% random pick = " + Integer.toHexString(bestHex));
        }

        D.ebugPrintln("!!! MOVING ROBBER !!!");
        client.moveRobber(game, ourPlayerData, bestHex);
        pause(2000);
    }

    /**
     * discard some resources
     *
     * @param numDiscards  the number of resources to discard
     */
    protected void discard(int numDiscards)
    {
        //D.ebugPrintln("DISCARDING...");

        /**
         * if we have a plan, then try to keep the resources
         * needed for that plan, otherwise discard at random
         */
        SOCResourceSet discards = new SOCResourceSet();

        /**
         * make a plan if we don't have one
         */
        if (buildingPlan.empty())
        {
            decisionMaker.planStuff(robotParameters.getStrategyType());
        }

        if (!buildingPlan.empty())
        {
            SOCPossiblePiece targetPiece = (SOCPossiblePiece) buildingPlan.peek();
            negotiator.setTargetPiece(ourPlayerData.getPlayerNumber(), targetPiece);

            //D.ebugPrintln("targetPiece="+targetPiece);
            SOCResourceSet targetResources = null;

            switch (targetPiece.getType())
            {
            case SOCPossiblePiece.CARD:
                targetResources = SOCGame.CARD_SET;

                break;

            case SOCPlayingPiece.ROAD:
                targetResources = SOCGame.ROAD_SET;

                break;

            case SOCPlayingPiece.SETTLEMENT:
                targetResources = SOCGame.SETTLEMENT_SET;

                break;

            case SOCPlayingPiece.CITY:
                targetResources = SOCGame.CITY_SET;

                break;
            }

            /**
             * figure out what resources are NOT the ones we need
             */
            SOCResourceSet leftOvers = ourPlayerData.getResources().copy();

            for (int rsrc = SOCResourceConstants.MIN;
                    rsrc < SOCResourceConstants.MAX; rsrc++)
            {
                if (leftOvers.getAmount(rsrc) > targetResources.getAmount(rsrc))
                {
                    leftOvers.subtract(targetResources.getAmount(rsrc), rsrc);
                }
                else
                {
                    leftOvers.setAmount(0, rsrc);
                }
            }

            SOCResourceSet neededRsrcs = ourPlayerData.getResources().copy();
            neededRsrcs.subtract(leftOvers);

            /**
             * figure out the order of resources from
             * easiest to get to hardest
             */

            //D.ebugPrintln("our numbers="+ourPlayerData.getNumbers());
            SOCBuildingSpeedEstimate estimate = new SOCBuildingSpeedEstimate(ourPlayerData.getNumbers());
            int[] rollsPerResource = estimate.getRollsPerResource();
	           // initialize resourceOrder is any order, then sort it:
            int[] resourceOrder = 
            {
                SOCResourceConstants.CLAY, SOCResourceConstants.ORE,
                SOCResourceConstants.SHEEP, SOCResourceConstants.WHEAT,
                SOCResourceConstants.WOOD
            };

	          // sort resourceOrder by rollsPerResource:
            for (int j = 4; j >= 0; j--)
            {
                for (int i = 0; i < j; i++)
                {
                    if (rollsPerResource[resourceOrder[i]] < rollsPerResource[resourceOrder[i + 1]])
                    {
                        int tmp = resourceOrder[i];
                        resourceOrder[i] = resourceOrder[i + 1];
                        resourceOrder[i + 1] = tmp;
                    }
                }
            }

            /**
             * pick the discards
             */
            int curRsrc = 0;

            while (discards.getTotal() < numDiscards)
            {
                /**
                 * choose from the left overs
                 */
                while ((discards.getTotal() < numDiscards) && (curRsrc < 5))
                {
                    //D.ebugPrintln("(1) dis.tot="+discards.getTotal()+" curRsrc="+curRsrc);
                    if (leftOvers.getAmount(resourceOrder[curRsrc]) > 0)
                    {
                        discards.add(1, resourceOrder[curRsrc]);
                        leftOvers.subtract(1, resourceOrder[curRsrc]);
                    }
                    else
                    {
                        curRsrc++;
                    }
                }

                curRsrc = 0;

                /**
                 * choose from what we need
                 */
                while ((discards.getTotal() < numDiscards) && (curRsrc < 5))
                {
                    //D.ebugPrintln("(2) dis.tot="+discards.getTotal()+" curRsrc="+curRsrc);
                    if (neededRsrcs.getAmount(resourceOrder[curRsrc]) > 0)
                    {
                        discards.add(1, resourceOrder[curRsrc]);
                        neededRsrcs.subtract(1, resourceOrder[curRsrc]);
                    }
                    else
                    {
                        curRsrc++;
                    }
                }
            }

            if (curRsrc == 5)
            {
                System.err.println("PROBLEM IN DISCARD - curRsrc == 5");
            }
        }
        else
        {
            /**
             *  choose discards at random
             */
            Vector<Integer> hand = new Vector<Integer>(16);
            int cnt = 0;

            // System.err.println("resources="+ourPlayerData.getResources());
            for (int rsrcType = SOCResourceConstants.MIN;
                    rsrcType < SOCResourceConstants.MAX;
		                rsrcType++)
            {
                for (int i = ourPlayerData.getResources().getAmount(rsrcType); i != 0; i--)
                {
                    hand.addElement(rsrcType);

                    // System.err.println("rsrcType="+rsrcType);
                }
            }

            /**
             * pick cards
             */
            for (; numDiscards > 0; numDiscards--)
            {
                // System.err.println("numDiscards="+numDiscards+"|hand.size="+hand.size());
		            int idx = rand.nextInt(hand.size());

                // System.err.println("idx="+idx);
                discards.add(1, hand.elementAt(idx).intValue());
                hand.removeElementAt(idx);
            }
        }

        D.ebugPrintln("!!! DISCARDING !!!");
        D.ebugPrintln("discards="+discards);
        client.discard(game, discards);
    }

    /**
     * choose a robber victim
     *
     * @param choices a boolean array representing which players are possible victims
     */
    protected void chooseRobberVictim(boolean[] choices)
    {
        int choice = -1;

        /**
         * choose the player with the smallest WGETA
         */
        for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
        {
            if (choices[i])
            {
                if (choice == -1)
                {
                    choice = i;
                }
                else
                {
                    SOCPlayerTracker tracker1 = playerTrackers.get(i);
                    SOCPlayerTracker tracker2 =  playerTrackers.get(choice);

                    if ((tracker1 != null) && (tracker2 != null) && (tracker1.getWinGameETA() < tracker2.getWinGameETA()))
                    {
                        //D.ebugPrintln("Picking a robber victim: pnum="+i+" VP="+game.getPlayer(i).getPublicVP());
                        choice = i;
                    }
                }
            }
        }

        /**
         * choose victim at random
         *
           do {
           choice = Math.abs(rand.nextInt() % SOCGame.MAXPLAYERS);
           } while (!choices[choice]);
         */
        client.choosePlayer(game, choice);
    }

    /**
     * calculate the number of builds before the next turn during init placement
     *
     */
    protected int numberOfEnemyBuilds()
    {
        int numberOfBuilds = 0;
        int pNum = game.getCurrentPlayerNumber();

        /**
         * This is the clockwise direction
         */
        if ((game.getGameState() == SOCGame.START1A) || (game.getGameState() == SOCGame.START1B))
        {
            do
            {
                /**
                 * look at the next player
                 */
                pNum++;

                if (pNum >= SOCGame.MAXPLAYERS)
                {
                    pNum = 0;
                }

                if (pNum != game.getFirstPlayer())
                {
                    numberOfBuilds++;
                }
            }
            while (pNum != game.getFirstPlayer());
        }

        /**
         * This is the counter-clockwise direction
         */
        do
        {
            /**
             * look at the next player
             */
            pNum--;

            if (pNum < 0)
            {
                pNum = SOCGame.MAXPLAYERS - 1;
            }

            if (pNum != game.getCurrentPlayerNumber())
            {
                numberOfBuilds++;
            }
        }
        while (pNum != game.getCurrentPlayerNumber());

        return numberOfBuilds;
    }

    /**
     * given a table of nodes/edges with scores, return the
     * best scoring pair
     *
     * @param nodes  the table of nodes/edges
     * @return the best scoring pair
     */
    protected BoardNodeScorePair findBestScoringNode(Hashtable<Integer, Integer> nodes)
    {
        BoardNodeScorePair bestNodePair = new BoardNodeScorePair(0, -1);
        Enumeration<Integer> nodesEnum = nodes.keys();

        while (nodesEnum.hasMoreElements())
        {
            Integer nodeCoord = nodesEnum.nextElement();
            Integer score = nodes.get(nodeCoord);

            //D.ebugPrintln("Checking:"+Integer.toHexString(nodeCoord.intValue())+" score:"+score);
            if (bestNodePair.getScore() < score.intValue())
            {
                bestNodePair.setScore(score.intValue());
                bestNodePair.setNode(nodeCoord.intValue());
            }
        }

        return bestNodePair;
    }

    /**
     * this is a function more for convience
     * given a set of nodes, run a bunch of metrics across them
     * to find which one is best for building a
     * settlement
     *
     * @param nodes          a hashtable of nodes, the scores in the table will be modified
     * @param numberWeight   the weight given to nodes on good numbers
     * @param miscPortWeight the weight given to nodes on 3:1 ports
     * @param portWeight     the weight given to nodes on good 2:1 ports
     */
    protected void scoreNodesForSettlements(Hashtable<Integer, Integer> nodes, int numberWeight, int miscPortWeight, int portWeight)
    {
        /**
         * favor spots with the most high numbers
         */
        bestSpotForNumbers(nodes, ourPlayerData, numberWeight);

        /**
         * favor spots on good ports
         */
        /**
         * check if this is on a 3:1 ports, only if we don't have one
         */
        if (!ourPlayerData.getPortFlag(SOCBoard.MISC_PORT))
        {
            Vector<Integer> miscPortNodes = game.getBoard().getPortCoordinates(SOCBoard.MISC_PORT);
            bestSpotInANodeSet(nodes, miscPortNodes, miscPortWeight);
        }

        /**
         * check out good 2:1 ports that we don't have
         */
        int[] resourceEstimates = estimateResourceRarity();

        for (int portType = SOCBoard.MIN_PORT; portType <= SOCBoard.MAX_PORT; portType++)
        {
            /**
             * if the chances of rolling a number on the resource is better than 1/3,
             * then it's worth looking at the port
             */
            if ((resourceEstimates[portType] > 33) && (!ourPlayerData.getPortFlag(portType)))
            {
                Vector<Integer> portNodes = game.getBoard().getPortCoordinates(portType);
                int estimatedPortWeight = (resourceEstimates[portType] * portWeight) / 56;
                bestSpotInANodeSet(nodes, portNodes, estimatedPortWeight);
            }
        }
    }

    /**
     * do some trading
     */
    protected void tradeStuff()
    {
        /**
         * make a tree of all the possible trades that we can
         * make with the bank or ports
         */
        SOCTradeTree treeRoot = new SOCTradeTree(ourPlayerData.getResources(), (SOCTradeTree) null);
        Hashtable<SOCResourceSet, SOCTradeTree> treeNodes = new Hashtable<SOCResourceSet, SOCTradeTree>();
        treeNodes.put(treeRoot.getResourceSet(), treeRoot);

        Queue queue = new Queue();
        queue.put(treeRoot);

        while (!queue.empty())
        {
            SOCTradeTree currentTreeNode = (SOCTradeTree) queue.get();

            //D.ebugPrintln("%%% Expanding "+currentTreeNode.getResourceSet());
            expandTradeTreeNode(currentTreeNode, treeNodes);

            Enumeration<SOCTradeTree> childrenEnum = currentTreeNode.getChildren().elements();

            while (childrenEnum.hasMoreElements())
            {
                SOCTradeTree child = childrenEnum.nextElement();

                //D.ebugPrintln("%%% Child "+child.getResourceSet());
                if (child.needsToBeExpanded())
                {
                    /**
                     * make a new table entry
                     */
                    treeNodes.put(child.getResourceSet(), child);
                    queue.put(child);
                }
            }
        }

        /**
         * find the best trade result and then perform the trades
         */
        SOCResourceSet bestTradeOutcome = null;
        int bestTradeScore = -1;
        Enumeration<SOCResourceSet> possibleTrades = treeNodes.keys();

        while (possibleTrades.hasMoreElements())
        {
            SOCResourceSet possibleTradeOutcome = possibleTrades.nextElement();

            //D.ebugPrintln("%%% "+possibleTradeOutcome);
            int score = scoreTradeOutcome(possibleTradeOutcome);

            if (score > bestTradeScore)
            {
                bestTradeOutcome = possibleTradeOutcome;
                bestTradeScore = score;
            }
        }

        /**
         * find the trade outcome in the tree, then follow
         * the chain of parents until you get to the root
         * all the while pushing the outcomes onto a stack.
         * then pop outcomes off of the stack and perfoem
         * the trade to get each outcome
         */
        Stack<SOCTradeTree> stack = new Stack<SOCTradeTree>();
        SOCTradeTree cursor = treeNodes.get(bestTradeOutcome);

        while (cursor != treeRoot)
        {
            stack.push(cursor);
            cursor = cursor.getParent();
        }

        SOCResourceSet give = new SOCResourceSet();
        SOCResourceSet get = new SOCResourceSet();
        SOCTradeTree currTreeNode;
        SOCTradeTree prevTreeNode;
        prevTreeNode = treeRoot;

        while (!stack.empty())
        {
            currTreeNode = (SOCTradeTree) stack.pop();
            give.setAmounts(prevTreeNode.getResourceSet());
            give.subtract(currTreeNode.getResourceSet());
            get.setAmounts(currTreeNode.getResourceSet());
            get.subtract(prevTreeNode.getResourceSet());

            /**
             * get rid of the negative numbers
             */
            for (int rt = SOCResourceConstants.MIN; rt < SOCResourceConstants.MAX; rt++)
            {
                if (give.getAmount(rt) < 0)
                {
                    give.setAmount(0, rt);
                }

                if (get.getAmount(rt) < 0)
                {
                    get.setAmount(0, rt);
                }
            }

            //D.ebugPrintln("Making bank trade:");
            //D.ebugPrintln("give: "+give);
            //D.ebugPrintln("get: "+get);
            client.bankTrade(game, give, get);
            pause(2000);
            prevTreeNode = currTreeNode;
        }
    }

    /**
     * expand a trade tree node
     *
     * @param currentTreeNode   the tree node that we're expanding
     * @param table  the table of all of the nodes in the tree except this one
     */
    protected void expandTradeTreeNode(SOCTradeTree currentTreeNode, Hashtable<SOCResourceSet, SOCTradeTree> table)
    {
        /**
         * the resources that we have to work with
         */
        SOCResourceSet rSet = currentTreeNode.getResourceSet();

        /**
         * go through the resources one by one, and generate all possible
         * resource sets that result from trading that type of resource
         */
        for (int giveResource = SOCResourceConstants.MIN;
                giveResource < SOCResourceConstants.MAX; giveResource++)
        {
            /**
             * find the ratio at which we can trade
             */
            int tradeRatio;

            if (ourPlayerData.getPortFlag(giveResource))
            {
                tradeRatio = 2;
            }
            else if (ourPlayerData.getPortFlag(SOCBoard.MISC_PORT))
            {
                tradeRatio = 3;
            }
            else
            {
                tradeRatio = 4;
            }

            /**
             * make sure we have enough resources to trade
             */
            if (rSet.getAmount(giveResource) >= tradeRatio)
            {
                /**
                 * trade the resource that we're looking at for one
                 * of every other resource
                 */
                for (int getResource = SOCResourceConstants.MIN;
                        getResource < SOCResourceConstants.MAX;
                        getResource++)
                {
                    if (getResource != giveResource)
                    {
                        SOCResourceSet newTradeResult = rSet.copy();
                        newTradeResult.subtract(tradeRatio, giveResource);
                        newTradeResult.add(1, getResource);

                        SOCTradeTree newTree = new SOCTradeTree(newTradeResult, currentTreeNode);

                        /**
                         * if the trade results in a set of resources that is
                         * equal to or worse than a trade we've already seen,
                         * then we don't want to expand this tree node
                         */
                        Enumeration<SOCResourceSet> tableEnum = table.keys();

                        while (tableEnum.hasMoreElements())
                        {
                            SOCResourceSet oldTradeResult = tableEnum.nextElement();

                            /*
                               //D.ebugPrintln("%%%     "+newTradeResult);
                               //D.ebugPrintln("%%%  <= "+oldTradeResult+" : "+
                               SOCResourceSet.lte(newTradeResult, oldTradeResult));
                             */
                            if (SOCResourceSet.lte(newTradeResult, oldTradeResult))
                            {
                                newTree.setNeedsToBeExpanded(false);

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * evaluate a trade outcome by calculating how much you could build with it
     *
     * @param tradeOutcome  a set of resources that would be the result of trading
     */
    protected int scoreTradeOutcome(SOCResourceSet tradeOutcome)
    {
        int score = 0;
        SOCResourceSet tempTO = tradeOutcome.copy();

        if ((ourPlayerData.getNumPieces(SOCPlayingPiece.SETTLEMENT) >= 1) && (ourPlayerData.hasPotentialSettlement()))
        {
            while (tempTO.contains(SOCGame.SETTLEMENT_SET))
            {
                score += 2;
                tempTO.subtract(SOCGame.SETTLEMENT_SET);
            }
        }

        if ((ourPlayerData.getNumPieces(SOCPlayingPiece.ROAD) >= 1) && (ourPlayerData.hasPotentialRoad()))
        {
            while (tempTO.contains(SOCGame.ROAD_SET))
            {
                score += 1;
                tempTO.subtract(SOCGame.ROAD_SET);
            }
        }

        if ((ourPlayerData.getNumPieces(SOCPlayingPiece.CITY) >= 1) && (ourPlayerData.hasPotentialCity()))
        {
            while (tempTO.contains(SOCGame.CITY_SET))
            {
                score += 2;
                tempTO.subtract(SOCGame.CITY_SET);
            }
        }

        //D.ebugPrintln("Score for "+tradeOutcome+" : "+score);
        return score;
    }

    /**
     * make trades to get the target resources
     *
     * @param targetResources  the resources that we want
     * @return true if we sent a request to trade
     */
    protected boolean tradeToTarget2(SOCResourceSet targetResources)
    {
        if (ourPlayerData.getResources().contains(targetResources))
        {
            return false;
        }

        SOCTradeOffer bankTrade = negotiator.getOfferToBank(targetResources, ourPlayerData.getResources());

        if ((bankTrade != null) && (ourPlayerData.getResources().contains(bankTrade.getGiveSet())))
        {
            client.bankTrade(game, bankTrade.getGiveSet(), bankTrade.getGetSet());
            pause(2000);

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * consider an offer made by another player
     *
     * @param offer  the offer to consider
     * @return a code that represents how we want to respond
     * note: a negative result means we do nothing
     */
    protected int considerOffer(SOCTradeOffer offer)
    {
        int response = -1;

        SOCPlayer offeringPlayer = game.getPlayer(offer.getFrom());

        if ((offeringPlayer.getCurrentOffer() != null) && (offer == offeringPlayer.getCurrentOffer()))
        {
            boolean[] offeredTo = offer.getTo();

            if (offeredTo[ourPlayerData.getPlayerNumber()])
            {
                response = negotiator.considerOffer2(offer, ourPlayerData.getPlayerNumber());
            }
        }

        return response;
    }

    /***
     * make an offer to another player
     *
     * @param target  the resources that we want
     * @return true if we made an offer
     */
    protected boolean makeOffer(SOCPossiblePiece target)
    {
        boolean result = false;
        SOCTradeOffer offer = negotiator.makeOffer(target);
        ourPlayerData.setCurrentOffer(offer);
        negotiator.resetWantsAnotherOffer();

        if (offer != null)
        {
            ///
            ///  reset the offerRejections flag
            ///
            for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
            {
                offerRejections[i] = false;
            }

            waitingForTradeResponse = true;
            counter = 0;
            client.offerTrade(game, offer);
            result = true;
        }
        else
        {
            doneTrading = true;
            waitingForTradeResponse = false;
        }

        return result;
    }

    /**
     * make a counter offer to another player
     *
     * @param offer their offer
     * @return true if we made an offer
     */
    protected boolean makeCounterOffer(SOCTradeOffer offer)
    {
        boolean result = false;
        SOCTradeOffer counterOffer = negotiator.makeCounterOffer(offer);
        ourPlayerData.setCurrentOffer(counterOffer);

        if (counterOffer != null)
        {
            ///
            ///  reset the offerRejections flag
            ///
            offerRejections[offer.getFrom()] = false;
            waitingForTradeResponse = true;
            counter = 0;
            client.offerTrade(game, counterOffer);
            result = true;
        }
        else
        {
            doneTrading = true;
            waitingForTradeResponse = false;
        }

        return result;
    }

    /**
     * this means that we want to play a discovery development card
     */
    protected void chooseFreeResources(SOCResourceSet targetResources)
    {
        /**
         * clear our resource choices
         */
        resourceChoices.clear();

        /**
         * find the most needed resource by looking at
         * which of the resources we still need takes the
         * longest to aquire
         */
        SOCResourceSet rsCopy = ourPlayerData.getResources().copy();
        SOCBuildingSpeedEstimate estimate = new SOCBuildingSpeedEstimate(ourPlayerData.getNumbers());
        int[] rollsPerResource = estimate.getRollsPerResource();

        for (int resourceCount = 0; resourceCount < 2; resourceCount++)
        {
            int mostNeededResource = -1;

            for (int resource = SOCResourceConstants.MIN;
                    resource < SOCResourceConstants.MAX; resource++)
            {
                if (rsCopy.getAmount(resource) < targetResources.getAmount(resource))
                {
                    if (mostNeededResource < 0)
                    {
                        mostNeededResource = resource;
                    }
                    else
                    {
                        if (rollsPerResource[resource] > rollsPerResource[mostNeededResource])
                        {
                            mostNeededResource = resource;
                        }
                    }
                }
            }

            resourceChoices.add(1, mostNeededResource);
            rsCopy.add(1, mostNeededResource);
        }
    }

    /**
     * choose a resource to monopolize
     * @return true if playing the card is worth it
     */
    protected boolean chooseMonopoly()
    {
        int bestResourceCount = 0;
        int bestResource = 0;

        for (int resource = SOCResourceConstants.MIN;
                resource < SOCResourceConstants.MAX; resource++)
        {
            //D.ebugPrintln("$$ resource="+resource);
            int freeResourceCount = 0;
            boolean twoForOne = false;
            boolean threeForOne = false;

            if (ourPlayerData.getPortFlag(resource))
            {
                twoForOne = true;
            }
            else if (ourPlayerData.getPortFlag(SOCBoard.MISC_PORT))
            {
                threeForOne = true;
            }

            int resourceTotal = 0;

            for (int pn = 0; pn < SOCGame.MAXPLAYERS; pn++)
            {
                if (ourPlayerData.getPlayerNumber() != pn)
                {
                    resourceTotal += game.getPlayer(pn).getResources().getAmount(resource);

                    //D.ebugPrintln("$$ resourceTotal="+resourceTotal);
                }
            }

            if (twoForOne)
            {
                freeResourceCount = resourceTotal / 2;
            }
            else if (threeForOne)
            {
                freeResourceCount = resourceTotal / 3;
            }
            else
            {
                freeResourceCount = resourceTotal / 4;
            }

            //D.ebugPrintln("freeResourceCount="+freeResourceCount);
            if (freeResourceCount > bestResourceCount)
            {
                bestResourceCount = freeResourceCount;
                bestResource = resource;
            }
        }

        if (bestResourceCount > 2)
        {
            monopolyChoice = bestResource;

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * this is for debugging
     */
    private void debugInfo()
    {
        /*
           if (D.ebugOn) {
           //D.ebugPrintln("$===============");
           //D.ebugPrintln("gamestate = "+game.getGameState());
           //D.ebugPrintln("counter = "+counter);
           //D.ebugPrintln("resources = "+ourPlayerData.getResources().getTotal());
           if (expectSTART1A)
           //D.ebugPrintln("expectSTART1A");
           if (expectSTART1B)
           //D.ebugPrintln("expectSTART1B");
           if (expectSTART2A)
           //D.ebugPrintln("expectSTART2A");
           if (expectSTART2B)
           //D.ebugPrintln("expectSTART2B");
           if (expectPLAY)
           //D.ebugPrintln("expectPLAY");
           if (expectPLAY1)
           //D.ebugPrintln("expectPLAY1");
           if (expectPLACING_ROAD)
           //D.ebugPrintln("expectPLACING_ROAD");
           if (expectPLACING_SETTLEMENT)
           //D.ebugPrintln("expectPLACING_SETTLEMENT");
           if (expectPLACING_CITY)
           //D.ebugPrintln("expectPLACING_CITY");
           if (expectPLACING_ROBBER)
           //D.ebugPrintln("expectPLACING_ROBBER");
           if (expectPLACING_FREE_ROAD1)
           //D.ebugPrintln("expectPLACING_FREE_ROAD1");
           if (expectPLACING_FREE_ROAD2)
           //D.ebugPrintln("expectPLACING_FREE_ROAD2");
           if (expectPUTPIECE_FROM_START1A)
           //D.ebugPrintln("expectPUTPIECE_FROM_START1A");
           if (expectPUTPIECE_FROM_START1B)
           //D.ebugPrintln("expectPUTPIECE_FROM_START1B");
           if (expectPUTPIECE_FROM_START2A)
           //D.ebugPrintln("expectPUTPIECE_FROM_START2A");
           if (expectPUTPIECE_FROM_START2B)
           //D.ebugPrintln("expectPUTPIECE_FROM_START2B");
           if (expectDICERESULT)
           //D.ebugPrintln("expectDICERESULT");
           if (expectDISCARD)
           //D.ebugPrintln("expectDISCARD");
           if (expectMOVEROBBER)
           //D.ebugPrintln("expectMOVEROBBER");
           if (expectWAITING_FOR_DISCOVERY)
           //D.ebugPrintln("expectWAITING_FOR_DISCOVERY");
           if (waitingForGameState)
           //D.ebugPrintln("waitingForGameState");
           if (waitingForOurTurn)
           //D.ebugPrintln("waitingForOurTurn");
           if (waitingForTradeMsg)
           //D.ebugPrintln("waitingForTradeMsg");
           if (waitingForDevCard)
           //D.ebugPrintln("waitingForDevCard");
           if (moveRobberOnSeven)
           //D.ebugPrintln("moveRobberOnSeven");
           if (waitingForTradeResponse)
           //D.ebugPrintln("waitingForTradeResponse");
           if (doneTrading)
           //D.ebugPrintln("doneTrading");
           if (ourTurn)
           //D.ebugPrintln("ourTurn");
           //D.ebugPrintln("whatWeWantToBuild = "+whatWeWantToBuild);
           //D.ebugPrintln("#===============");
           }
         */
    }

    private void printResources()
    {
        if (D.ebugOn)
        {
            for (int i = 0; i < SOCGame.MAXPLAYERS; i++)
            {
                SOCResourceSet rsrcs = game.getPlayer(i).getResources();
                String resourceMessage = "PLAYER " + i + " RESOURCES: ";
                resourceMessage += (rsrcs.getAmount(SOCResourceConstants.CLAY) + " ");
                resourceMessage += (rsrcs.getAmount(SOCResourceConstants.ORE) + " ");
                resourceMessage += (rsrcs.getAmount(SOCResourceConstants.SHEEP) + " ");
                resourceMessage += (rsrcs.getAmount(SOCResourceConstants.WHEAT) + " ");
                resourceMessage += (rsrcs.getAmount(SOCResourceConstants.WOOD) + " ");
                resourceMessage += (rsrcs.getAmount(SOCResourceConstants.UNKNOWN) + " ");
                D.ebugPrintln(resourceMessage);
            }
        }
    }
}
