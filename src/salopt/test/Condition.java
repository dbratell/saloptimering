package salopt.test;

import salopt.test.Variable;
import salopt.test.SlackVariable;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 26, 2002
 * Time: 11:21:03 PM
 * To change this template use Options | File Templates.
 */
public class Condition
{
    public static final int EQUAL = 0;
//    public static final int LESS_THAN = 1;
//    public static final int GREATER_THAN = 2;
    public static final int LESS_THAN_OR_EQUAL = 3;
    public static final int GREATER_THAN_OR_EQUAL = 4;

    private final ArrayList mCoefficients;
    private double mLeftHandSide;

    public int getType()
    {
        return mCondition;
    }

    private int mCondition;
    private final String mComment;

    public Condition(String comment, int totalWidth)
    {
        mComment = comment;
        mCoefficients = new ArrayList(totalWidth);
        while (totalWidth-- > 0)
        {
            mCoefficients.add(new Double(0f));
        }
    }

    public void setCoefficient(Variable variable, double value)
    {
        setCoefficient(variable.getColumnNo(), value);
    }

    private void setCoefficient(int columnNo, double value)
    {
        mCoefficients.set(columnNo, new Double(value));
    }

    public double getCoefficient(int columNo)
    {
        return ((Double)mCoefficients.get(columNo)).doubleValue();
    }

    public void setLeftHandSide(int condition, double value)
    {
        mCondition = condition;
        mLeftHandSide = value;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < mCoefficients.size(); i++)
        {
            double coefficient = ((Double)mCoefficients.get(i)).doubleValue();
            buf.append(coefficient);
            if (coefficient != 0f)
            {
                buf.append("*x");
                buf.append(i);
            }
            buf.append(" ");
        }

        switch (mCondition)
        {
            case EQUAL:
                buf.append("=");
                break;
                //          case LESS_THAN:
//                buf.append("<");
//                break;
            case LESS_THAN_OR_EQUAL:
                buf.append("<=");
                break;
//            case GREATER_THAN:
//                buf.append(">");
//                break;
            case GREATER_THAN_OR_EQUAL:
                buf.append(">=");
                break;
            default:
                throw new IllegalStateException("Condition " + mCondition + "?");
        }
        buf.append(" ");
        buf.append(mLeftHandSide);
        buf.append(" ; ");
        buf.append(mComment);

        return buf.toString();
    }

    public double getLeftHandSide()
    {
        return mLeftHandSide;
    }

    private void invert()
    {
        for (int i = 0; i < mCoefficients.size(); i++)
        {
            double coefficient = getCoefficient(i);
            if (coefficient != 0f)
            {
                setCoefficient(i, -coefficient);
            }
        }
        if (mLeftHandSide != 0f)
        {
            mLeftHandSide = -mLeftHandSide;
        }
        switch (mCondition)
        {
            case EQUAL:
                break;
//          case GREATER_THAN:
//                mCondition = LESS_THAN;
//                break;
            case GREATER_THAN_OR_EQUAL:
                mCondition = LESS_THAN_OR_EQUAL;
                break;
//            case LESS_THAN:
//                mCondition = GREATER_THAN;
//                break;
            case LESS_THAN_OR_EQUAL:
                mCondition = GREATER_THAN_OR_EQUAL;
                break;
            default:
                throw new IllegalStateException("Illegal mCondition");

        }
    }

    public void addVariable(Variable variable)
    {
        int columnNo = variable.getColumnNo();
        while (columnNo >= mCoefficients.size())
        {
            mCoefficients.add(new Double(0f));
        }
    }

    public boolean normalize()
    {
        if (mCondition == GREATER_THAN_OR_EQUAL
//                || mCondition == GREATER_THAN
        )
        {
            invert();
        }
        switch (mCondition)
        {
            case EQUAL: // Good
                break;
            case LESS_THAN_OR_EQUAL:
                // Need slack
                return true;
            default:
                throw new IllegalStateException("Doesn't support type " + mCondition);
        }
        return false;
    }

    public void normalize(SlackVariable slack)
    {
        if (mCondition != LESS_THAN_OR_EQUAL)
        {
            throw new IllegalStateException("Doesn't support type " + mCondition);
        }
        setCoefficient(slack, 1f);
        mCondition = EQUAL;
    }
}
