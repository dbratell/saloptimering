package salopt.test;

import salopt.Room;
import salopt.Group;

import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 2, 2002
 * Time: 2:20:46 PM
 * To change this template use Options | File Templates.
 */
public class PrototypeSimplex
{
    private final ArrayList mRooms = new ArrayList();
    private final ArrayList mGroups = new ArrayList();
    private final ArrayList mVariables = new ArrayList();
    private final ArrayList mConditions = new ArrayList();
    private Cost mCost;

    private static final boolean TRY_SIMPLEX = false;
    private static final boolean TRY_BRUTE_FORCE = false;
    private static final boolean USE_ROOM_BOOKING = false;

    public static void main(String[] args)
    {
        if (Calendar.getInstance().get(GregorianCalendar.YEAR) > 2002)
        {
            throw new IllegalStateException("Update prototype. " +
                                            "This is not to be used after 2002.");
        }
        new PrototypeSimplex().execute();
    }


    private void execute()
    {
        addRoom("A210", 80 / 2, 0.82f, 18000, 8, 1500);
        addRoom("C203", 144 / 2, 0.9f, 114400, 14, 1500);
        addRoom("D207", 130 / 2, 0.9f, 113000, 13, 1500);
        addRoom("D209", 97 / 2, 0.85f, 19700, 10, 1500);
        addRoom("D211", 97 / 2, 0.85f, 19700, 10, 1500);
        addRoom("E308", 65 / 2, 0.8f, 16500, 7, 1500);
        addRoom("E310", 102 / 2, 0.9f, 110200, 10, 1500);
        addRoom("E408", 65 / 2, 0.8f, 16500, 7, 1500);
        addRoom("M204", 250 / 2, 0.9f, 125000 * 2, 25, 1500);
        addRoom("M202", 200 / 2, 0.9f, 120200 * 2, 25, 1500);


        addGroup("PIA02", 2);
        addGroup("SSK15", 5);
        addGroup("SSK16a", 50);
        addGroup("SSK17a", 50);
        addGroup("SSK17b", 50);
        addGroup("SSK19a", 50);
        addGroup("SSK19b", 50);
        addGroup("SSK18b", 60);
        addGroup("AN", 10);


        if (TRY_SIMPLEX || TRY_BRUTE_FORCE)
        {
            generateVariables();
            generateConditions();
            if (TRY_SIMPLEX)
            {
                normalizeConditions();
            }
            for (Iterator iterator = mConditions.iterator(); iterator.hasNext();)
            {
                Condition condition = (Condition)iterator.next();
                System.out.println(condition.toString());
            }
            generateCostCalculation();
            System.out.println(mCost.toString());
            int number = 0;
            for (Iterator iterator = mVariables.iterator(); iterator.hasNext();)
            {
                Variable variable = (Variable)iterator.next();
                System.out.println("x" + number + ": " + variable.getDescription());
                number++;
            }

        }

        if (TRY_SIMPLEX)
        {
            trySimplex();
        }
        if (TRY_BRUTE_FORCE)
        {
            tryBruteForce();
        }
    }

