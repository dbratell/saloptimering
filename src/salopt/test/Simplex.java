package salopt.test;

import jmat.MatlabSyntax;
import jmat.data.Matrix;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 27, 2002
 * Time: 3:00:11 PM
 * To change this template use Options | File Templates.
 */
public class Simplex
{
    private final Matrix mA;
    private final Matrix mb;
    private final Matrix mc;
    private static final double SQRTEPS = 0.000001;

    private int[] mOptimalBase;
    private Matrix mOptimalValues;
    private double mOptimalCost;

    /**
     *
     * @param A
     * @param b
     * @param c
     */
    public Simplex(Matrix A, Matrix b, Matrix c)
    {
        mA = A;
        mb = b;
        mc = c;
    }

    /**
     *
     * @return
     */
    public int[] selectStartBase()
    {
        System.out.println("mA.rank() = " + mA.rank());
        int AWidth = mA.getColumnDimension();
        int AHeight = mA.getRowDimension();

        // Adding artifical variables
        Matrix Aa = mA.mergeColumns(MatlabSyntax.eye(AHeight, AHeight));
        Matrix c = MatlabSyntax.zeros(1, AWidth).mergeColumns(MatlabSyntax.ones(1, AHeight));
        // Start base
        //       int[] allvariables = range(0, AWidth + AHeight - 1);
        int[] B = range(AWidth, AWidth + AHeight - 1);
//        int[] D = setDiff(allvariables, B);
        int[] D = range(0, AWidth - 1);
        Matrix bInBase /* = mb */;
        while (true)
        {
            printCurrentSolution(Aa, B);
            System.err.print("." + Aa.rank());
            Matrix AB = Aa.getColumns(B);
//            System.out.println("AB = \n" + AB);
            Matrix AD = Aa.getColumns(D);
            Matrix cB = c.getColumns(B);
            Matrix cD = c.getColumns(D);

            int incomingBaseIndexInD = selectIncomingBase(AB, AD, cB, cD);

            if (incomingBaseIndexInD == -1)
            {
                Matrix optimalvalues = MatlabSyntax.solve(AB, mb);
                double optimalcost = MatlabSyntax.times(cB, optimalvalues).get(0, 0);
                if (optimalcost > 0)
                {
                    throw new IllegalArgumentException("Can not reduce to a " +
                                                       "solution without " +
                                                       "artificial variables. " +
                                                       "The problem has no " +
                                                       "feasible solutions");
                }

                if (max(B) > AWidth)
                {
                    throw new IllegalArgumentException("An artificial variable " +
                                                       "with the value 0 is in " +
                                                       "the base. Must be " +
                                                       "replaced by pivoting");
                }

                int[] startbase = B;
                return startbase;
            }

            // Now we have selected an incoming base. Let's calculate which
            // base will be going out.
            Matrix incomingColumn = AD.getColumn(incomingBaseIndexInD);
            Matrix incomingColumnInBase = MatlabSyntax.solve(AB, incomingColumn);
            bInBase = MatlabSyntax.solve(AB, mb);
            int outgoingBaseIndexInB = selectOutgoingBase(incomingColumnInBase, bInBase);
            int incomingbase = D[incomingBaseIndexInD];
            int outgoingbase = B[outgoingBaseIndexInB];

            System.out.print("incomingbase = " + incomingbase);
            System.out.println(", outgoingbase = " + outgoingbase);

            if (outgoingbase > AWidth)
            {
                // The outgoing base variable is an artifical variable
                // Remove it completely to simplify the rest of the
                // calculations
                // Remove it from c and Aa.
                // This will cause renumbering of B and D
                B[outgoingBaseIndexInB] = incomingbase;
                // remove it from D and Aa
                D = removeElementWithIndex(D, incomingBaseIndexInD);
                Aa = deleteColumn(Aa, outgoingbase); // buggy in JMat
                // Renumber B and D
                decreaseIfLargerThan(B, outgoingbase);
                decreaseIfLargerThan(D, outgoingbase);
            }
            else
            {
                // Not an artificial variable
                // Swap to the new base by replacing the outgoing base with the
                // incoming in B and the incoming with the outgoing in D
                B[outgoingBaseIndexInB] = incomingbase;
                D[incomingBaseIndexInD] = outgoingbase;
            }

            // Just for fun - not needed
            Arrays.sort(B);
            Arrays.sort(D);
        }
    }

