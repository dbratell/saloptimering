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
        return 3;
    }

    public PlacementWithCost getSolution()
    {
        return mSolution;
    }

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
        switch (column)
        {
            case 0:
                return "Benämning";
            case 1:
                return "Tentander";
            case 2:
                return "Lokal";
            default:
                throw new IllegalArgumentException("No column " + column);
        }
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

    public Class getColumnClass(int col)
    {
        switch (col)
        {
            case 0: // Name
                return String.class;
            case 1: // Size
                return Integer.class;
            case 2: // Rooms
                return String.class;
            default:
                throw new IllegalArgumentException("No column " + col);
        }
    }
}
