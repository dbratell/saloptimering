package salopt.test;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 28, 2002
 * Time: 4:36:07 PM
 * To change this template use Options | File Templates.
 */
public class BruteForce
{
    private final ArrayList mConditions;
    private final ArrayList mVariables;
    private final Cost mCost;
    private byte[] mCheapestSolution;
    private double mLowestCost = Double.MAX_VALUE;
    private static final double SQRTEPS = 0.000001;

    public BruteForce(ArrayList conditions, ArrayList variables, Cost cost)
    {
        mConditions = conditions;
        mVariables = variables;
        mCost = cost;

        if (mVariables.size() > 63)
        {
            throw new IllegalArgumentException("Doesn't support that size of problem");
        }
    }

    public void search()
    {
        long i;
        int largestNumber = 1 << mVariables.size();
        System.out.println(largestNumber + " number of possible solutions.");
        byte[] pattern = new byte[mVariables.size()];
        for (i = 0; i < largestNumber; i++)
        {
            longToByteArray(i, pattern);
            if (isFeasable(pattern))
            {
                double cost = calculateCost(pattern);
                if (cost < mLowestCost)
                {
                    mLowestCost = cost;
                    mCheapestSolution = pattern;
                    pattern = new byte[mVariables.size()];
                    // New solution
                    System.out.print("New solution " + arrayToString(mCheapestSolution));
                    System.out.println("  cost = " + cost);
                }
            }
        }
    }

    private static String arrayToString(byte[] bytes)
    {
        StringBuffer buf = new StringBuffer(bytes.length);
        for (int i = 0; i < bytes.length; i++)
        {
            buf.append(String.valueOf(bytes[i]));
        }
        return buf.toString();
    }

    private double calculateCost(byte[] pattern)
    {
        double cost = 0f;
        for (int i = 0; i < pattern.length; i++)
        {
            cost += pattern[i] * mCost.getCoefficient(i);
        }
        return cost;
    }

    private static void longToByteArray(long i, byte[] pattern)
    {
        for (int j = pattern.length; j > 0; j--)
        {
            pattern[j - 1] = (byte)(i % 2);
            i /= 2;
        }
    }

    private boolean isFeasable(byte[] pattern)
    {
        for (Iterator iterator = mConditions.iterator(); iterator.hasNext();)
        {
            Condition condition = (Condition)iterator.next();
            double value = 0;
            for (int i = 0; i < mVariables.size(); i++)
            {
                value += pattern[i] * condition.getCoefficient(i);
            }
            if (condition.getType() == Condition.EQUAL &&
                    Math.abs(value - condition.getLeftHandSide()) > SQRTEPS)
            {
                return false;
            }
            if (condition.getType() == Condition.LESS_THAN_OR_EQUAL &&
                    value - SQRTEPS > condition.getLeftHandSide())
            {
                return false;
            }
            if (condition.getType() == Condition.GREATER_THAN_OR_EQUAL &&
                    value + SQRTEPS < condition.getLeftHandSide())
            {
                return false;
            }
        }
        return true;
    }
}