    private void printCurrentSolution(Matrix A, int[] variables)
    {
        Matrix baseA = A.getColumns(variables);
        Matrix bInBase = MatlabSyntax.solve(baseA, mb);
        String seperator = "";
        for (int i = 0; i < variables.length; i++)
        {
            int variable = variables[i];
            System.out.print(seperator + "x" + variable + "=" + bInBase.get(i, 0));
            seperator = ", ";
        }
        System.out.println("");

        Matrix x = new Matrix(A.getColumnDimension(), 1);
        for (int i = 0; i < variables.length; i++)
        {
            int variable = variables[i];
            x.set(variable, 0, bInBase.get(i, 0));
        }

        Matrix Ax = A.times(x);
        System.out.println("Ax = \n" + Ax);

    }

    /**
     *
     * @param incomingBaseInOldBase
     * @param bInOldBase
     * @return
     */
    private static int selectOutgoingBase(Matrix incomingBaseInOldBase, Matrix bInOldBase)
    {
        // First some sanity checks
        //  if isempty(find(incomingBaseInOldBase ~= 0))
        //     error('A null base?');
        // end
        // if ~isempty(find(bInOldBase<-sqrt(eps)))
        //    error('Negative values in the base. Not feasible');
        // end

        // Now we have selected an incoming base. Let's calculate which
        // base will be going out.
        int[] posRowsInIncomingBaseColumn =
                findMatrixToInts(MatlabSyntax.find(incomingBaseInOldBase, ">", SQRTEPS));
        Matrix posIncomingBase = incomingBaseInOldBase.getRows(posRowsInIncomingBaseColumn);
        Matrix matchingRowsInB = bInOldBase.getRows(posRowsInIncomingBaseColumn);
        Matrix ratios = matchingRowsInB.ebeDivide(posIncomingBase);

        //  Must find smallest positive (>0) ratio and take that as the outgoing base
        // If all ratios are <=0, than the solution is unlimited
        int[] posIndicesInRatios = findMatrixToInts(MatlabSyntax.find(ratios, ">", -SQRTEPS));
        if (posIndicesInRatios.length == 0)
        {
            throw new IllegalArgumentException("Unlimited problem. How small " +
                                               "do you want the cost?");
        }

        Matrix posRatios = ratios.getRows(posIndicesInRatios);
        int minRatioIndexInPosIndexesInRatios = 0;
        double minRatio = posRatios.get(0, 0);
        int rowDimension = posRatios.getRowDimension();
        for (int i = 1; i < rowDimension; i++)
        {
            double value = posRatios.get(i, 0);
            if (minRatio > value)
            {
                minRatioIndexInPosIndexesInRatios = i;
                minRatio = value;
            }
        }

        if (minRatio < SQRTEPS)
        {
            // Zero ratio - just switch variables
        }

        int minPosRatioIndexInRatios = posIndicesInRatios[minRatioIndexInPosIndexesInRatios];

        int outgoingIndex = posRowsInIncomingBaseColumn[minPosRatioIndexInRatios];
        return outgoingIndex;
    }

    /**
     *
     * @param matrix
     * @return
     */
    private static int[] findMatrixToInts(Matrix matrix)
    {
        // Look at the first column
        int[] ints = new int[matrix.getRowDimension()];
        for (int i = 0; i < ints.length; i++)
        {
            ints[i] = (int)(matrix.get(i, 0) + 0.02) - 1;

        }
        return ints;
    }


    /**
     *
     * @param AB
     * @param AD
     * @param cB
     * @param cD
     * @return will return -1 if the current solution is optimal
     */
    private static int selectIncomingBase(Matrix AB, Matrix AD, Matrix cB, Matrix cD)
    {
        //  will return -1 if the current solution is optimal

        Matrix piT = cB.divide(AB);
        Matrix reducedvalues = cD.minus(piT.times(AD));

        // A negative value means that the current solution isn't optimal
        // select the most negative value in reducedvalues as the incoming base
        int minvalueindexinD = 0;
        double minvalue = reducedvalues.get(0, 0);
        int columnDimension = reducedvalues.getColumnDimension();
        for (int i = 1; i < columnDimension; i++)
        {
            if (minvalue > reducedvalues.get(0, i))
            {
                minvalueindexinD = i;
                minvalue = reducedvalues.get(0, i);
            }
        }

        if (minvalue >= 0)
        {
            return -1;
        }

        return minvalueindexinD;
    }

    /**
     *
     * @param ints
     * @param limit
     */
    private static void decreaseIfLargerThan(int[] ints, int limit)
    {
        for (int i = 0; i < ints.length; i++)
        {
            int anInt = ints[i];
            if (anInt > limit)
            {
                ints[i] = anInt - 1;
            }
        }
    }

