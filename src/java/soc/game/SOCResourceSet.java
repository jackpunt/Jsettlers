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


/**
 * This represents a collection of
 * clay, ore, sheep, wheat, and wood resources.
 */
public class SOCResourceSet implements Serializable, Cloneable, SOCResourceConstants
{
    public static final SOCResourceSet EMPTY_SET = new SOCResourceSet();

    /**
     * the number of resources
     */
    private int[] resources;

    /**
     * Make an empty resource set
     */
    public SOCResourceSet()
    {
        resources = new int[SOCResourceConstants.MAXPLUSONE];
        clear();
    }

    /**
     * Make a resource set with stuff in it
     *
     * @param cl  number of clay resources
     * @param or  number of ore resources
     * @param sh  number of sheep resources
     * @param wh  number of wheat resources
     * @param wo  number of wood resources
     * @param uk  number of unknown resources
     */
    public SOCResourceSet(int cl, int or, int sh, int wh, int wo, int uk)
    {
        resources = new int[SOCResourceConstants.MAXPLUSONE];

        resources[SOCResourceConstants.CLAY] = cl;
        resources[SOCResourceConstants.ORE] = or;
        resources[SOCResourceConstants.SHEEP] = sh;
        resources[SOCResourceConstants.WHEAT] = wh;
        resources[SOCResourceConstants.WOOD] = wo;
        resources[SOCResourceConstants.UNKNOWN] = uk;
    }

    /**
     * set the number of resources to zero
     */
    public void clear()
    {
        for (int i = SOCResourceConstants.MIN; i < SOCResourceConstants.MAXPLUSONE; i++)
        {
            resources[i] = 0;
        }
    }

    /**
     * @return the number of a kind of resource
     *
     * @param rtype  the type of resource
     */
    public int getAmount(int rtype)
    {
        return resources[rtype];
    }

    /**
     * @return the total number of resources
     */
    public int getTotal()
    {
        int sum = 0;

        for (int i = SOCResourceConstants.MIN; i < SOCResourceConstants.MAXPLUSONE; i++)
        {
            sum += resources[i];
        }

        return sum;
    }

    /**
     * set the amount of a resource
     *
     * @param rtype the type of resource
     * @param amt   the amount
     */
    public void setAmount(int amt, int rtype)
    {
        resources[rtype] = amt;
    }

    /**
     * add an amount to a resource
     *
     * @param rtype the type of resource
     * @param amt   the amount
     */
    public void add(int amt, int rtype)
    {
        resources[rtype] += amt;
    }

    /**
     * subtract an amount from a resource
     *
     * @param rtype the type of resource
     * @param amt   the amount
     */
    public void subtract(int amt, int rtype)
    {
        /**
         * if we're subtracting more from a resource than
         * there are of that resource, set that resource
         * to zero, and then take the difference away
         * from the UNKNOWN resources
         */
	resources[rtype] -= amt;
	if (resources[rtype] < 0) {
	    resources[UNKNOWN] += resources[rtype];
	    resources[rtype] = 0;
	}

        if (resources[UNKNOWN] < 0)
        {
	    // this happens when RobotNegotiator is looking for a trade; often wants more than anyone has:
  	    resources[UNKNOWN] = 0; // this seems right...
        }
    }

    /**
     * add an entire resource set
     *
     * @param rs  the resource set
     */
    public void add(SOCResourceSet rs)
    {
        resources[SOCResourceConstants.CLAY] += rs.getAmount(SOCResourceConstants.CLAY);
        resources[SOCResourceConstants.ORE] += rs.getAmount(SOCResourceConstants.ORE);
        resources[SOCResourceConstants.SHEEP] += rs.getAmount(SOCResourceConstants.SHEEP);
        resources[SOCResourceConstants.WHEAT] += rs.getAmount(SOCResourceConstants.WHEAT);
        resources[SOCResourceConstants.WOOD] += rs.getAmount(SOCResourceConstants.WOOD);
        resources[SOCResourceConstants.UNKNOWN] += rs.getAmount(SOCResourceConstants.UNKNOWN);
    }

    /**
     * subtract an entire resource set
     *
     * @param rs  the resource set
     */
    public void subtract(SOCResourceSet rs) {
	for (int i = MIN; i<MAXPLUSONE; i++) {
	    subtract(rs.getAmount(i),i); // TODO: !! may also remove from .UNKNOWN !!
//  	    resources[SOCResourceConstants.CLAY] -= rs.getAmount(SOCResourceConstants.CLAY);
//  	    if (resources[SOCResourceConstants.CLAY] < 0) {
//  		resources[SOCResourceConstants.CLAY] = 0;
//  	    }
	}
    }


