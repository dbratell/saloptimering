package salopt;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 28, 2002
 * Time: 6:20:54 PM
 * To change this template use Options | File Templates.
 */
public class SimAnneal
{
    private final ArrayList mRooms;
    private final ArrayList mGroups;

    // Caches to speed up
    private final int[] mGroupSizes;
    private final int[] mRoomSizes;
    private final int[] mRoomBaseCosts;
    private final int[] mRoomPerPersonCost;
    private final int[] mNoOfPeopleInRoom;
    private final int[] mRoomOptimalNoOfPersons;
    private final int[] mRoomNonOptimalPersonCost;

    private PlacementWithCost mCheapestSolution;
    private int mTemperature;
    private final ArrayList mGenePool = new ArrayList();
    private final NonLockedRandom mRandom = new NonLockedRandom();

    private final Object mStopSignal = new Object();
    private volatile boolean mStopSearch = true;

    // Debug
    private static final boolean DEBUG_GENE_POOL = false;
//    private static final boolean KEEP_HISTORY = false;

    // Tuning
    private static final int FORCE = 10;
    private static final int COST_OF_A_STANDING_PERSON = 2000000;
    private static final int NO_OF_TOP_GENES = 3 * FORCE; // 3 works
    private static final int NO_OF_RANDOM_GENES = 5 * FORCE; // 5 works
//    private static final int TRIES_PER_TEMPERATURE = 2*FORCE; // 3-5 works
    private static final int NO_OF_NEW_PLACEMENTS = 200 * FORCE; // 200 works
    private SimAnnealProgressListener mProgressListener;

    public SimAnneal(ArrayList rooms, ArrayList groups, int randomSeed)
    {
        this(rooms, groups);
        mRandom.setSeed(randomSeed);
    }

    public SimAnneal(ArrayList rooms, ArrayList groups)
    {
        mRooms = rooms;
        mGroups = groups;

        int groupCount = mGroups.size();
        if (groupCount < 2)
        {
            // Can not work with so few groups
            throw new IllegalArgumentException("Need at least two groups");
        }

        mGroupSizes = new int[groupCount];
        for (int i = 0; i < groupCount; i++)
        {
            Group group = (Group)mGroups.get(i);
            mGroupSizes[i] = group.getSize();
        }

        int roomCount = mRooms.size();
        mRoomSizes = new int[roomCount];
        mRoomBaseCosts = new int[roomCount];
        mRoomPerPersonCost = new int[roomCount];
        mRoomOptimalNoOfPersons = new int[roomCount];
        mRoomNonOptimalPersonCost = new int[roomCount];
        for (int i = 0; i < roomCount; i++)
        {
            Room room = (Room)mRooms.get(i);
            mRoomSizes[i] = room.getSize();
            mRoomBaseCosts[i] = room.getBaseCost();
            mRoomPerPersonCost[i] = room.getPerPersonCost();
            mRoomOptimalNoOfPersons[i] = (int)(room.getOptimalNonEmptyLoad() * mRoomSizes[i]);
            mRoomNonOptimalPersonCost[i] = room.getNonOptimalPersonCost();
        }

        mNoOfPeopleInRoom = new int[roomCount];
    }

    public void addListener(SimAnnealProgressListener listener)
    {
        if (mProgressListener != null)
        {
            throw new IllegalStateException("There is already a listener.");
        }
        mProgressListener = listener;
    }

