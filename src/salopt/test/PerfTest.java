package salopt.test;

import salopt.SimAnneal;
import salopt.Group;
import salopt.Room;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 4, 2002
 * Time: 3:49:03 PM
 * To change this template use Options | File Templates.
 */
public class PerfTest
{
    private static ArrayList mRooms = new ArrayList();
    private static ArrayList mGroups = new ArrayList();

    public static void main(String[] args)
    {
        setupRoomsAndGroups();
        SimAnneal simAnneal = new SimAnneal(mRooms, mGroups, 10);
        long startTime = System.currentTimeMillis();
        simAnneal.search();
        String message = "Total time: " + (System.currentTimeMillis() - startTime) + " ms";
        System.out.println(message);
         simAnneal = new SimAnneal(mRooms, mGroups, 10);
         startTime = System.currentTimeMillis();
        simAnneal.search();
         message = "Total time: " + (System.currentTimeMillis() - startTime) + " ms";
        System.out.println(message);
    }

    private static void setupRoomsAndGroups()
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
        addGroup("SSK16a", 30);
        addGroup("SSK17a", 40);
        addGroup("SSK17b", 52);
        addGroup("SSK19a", 45);
        addGroup("SSK19b", 32);
        addGroup("SSK18b", 60);
        addGroup("AN", 10);
    }

    private static void addGroup(String name, int size)
    {
        Group g1 = new Group(name, size);
        mGroups.add(g1);
    }

    private static void addRoom(String name, int size, float optimalNonEmptyLoad,
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
