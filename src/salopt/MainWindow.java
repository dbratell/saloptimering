package salopt;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;
import javax.swing.Action;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Container;
import java.awt.BorderLayout;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 2, 2002
 * Time: 1:01:01 PM
 * To change this template use Options | File Templates.
 */
public class MainWindow extends JFrame
{
    private static final String TITLE = "Salsbokning - prototyp (c) Daniel Bratell";

    private final Collection mStuffNotUseableWhileSearching = new HashSet();
    private final Collection mStuffNotUseableUnlessSearching = new HashSet();

    public MainWindow()
    {
        super(TITLE);

        setupIcon(this);
        Container content = getContentPane();
        content.add(getHeaderComponent(), BorderLayout.NORTH);
    }

    private static void setupIcon(JFrame window)
    {
        try
        {
            ClassLoader loader = Prototype.class.getClassLoader();
            String iconFileName = "salopt-icon.gif";
            URL resource = loader.getResource(iconFileName);
//            System.out.println("resource = " + resource);
            if (resource != null)
            {
                ImageIcon icon = new ImageIcon(resource);
                window.setIconImage(icon.getImage());
            }
        }
        catch (RuntimeException re)
        {
              re.printStackTrace();
        }
    }

    public Component getHeaderComponent()
    {
        JLabel title = new SmoothJLabel(TITLE, JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));

        return title;
    }

    static class SmoothJLabel extends JLabel
    {
        public SmoothJLabel(String text, int horizontalAlignment)
        {
            super(text, horizontalAlignment);
        }

        protected void paintComponent(Graphics g)
        {
            Graphics2D g2 = (Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            super.paintComponent(g);
        }
    }

    public void setEnabledForComponents(final boolean searchRunning)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                for (Iterator iterator = mStuffNotUseableUnlessSearching.iterator();
                     iterator.hasNext();)
                {
                    Object actOrComp = iterator.next();
                    if (actOrComp instanceof Action)
                    {
                        ((Action)actOrComp).setEnabled(searchRunning);
                    }
                    else
                    {
                        ((Component)actOrComp).setEnabled(searchRunning);
                    }
                }

                for (Iterator iterator = mStuffNotUseableWhileSearching.iterator();
                     iterator.hasNext();)
                {
                    Object actOrComp = iterator.next();
                    if (actOrComp instanceof Action)
                    {
                        ((Action)actOrComp).setEnabled(!searchRunning);
                    }
                    else
                    {
                        ((Component)actOrComp).setEnabled(!searchRunning);
                    }
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    public void addStuffNotUseableUnlessSearching(Component component)
    {
        mStuffNotUseableUnlessSearching.add(component);
    }

    public void addStuffNotUseableUnlessSearching(Action action)
    {
        mStuffNotUseableUnlessSearching.add(action);
    }

    public void addStuffNotUseableWhileSearching(Component component)
    {
        mStuffNotUseableWhileSearching.add(component);
    }

    public void addStuffNotUseableWhileSearching(Action action)
    {
        mStuffNotUseableWhileSearching.add(action);
    }

    public void setStatusBar(Component statusBar)
    {
        Container content = getContentPane();
        content.add(statusBar, BorderLayout.SOUTH);
    }

    public void setCentralComponent(Component component)
    {
        Container content = getContentPane();
        content.add(component, BorderLayout.CENTER);
    }

}
