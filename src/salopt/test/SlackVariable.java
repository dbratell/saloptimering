package salopt.test;

import salopt.test.Variable;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 27, 2002
 * Time: 1:33:53 PM
 * To change this template use Options | File Templates.
 */
public class SlackVariable extends Variable
{
    private final Condition mCondition;

    public SlackVariable(Condition condition)
    {
        mCondition = condition;

        mDescription = "Slack variable";
    }
}