    /**
     *
     * @param ints
     * @param index
     * @return
     */
    private static int[] removeElementWithIndex(int[] ints, int index)
    {
        int[] newArray = new int[ints.length - 1];
        System.arraycopy(ints, 0, newArray, 0, index);
        System.arraycopy(ints, index + 1, newArray, index, ints.length - index - 1);
        return newArray;
    }

    /**
     *
     * @param values
     * @return
     */
    private static int max(int[] values)
    {
        int max = values[0];
        for (int i = 1; i < values.length; i++)
        {
            int value = values[i];
            if (value > max)
            {
                max = value;
            }
        }
        return max;
    }

    /**
     *
     * @param startBase
     */
    public void optimize(int[] startBase)
    {
        // Performs the simplex method on a problem with the objective to
        // minimize cx under the condition Ax = b.

        int Awidth = mA.getColumnDimension();

        int[] B = startBase;
        int[] allvariables = range(0, Awidth - 1);
        int[] D = setDiff(allvariables, B);

        while (true)
        {
            Matrix AB = mA.getColumns(B);
            Matrix AD = mA.getColumns(D);

            Matrix cB = mc.getColumns(B);
            Matrix cD = mc.getColumns(D);

            int incomingBaseIndexInD = selectIncomingBase(AB, AD, cB, cD);

            if (incomingBaseIndexInD == -1)
            {
                mOptimalBase = B;
                mOptimalValues = MatlabSyntax.solve(AB, mb);
                mOptimalCost = cB.times(mOptimalValues).get(0, 0);
                return;
            }

            // Now we have selected an incoming base. Let's calculate which
            // base will be going out.
            Matrix incomingBaseColumn = AD.getColumn(incomingBaseIndexInD);
            Matrix incomingBaseColumnInBase = MatlabSyntax.solve(AB, incomingBaseColumn);
            Matrix bInBase = MatlabSyntax.solve(AB, mb);

            int outgoingBaseIndexInB = selectOutgoingBase(incomingBaseColumnInBase, bInBase);

            // Swap to the new base by replacing the outgoing base with the
            // incoming in B and the incoming with the outgoing in D
            int outgoingBase = B[outgoingBaseIndexInB];
            int incomingBase = D[incomingBaseIndexInD];
            B[outgoingBaseIndexInB] = incomingBase;
            D[incomingBaseIndexInD] = outgoingBase;
        }
    }

    /**
     *
     * @param bigSet
     * @param setToSubtract
     * @return
     */
    private static int[] setDiff(int[] bigSet, int[] setToSubtract)
    {
        Set set = new HashSet();
        for (int i = 0; i < bigSet.length; i++)
        {
            set.add(new Integer(bigSet[i]));
        }

        for (int i = 0; i < setToSubtract.length; i++)
        {
            set.remove(new Integer(setToSubtract[i]));
        }
        int[] m = new int[set.size()];
        int i = 0;
        for (Iterator iterator = set.iterator(); iterator.hasNext();)
        {
            Integer aInteger = (Integer)iterator.next();
            int value = aInteger.intValue();
            m[i++] = value;
        }
        Arrays.sort(m);
        return m;
    }

    /**
     *
     * @param start
     * @param end
     * @return
     */
    private static int[] range(int start, int end)
    {
        int[] m = new int[end - start + 1];
        for (int i = start; i <= end; i++)
        {
            m[i - start] = i;
        }
        return m;
    }


    /**
     * This is buggy in JMat
     * @param inMatrix
     * @param J
     * @return
     */
    private static Matrix deleteColumn(Matrix inMatrix, int J)
    {
        int n = inMatrix.getColumnDimension();
        if (J > n)
        {
            throw new IllegalArgumentException("Matrix Columns dimensions must be > " + J);
        }
        int m = inMatrix.getRowDimension();
        Matrix outMatrix = new Matrix(m, n - 1);
        int j = 0;
        for (int j2 = 0; j2 < n - 1; j2++)
        {
            if (j2 == J)
            {
                j++; // Skip the column
            }

            for (int i = 0; i < m; i++)
            {
                outMatrix.set(i, j2, inMatrix.get(i, j));
            }
            j++;
        }
        return outMatrix;
    }

    public int[] getOptimalBase()
    {
        return mOptimalBase;
    }

    public Matrix getOptimalValues()
    {
        return mOptimalValues;
    }

