package salopt;

import javax.swing.JTable;
import java.awt.Color;
import java.awt.Component;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 2, 2002
 * Time: 12:04:55 PM
 * To change this template use Options | File Templates.
 */
public class LoadCellRenderer extends PercentCellRenderer
{
    private final TableRowToRoomTranslator mRoomGetter;
    private final Color LOW_LOAD_COLOR = Color.YELLOW;
    private final Color BELOW_OPTIMAL_LOAD_COLOR = Color.GREEN;
    private final Color ABOVE_OPTIMAL_LOAD_COLOR = Color.ORANGE;
    private final Color HIGH_LOAD_COLOR = Color.RED;

    public LoadCellRenderer(TableRowToRoomTranslator roomGetter)
    {
        mRoomGetter = roomGetter;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component renderingComponent;
        Color defaultBackground;
        if (isSelected)
        {
            defaultBackground = table.getSelectionBackground();
        }
        else
        {
            defaultBackground = table.getBackground();
        }

        Color specialBackground = null;
        if (value instanceof Float)
        {
            float load = ((Float)value).floatValue();
            renderingComponent =
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (load > 0)
            {
                Room room = mRoomGetter.translate(row);
                float optLoad = room.getOptimalNonEmptyLoad();
                if (load < optLoad / 2)
                {
                    specialBackground = LOW_LOAD_COLOR;
                }
                else if (load < optLoad)
                {
                    specialBackground = BELOW_OPTIMAL_LOAD_COLOR;
                }
                else if (load < (optLoad + 1) / 2)
                {
                    specialBackground = ABOVE_OPTIMAL_LOAD_COLOR;
                }
                else
                {
                    specialBackground = HIGH_LOAD_COLOR;
                }
            }
        }
        else
        {
            renderingComponent =
                    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }

        renderingComponent.setBackground(mixColors(specialBackground,
                                                   defaultBackground));

        return renderingComponent;
    }

    private Color mixColors(Color specialColour, Color defaultColour)
    {
        if (specialColour == null)
        {
            return defaultColour;
        }
        // Mix in HSV
        float[] c1hsb = toHSB(specialColour);
        float[] c2hsb = toHSB(defaultColour);
        float h = c1hsb[0]; // the same "colour" as the base.
        float s = (c1hsb[1]+c2hsb[1])/2;
        float b = (c1hsb[2]+c2hsb[2])/2;
        Color mix = Color.getHSBColor(h, s, b);
        return mix;
    }

    private float[] toHSB(Color color)
    {
        float[] rgb = color.getRGBComponents(null);
        float[] hsb = Color.RGBtoHSB((int)(255*rgb[0]),
                                       (int)(255*rgb[1]),
                                       (int)(255*rgb[2]),
                                       null);
        return hsb;
    }
}