    private void tryBruteForce()
    {
        BruteForce brute = new BruteForce(mConditions, mVariables, mCost);
        long startTime = System.currentTimeMillis();
        brute.search();
        System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    private void trySimplex()
    {
        /*
        Matrix A = buildA();
        Matrix b = buildB();
        Matrix c = buildc();

        Simplex simplex = new Simplex(A, b, c);
        int[] startBase = simplex.selectStartBase();
        System.out.print("startBase variables = ");
        for (int i = 0; i < startBase.length; i++)
        {
            System.out.print(" x"+startBase[i]);
        }
        System.out.println("");

        simplex.optimize(startBase);
        Matrix optimalValues = simplex.getOptimalValues();
        System.out.println("optimalValues = " + optimalValues);
        int[] optimalVariables = simplex.getOptimalBase();
        System.out.println("optimalVariables = " + optimalVariables);
        double optimalCost = simplex.getOptimalCost();
        System.out.println("optimalCost = " + optimalCost);
        */
    }

/*    private Matrix buildc()
    {
        int size = mVariables.size();
        Matrix c = new Matrix(1, size);
        for (int i = 0; i < size; i++)
        {
            c.set(0, i, mCost.getCoefficient(i));
        }
        return c;
    }
    */

    /*
    private Matrix buildB()
    {
        int noOfConditions = mConditions.size();
        Matrix b = new Matrix(noOfConditions, 1);
        for(int row = 0; row < noOfConditions; row++)
        {
            Condition condition = (Condition)mConditions.get(row);
            b.set(row, 0, condition.getLeftHandSide());
        }
        return b;
    }

    */

    /*
    private Matrix buildA()
    {
        int noOfVariables = mVariables.size();
        int noOfConditions = mConditions.size();
        Matrix A = new Matrix(noOfConditions, noOfVariables);
        for(int row = 0; row < noOfConditions; row++)
        {
            Condition condition = (Condition)mConditions.get(row);
            for(int column = 0; column < noOfVariables; column++)
            {
                A.set(row, column, condition.getCoefficient(column));
            }
        }
        return A;
    }
*/
    private void normalizeConditions()
    {
        for (Iterator iterator = mConditions.iterator(); iterator.hasNext();)
        {
            Condition condition = (Condition)iterator.next();
            boolean needSlack = condition.normalize();
            if (needSlack)
            {
                SlackVariable slack = new SlackVariable(condition);
                addVariable(slack);
                condition.normalize(slack);
            }
        }
    }


    private void generateCostCalculation()
    {
        int noOfVariables = mVariables.size();
        mCost = new Cost(noOfVariables);
        // A used room has a mBase cost
        for (Iterator iterator = mRooms.iterator(); iterator.hasNext();)
        {
            Room room = (Room)iterator.next();
            if (USE_ROOM_BOOKING)
            {
                Variable roomInUseVariable = room.getRoomInUseVariable();
                mCost.setCoefficient(roomInUseVariable, room.getBaseCost());
            }
            Collection groupVariables = room.getGroupInRoomVariables();
            for (Iterator groupIterator = groupVariables.iterator();
                 groupIterator.hasNext();)
            {
                GroupInRoomVariable variable =
                        (GroupInRoomVariable)groupIterator.next();
                Group group = variable.getGroup();
                mCost.setCoefficient(variable,
                                     room.getPerPersonCost() * group.getSize());
            }
        }

    }

    private void generateConditions()
    {
        int noOfVariables = mVariables.size();
        // Exactly one room per group
        for (Iterator iterator = mGroups.iterator(); iterator.hasNext();)
        {
            Group group = (Group)iterator.next();
            Condition condition = new Condition("Exactly one room for " +
                                                "the group '" +
                                                group.getName() + "'",
                                                noOfVariables);
            Collection roomVariables = group.getGroupInRoomVariables();
            for (Iterator roomVariableIt = roomVariables.iterator();
                 roomVariableIt.hasNext();)
            {
                Variable variable = (Variable)roomVariableIt.next();
                condition.setCoefficient(variable, 1);
            }
            condition.setLeftHandSide(Condition.EQUAL, 1f);
            mConditions.add(condition);
        }

        // Don't overfill the rooms
        for (Iterator iterator = mRooms.iterator(); iterator.hasNext();)
        {
            Room room = (Room)iterator.next();
            Condition condition = new Condition("Don't overfill room '" +
                                                room.getName() + "'",
                                                noOfVariables);
            Collection groupVariables = room.getGroupInRoomVariables();
            for (Iterator groupIterator = groupVariables.iterator();
                 groupIterator.hasNext();)
            {
                GroupInRoomVariable variable =
                        (GroupInRoomVariable)groupIterator.next();
                Group group = variable.getGroup();
                condition.setCoefficient(variable, group.getSize());
            }
            condition.setLeftHandSide(Condition.LESS_THAN_OR_EQUAL,
                                      room.getSize());
            mConditions.add(condition);
        }

        // Only used allocated rooms
        if (USE_ROOM_BOOKING)
        {
            for (Iterator iterator = mRooms.iterator(); iterator.hasNext();)
            {
                Room room = (Room)iterator.next();
                Variable roomInUseVariable = room.getRoomInUseVariable();
                Collection groupVariables = room.getGroupInRoomVariables();
                for (Iterator groupIterator = groupVariables.iterator();
                     groupIterator.hasNext();)
                {
                    GroupInRoomVariable variable =
                            (GroupInRoomVariable)groupIterator.next();
                    Condition condition = new Condition("Only use room '" +
                                                        room.getName() + "' if allocated.",
                                                        noOfVariables);
                    condition.setCoefficient(roomInUseVariable, 1);
                    condition.setCoefficient(variable, -1);
                    condition.setLeftHandSide(Condition.GREATER_THAN_OR_EQUAL, 0);
                    mConditions.add(condition);
                }
            }
        }
    }

    private void generateVariables()
    {
        if (USE_ROOM_BOOKING)
        {
            for (Iterator roomIt = mRooms.iterator(); roomIt.hasNext();)
            {
                Room room = (Room)roomIt.next();
                RoomInUseVariable variable = new RoomInUseVariable(room);
                addVariable(variable);
                room.setRoomInUseVariable(variable);
            }
        }

        for (int groupNo = 0; groupNo < mGroups.size(); groupNo++)
        {
            Group group = (Group)mGroups.get(groupNo);

            for (int roomNo = 0; roomNo < mRooms.size(); roomNo++)
            {
                Room room = (Room)mRooms.get(roomNo);

                GroupInRoomVariable variable =
                        new GroupInRoomVariable(group, room);
                group.addGroupInRoomVariable(variable);
                room.addGroupInRoomVariable(variable);
                addVariable(variable);
            }
        }
    }

    private void addVariable(Variable var)
    {
        int columnNo = mVariables.size();
        var.setColumnNo(columnNo);
        mVariables.add(var);

        for (Iterator iterator = mConditions.iterator(); iterator.hasNext();)
        {
            Condition condition = (Condition)iterator.next();
            condition.addVariable(var);
        }
    }

    private void addGroup(String name, int size)
    {
        Group g1 = new Group(name, size);
        mGroups.add(g1);
    }

    private void addRoom(String name, int size, float optimalNonEmptyLoad,
                         int baseCost, int perPersonCost, int nonOptimalPersonCost)
    {
        Room r1 = new Room(name, size);
        r1.setBaseCost(baseCost);
        r1.setPerPersonCost(perPersonCost);
        r1.setOptimalNonEmptyLoad(optimalNonEmptyLoad);
        r1.setNonOptimalPersonCost(nonOptimalPersonCost);
        mRooms.add(r1);
    }


}
