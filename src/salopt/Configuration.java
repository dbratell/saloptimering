package salopt;

import java.awt.Dimension;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 1, 2002
 * Time: 12:08:32 PM
 * To change this template use Options | File Templates.
 */
public class Configuration
{
    private static final Preferences sPref =
            Preferences.userNodeForPackage(Configuration.class);
    private static final String WINDOW_HEIGHT_KEY = "windowHeight";
    private static final String WINDOW_WIDTH_KEY = "windowWidth";

    public static Dimension getWindowSize()
    {
        int height = sPref.getInt(WINDOW_HEIGHT_KEY, -1);
        int width = (height == -1) ? -1 : sPref.getInt(WINDOW_WIDTH_KEY, -1);

        return new Dimension(width, height);
    }

    public static void saveWindowSize(int width, int height)
    {
        if (height < 10 || width < 10)
        {
            height = -1;
            width = -1;
        }

        sPref.putInt(WINDOW_HEIGHT_KEY, height);
        sPref.putInt(WINDOW_WIDTH_KEY, width);
    }
}