    public void search()
    {
        synchronized (mStopSignal)
        {
            mStopSearch = false;
        }

        try
        {
            final int noOfGroups = mGroups.size();
            if (mCheapestSolution == null)
            {
                // Need something to start with
                mCheapestSolution = PlacementWithCost.create(noOfGroups);
                int[] startGroupPlacement = mCheapestSolution.mPlacement;
                for (int i = 0; i < noOfGroups; i++)
                {
                    // All in room 0
                    startGroupPlacement[i] = 0;

                }
                calculateCost(mCheapestSolution);
            }
            // Start with fresh gene pool
            destroyPlacementObjects(mGenePool);
            mGenePool.add(mCheapestSolution.copy());

//        int noOfTriesForTemperature = 0;
            final int totalRounds = noOfGroups /**TRIES_PER_TEMPERATURE */;
            ArrayList newPlacements = new ArrayList(mGenePool.size() * NO_OF_NEW_PLACEMENTS);
            mTemperature = noOfGroups;
            while (mTemperature > 0)
            {
                for (Iterator iterator = mGenePool.iterator(); iterator.hasNext();)
                {
                    PlacementWithCost seedObj = (PlacementWithCost)iterator.next();
                    generateNewGroupPlacements(seedObj, newPlacements);
                }

                if (checkStopSearch())
                {
                    break;
                }

                // Score them
                boolean newBest = checkForNewBest(newPlacements);
                if (checkStopSearch())
                {
                    break;
                }

                generateNewGeenPool(newPlacements);
                destroyPlacementObjects(newPlacements);

//            if (noOfTriesForTemperature < TRIES_PER_TEMPERATURE && newBest)
                if (newBest)
                {
                    if (mTemperature < noOfGroups)
                    {
//                        mTemperature++; // XXX - the right thing to do? It slows things down
                    }
                    mTemperature--; 
//                noOfTriesForTemperature++;
                }
                else
                {
                    mTemperature--;
//                noOfTriesForTemperature = 0;
                }
                if (checkStopSearch())
                {
                    break;
                }

                reportProgress((noOfGroups - mTemperature) /* * TRIES_PER_TEMPERATURE*/ /*+
                               noOfTriesForTemperature */, totalRounds);
            }
            destroyPlacementObjects(newPlacements); // If search is breaked

            if (DEBUG_GENE_POOL) mGenePool.get(0).equals(mGenePool.get(1));

//        if (KEEP_HISTORY)
//        {
//            PlacementWithCost p = mCheapestSolution;
//            while (p != null && p.mMethod != null)
//            {
//                System.out.print(" "+p.mMethod);
//                p = p.mParent;
//            }
//            System.out.println("");
//        }
        }
        finally
        {
            mStopSearch = true;

            reportFinished(mCheapestSolution, arrayToString(mCheapestSolution.mPlacement),
                           mCheapestSolution.getCost());
            System.out.println("PlacementWithCost.sObjCreatedCount = " + PlacementWithCost.sObjCreatedCount); // XXX
        }
    }

    private boolean checkStopSearch()
    {
        synchronized (mStopSignal)
        {
            return mStopSearch;
        }
    }

    private boolean checkForNewBest(ArrayList newPlacements)
    {
        boolean newBest = false;
        for (Iterator iterator = newPlacements.iterator(); iterator.hasNext();)
        {
            PlacementWithCost newGroupPlacement = (PlacementWithCost)iterator.next();
            if (newGroupPlacement.getCost() < mCheapestSolution.getCost())
            {
                mCheapestSolution = newGroupPlacement.copy();
                // New solution
                System.out.print("New solution(" + mTemperature +
                                 "): " + arrayToString(mCheapestSolution.mPlacement));
                System.out.println("  cost = " + mCheapestSolution.getCost());
                reportNewBest(mCheapestSolution, arrayToString(mCheapestSolution.mPlacement),
                              mCheapestSolution.getCost());
                newBest = true;
            }
        }
        return newBest;
    }

    private void reportProgress(int i, int totalRounds)
    {
        if (mProgressListener != null)
        {
            mProgressListener.onProgress(i, totalRounds);
        }
    }

    private void reportNewBest(PlacementWithCost solution,
                               String solutionDescription, int cost)
    {
        if (mProgressListener != null)
        {
            mProgressListener.onNewBest(solution.copy(), solutionDescription, cost);
        }
    }

    private void reportFinished(PlacementWithCost solution,
                                String solutionDescription, int cost)
    {
        synchronized (mStopSignal)
        {
            mStopSearch = true;
            mStopSignal.notifyAll();
        }

        if (mProgressListener != null)
        {
            mProgressListener.onFinished(solution.copy(), solutionDescription, cost);
        }
    }

    private static void destroyPlacementObjects(ArrayList newPlacements)
    {
        // Return the objects to the pool
        for (Iterator iterator = newPlacements.iterator(); iterator.hasNext();)
        {
            PlacementWithCost placementWithCost = (PlacementWithCost)iterator.next();
            placementWithCost.destroy();
        }
        newPlacements.clear();
    }


