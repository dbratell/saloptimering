package salopt;

/**
 * A sorter for TableModels. The sorter has a mModel (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy
 * the data in the TableModel, instead it maintains an array of
 * integers which it keeps the same size as the number of rows in its
 * mModel. When the mModel changes it notifies the sorter that something
 * has changed eg. "rowsAdded" so that its internal array of integers
 * can be reallocated. As requests are made of the sorter (like
 * getValueAt(row, col) it redirects them to its mModel via the mapping
 * array. That way the TableSorter appears to hold another copy of the table
 * with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison
 * function returns 0 to denote that they are equivalent.
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

public class TableSorter extends TableMap
{
    private int[] mIndexes;
    private int mSortingColumn;
    private boolean mAscending = true;
    private static final boolean SORT_STATISTICS = false;
    private int mNoOfCompares;

// --Recycle Bin START (12/2/02 2:05 PM):
//    public TableSorter()
//    {
//        mIndexes = new int[0]; // for consistency
//    }
// --Recycle Bin STOP (12/2/02 2:05 PM)

    public TableSorter(TableModel model)
    {
        setModel(model);
    }

    void setModel(TableModel model)
    {
        super.setModel(model);
        reallocateIndexes();
    }

    private int compareRowsByColumn(int row1, int row2, int column)
    {
        Class type = mModel.getColumnClass(column);
        TableModel data = mModel;

        // Check for nulls.

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null, return 0.
        if (o1 == null && o2 == null)
        {
            return 0;
        }
        else if (o1 == null)
        { // Define null less than everything.
            return -1;
        }
        else if (o2 == null)
        {
            return 1;
        }

        /*
         * We copy all returned values from the getValue call in case
         * an optimised mModel is reusing one object to return many
         * values.  The Number subclasses in the JDK are immutable and
         * so will not be used in this way but other subclasses of
         * Number might want to do this to save space and avoid
         * unnecessary heap allocation.
         */

        if (type.getSuperclass() == java.lang.Number.class)
        {
            Number n1 = (Number)data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number)data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            if (d1 < d2)
            {
                return -1;
            }
            else if (d1 > d2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == java.util.Date.class)
        {
            Date d1 = (Date)data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date)data.getValueAt(row2, column);
            long n2 = d2.getTime();

            if (n1 < n2)
            {
                return -1;
            }
            else if (n1 > n2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == String.class)
        {
            String s1 = (String)data.getValueAt(row1, column);
            String s2 = (String)data.getValueAt(row2, column);
            int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            }
            else if (result > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == Boolean.class)
        {
            Boolean bool1 = (Boolean)data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean)data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2)
            {
                return 0;
            }
            else if (b1)
            { // Define false < true
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            }
            else if (result > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }

    private int compare(int row1, int row2)
    {
        if (SORT_STATISTICS)
        {
            mNoOfCompares++;
        }

        int column = mSortingColumn;
        int result = compareRowsByColumn(row1, row2, column);
        return mAscending ? result : -result;
    }

    private void reallocateIndexes()
    {
        int rowCount = mModel.getRowCount();

        // Set up a new array of mIndexes with the right number of elements
        // for the new data mModel.
        mIndexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++)
        {
            mIndexes[row] = row;
        }
    }

    public void tableChanged(TableModelEvent e)
    {
        //System.out.println("Sorter: tableChanged");
        reallocateIndexes();

        super.tableChanged(e);
    }

    private void checkModel()
    {
        if (mIndexes.length != mModel.getRowCount())
        {
            System.err.println("Sorter not informed of a change in mModel.");
        }
    }

    private void sort(/*Object sender */)
    {
        checkModel();

        if (SORT_STATISTICS)
        {
            mNoOfCompares = 0;
        }
        // n2sort();
        // qsort(0, mIndexes.length-1);
        shuttlesort((int[])mIndexes.clone(), mIndexes, 0, mIndexes.length);
        if (SORT_STATISTICS)
        {
            System.out.println("Compares: "+mNoOfCompares);
        }
    }

