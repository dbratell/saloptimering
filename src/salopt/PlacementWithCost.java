package salopt;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 29, 2002
 * Time: 3:59:29 PM
 * To change this template use Options | File Templates.
 */
public class PlacementWithCost
{
    static int sObjCreatedCount;

    int[] mPlacement;
    private int mCost;
//    PlacementWithCost mParent;
//    String mMethod;

    private static final int INITIAL_POOL_SIZE = 10000;
    private static PlacementWithCost[] sPool = new PlacementWithCost[INITIAL_POOL_SIZE];
    private static int sPoolUseCount = 0;

    private PlacementWithCost(int placementLength)
    {
        init(placementLength);
        sObjCreatedCount++;
    }

    private void init(int placementLength)
    {
        if (mPlacement == null || mPlacement.length != placementLength)
        {
            mPlacement = new int[placementLength];
        }
//        mParent = null;
//        mMethod = null;
        mCost = -1;
    }

    public static PlacementWithCost create(int placementLength)
    {
        PlacementWithCost placementObj;

        if (sPoolUseCount > 0)
        {
            placementObj = sPool[--sPoolUseCount];
            placementObj.init(placementLength);
        }
        else
        {
            placementObj = new PlacementWithCost(placementLength);
        }
        return placementObj;
    }

    public void destroy()
    {
        if (sPoolUseCount == sPool.length)
        {
            // Grow it by doubling
            PlacementWithCost[] newPool = new PlacementWithCost[sPoolUseCount * 2];
            System.arraycopy(sPool, 0, newPool, 0, sPoolUseCount);
            sPool = newPool;
        }

        sPool[sPoolUseCount++] = this;
    }

    public PlacementWithCost copy()
    {
        PlacementWithCost copy = create(mPlacement.length);
        int[] copyArr = copy.mPlacement;
        System.arraycopy(mPlacement, 0, copyArr, 0, mPlacement.length);
        copy.mCost = mCost;
//        if (mParent != null)
//        {
//            copy.mParent = mParent.copy();
//        }
//        copy.mMethod = mMethod;

        return copy;
    }

//    public void setParent(PlacementWithCost parent, String method)
//    {
//        mParent = parent;
//        mMethod = method;
//    }

    public int hashCode()
    {
        int hash = 0;
        for (int i = 0; i < mPlacement.length; i++)
        {
            hash *= 31;
            hash += mPlacement[i];
        }
        return hash * 31 + new Double(getCost()).hashCode();
    }

    public boolean equals(Object obj)
    {
        if ((obj instanceof PlacementWithCost))
        {
            PlacementWithCost other = (PlacementWithCost)obj;
            if (other.getCost() == this.getCost() &&
                    other.mPlacement.length == this.mPlacement.length)
            {
                for (int i = 0; i < this.mPlacement.length; i++)
                {
                    if (other.mPlacement[i] != this.mPlacement[i])
                    {
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }

    public int getCost()
    {
        if (mCost == -1)
        {
            throw new IllegalStateException("Cost not set!");
        }
        return mCost;
    }

    public void setCost(int cost)
    {
        mCost = cost;
    }
}
