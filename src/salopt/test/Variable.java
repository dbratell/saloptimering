package salopt.test;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 26, 2002
 * Time: 3:19:12 PM
 * To change this template use Options | File Templates.
 */
public class Variable
{
    protected String mDescription = "A generic variable";

    public int getColumnNo()
    {
        return mColumNo;
    }

    private int mColumNo;

    public void setColumnNo(int variablePos)
    {
        mColumNo = variablePos;
    }

    public String getDescription()
    {
        return mDescription;
    }
}