    /**
     *  The best and some random
     * @param newPlacements
     */
    private void generateNewGeenPool(ArrayList newPlacements)
    {
        // The best and some random
        PlacementWithCost[] topList = new PlacementWithCost[NO_OF_TOP_GENES];
        int lastIndexInTopList = topList.length - 1;
        int treshold = Integer.MAX_VALUE;
        outer:
        for (Iterator iterator = newPlacements.iterator(); iterator.hasNext();)
        {
            PlacementWithCost placement = (PlacementWithCost)iterator.next();
            int cost = placement.getCost();
            if (cost >= treshold)
            {
                continue;
            }
            // Insertion sort
            for (int i = 0; i < topList.length; i++)
            {
                if (topList[i] != null)
                {
                    if (placement.equals(topList[i]))
                    {
                        continue outer; // Already in pool
                    }
                    if (cost >= topList[i].getCost())
                    {
                        continue; // not here
                    }
                }

                // Push the rest down
                for (int j = lastIndexInTopList; j > i; j--)
                {
                    topList[j] = topList[j - 1];
                }
                topList[i] = placement.copy();
                break; // Finished with this one
            }

            if (topList[lastIndexInTopList] != null)
            {
                treshold = topList[lastIndexInTopList].getCost();
            }
        }

        PlacementWithCost[] randomGenes = new PlacementWithCost[NO_OF_RANDOM_GENES];
        int size = newPlacements.size();
        for (int i = 0; i < NO_OF_RANDOM_GENES; i++)
        {
            final int index = mRandom.cheapNextInt(size);
            PlacementWithCost plac = (PlacementWithCost)newPlacements.get(index);
            randomGenes[i] = plac;
        }

        destroyPlacementObjects(mGenePool);
        for (int i = 0; i < topList.length; i++)
        {
            if (topList[i] != null)
            {
                mGenePool.add(topList[i].copy());
            }
        }
        for (int i = 0; i < randomGenes.length; i++)
        {
            mGenePool.add(randomGenes[i].copy());
        }

        // Debug
        if (DEBUG_GENE_POOL)
        {
            String seperator = "";
            System.out.print("Genes: ");
            for (Iterator iterator = mGenePool.iterator(); iterator.hasNext();)
            {
                PlacementWithCost pla = (PlacementWithCost)iterator.next();
                System.out.print(seperator + pla.getCost());
                seperator = ", ";
            }
            System.out.println("");
        }
    }

    private void generateNewGroupPlacements(PlacementWithCost seedObject,
                                            ArrayList targetContainer)
    {
        // Keep the original  (? not sure if this is good)
        targetContainer.add(seedObject);

        // Half is random. Half is swapping.
        int half = (NO_OF_NEW_PLACEMENTS - 1) / 2;
        generateRandomPlacements(seedObject, half, targetContainer);

        generateSwappedPlacements(seedObject, half, targetContainer);
    }

    private void generateSwappedPlacements(PlacementWithCost groupPlacementObj, int count, ArrayList targetContainer)
    {
        int[] groupPlacement = groupPlacementObj.mPlacement;

        int groupCount = mGroups.size();
        NonLockedRandom rand = mRandom;
        int temperature = mTemperature;
        for (int i = 0; i < count; i++)
        {
            PlacementWithCost newGroupPlacement;
            newGroupPlacement = PlacementWithCost.create(groupCount);
            int[] newPlacement = newGroupPlacement.mPlacement;
            System.arraycopy(groupPlacement, 0, newPlacement, 0, groupCount);

            for (int j = 0; j < temperature; j++)
            {
//                int swap1 = mRandom.nextInt(groupCount);
//                int swap2 = mRandom.nextInt(groupCount-1);
                // This saves us one nextInt()-call
//                int random = mRandom.nextInt(groupCount*(groupCount-1));
                int random = rand.cheapNextInt(groupCount * (groupCount - 1));
                int swap1 = random % groupCount; // between 0 and groupCount-1
                int swap2 = random / groupCount; // between 0 and groupCount-2
                if (swap2 >= swap1)
                {
                    swap2++; // swap2 should be all numbers except swap1.
                }
                int temp = newPlacement[swap1];
                newPlacement[swap1] = newPlacement[swap2];
                newPlacement[swap2] = temp;
            }
            calculateCost(newGroupPlacement);

//            if (KEEP_HISTORY)
//            {
//                newGroupPlacement.setParent(groupPlacementObj, "Swap"+mTemperature);
//            }
            targetContainer.add(newGroupPlacement);
        }
    }

    private void generateRandomPlacements(PlacementWithCost groupPlacementObj, int count, ArrayList targetContainer)
    {
        int[] groupPlacement = groupPlacementObj.mPlacement;
        int roomCount = mRooms.size();
        int groupCount = groupPlacement.length;

        NonLockedRandom rand = mRandom;
        int currentTemp = mTemperature;
        for (int i = 0; i < count; i++)
        {
            PlacementWithCost newGroupPlacement;
            newGroupPlacement = PlacementWithCost.create(groupCount);
            int[] newPlacement = newGroupPlacement.mPlacement;
            for (int j = 0; j < groupCount; j++)
            {
                int changeGroupPosCheck = rand.cheapNextInt(groupCount);
                if (changeGroupPosCheck < currentTemp)
                {
                    // Switch room
                    newPlacement[j] = rand.cheapNextInt(roomCount);
                }
                else
                {
                    // Keep room
                    newPlacement[j] = groupPlacement[j];
                }
            }
            calculateCost(newGroupPlacement);
//            if (KEEP_HISTORY)
//            {
//                newGroupPlacement.setParent(groupPlacementObj, "Random"+mTemperature);
//            }
            targetContainer.add(newGroupPlacement);
        }
    }