    public double getOptimalCost()
    {
        return mOptimalCost;
    }

/*
    function startbase = simplexCreatingStartbase(A, b)
    % Performs the simplex method on a problem with the objective to minimize
    % cx under the condition Ax = b.

    Awidth = size(A,2);
    Aheight = size(A,1);

    % Adding artifical variables
    Aa = [A speye(Aheight)];
    B = (Awidth+1):(Awidth+Aheight);
    c = [zeros(1, Awidth) ones(1, Aheight)];

    allvariables = 1:(Awidth+Aheight);
    D = setdiff(allvariables, B);

    while 1
        AB = Aa(:,B);
        AD = Aa(:,D);
        cB = c(:,B);
    cD = c(:,D);

    incomingBaseIndexInD = selectIncomingBase(AB, AD, cB, cD);

    if incomingBaseIndexInD == -1
        optimalvalues = AB\b;
        optimalcost = cB*optimalvalues;
        if optimalcost > 0
            error('Can not reduce to a solution without artificial variables. The problem has no feasible solutions');
        end
        if max(B) > Awidth
            error('An artificial variable with the value 0 is in the base. Must be replaced by pivoting');
        end
        startbase = B;
        return
    end

    % Now we have selected an incoming base. Let's calculate which
    % base will be going out.
    incomingbasecolumn = AD(:,incomingBaseIndexInD);
    incomingbasecolumninbase = AB\incomingbasecolumn;
    binbase = AB\b;

    outgoingBaseIndexInB = selectOutgoingBase(incomingbasecolumninbase, binbase);

    incomingbase = D(incomingBaseIndexInD);
    outgoingbase = B(outgoingBaseIndexInB);

    if outgoingbase > Awidth
        % The outgoing base variable is an artifical variable
        % Remove it completely to simplify the rest of the
        % calculations
        % Remove it from c and Aa.
        % This will cause renumbering of B and D
        B(outgoingBaseIndexInB) = incomingbase;
        % remove it from D and Aa
        D(incomingBaseIndexInD) = [];
        Aa(:,outgoingbase) = [];
        % Renumber B and D
        toolargeindices = find(B>outgoingbase);
        B(toolargeindices) = B(toolargeindices)-1;
        toolargeindices = find(D>outgoingbase);
        D(toolargeindices) = D(toolargeindices)-1;
    else
        % Not an artificial variable
        % Swap to the new base by replacing the outgoing base with the
        % incoming in B and the incoming with the outgoing in D
        B(outgoingBaseIndexInB) = incomingbase;
        D(incomingBaseIndexInD) = outgoingbase;
    end
end
*/

/*
    function index = selectIncomingBase(AB, AD, cB, cD)
    % will return -1 if the current solution is optimal

        piT = cB/AB;
        reducedvalues = cD - piT*AD;

        % A negative value means that the current solution isn't optimal
        % select the most negative value in reducedvalues as the incoming base
        [minvalue, minvalueindexinD] = min(reducedvalues);
        if minvalue >= 0
            index = -1;
            return;
        end

        index = minvalueindexinD;
        */
    /*
        function outgoingIndex = selectOutgoingBase(incomingBaseInOldBase, bInOldBase)

    % First some sanity checks
    % if isempty(find(incomingBaseInOldBase ~= 0))
    %     error('A null base?');
    % end
    % if ~isempty(find(bInOldBase<-sqrt(eps)))
    %     error('Negative values in the base. Not feasible');
    % end

    % Now we have selected an incoming base. Let's calculate which
    % base will be going out.
    posrowsinincomingbasecolumn = find(incomingBaseInOldBase > sqrt(eps)); % > eps? > sqrt(eps)?
    ratios = bInOldBase(posrowsinincomingbasecolumn) ./ incomingBaseInOldBase(posrowsinincomingbasecolumn);

    % Must find smallest positive (>0) ratio and take that as the outgoing base
    % If all ratios are <=0, than the solution is unlimited
    posindexesinratios = find(ratios > -sqrt(eps)); % > eps? > sqrt(eps)?
    if isempty(posindexesinratios)
        error('Unlimited problem. How small do you want the cost?');
    end
    posratios = ratios(posindexesinratios); % indices, not indexes!
    [minratio, minratioindexinposindexesinratios] = min(posratios);

    minposratioindexinratios = posindexesinratios(minratioindexinposindexesinratios);

    outgoingIndex = posrowsinincomingbasecolumn(minposratioindexinratios);

*/


}
