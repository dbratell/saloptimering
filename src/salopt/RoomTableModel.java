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
public class RoomTableModel extends AbstractTableModel
{
    private final ArrayList mRooms;

    private final ArrayList mGroups;

    private static final Class[] mColumnClasses =
            {String.class, // Name
             Integer.class, // Size
             Float.class, // load
             Integer.class, // Base cost
             Integer.class, // per person cost
             Integer.class, // non optimal cost
             String.class, // Groups
             Float.class // Load
            };

    private static final String[] mColumnNames = {
        "Sal", // Name
        "Platser", // Size
        "Opt. fullt", // Opt load
        "Baspris", // base cost
        "/pers.", // Per person cost
        "opt.straff", // Non optimal cost
        "Grupper", // Groups
        "Utn.grad" // Load
    };

    private static final int[] mColumnRelativeWidth = new int[]{
        4, // Name
        2, // Size
        2, // Opt load
        2, // base cost
        1, // Per person cost
        2, // Non optimal cost
        6, // Groups
        2 // Load
    };

    private static final int NAME_INDEX = 0;
    private static final int SIZE_INDEX = 1;
    private static final int OPT_LOAD_INDEX = 2;
    private static final int BASE_INDEX = 3;
    private static final int PER_PERSON_INDEX = 4;
    private static final int NON_OPT_INDEX = 5;
    private static final int GROUPS_INDEX = 6;
    static final int LOAD_INDEX = 7;

    private PlacementWithCost mSolution;

    public RoomTableModel(ArrayList rooms, ArrayList groups)
    {
        mRooms = rooms;
        mGroups = groups;
    }

    public int getRowCount()
    {
        return mRooms.size();
    }

    public int getColumnCount()
    {
        return mColumnClasses.length;
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
        Room room = ((Room)mRooms.get(rowIndex));
        switch (columnIndex)
        {
            case NAME_INDEX:
                return room.getName();
            case SIZE_INDEX:
                return new Integer(room.getSize());
            case OPT_LOAD_INDEX:
                return new Float(room.getOptimalNonEmptyLoad());
            case BASE_INDEX:
                return new Integer(room.getBaseCost());
            case PER_PERSON_INDEX:
                return new Integer(room.getPerPersonCost());
            case NON_OPT_INDEX:
                return new Integer(room.getNonOptimalPersonCost());
            case GROUPS_INDEX:
            case LOAD_INDEX:
                if (mSolution == null)
                {
                    return "";
                }
                int[] groupPlacements = mSolution.mPlacement;
                if (columnIndex == GROUPS_INDEX)
                {
                    // XXX bad and slow if many groups/rooms
                    String seperator = "";
                    StringBuffer buf = new StringBuffer();
                    for (int i = 0; i < groupPlacements.length; i++)
                    {
                        if (groupPlacements[i] == rowIndex)
                        {
                            Group group = (Group)mGroups.get(i);
                            buf.append(seperator + group.getName());
                            seperator = ", ";
                        }
                    }
                    return buf.toString();
                }
                else // columnIndex == LOAD_INDEX
                {
                    // XXX bad and slow if many groups/rooms
                    int peopleInRoom = 0;
                    for (int i = 0; i < groupPlacements.length; i++)
                    {
                        if (groupPlacements[i] == rowIndex)
                        {
                            Group group = (Group)mGroups.get(i);
                            peopleInRoom += group.getSize();
                        }
                    }
                    return new Float((float)peopleInRoom / room.getSize());
                }
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
        // not the two last columns
        return col < mColumnClasses.length - 2; // Only columns from Room
    }


    public void setValueAt(Object value, int row, int col)
    {
        Room room = (Room)mRooms.get(row);
        switch (col)
        {
            case NAME_INDEX: // Name
                room.setName((String)value);
                break;
            case SIZE_INDEX: // Size
                int size = ((Integer)value).intValue();
                if (size >= 0)
                {
                    room.setSize(size);
                }
                break;
            case OPT_LOAD_INDEX: // Optimal load
                float optimalNonEmptyLoad = ((Float)value).floatValue();
                if (optimalNonEmptyLoad >= 0f && optimalNonEmptyLoad <= 1f)
                {
                    room.setOptimalNonEmptyLoad(optimalNonEmptyLoad);
                }
                break;
            case BASE_INDEX: // Base cost
                room.setBaseCost(((Integer)value).intValue());
                break;
            case PER_PERSON_INDEX: // Per person cost
                room.setPerPersonCost(((Integer)value).intValue());
                break;
            case NON_OPT_INDEX: // Over optimal cost
                room.setNonOptimalPersonCost(((Integer)value).intValue());
                break;
            default:
                throw new IllegalArgumentException("No editable column " + col);
        }
        fireTableCellUpdated(row, col);
    }

    public Class getColumnClass(int col)
    {
        if (col < 0 || col >= mColumnClasses.length)
        {
            throw new IllegalArgumentException("No column " + col);
        }

        return mColumnClasses[col];
    }

    protected void finalize() throws Throwable
    {
        if (mSolution != null)
        {
            mSolution.destroy();
        }
        super.finalize();
    }

    public int getPreferredRelativeWidth(int column)
    {
        if (column < 0 || column >= mColumnNames.length)
        {
            throw new IllegalArgumentException("No column " + column);
        }

        return mColumnRelativeWidth[column];
    }
}
