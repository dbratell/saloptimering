package salopt.test;

import salopt.test.Variable;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 27, 2002
 * Time: 12:33:07 PM
 * To change this template use Options | File Templates.
 */
public class Cost
{
    private final double[] mCoefficient;

    public Cost(int noOfVariables)
    {
        mCoefficient = new double[noOfVariables];
    }

    public void setCoefficient(Variable variable, double value)
    {
        mCoefficient[variable.getColumnNo()] = value;
    }

    public double getCoefficient(int columnNo)
    {
        return mCoefficient[columnNo];
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        boolean hasWritten = false;
        for (int i = 0; i < mCoefficient.length; i++)
        {
            double coefficient = mCoefficient[i];
            if (coefficient != 0f)
            {
                if (hasWritten)
                {
                    buf.append(" + ");
                }
                buf.append(coefficient);
                buf.append("*x");
                buf.append(i);
                hasWritten = true;
            }
        }

        return buf.toString();
    }
}
