package salopt;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 29, 2002
 * Time: 7:30:58 PM
 * To change this template use Options | File Templates.
 */
public class NonLockedRandom extends java.util.Random
{
    private long mSeed;
    private final static long MULTIPLIER = 0x5DEECE66DL;
    private final static long ADDEND = 0xBL;
    private final static long MASK = (1L << 48) - 1;

    public int next(int bits)
    {
        mSeed = (mSeed * MULTIPLIER + ADDEND) & MASK;
        return (int)(mSeed >>> (48 - bits));
    }

    synchronized public void setSeed(long seed)
    {
        System.out.println("MIN EGEN RANDOM! - setSeed");
        super.setSeed(seed);
        mSeed = (seed ^ MULTIPLIER) & MASK;
    }

    public int cheapNextInt(int n)
    {
        int index = next(16) % n;
        return index;
    }

}