// --Recycle Bin START (12/2/02 2:05 PM):
//    public void n2sort()
//    {
//        for (int i = 0; i < getRowCount(); i++)
//        {
//            for (int j = i + 1; j < getRowCount(); j++)
//            {
//                if (compare(mIndexes[i], mIndexes[j]) == -1)
//                {
//                    swap(i, j);
//                }
//            }
//        }
//    }
// --Recycle Bin STOP (12/2/02 2:05 PM)

    // This is a home-grown implementation which we have not had time
    // to research - it may perform poorly in some circumstances. It
    // requires twice the space of an in-place algorithm and makes
    // NlogN assigments shuttling the values between the two
    // arrays. The number of mNoOfCompares appears to vary between N-1 and
    // NlogN depending on the initial order but the main reason for
    // using it here is that, unlike qsort, it is stable.
    private void shuttlesort(int from[], int to[], int low, int high)
    {
        if (high - low < 2)
        {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /* This is an optional short-cut; at each recursive call,
        check to see if the elements in this subset are already
        ordered.  If so, no further comparisons are needed; the
        sub-array can just be copied.  The array must be copied rather
        than assigned otherwise sister calls in the recursion might
        get out of sinc.  When the number of elements is three they
        are partitioned so that the first set, [low, mid), has one
        element and and the second, [mid, high), has two. We skip the
        optimisation when the number of elements is three or less as
        the first compare in the normal merge will produce the same
        sequence of steps. This optimisation seems to be worthwhile
        for partially ordered lists but some analysis is needed to
        find out how the performance drops to Nlog(N) as the initial
        order diminishes - it may drop very quickly.  */

        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0)
        {
            for (int i = low; i < high; i++)
            {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.

        for (int i = low; i < high; i++)
        {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0))
            {
                to[i] = from[p++];
            }
            else
            {
                to[i] = from[q++];
            }
        }
    }

// --Recycle Bin START (12/2/02 2:05 PM):
//    public void swap(int i, int j)
//    {
//        int tmp = mIndexes[i];
//        mIndexes[i] = mIndexes[j];
//        mIndexes[j] = tmp;
//    }
// --Recycle Bin STOP (12/2/02 2:05 PM)

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "mIndexes".

    public Object getValueAt(int aRow, int aColumn)
    {
        checkModel();
        return mModel.getValueAt(mIndexes[aRow], aColumn);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        checkModel();
        mModel.setValueAt(aValue, mIndexes[aRow], aColumn);
    }

// --Recycle Bin START (12/2/02 2:05 PM):
//    public void sortByColumn(int column)
//    {
//        sortByColumn(column, true);
//    }
// --Recycle Bin STOP (12/2/02 2:05 PM)

    private void sortByColumn(int column, boolean ascending)
    {
        mAscending = ascending;
        mSortingColumn = column;
        sort(/*this*/);
        super.tableChanged(new TableModelEvent(this));
    }

    // There is no-where else to put this.
    // Add a mouse listener to the Table to trigger a table sort
    // when a column heading is clicked in the JTable.
    public void addMouseListenerToHeaderInTable(JTable table)
    {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1)
                {
                    //System.out.println("Sorting ...");
                    boolean shiftPressed = (e.getModifiers() & InputEvent.SHIFT_MASK) != 0;
                    boolean ascending;
                    if (shiftPressed)
                    {
                        ascending = false;
                    }
                    else
                    // Also switch order if it's the second click on the same
                    // column
                    if (column == mSortingColumn)
                    {
                        ascending = !mAscending;
                    }
                    else
                    {
                        ascending = true;
                    }

                    sorter.sortByColumn(column, ascending);
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }

    public int translateOuterRowNumberToInnerRowNumber(int outerRow)
    {
        checkModel();
        return mIndexes[outerRow];
    }
}
