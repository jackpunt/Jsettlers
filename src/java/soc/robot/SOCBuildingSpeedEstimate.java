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

import soc.disableDebug.D;

import soc.game.SOCBoard;
import soc.game.SOCGame;
import soc.game.SOCPlayerNumbers;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;

import soc.util.CutoffExceededException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


/**
 * This class calculates approximately how
 * long it would take a player to build something.
 */
public class SOCBuildingSpeedEstimate
{
    public static final int MIN = 0;
    public static final int ROAD = 0;
    public static final int SETTLEMENT = 1;
    public static final int CITY = 2;
    public static final int CARD = 3;
    public static final int MAX = 3;
    public static final int MAXPLUSONE = 4;
    public static final int DEFAULT_ROLL_LIMIT = 40;
    
    /** collection of resource sets to compute estimated build time.
     * 2*wood, 2*clay, 2*sheep, 4*wheat, 4*ore
     */
    static SOCResourceSet[] buildTargets = new SOCResourceSet[MAXPLUSONE];
    // this must be synched:: bt[ROAD] = SOCGame.ROAD_SET;
    static {
	buildTargets[ROAD] =  SOCGame.ROAD_SET;
	buildTargets[SETTLEMENT] = SOCGame.SETTLEMENT_SET;
	buildTargets[CITY] = SOCGame.CITY_SET;
	buildTargets[CARD] = SOCGame.CARD_SET;
    }

    protected static boolean recalc;
    int[] estimatesFromNothing;
    int[] estimatesFromNow;
    int[] rollsPerResource;
    SOCResourceSet[] resourcesForRoll;
    SOCResourceSet emptySet = new SOCResourceSet();

    /**
     * this is a constructor
     *
     * @param numbers  the numbers that the player's pieces are touching
     */
    public SOCBuildingSpeedEstimate(SOCPlayerNumbers numbers)
    {
        estimatesFromNothing = new int[MAXPLUSONE];
        estimatesFromNow = new int[MAXPLUSONE];
        rollsPerResource = new int[SOCResourceConstants.MAX];
        recalculateRollsPerResource(numbers);
        resourcesForRoll = new SOCResourceSet[13];
        recalculateResourcesForRoll(numbers);
    }

    /**
     * this is a constructor
     */
    public SOCBuildingSpeedEstimate()
    {
        estimatesFromNothing = new int[MAXPLUSONE];
        estimatesFromNow = new int[MAXPLUSONE];
        rollsPerResource = new int[SOCResourceConstants.MAX]; // [0..5]
        resourcesForRoll = new SOCResourceSet[13];
    }

    /**
     * compute number of rolls to complete each ResourceSet, from initial resources.
     * @param estimates is an int[] to hold rolls for each resource set
     * @return total rolls across the estimates array.
     */
    public int getEstimatesFast(int[] estimates, SOCResourceSet resources, boolean[] ports, int limit)
    {
	int total = 0;
	for (int rst = MIN; rst < MAXPLUSONE; rst++) {
	    int rolls = calculateRollsFast(resources, buildTargets[rst], limit, ports);
	    if (estimates != null) estimates[rst] = rolls;
	    total += rolls;
	}
        return total;
    }

    /**
     * compute number of rolls to complete each ResourceSet, from initial resources.
     * @param estimates is an int[] to hold rolls for each resource set
     * @return total rolls across the estimates array.
     */
    public int getEstimatesAccurate(int[] estimates, SOCResourceSet resources, boolean[] ports, int limit)
    {
	int total = 0;
	for (int rst = MIN; rst < MAXPLUSONE; rst++) {
	    int rolls = calculateRollsAccurate(resources, buildTargets[rst], limit, ports);
	    if (estimates != null) estimates[rst] = rolls;
	    total += rolls;
	}
        return total;
    }