    /**
     * @return true if each resource type in set A is >= each resource type in set B
     *
     * @param a   set A
     * @param b   set B
     */
    static public boolean gte(SOCResourceSet a, SOCResourceSet b)
    {
        return ((a.getAmount(SOCResourceConstants.CLAY) >= b.getAmount(SOCResourceConstants.CLAY))
		&& (a.getAmount(SOCResourceConstants.ORE) >= b.getAmount(SOCResourceConstants.ORE))
		&& (a.getAmount(SOCResourceConstants.SHEEP) >= b.getAmount(SOCResourceConstants.SHEEP))
		&& (a.getAmount(SOCResourceConstants.WHEAT) >= b.getAmount(SOCResourceConstants.WHEAT))
		&& (a.getAmount(SOCResourceConstants.WOOD) >= b.getAmount(SOCResourceConstants.WOOD))
		&& (a.getAmount(SOCResourceConstants.UNKNOWN) >= b.getAmount(SOCResourceConstants.UNKNOWN)));
    }

    /**
     * @return true if each resource type in set A is <= each resource type in set B
     *
     * @param a   set A
     * @param b   set B
     */
    static public boolean lte(SOCResourceSet a, SOCResourceSet b)
    {
        return ((a.getAmount(SOCResourceConstants.CLAY) <= b.getAmount(SOCResourceConstants.CLAY))
		&& (a.getAmount(SOCResourceConstants.ORE) <= b.getAmount(SOCResourceConstants.ORE))
		&& (a.getAmount(SOCResourceConstants.SHEEP) <= b.getAmount(SOCResourceConstants.SHEEP))
		&& (a.getAmount(SOCResourceConstants.WHEAT) <= b.getAmount(SOCResourceConstants.WHEAT))
		&& (a.getAmount(SOCResourceConstants.WOOD) <= b.getAmount(SOCResourceConstants.WOOD))
		&& (a.getAmount(SOCResourceConstants.UNKNOWN) <= b.getAmount(SOCResourceConstants.UNKNOWN)));
    }

    /**
     * @return a human readable form of the set
     */
    public String toString()
    {
        String s = ("|ore=" + resources[SOCResourceConstants.ORE] +
		    "|wheat=" + resources[SOCResourceConstants.WHEAT] +
		    "|sheep=" + resources[SOCResourceConstants.SHEEP] +
		    "|clay=" + resources[SOCResourceConstants.CLAY] +
		    "|wood=" + resources[SOCResourceConstants.WOOD] +
		    "|unknown=" + resources[SOCResourceConstants.UNKNOWN]);

        return s;
    }

    /**
     * @return a human readable form of the set
     */
    public String toShortString()
    {
        String s = ("Resources: " +
		    resources[SOCResourceConstants.ORE] + " " +
		    resources[SOCResourceConstants.WHEAT] + " " +
		    resources[SOCResourceConstants.SHEEP] + " " +
		    resources[SOCResourceConstants.CLAY] + " " +
		    resources[SOCResourceConstants.WOOD] + " " +
		    resources[SOCResourceConstants.UNKNOWN]);

        return s;
    }

    /**
     * @return true if sub is in this set
     *
     * @param sub  the sub set
     */
    public boolean contains(SOCResourceSet sub)
    {
        return gte(this, sub);
    }

    /**
     * @return true if the argument contains the same data
     *
     * @param anObject  the object in question
     */
    public boolean equals(Object anObject)
    {
        return
	    ((anObject instanceof SOCResourceSet)
	     && (((SOCResourceSet) anObject).getAmount(CLAY) == resources[CLAY])
	     && (((SOCResourceSet) anObject).getAmount(ORE) == resources[ORE])
	     && (((SOCResourceSet) anObject).getAmount(SHEEP) == resources[SHEEP])	// int 

	     && (((SOCResourceSet) anObject).getAmount(WHEAT) == resources[WHEAT])
	     && (((SOCResourceSet) anObject).getAmount(WOOD) == resources[WOOD])
	     && (((SOCResourceSet) anObject).getAmount(UNKNOWN) == resources[UNKNOWN]));
    }

    /**
     * @return a hashcode for this data
     */
    public int hashCode()
    {
	int hc = 0;
	// hc = this.toShortString().hashCode();
	for (int i = MIN; i < MAXPLUSONE; i++)
	    hc += (resources[i]<<(2*i));
        return hc;
    }

    /**
     * @return a copy of this resource set
     */
    public SOCResourceSet copy()
    {
        SOCResourceSet copy = new SOCResourceSet();
        copy.add(this);

        return copy;
    }

    /**
     * copy a resource set into this one
     *
     * @param set  the set to copy
     */
    public void setAmounts(SOCResourceSet rs)
    {
	for (int i = MIN; i < MAXPLUSONE; i++)
	    resources[i] = rs.getAmount(i);
    }
}
