package salopt;

/**
 * In a chain of data manipulators some behaviour is common. TableMap
 * provides most of this behavour and can be subclassed by filters
 * that only need to override a handful of specific methods. TableMap
 * implements TableModel by routing all requests to its mModel, and
 * TableModelListener by routing all events to its listeners. Inserting
 * a TableMap which has not been subclassed into a chain of table filters
 * should have no effect.
 *
 * @version 1.4 12/17/97
 * @author Philip Milne */

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class TableMap extends AbstractTableModel
        implements TableModelListener
{
    protected TableModel mModel;

// --Recycle Bin START (12/2/02 2:05 PM):
//    public TableModel getModel()
//    {
//        return mModel;
//    }
// --Recycle Bin STOP (12/2/02 2:05 PM)

    void setModel(TableModel model)
    {
        this.mModel = model;
        model.addTableModelListener(this);
    }

    // By default, implement TableModel by forwarding all messages
    // to the mModel.

    public Object getValueAt(int aRow, int aColumn)
    {
        return mModel.getValueAt(aRow, aColumn);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        mModel.setValueAt(aValue, aRow, aColumn);
    }

    public int getRowCount()
    {
        return (mModel == null) ? 0 : mModel.getRowCount();
    }

    public int getColumnCount()
    {
        return (mModel == null) ? 0 : mModel.getColumnCount();
    }

    public String getColumnName(int aColumn)
    {
        return mModel.getColumnName(aColumn);
    }

    public Class getColumnClass(int aColumn)
    {
        return mModel.getColumnClass(aColumn);
    }

    public boolean isCellEditable(int row, int column)
    {
        return mModel.isCellEditable(row, column);
    }
//
// Implementation of the TableModelListener interface,
//
    // By default forward all events to all the listeners.
    public void tableChanged(TableModelEvent e)
    {
        fireTableChanged(e);
    }
}
