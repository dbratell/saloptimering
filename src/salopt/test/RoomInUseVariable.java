package salopt.test;

import salopt.test.Variable;
import salopt.Room;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 27, 2002
 * Time: 12:17:58 PM
 * To change this template use Options | File Templates.
 */
public class RoomInUseVariable extends Variable
{
    private final Room mRoom;

    public RoomInUseVariable(Room room)
    {
        mRoom = room;
        mDescription = room.getName() + " in use";
    }

    public Room getRoom()
    {
        return mRoom;
    }
}