    /**
     * @return the estimates from nothing
     *
     * @param ports  the port flags for the player
     */
    public int[] getEstimatesFromNothingAccurate(boolean[] ports)
    {
        if (recalc)
        {
	    getEstimatesAccurate(estimatesFromNothing, emptySet, ports, DEFAULT_ROLL_LIMIT);
        }
        return estimatesFromNothing;
    }

    /**
     * @return the estimates from nothing
     *
     * @param ports  the port flags for the player
     */
    public int[] getEstimatesFromNothingFast(boolean[] ports)
    {
	return getEstimatesFromNothingFast(ports, DEFAULT_ROLL_LIMIT);
    }

    /**
     * @return the estimates from nothing
     *
     * @param ports  the port flags for the player
     */
    public int[] getEstimatesFromNothingFast(boolean[] ports, int limit)
    {
        if (recalc)
        {
	    getEstimatesFast(estimatesFromNothing, emptySet, ports, limit);
        }

        return estimatesFromNothing;
    }

    /**
     * @return the estimates from now
     *
     * @param resources  the player's current resources
     * @param ports      the player's port flags
     */
    public int[] getEstimatesFromNowAccurate(SOCResourceSet resources, boolean[] ports)
    {
	getEstimatesAccurate(estimatesFromNow, resources, ports, DEFAULT_ROLL_LIMIT);
	return estimatesFromNow;
    }

    /**
     * @return the estimates from now
     *
     * @param resources  the player's current resources
     * @param ports      the player's port flags
     */
    public int[] getEstimatesFromNowFast(SOCResourceSet resources, boolean[] ports)
    {
	getEstimatesFast(estimatesFromNow, resources, ports, DEFAULT_ROLL_LIMIT);
	return estimatesFromNow;
    }

    /**
     * recalculate both rollsPerResource and resourcesPerRoll
     */
    public void recalculateEstimates(SOCPlayerNumbers numbers)
    {
        recalculateRollsPerResource(numbers);
        recalculateResourcesForRoll(numbers);
    }

    /**
     * recalculate both rollsPerResource and resourcesPerRoll
     * using the robber information
     */
    public void recalculateEstimates(SOCPlayerNumbers numbers, int robberHex)
    {
        recalculateRollsPerResource(numbers, robberHex);
        recalculateResourcesForRoll(numbers, robberHex);
    }

    /**
     * calculate the estimates
     *
     * could arrange to call recalculateRollsPerResource(numbers, -1);
     * @param numbers  the numbers that the player is touching
     */
    public void recalculateRollsPerResource(SOCPlayerNumbers numbers)
    {
        //D.ebugPrintln("@@@@@@@@ recalculateRollsPerResource");
        //D.ebugPrintln("@@@@@@@@ numbers = "+numbers);
        recalc = true;

        /**
         * figure out how many resources we get per roll
         */
        for (int resource = SOCResourceConstants.MIN; resource < SOCResourceConstants.MAX; resource++)
        {
            //D.ebugPrintln("resource: "+resource);
            float totalProbability = 0.0f;

            Enumeration numbersEnum = numbers.getNumbersForResource(resource).elements();

            while (numbersEnum.hasMoreElements())
            {
                Integer number = (Integer) numbersEnum.nextElement();
                totalProbability += SOCNumberProbabilities.FLOAT_VALUES[number.intValue()];
            }

            //D.ebugPrintln("totalProbability: "+totalProbability);
	    rollsPerResource[resource] = Math.round(1.0f / Math.max(totalProbability, 0.0001f));
            //D.ebugPrintln("rollsPerResource: "+rollsPerResource[resource]);
        }
    }

