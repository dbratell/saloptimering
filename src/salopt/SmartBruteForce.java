package salopt;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 28, 2002
 * Time: 5:22:32 PM
 * To change this template use Options | File Templates.
 */
public class SmartBruteForce
{
    private final ArrayList mRooms;
    private final ArrayList mGroups;
    private int[] mCheapestSolution;
    private double mLowestCost = Double.MAX_VALUE;

    public SmartBruteForce(ArrayList rooms, ArrayList groups)
    {
        mRooms = rooms;
        mGroups = groups;
    }

    public void search()
    {
        int[] groupPlacement = new int[mGroups.size()];
        for (int i = 0; i < groupPlacement.length; i++)
        {
//            groupPlacement[i] = -1; // No room

        }

        groupPlacement[groupPlacement.length - 1] = -1;

        int noOfRooms = mRooms.size();
        while (true)
        {
            boolean overflow;
            int posToIncrease = mGroups.size() - 1;
            do
            {
                int currentRoom = groupPlacement[posToIncrease];
                if (currentRoom == noOfRooms - 1)
                {
                    // Overflow
                    groupPlacement[posToIncrease--] = 0;
                    overflow = true;
                }
                else
                {
                    groupPlacement[posToIncrease]++;
                    overflow = false;
                }

            } while (overflow && posToIncrease >= 0);

            if (posToIncrease == -1)
            {
                // Finished
                break;
            }

            if (isFeasable(groupPlacement))
            {
                double cost = calculateCost(groupPlacement);
                if (cost < mLowestCost)
                {
                    mLowestCost = cost;
                    mCheapestSolution = groupPlacement;
                    groupPlacement = new int[groupPlacement.length];
                    System.arraycopy(mCheapestSolution, 0, groupPlacement, 0, groupPlacement.length);
                    // New solution
                    System.out.print("New solution: " + arrayToString(mCheapestSolution));
                    System.out.println("  cost = " + cost);
                }
            }
        }
    }

    private double calculateCost(int[] groupPlacement)
    {
        int[] noOfPeopleInRoom = calculatePeopleInRooms(groupPlacement);

        double cost = 0f;
        for (int i = 0; i < noOfPeopleInRoom.length; i++)
        {
            int noOfPeople = noOfPeopleInRoom[i];
            if (noOfPeople > 0)
            {
                Room room = (Room)mRooms.get(i);
                cost += room.getBaseCost();
                cost += room.getPerPersonCost() * noOfPeople;
            }
        }
        return cost;
    }

    private boolean isFeasable(int[] groupPlacement)
    {
        int[] noOfPeopleInRoom = calculatePeopleInRooms(groupPlacement);
        if (noOfPeopleInRoom == null)
        {
            return false;
        }

        for (int i = 0; i < noOfPeopleInRoom.length; i++)
        {
            int noOfPeople = noOfPeopleInRoom[i];
            if (noOfPeople >= ((Room)mRooms.get(i)).getSize())
            {
                return false;
            }
        }
        return true;
    }

    private int[] calculatePeopleInRooms(int[] groupPlacement)
    {
        int[] noOfPeopleInRoom = new int[mRooms.size()];
        for (int i = 0; i < groupPlacement.length; i++)
        {
            int roomNo = groupPlacement[i];
            if (roomNo == -1)
            {
                // No room placement. Give up.
                return null;
            }
            noOfPeopleInRoom[roomNo] += ((Group)mGroups.get(i)).getSize();
        }
        return noOfPeopleInRoom;
    }

    private String arrayToString(int[] solution)
    {
        StringBuffer buf = new StringBuffer();
        String seperator = "";
        for (int i = 0; i < solution.length; i++)
        {
            int roomNo = solution[i];
            String groupName = ((Group)mGroups.get(i)).getName();
            String roomName = ((Room)mRooms.get(roomNo)).getName();
            buf.append(seperator + groupName + " in " + roomName);
            seperator = ", ";
        }
        return buf.toString();
    }
}
