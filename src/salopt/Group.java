package salopt;

import salopt.test.GroupInRoomVariable;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 26, 2002
 * Time: 3:04:32 PM
 * To change this template use Options | File Templates.
 */
public class Group
{
    private String mName;
    private int mSize;
    private final ArrayList mGroupInRoomVariables = new ArrayList();

    public ArrayList getGroupInRoomVariables()
    {
        return mGroupInRoomVariables;
    }

    public Group(String name, int size)
    {
        mName = name;
        mSize = size;
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

    public void setName(String name)
    {
        mName = name;
    }

    public void setSize(int size)
    {
        mSize = size;
    }

}
