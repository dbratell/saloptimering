package salopt;

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Dec 2, 2002
 * Time: 12:04:55 PM
 * To change this template use Options | File Templates.
 */
public class PercentCellRenderer extends DefaultTableCellRenderer
{
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component renderingComponent;

        String toDisplay;
        if (value instanceof Float)
        {
            toDisplay = toPercentString(((Float)value).floatValue());
        }
        else if (value instanceof Double)
        {
            toDisplay = toPercentString(((Double)value).doubleValue());
        }
        else
        {
            toDisplay = String.valueOf(value);
        }

        renderingComponent =
                super.getTableCellRendererComponent(table, toDisplay, isSelected, hasFocus, row, column);

        if (renderingComponent instanceof JLabel)
        {
            ((JLabel)renderingComponent).setHorizontalAlignment(JLabel.RIGHT);
        }

        return renderingComponent;
    }

    private static String toPercentString(double value)
    {
        return String.valueOf((int)((value * 100) + 0.5)) + "%";
    }
}