    private void calculateCost(PlacementWithCost groupPlacement)
    {
        // This is using shared memory
        calculatePeopleInRooms(groupPlacement.mPlacement);

        int cost = 0;
        int[] roomSizes = mRoomSizes;
        int[] roomPerPersonCost = mRoomPerPersonCost;
        int[] roomBaseCosts = mRoomBaseCosts;
        int[] noOfPeopleInRoom = mNoOfPeopleInRoom;
        int[] roomOptimalPersons = mRoomOptimalNoOfPersons;
        int[] roomNonOptimalPersonCost = mRoomNonOptimalPersonCost;
        int roomCount = mNoOfPeopleInRoom.length;
        for (int roomNo = 0; roomNo < roomCount; roomNo++)
        {
            int noOfPeople = noOfPeopleInRoom[roomNo];
            if (noOfPeople > 0)
            {
                cost += roomBaseCosts[roomNo] + roomPerPersonCost[roomNo] * noOfPeople;
                int nonOptimalPersons = noOfPeople - roomOptimalPersons[roomNo];
                if (nonOptimalPersons > 0)
                {
                    int roomSize = roomSizes[roomNo];
                    int standingPeople = noOfPeople - roomSize;
                    standingPeople = (standingPeople < 0) ? 0 : standingPeople;
                    nonOptimalPersons -= standingPeople;
                    int extraPercent = roomSize <= 0 ? 0 : (100 * nonOptimalPersons / roomSize);
                    int extraPersonCount = extraPercent * (nonOptimalPersons + nonOptimalPersons * nonOptimalPersons / 4); // Magical formula
                    cost += roomNonOptimalPersonCost[roomNo] * extraPersonCount;
                    if (standingPeople > 0)
                    {
                        cost += COST_OF_A_STANDING_PERSON * standingPeople;
                    }
                }
            }
        }

        if (cost < 0)
        {
            cost = Integer.MAX_VALUE;
        }

        groupPlacement.setCost(cost);
    }

    /**
     * Changes mNoOfPeopleInRoom
     * @param groupPlacement
     */
    private void calculatePeopleInRooms(int[] groupPlacement)
    {
        int[] noOfPeopleInRoom = mNoOfPeopleInRoom;
        int roomCount = noOfPeopleInRoom.length;
        for (int i = 0; i < roomCount; i++)
        {
            noOfPeopleInRoom[i] = 0;
        }
        int groupCount = groupPlacement.length;
        int[] groupSizes = mGroupSizes;
        for (int groupNo = 0; groupNo < groupCount; groupNo++)
        {
            int roomNo = groupPlacement[groupNo];
            noOfPeopleInRoom[roomNo] += groupSizes[groupNo];
        }
    }

    private String arrayToString(int[] solution)
    {
        if (solution == null)
        {
            return "null solution";
        }

        StringBuffer buf = new StringBuffer();
        String seperator = "";
        int[] roomUsage = new int[mRooms.size()];
        int groupCount = solution.length;
        for (int groupNo = 0; groupNo < groupCount; groupNo++)
        {
            int roomNo = solution[groupNo];
            Group group = (Group)mGroups.get(groupNo);
            String groupName = group.getName();
            Room room = (Room)mRooms.get(roomNo);
            String roomName = room.getName();
            buf.append(seperator + groupName + " i " + roomName);
            seperator = ", ";
            roomUsage[roomNo] += group.getSize();
        }

        buf.append(".");
        seperator = " ";
        for (int roomNo = 0; roomNo < roomUsage.length; roomNo++)
        {
            int people = roomUsage[roomNo];
            if (people != 0)
            {
                Room room = (Room)mRooms.get(roomNo);
                int size = room.getSize();
                int percentageFull = size <= 0 ? 0 : (100 * people / size);
                buf.append(seperator + people + " personer i " + room.getName() +
                           " (" + percentageFull + "% fullt)");
                seperator = ", ";
            }
        }

        buf.append(".");
        seperator = " Tomma lokaler: ";
        for (int roomNo = 0; roomNo < roomUsage.length; roomNo++)
        {
            int people = roomUsage[roomNo];
            if (people == 0)
            {
                Room room = (Room)mRooms.get(roomNo);
                buf.append(seperator + room.getName());
                seperator = ", ";
            }
        }

        return buf.toString();
    }

    public void stopSearch()
    {
        synchronized (mStopSignal)
        {
            if (!mStopSearch)
            {
                mStopSearch = true;
                try
                {
                    mStopSignal.wait();
                }
                catch (InterruptedException e)
                {
                }
            }
        }
    }

    public void removeListener(SimAnnealProgressListener listener)
    {
        if (mProgressListener == listener)
        {
            mProgressListener = null;
        }
    }

    public boolean isSearchRunning()
    {
        return !mStopSearch; // XXX: A little optimistic. Well.
    }

}