    /**
     * calculate the estimates assuming that the robber is working
     *
     * @param numbers    the numbers that the player is touching
     * @param robberHex  where the robber is
     */
    public void recalculateRollsPerResource(SOCPlayerNumbers numbers, int robberHex)
    {
        D.ebugPrintln("@@@@@@@@ recalculateRollsPerResource");
        D.ebugPrintln("@@@@@@@@ numbers = " + numbers);
        D.ebugPrintln("@@@@@@@@ robberHex = " + Integer.toHexString(robberHex));
        recalc = true;

        /**
         * figure out how many resources we get per roll
         */
        for (int resource = SOCResourceConstants.MIN;
	         resource < SOCResourceConstants.MAX;
	         resource++)
        {
            D.ebugPrintln("resource: " + resource);

            float totalProbability = 0.0f;

            Enumeration numbersEnum = numbers.getNumbersForResource(resource, robberHex).elements();

            while (numbersEnum.hasMoreElements())
            {
                Integer number = (Integer) numbersEnum.nextElement();
                totalProbability += SOCNumberProbabilities.FLOAT_VALUES[number.intValue()];
            }

            D.ebugPrintln("totalProbability: " + totalProbability);
	    rollsPerResource[resource] = Math.round(1.0f / Math.max(totalProbability, 0.0001f));
            D.ebugPrintln("rollsPerResource: " + rollsPerResource[resource]);
        }
    }

    /**
     * calculate what resources this player will get on each
     * die roll
     *
     * @param numbers  the numbers that the player is touching
     */
    public void recalculateResourcesForRoll(SOCPlayerNumbers numbers)
    {
        //D.ebugPrintln("@@@@@@@@ recalculateResourcesForRoll");
        //D.ebugPrintln("@@@@@@@@ numbers = "+numbers);
        recalc = true;

        for (int diceResult = 2; diceResult <= 12; diceResult++)
        {
            Vector resources = numbers.getResourcesForNumber(diceResult);

            if (resources != null)
            {
                SOCResourceSet resourceSet;

                if (resourcesForRoll[diceResult] == null)
                {
                    resourceSet = new SOCResourceSet();
                    resourcesForRoll[diceResult] = resourceSet;
                }
                else
                {
                    resourceSet = resourcesForRoll[diceResult];
                    resourceSet.clear();
                }

                Enumeration resourcesEnum = resources.elements();

                while (resourcesEnum.hasMoreElements())
                {
                    Integer resourceInt = (Integer) resourcesEnum.nextElement();
                    resourceSet.add(1, resourceInt.intValue());
                }

                //D.ebugPrintln("### resources for "+diceResult+" = "+resourceSet);
            }
        }
    }

    /**
     * calculate what resources this player will get on each
     * die roll taking the robber into account
     *
     * @param numbers  the numbers that the player is touching
     */
    public void recalculateResourcesForRoll(SOCPlayerNumbers numbers, int robberHex)
    {
        //D.ebugPrintln("@@@@@@@@ recalculateResourcesForRoll");
        //D.ebugPrintln("@@@@@@@@ numbers = "+numbers);
        //D.ebugPrintln("@@@@@@@@ robberHex = "+Integer.toHexString(robberHex));
        recalc = true;

        for (int diceResult = 2; diceResult <= 12; diceResult++)
        {
            Vector resources = numbers.getResourcesForNumber(diceResult, robberHex);

            if (resources != null)
            {
                SOCResourceSet resourceSet;

                if (resourcesForRoll[diceResult] == null)
                {
                    resourceSet = new SOCResourceSet();
                    resourcesForRoll[diceResult] = resourceSet;
                }
                else
                {
                    resourceSet = resourcesForRoll[diceResult];
                    resourceSet.clear();
                }

                Enumeration resourcesEnum = resources.elements();

                while (resourcesEnum.hasMoreElements())
                {
                    Integer resourceInt = (Integer) resourcesEnum.nextElement();
                    resourceSet.add(1, resourceInt.intValue());
                }

                //D.ebugPrintln("### resources for "+diceResult+" = "+resourceSet);
            }
        }
    }

    /**
     * @return the rolls per resource results
     */
    public int[] getRollsPerResource()
    {
        return rollsPerResource;
    }

