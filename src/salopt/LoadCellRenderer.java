package salopt;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 2, 2002
 * Time: 12:04:55 PM
 * To change this template use Options | File Templates.
 */
public class LoadCellRenderer extends DefaultTableCellRenderer
{
    private final ArrayList mRooms;
    private final Color LOW_LOAD_COLOR = Color.YELLOW.brighter(); // XXX Should be something else
    private final Color BELOW_OPTIMAL_LOAD_COLOR = Color.GREEN.brighter();
    private final Color ABOVE_OPTIMAL_LOAD_COLOR = Color.ORANGE.brighter();
    private final Color HIGH_LOAD_COLOR = Color.RED.brighter();
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;

    public LoadCellRenderer(ArrayList rooms)
    {
        mRooms = rooms;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component renderingComponent;

        if (value instanceof Float)
        {
            float load = ((Float)value).floatValue();
            String valueStr = String.valueOf((int)((load * 100) + 0.5)) + "%";
            renderingComponent =
                    super.getTableCellRendererComponent(table, valueStr, isSelected, hasFocus, row, column);
            if (renderingComponent instanceof JLabel)
            {
                ((JLabel)renderingComponent).setHorizontalAlignment(JLabel.RIGHT);
            }
            Room room = getRoom(row);
            float optLoad = room.getOptimalNonEmptyLoad();
            if (load <= 0)
            {
                renderingComponent.setBackground(DEFAULT_BACKGROUND_COLOR); // XXX (dynamic)
            }
            if (load < optLoad / 2)
            {
                renderingComponent.setBackground(LOW_LOAD_COLOR);
            }
            else if (load < optLoad)
            {
                renderingComponent.setBackground(BELOW_OPTIMAL_LOAD_COLOR);
            }
            else if (load < (optLoad + 1) / 2)
            {
                renderingComponent.setBackground(ABOVE_OPTIMAL_LOAD_COLOR);
            }
            else
            {
                renderingComponent.setBackground(HIGH_LOAD_COLOR);
            }
        }
        else
        {
            renderingComponent =
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            renderingComponent.setBackground(DEFAULT_BACKGROUND_COLOR); // XXX (dynamic)
        }

        return renderingComponent;
    }

    private Room getRoom(int index)
    {
        if (index < 0 || index >= mRooms.size())
        {
            return null;
        }

        return (Room)mRooms.get(index);
    }
}
