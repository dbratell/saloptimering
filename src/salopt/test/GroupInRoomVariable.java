package salopt.test;

import salopt.Group;
import salopt.Room;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 26, 2002
 * Time: 3:20:37 PM
 * To change this template use Options | File Templates.
 */
public class GroupInRoomVariable extends Variable
{
    public Group getGroup()
    {
        return mGroup;
    }

    public Room getRoom()
    {
        return mRoom;
    }

    private final Group mGroup;
    private final Room mRoom;

    public GroupInRoomVariable(Group group, Room room)
    {
        mGroup = group;
        mRoom = room;

        mDescription = "Group '" + group.getName() + "' in room '" + room.getName() + "'";
    }
}