    /**
     * this figures out how many rolls it would take this
     * player to get the target set of resources given
     * a starting set
     *
     * @param startingResources   the starting resources
     * @param targetResources     the target resources
     * @param cutoff              throw an exception if the total speed is greater than this
     * @param ports               a list of port flags
     *
     * @return the number of rolls
     */
    protected int calculateRollsFast(SOCResourceSet startingResources, SOCResourceSet targetResources, int cutoff, boolean[] ports)
    {
	try {
	    return calculateBothFast(startingResources, targetResources, cutoff, ports).getRolls();
	} catch (CutoffExceededException e) {
	    return cutoff;
	} 
    }
																			      
    protected SOCResSetBuildTimePair calculateBothFast(SOCResourceSet startingResources, SOCResourceSet targetResources, int cutoff, boolean[] ports) throws CutoffExceededException
    {
        //D.ebugPrintln("calculateRolls");
        //D.ebugPrintln("  start: "+startingResources);
        //D.ebugPrintln("  target: "+targetResources);
        SOCResourceSet ourResources = startingResources.copy();
        int rolls = 0;

        if (!ourResources.contains(targetResources))
        {
            /**
             * do any possible trading with the bank/ports
             */
            for (int giveResource = SOCResourceConstants.MIN;
		     giveResource < SOCResourceConstants.MAX;
		     giveResource++)
            {
                /**
                 * find the ratio at which we can trade
                 */
                int tradeRatio;
		tradeRatio = (ports[giveResource] ? 2 : (ports[SOCBoard.MISC_PORT] ? 3 : 4));

                /**
                 * get the target resources
                 */
                int numTrades = (ourResources.getAmount(giveResource) - targetResources.getAmount(giveResource)) / tradeRatio;

                //D.ebugPrintln("))) ***");
                //D.ebugPrintln("))) giveResource="+giveResource);
                //D.ebugPrintln("))) tradeRatio="+tradeRatio);
                //D.ebugPrintln("))) ourResources="+ourResources);
                //D.ebugPrintln("))) targetResources="+targetResources);
                //D.ebugPrintln("))) numTrades="+numTrades);
                for (int trades = 0; trades < numTrades; trades++)
                {
                    /**
                     * find the most needed resource by looking at
                     * which of the resources we still need takes the
                     * longest to aquire
                     */
                    int mostNeededResource = -1;

                    for (int resource = SOCResourceConstants.MIN;
			 resource < SOCResourceConstants.MAX;
			 resource++)
                    {
                        if (ourResources.getAmount(resource) < targetResources.getAmount(resource))
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

                    /**
                     * make the trade
                     */

                    //D.ebugPrintln("))) want to trade "+tradeRatio+" "+giveResource+" for a "+mostNeededResource);
                    if ((mostNeededResource != -1) && (ourResources.getAmount(giveResource) >= tradeRatio))
                    {
                        //D.ebugPrintln("))) trading...");
                        ourResources.add(1, mostNeededResource);

                        if (ourResources.getAmount(giveResource) < tradeRatio)
                        {
                            System.err.println("@@@ rsrcs=" + ourResources);
                            System.err.println("@@@ tradeRatio=" + tradeRatio);
                            System.err.println("@@@ giveResource=" + giveResource);
                            System.err.println("@@@ target=" + targetResources);
                        }

                        ourResources.subtract(tradeRatio, giveResource);

                        //D.ebugPrintln("))) ourResources="+ourResources);
                    }

                    if (ourResources.contains(targetResources))
                    {
                        break;
                    }
                }

                if (ourResources.contains(targetResources))
                {
                    break;
                }
            }
        }

        while (!ourResources.contains(targetResources))
        {
            //D.ebugPrintln("roll: "+rolls);
            //D.ebugPrintln("resources: "+ourResources);
            rolls++;

            if (rolls > cutoff)
            {
                //D.ebugPrintln("startingResources="+startingResources+"\ntargetResources="+targetResources+"\ncutoff="+cutoff+"\nourResources="+ourResources);
		throw new CutoffExceededException(); // rolls=cutoff, resources not valid...
            }

            for (int resource = SOCResourceConstants.MIN;
                    resource < SOCResourceConstants.MAX; resource++)
            {
                //D.ebugPrintln("resource: "+resource);
                //D.ebugPrintln("rollsPerResource: "+rollsPerResource[resource]);

                /**
                 * get our resources for the roll
                 */
                if ((rollsPerResource[resource] == 0) || ((rolls % rollsPerResource[resource]) == 0))
                {
                    ourResources.add(1, resource);
                }
            }

            if (!ourResources.contains(targetResources))
            {
                /**
                 * do any possible trading with the bank/ports
                 */
                for (int giveResource = SOCResourceConstants.MIN;
		         giveResource < SOCResourceConstants.MAX;
                        giveResource++)
                {
                    /**
                     * find the ratio at which we can trade
                     */
                    int tradeRatio;
		    tradeRatio = (ports[giveResource] ? 2 : (ports[SOCBoard.MISC_PORT] ? 3 : 4));

                    /**
                     * get the target resources
                     */
                    int numTrades = (ourResources.getAmount(giveResource) - targetResources.getAmount(giveResource)) / tradeRatio;

                    //D.ebugPrintln("))) ***");
                    //D.ebugPrintln("))) giveResource="+giveResource);
                    //D.ebugPrintln("))) tradeRatio="+tradeRatio);
                    //D.ebugPrintln("))) ourResources="+ourResources);
                    //D.ebugPrintln("))) targetResources="+targetResources);
                    //D.ebugPrintln("))) numTrades="+numTrades);
                    for (int trades = 0; trades < numTrades; trades++)
                    {
                        /**
                         * find the most needed resource by looking at
                         * which of the resources we still need takes the
                         * longest to aquire
                         */
                        int mostNeededResource = -1;

                        for (int resource = SOCResourceConstants.MIN;
                                 resource < SOCResourceConstants.MAX;
                                resource++)
                        {
                            if (ourResources.getAmount(resource) < targetResources.getAmount(resource))
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

                        /**
                         * make the trade
                         */

                        //D.ebugPrintln("))) want to trade "+tradeRatio+" "+giveResource+" for a "+mostNeededResource);
                        if ((mostNeededResource != -1) && (ourResources.getAmount(giveResource) >= tradeRatio))
                        {
                            //D.ebugPrintln("))) trading...");
                            ourResources.add(1, mostNeededResource);

                            if (ourResources.getAmount(giveResource) < tradeRatio)
                            {
                                System.err.println("@@@ rsrcs=" + ourResources);
                                System.err.println("@@@ tradeRatio=" + tradeRatio);
                                System.err.println("@@@ giveResource=" + giveResource);
                                System.err.println("@@@ target=" + targetResources);
                            }

                            ourResources.subtract(tradeRatio, giveResource);

                            //D.ebugPrintln("))) ourResources="+ourResources);
                        }

                        if (ourResources.contains(targetResources))
                        {
                            break;
                        }
                    }

                    if (ourResources.contains(targetResources))
                    {
                        break;
                    }
                }
            }
        }

        return (new SOCResSetBuildTimePair(ourResources, rolls));
    }

    /**
     * this figures out how many rolls it would take this
     * player to get the target set of resources given
     * a starting set
     *
     * @param startingResources   the starting resources
     * @param targetResources     the target resources
     * @param cutoff              throw an exception if the total speed is greater than this
     * @param ports               a list of port flags
     *
     * @return the number of rolls
     */
    protected int calculateRollsAccurate(SOCResourceSet startingResources, SOCResourceSet targetResources, int cutoff, boolean[] ports)
	{
	    try {
		return calculateBothAccurate(startingResources, targetResources, cutoff, ports).getRolls();
	    } catch (CutoffExceededException e) {
		return cutoff;
	    }
	}

    protected SOCResSetBuildTimePair calculateBothAccurate(SOCResourceSet startingResources, SOCResourceSet targetResources, int cutoff, boolean[] ports) throws CutoffExceededException
    {
        D.ebugPrintln("calculateRollsAccurate");
        D.ebugPrintln("  start: " + startingResources);
        D.ebugPrintln("  target: " + targetResources);

        SOCResourceSet ourResources = startingResources.copy();
        int rolls = 0;
        Hashtable[] resourcesOnRoll = new Hashtable[2];
        resourcesOnRoll[0] = new Hashtable();
        resourcesOnRoll[1] = new Hashtable();

        int lastRoll = 0;
        int thisRoll = 1;

        resourcesOnRoll[lastRoll].put(ourResources, (1.0));

        boolean targetReached = ourResources.contains(targetResources);
        SOCResourceSet targetReachedResources = null;
        float targetReachedProb = (float) 0.0;

        while (!targetReached)
        {
            if (D.ebugOn)
            {
                D.ebugPrintln("roll: " + rolls);
                D.ebugPrintln("resourcesOnRoll[lastRoll]:");

                Enumeration roltEnum = resourcesOnRoll[lastRoll].keys();

                while (roltEnum.hasMoreElements())
                {
                    SOCResourceSet rs = (SOCResourceSet) roltEnum.nextElement();
                    Float prob = (Float) resourcesOnRoll[lastRoll].get(rs);
                    D.ebugPrintln("---- prob:" + prob);
                    D.ebugPrintln("---- rsrcs:" + rs);
                    D.ebugPrintln();
                }

                D.ebugPrintln("targetReachedProb: " + targetReachedProb);
                D.ebugPrintln("===================================");
            }

            rolls++;

            if (rolls > cutoff)
            {
                D.ebugPrintln("startingResources=" + startingResources + "\ntargetResources=" + targetResources + "\ncutoff=" + cutoff + "\nourResources=" + ourResources);
		throw new CutoffExceededException(); // rolls=cutoff, resources not known
            }

            //
            //  get our resources for the roll
            //
            for (int diceResult = 2; diceResult <= 12; diceResult++)
            {
                SOCResourceSet gainedResources = resourcesForRoll[diceResult];
                float diceProb = SOCNumberProbabilities.FLOAT_VALUES[diceResult];

                //
                //  add the resources that we get on this roll to 
                //  each set of resources that we got on the last
                //  roll and multiply the probabilities
                //
                Enumeration lastResourcesEnum = resourcesOnRoll[lastRoll].keys();

                while (lastResourcesEnum.hasMoreElements())
                {
                    SOCResourceSet lastResources = (SOCResourceSet) lastResourcesEnum.nextElement();
                    Float lastProb = (Float) resourcesOnRoll[lastRoll].get(lastResources);
                    SOCResourceSet newResources = lastResources.copy();
                    newResources.add(gainedResources);

                    float newProb = lastProb.floatValue() * diceProb;

                    if (!newResources.contains(targetResources))
                    {
                        //
                        // do any possible trading with the bank/ports
                        //
                        for (int giveResource = SOCResourceConstants.MIN;
			         giveResource < SOCResourceConstants.MAX;
                                 giveResource++)
                        {
                            if ((newResources.getAmount(giveResource) - targetResources.getAmount(giveResource)) > 1)
                            {
                                //
                                // find the ratio at which we can trade
                                //
                                int tradeRatio;
				tradeRatio = (ports[giveResource] ? 2 : (ports[SOCBoard.MISC_PORT] ? 3 : 4));

                                //
                                // get the target resources
                                //
                                int numTrades = (newResources.getAmount(giveResource) - targetResources.getAmount(giveResource)) / tradeRatio;

                                //D.ebugPrintln("))) ***");
                                //D.ebugPrintln("))) giveResource="+giveResource);
                                //D.ebugPrintln("))) tradeRatio="+tradeRatio);
                                //D.ebugPrintln("))) newResources="+newResources);
                                //D.ebugPrintln("))) targetResources="+targetResources);
                                //D.ebugPrintln("))) numTrades="+numTrades);
                                for (int trades = 0; trades < numTrades; trades++)
                                {
                                    // 
                                    // find the most needed resource by looking at 
                                    // which of the resources we still need takes the
                                    // longest to aquire
                                    //
                                    int mostNeededResource = -1;

                                    for (int resource = SOCResourceConstants.MIN;
                                            resource < SOCResourceConstants.MAX;
                                            resource++)
                                    {
                                        if (newResources.getAmount(resource) < targetResources.getAmount(resource))
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

                                    //
                                    // make the trade
                                    //
                                    //D.ebugPrintln("))) want to trade "+tradeRatio+" "+giveResource+" for a "+mostNeededResource);
                                    if ((mostNeededResource != -1) && (newResources.getAmount(giveResource) >= tradeRatio))
                                    {
                                        //D.ebugPrintln("))) trading...");
                                        newResources.add(1, mostNeededResource);

                                        if (newResources.getAmount(giveResource) < tradeRatio)
                                        {
                                            System.err.println("@@@ rsrcs=" + newResources);
                                            System.err.println("@@@ tradeRatio=" + tradeRatio);
                                            System.err.println("@@@ giveResource=" + giveResource);
                                            System.err.println("@@@ target=" + targetResources);
                                        }

                                        newResources.subtract(tradeRatio, giveResource);

                                        //D.ebugPrintln("))) newResources="+newResources);
                                    }

                                    if (newResources.contains(targetResources))
                                    {
                                        break;
                                    }
                                }

                                if (newResources.contains(targetResources))
                                {
                                    break;
                                }
                            }
                        }
                    }

                    //
                    //  if this set of resources is already in the list
                    //  of possible outcomes, add this probability to
                    //  that one, else just add this to the list
                    //
                    Float probFloat = (Float) resourcesOnRoll[thisRoll].get(newResources);
                    float newProb2 = newProb;

                    if (probFloat != null)
                    {
                        newProb2 = probFloat.floatValue() + newProb;
                    }

                    //
                    //  check to see if we reached our target
                    //
                    if (newResources.contains(targetResources))
                    {
                        D.ebugPrintln("-----> TARGET HIT *");
                        D.ebugPrintln("newResources: " + newResources);
                        D.ebugPrintln("newProb: " + newProb);
                        targetReachedProb += newProb;

                        if (targetReachedResources == null)
                        {
                            targetReachedResources = newResources;
                        }

                        if (targetReachedProb >= 0.5)
                        {
                            targetReached = true;
                        }
                    }
                    else
                    {
                        resourcesOnRoll[thisRoll].put(newResources, (newProb2));
                    }
                }
            }

            //
            //  copy the resourcesOnRoll[thisRoll] table to the
            //  resourcesOnRoll[lastRoll] table and clear the
            //  resourcesOnRoll[thisRoll] table
            //
            int tmp = lastRoll;
            lastRoll = thisRoll;
            thisRoll = tmp;
            resourcesOnRoll[thisRoll].clear();
        }

        if (D.ebugOn)
        {
            float probSum = (float) 0.0;
            D.ebugPrintln("**************** TARGET REACHED ************");
            D.ebugPrintln("targetReachedResources: " + targetReachedResources);
            D.ebugPrintln("targetReachedProb: " + targetReachedProb);
            D.ebugPrintln("roll: " + rolls);
            D.ebugPrintln("resourcesOnRoll[lastRoll]:");

            Enumeration roltEnum = resourcesOnRoll[lastRoll].keys();

            while (roltEnum.hasMoreElements())
            {
                SOCResourceSet rs = (SOCResourceSet) roltEnum.nextElement();
                Float prob = (Float) resourcesOnRoll[lastRoll].get(rs);
                probSum += prob.floatValue();
                D.ebugPrintln("---- prob:" + prob);
                D.ebugPrintln("---- rsrcs:" + rs);
                D.ebugPrintln();
            }

            D.ebugPrintln("probSum = " + probSum);
            D.ebugPrintln("===================================");
        }

        return (new SOCResSetBuildTimePair(targetReachedResources, rolls));
    }
}
