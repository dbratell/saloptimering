package salopt;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 30, 2002
 * Time: 11:50:20 AM
 * To change this template use Options | File Templates.
 */
public interface SimAnnealProgressListener
{
    void onProgress(int progress, int max);

    void onNewBest(PlacementWithCost solution, String solutionDescription, int cost);

    void onFinished(PlacementWithCost solution, String bestSolutionDescription, int bestCost);
}
