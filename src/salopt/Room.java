package salopt;

import salopt.test.GroupInRoomVariable;
import salopt.test.RoomInUseVariable;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 26, 2002
 * Time: 3:04:32 PM
 * To change this template use Options | File Templates.
 */
public class Room
{
    private String mName;
    private int mSize;
    private final ArrayList mGroupInRoomVariables = new ArrayList();
    private int mBaseCost;
    private RoomInUseVariable mRoomInUseVariable;
    private float mOptimalNonEmptyLoad = 1.0f;
    private int mPerPersonCost = 1;
    private int mNonOptimalPersonCost = 1;

    public Room(String name, int size)
    {
        mName = name;
        mSize = size;
    }

    public RoomInUseVariable getRoomInUseVariable()
    {
        return mRoomInUseVariable;
    }

    public void setRoomInUseVariable(RoomInUseVariable roomInUseVariable)
    {
        mRoomInUseVariable = roomInUseVariable;
    }

    public int getNonOptimalPersonCost()
    {
        return mNonOptimalPersonCost;
    }

    public void setNonOptimalPersonCost(int nonOptimalPersonCost)
    {
        mNonOptimalPersonCost = nonOptimalPersonCost;
    }

    public float getOptimalNonEmptyLoad()
    {
        return mOptimalNonEmptyLoad;
    }

    public void setOptimalNonEmptyLoad(float optimalNonEmptyLoad)
    {
        mOptimalNonEmptyLoad = optimalNonEmptyLoad;
    }

    public int getPerPersonCost()
    {
        return mPerPersonCost;
    }

    public void setPerPersonCost(int perPersonCost)
    {
        mPerPersonCost = perPersonCost;
    }

    public String getName()
    {
        return mName;
    }

    public int getSize()
    {
        return mSize;
    }

    public void addGroupInRoomVariable(GroupInRoomVariable variable)
    {
        mGroupInRoomVariables.add(variable);
    }

    public ArrayList getGroupInRoomVariables()
    {
        return mGroupInRoomVariables;
    }

    public int getBaseCost()
    {
        return mBaseCost;
    }

    public void setBaseCost(int baseCost)
    {
        mBaseCost = baseCost;
    }

    public void setSize(int size)
    {
        mSize = size;
    }

    public void setName(String newName)
    {
        mName = newName;
    }
}
