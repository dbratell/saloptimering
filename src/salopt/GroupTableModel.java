package salopt;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 1, 2002
 * Time: 2:31:52 PM
 * To change this template use Options | File Templates.
 */
public class GroupTableModel extends AbstractTableModel
{
    private final ArrayList mGroups;
    private final ArrayList mRooms;

    private PlacementWithCost mSolution;

    private static final Class[] mColumnClasses =
            {String.class, // Name
             Integer.class, // Size
             String.class  //  Room
            };

    private static final String[] mColumnNames = {
        "Grupp", // Name
        "Antal", // Size
        "Bästa lokal" // Room
    };



    public GroupTableModel(ArrayList groups, ArrayList rooms)
    {
        mGroups = groups;
        mRooms = rooms;
    }

    public int getRowCount()
    {
        return mGroups.size();
    }

    public int getColumnCount()
    {
        return mColumnNames.length;
    }

// --Recycle Bin START (12/4/02 6:54 PM):
//    public PlacementWithCost getSolution()
//    {
//        return mSolution;
//    }
// --Recycle Bin STOP (12/4/02 6:54 PM)

    public void setSolution(PlacementWithCost solution)
    {
        boolean hadOldSolution = (mSolution != null);
        if (hadOldSolution)
        {
            mSolution.destroy();
        }

        mSolution = solution;

        if (hadOldSolution || solution != null)
        {
            fireTableDataChanged();
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        Group group = ((Group)mGroups.get(rowIndex));
        switch (columnIndex)
        {
            case 0:
                return group.getName();
            case 1:
                return new Integer(group.getSize());
            case 2:
                if (mSolution == null)
                {
                    return "";
                }
                Room room = (Room)mRooms.get(mSolution.mPlacement[rowIndex]);
                return room.getName();
            default:
                throw new IllegalArgumentException("No column " + columnIndex);
        }
    }

    public String getColumnName(int column)
    {
        if (column < 0 || column >= mColumnNames.length)
        {
            throw new IllegalArgumentException("No column " + column);
        }

        return mColumnNames[column];
    }

    public boolean isCellEditable(int row, int col)
    {
        return col < 2; // Only column 0 and 1
    }


    public void setValueAt(Object value, int row, int col)
    {
        Group group = (Group)mGroups.get(row);
        switch (col)
        {
            case 0: // Name
                group.setName((String)value);
                break;
            case 1: // Size
                group.setSize(((Integer)value).intValue());
                break;
            default:
                throw new IllegalArgumentException("No editable column " + col);
        }
        fireTableCellUpdated(row, col);
    }

    public Class getColumnClass(int column)
    {
        if (column < 0 || column >= mColumnClasses.length)
        {
            throw new IllegalArgumentException("No column " + column);
        }

        return mColumnClasses[column];
    }
}
