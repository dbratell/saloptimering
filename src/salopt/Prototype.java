package salopt;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.BadLocationException;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Properties;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Bratell
 * Date: Nov 26, 2002
 * Time: 3:00:34 PM
 * To change this template use Options | File Templates.
 */
public class Prototype implements SimAnnealProgressListener
{
    private static final boolean TRY_SMART_BRUTE_FORCE = false;
    private static final boolean SORTED_ROOM_TABLE = true;
    private static final boolean SORTED_GROUP_TABLE = true;

    // Data
    private final ArrayList mRooms = new ArrayList();
    private final ArrayList mGroups = new ArrayList();
    private SimAnneal mSimAnneal;

    // UI models/data
    private RoomTableModel mRoomListModel;
    private GroupTableModel mGroupListModel;

    // UI
    private MainWindow mMainWindow;
    private JTable mRoomTable;
    private JTable mGroupTable;
    private JTextArea mMessageBox;
    private JProgressBar mProgressBar;
    private JLabel mStatusMessage;

    public static void main(String[] args)
    {
        if (Calendar.getInstance().get(GregorianCalendar.YEAR) > 2002)
        {
            JOptionPane.showMessageDialog(null, "Prototypen är satt att fungera under\n" +
                                                "2002 och inte senare.\n" +
                                                "\n" +
                                                "Kontakta Daniel Bratell för en nyare version.\n" +
                                                "bratell@lysator.liu.se.",
                                          "Slut på provperioden", JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException("Update prototype. " +
                                            "This is not to be used after 2002.");
        }
        new Prototype().execute();
    }

    private void execute()
    {
        setupUI();

        addRoom("A210", 80 / 2, 0.82f, 18000, 8, 1500);
        addRoom("C203", 144 / 2, 0.9f, 114400, 14, 1500);
        addRoom("D207", 130 / 2, 0.9f, 113000, 13, 1500);
        addRoom("D209", 97 / 2, 0.85f, 19700, 10, 1500);
        addRoom("D211", 97 / 2, 0.85f, 19700, 10, 1500);
        addRoom("E308", 65 / 2, 0.8f, 16500, 7, 1500);
        addRoom("E310", 102 / 2, 0.9f, 110200, 10, 1500);
        addRoom("E408", 65 / 2, 0.8f, 16500, 7, 1500);
        addRoom("M204", 250 / 2, 0.9f, 125000 * 2, 25, 1500);
        addRoom("M202", 200 / 2, 0.9f, 120200 * 2, 25, 1500);


        addGroup("PIA02", 2);
        addGroup("SSK15", 5);
        addGroup("SSK16a", 50);
        addGroup("SSK17a", 50);
        addGroup("SSK17b", 50);
        addGroup("SSK19a", 50);
        addGroup("SSK19b", 50);
        addGroup("SSK18b", 60);
        addGroup("AN", 10);


        if (TRY_SMART_BRUTE_FORCE)
        {
            trySmartBruteForce();
        }
//        trySimulatedAnnealing();

    }

    private void setupUI()
    {
        try
        {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        catch (UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        mMainWindow = new MainWindow();
        mMainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addMainWindowEventHandlers(mMainWindow);

        LoadRoomsAction loadRoomsAction = new LoadRoomsAction("Ladda lokaler",
                                                              getIcon("open16.gif"),
                                                              "Läser in fler lokaler från en fil",
                                                              new Integer(KeyEvent.VK_L));
        mMainWindow.addStuffNotUseableWhileSearching(loadRoomsAction);
        SaveRoomsAction saveRoomsAction = new SaveRoomsAction("Spara lokaler",
                                                              getIcon("save16.gif"),
                                                              "Skriver ner all lokaldata till en fil som senare kan läsas tillbaka in i programmet",
                                                              new Integer(KeyEvent.VK_S));
        mMainWindow.addStuffNotUseableWhileSearching(saveRoomsAction);
        StartSearchAction startSearchAction = new StartSearchAction("Starta sökning",
                                                                    getIcon("find16.gif"),
                                                                    "Påbörjar en ny sökning med givna villkor",
                                                                    new Integer(KeyEvent.VK_S));
        mMainWindow.addStuffNotUseableWhileSearching(startSearchAction);
        StopSearchAction stopSearchAction = new StopSearchAction("Avbryt sökning",
                                                                 getIcon("stop16.gif"),
                                                                 "Avlutar en sökning i det läget den befinner sig",
                                                                 new Integer(KeyEvent.VK_A));
        mMainWindow.addStuffNotUseableUnlessSearching(stopSearchAction);
        ContinueSearchAction continueSearchAction = new ContinueSearchAction("Fortsätt sökning",
                                                                 getIcon("findagain16.gif"),
                                                                 "Fortsätter en sökning för att försöka hitta ännu bättre lösningar",
                                                                 new Integer(KeyEvent.VK_F));
        mMainWindow.addStuffNotUseableWhileSearching(continueSearchAction);

        setupMenu(mMainWindow, loadRoomsAction, saveRoomsAction, startSearchAction, stopSearchAction, continueSearchAction);

        Box roomListBox = createRoomListBox();
        Box groupListBox = createGroupListBox();

        JSplitPane listPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                              roomListBox,
                                              groupListBox);
        listPanel.setResizeWeight(0.8); // Most space to the left

        mMessageBox = new JTextArea();
        mMessageBox.setEditable(false);
        JScrollPane scrollingMessageBox = new JScrollPane(mMessageBox);

        JSplitPane listMessagePanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                listPanel, scrollingMessageBox);
        listMessagePanel.setResizeWeight(0.5);

        JToolBar toolBar = createToolBar(loadRoomsAction, saveRoomsAction, startSearchAction, stopSearchAction, continueSearchAction);

        JPanel centerPanel = new JPanel();
        LayoutManager layout = new BorderLayout();
        centerPanel.setLayout(layout); // For a toolbar to work
        centerPanel.add(toolBar, BorderLayout.NORTH);
        centerPanel.add(listMessagePanel, BorderLayout.CENTER);

        Component statusBar = createStatusBar();

        mMainWindow.setStatusBar(statusBar);
        mMainWindow.setCentralComponent(centerPanel);

        Dimension size = Configuration.getWindowSize();
        if (size.height != -1)
        {
            mMainWindow.setSize(size);
        }
        else
        {
            mMainWindow.pack();
        }
        mMainWindow.setEnabledForRunningSearch(isSimAnnealSearchRunning());
        mMainWindow.setVisible(true);
    }

    private Box createStatusBar()
    {
        mProgressBar = new JProgressBar();
        mProgressBar.setStringPainted(true);
        mMainWindow.addStuffNotUseableUnlessSearching(mProgressBar);

        mStatusMessage = new JLabel();

        Box statusBar = new Box(BoxLayout.X_AXIS);
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(mStatusMessage);
        statusBar.add(Box.createHorizontalStrut(5));
        statusBar.add(mProgressBar);
//        statusBar.add(Box.createHorizontalGlue());
        return statusBar;
    }

    private static JToolBar createToolBar(LoadRoomsAction loadRoomsAction,
                                   SaveRoomsAction saveRoomsAction,
                                   StartSearchAction startSearchAction,
                                   StopSearchAction stopSearchAction,
                                   ContinueSearchAction continueSearchAction)
    {
        JToolBar toolBar = new JToolBar();

        toolBar.add(new JButton(loadRoomsAction));
        toolBar.add(new JButton(saveRoomsAction));
        toolBar.addSeparator();
        toolBar.add(new JButton(startSearchAction));
        toolBar.add(new JButton(continueSearchAction));
        toolBar.add(new JButton(stopSearchAction));

        // Remove texts
        Component[] components = toolBar.getComponents();
        for (int i = 0; i < components.length; i++)
        {
            Component component = components[i];
            if (component instanceof JButton)
            {
                ((JButton)component).setText("");
            }
        }

        return toolBar;
    }

    private Box createGroupListBox()
    {
        mGroupListModel = new GroupTableModel(mGroups, mRooms);
        if (SORTED_GROUP_TABLE)
        {
            TableSorter sorter = new TableSorter(mGroupListModel); //Sorting
            mGroupTable = new JTable(sorter);             //Sorting
            sorter.addMouseListenerToHeaderInTable(mGroupTable); //Sorting
        }
        else
        {
            mGroupTable = new JTable(mGroupListModel);  // Without sorting
        }

        mGroupListModel.addTableModelListener(new TableModelListener()
        {
            public void tableChanged(TableModelEvent e)
            {
                if (isSimAnnealSearchRunning())
                {
                    // XXX - this must not happen
                    return;
                }

                clearSearch();
            }
        });
        JScrollPane scrollingGroupList = new JScrollPane(mGroupTable);
        JPanel groupListButtons = new JPanel();
        JButton addGroupButton = new JButton("En grupp till");
        setupButtonAddGroupAction(addGroupButton);
        mMainWindow.addStuffNotUseableWhileSearching(addGroupButton);
        JButton removeGroupButton = new JButton("Ta bort grupp");
        setupButtonRemoveGroupAction(removeGroupButton);
        mMainWindow.addStuffNotUseableWhileSearching(removeGroupButton);
        groupListButtons.add(addGroupButton);
        groupListButtons.add(removeGroupButton);
        Box groupListBox = Box.createVerticalBox();
        groupListBox.add(scrollingGroupList);
        groupListBox.add(groupListButtons);
        mMainWindow.addStuffNotUseableWhileSearching(mGroupTable);
        return groupListBox;
    }

    private void setupButtonRemoveGroupAction(JButton removeGroupButton)
    {
        removeGroupButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (isSimAnnealSearchRunning())
                        {
                            searchRunningDialog();
                            return;
                        }

                        // Check selection
                        int[] selectedRows = mGroupTable.getSelectedRows();
                        if (selectedRows.length > 0)
                        {
                            for (int i = selectedRows.length - 1; i >= 0; i--)
                            {
                                int selectedRow = selectedRows[i];
                                mGroups.remove(selectedRow);
                                mGroupListModel.fireTableRowsDeleted(selectedRow,
                                                                     selectedRow);
                            }
                        }
                    }
                }
        );
    }

    private void searchRunningDialog()
    {
        JOptionPane.showMessageDialog(mMainWindow,
                                      "Funkar inte medan en sökning pågår.\nAvsluta sökningen och försök igen.");
    }

    private void setupButtonAddGroupAction(JButton addGroupButton)
    {
        addGroupButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (isSimAnnealSearchRunning())
                        {
                            searchRunningDialog();
                            return;
                        }

                        addGroup("Ny grupp", 0);
                    }
                }
        );

    }


    private void setupButtonRemoveRoomAction(JButton removeRoomButton)
    {
        removeRoomButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (isSimAnnealSearchRunning())
                        {
                            searchRunningDialog();
                            return;
                        }

                        // Check selection
                        int[] selectedRows = mRoomTable.getSelectedRows();
                        if (selectedRows.length > 0)
                        {
                            for (int i = selectedRows.length - 1; i >= 0; i--)
                            {
                                int selectedRow = selectedRows[i];
                                mRooms.remove(selectedRow);
                                mRoomListModel.fireTableRowsDeleted(selectedRow,
                                                                    selectedRow);
                            }
                        }
                    }
                }
        );
    }

    private void setupButtonAddRoomAction(JButton addGroupButton)
    {
        addGroupButton.addActionListener(
                new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (isSimAnnealSearchRunning())
                        {
                            searchRunningDialog();
                            return;
                        }

                        addRoom("Ny lokal", 0, 0.85f, 10000, 20, 500); // Good defaults?
                    }
                }
        );

    }

    private Box createRoomListBox()
    {
        mRoomListModel = new RoomTableModel(mRooms, mGroups);
        TableRowToRoomTranslator translator;
        if (SORTED_ROOM_TABLE)
        {
            final TableSorter sorter = new TableSorter(mRoomListModel); //Sorting
            mRoomTable = new JTable(sorter);             //Sorting
            sorter.addMouseListenerToHeaderInTable(mRoomTable); //Sorting
            translator = new TableRowToRoomTranslator(){
                    public Room translate(int rowNumber)
                    {
                        int innerRowNo = sorter.translateOuterRowNumberToInnerRowNumber(rowNumber);
                        return (Room)mRooms.get(innerRowNo);
                    }
                };
        }
        else
        {
            mRoomTable = new JTable(mRoomListModel);  // Without sorting
            translator = new TableRowToRoomTranslator(){
                    public Room translate(int rowNumber)
                    {
                        return (Room)mRooms.get(rowNumber);
                    }
                };
        }

        int columnCount = mRoomListModel.getColumnCount();
        TableColumnModel columnModel = mRoomTable.getColumnModel();
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
        {
            TableColumn column = columnModel.getColumn(columnIndex);
            column.setPreferredWidth(20 * mRoomListModel.getPreferredRelativeWidth(columnIndex));
            if (columnIndex == mRoomListModel.LOAD_INDEX)
            {
                column.setCellRenderer(new LoadCellRenderer(translator));
            }
            else if (columnIndex == mRoomListModel.OPT_LOAD_INDEX)
            {
                column.setCellRenderer(new PercentCellRenderer());
            }
        }

        mRoomListModel.addTableModelListener(new TableModelListener()
        {
            public void tableChanged(TableModelEvent e)
            {
                if (isSimAnnealSearchRunning())
                {
                    // XXX - this must not happen
                    return;
                }

                clearSearch();
            }
        });

        JScrollPane scrollingRoomList = new JScrollPane(mRoomTable);
        JPanel roomListButtons = new JPanel();
        JButton addRoomButton = new JButton("En lokal till");
        setupButtonAddRoomAction(addRoomButton);
        mMainWindow.addStuffNotUseableWhileSearching(addRoomButton);
        JButton removeRoomButton = new JButton("Ta bort lokal");
        setupButtonRemoveRoomAction(removeRoomButton);
        mMainWindow.addStuffNotUseableWhileSearching(removeRoomButton);
        roomListButtons.add(addRoomButton);
        roomListButtons.add(removeRoomButton);
        Box roomListBox = Box.createVerticalBox();
        roomListBox.add(scrollingRoomList);
        roomListBox.add(roomListButtons);
        mMainWindow.addStuffNotUseableWhileSearching(mRoomTable);
        return roomListBox;
    }

    private static void addMainWindowEventHandlers(final JFrame mainWindow)
    {
        mainWindow.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                int height = mainWindow.getHeight();
                int width = mainWindow.getWidth();
                Configuration.saveWindowSize(width, height);
            }
        });
    }

    private void setupMenu(JFrame mainWindow,
                           LoadRoomsAction loadRoomsAction,
                           SaveRoomsAction saveRoomsAction,
                           StartSearchAction startSearchAction,
                           StopSearchAction stopSearchAction,
                           ContinueSearchAction continueSearchAction)
    {
        JMenuBar menuBar = new JMenuBar();
        mainWindow.setJMenuBar(menuBar);

        // The first menu
        JMenu fileMenu = new JMenu("Arkiv");
        fileMenu.setMnemonic(KeyEvent.VK_A);
        menuBar.add(fileMenu);
        fileMenu.add(new JMenuItem(loadRoomsAction));
        fileMenu.add(new JMenuItem(saveRoomsAction));
        fileMenu.addSeparator();
        JMenuItem menuItem = new JMenuItem("Avsluta", KeyEvent.VK_X);
        setupMenuExitAction(menuItem);
        fileMenu.add(menuItem);

        // Second menu
        JMenu searchMenu = new JMenu("Sökning");
        searchMenu.setMnemonic(KeyEvent.VK_S);
        menuBar.add(searchMenu);
        searchMenu.add(new JMenuItem(startSearchAction));
        searchMenu.add(new JMenuItem(continueSearchAction));
        searchMenu.add(new JMenuItem(stopSearchAction));

        removeIcons(fileMenu);
        removeIcons(searchMenu);
    }

    private static void removeIcons(JMenu menu)
    {
        Component[] components = menu.getMenuComponents();
        for (int i = 0; i < components.length; i++)
        {
            Component component = components[i];
            if (component instanceof JMenuItem)
            {
                ((JMenuItem)component).setIcon(null);
            }
        }
    }

    private void saveRoomsFile(File selectedFile)
    {
        Properties rooms = new Properties();
        int index = 0;
        for (Iterator iterator = mRooms.iterator(); iterator.hasNext();)
        {
            String propPrefix = "room." + index + ".";
            Room room = (Room)iterator.next();
            rooms.setProperty(propPrefix + "name", room.getName());
            rooms.setProperty(propPrefix + "size", String.valueOf(room.getSize()));
            rooms.setProperty(propPrefix + "optimalLoad", String.valueOf(room.getOptimalNonEmptyLoad()));
            rooms.setProperty(propPrefix + "baseCost", String.valueOf(room.getBaseCost()));
            rooms.setProperty(propPrefix + "perPersonCost", String.valueOf(room.getPerPersonCost()));
            rooms.setProperty(propPrefix + "nonOptimalCost", String.valueOf(room.getNonOptimalPersonCost()));
            ++index;
        }

        try
        {
            rooms.store(new FileOutputStream(selectedFile), "This is a generated file");
        }
        catch (IOException e)
        {
            displayMessage("Kunde inte skriva till lokalfilen: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
    }

    private void loadRoomsFile(File selectedFile)
    {
        Properties rooms = new Properties();
        try
        {
            rooms.load(new FileInputStream(selectedFile));
        }
        catch (IOException e)
        {
            displayMessage("Kunde inte läsa från lokalfilen: " + e.getMessage());
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        int index = 0;
        String roomName;
        while ((roomName = rooms.getProperty("room." + index + ".name")) != null)
        {
            String propPrefix = "room." + index + ".";
            int size = Integer.parseInt(rooms.getProperty(propPrefix + "size", "0"));
            float optimalNonEmptyLoad = Float.parseFloat(rooms.getProperty(propPrefix + "optimalLoad", "0.85"));
            int baseCost = Integer.parseInt(rooms.getProperty(propPrefix + "baseCost", "0"));
            int perPersonCost = Integer.parseInt(rooms.getProperty(propPrefix + "perPersonCost", "0"));
            int nonOptimalPersonCost = Integer.parseInt(rooms.getProperty(propPrefix + "nonOptimalCost", "0"));

            addRoom(roomName, size, optimalNonEmptyLoad, baseCost, perPersonCost, nonOptimalPersonCost);
            ++index;
        }
    }

    private static void setupMenuExitAction(JMenuItem menuItem)
    {
        menuItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(1);
            }
        });
    }

    private void addGroup(String name, int size)
    {
        Group g1 = new Group(name, size);
        mGroups.add(g1);

        if (mGroupListModel != null)
        {
//            int tableRow = mGroups.size()-1;
            mGroupListModel.fireTableDataChanged();
        }
    }

    private void addRoom(String name, int size, float optimalNonEmptyLoad,
                         int baseCost, int perPersonCost, int nonOptimalPersonCost)
    {
        Room r1 = new Room(name, size);
        r1.setBaseCost(baseCost);
        r1.setPerPersonCost(perPersonCost);
        r1.setOptimalNonEmptyLoad(optimalNonEmptyLoad);
        r1.setNonOptimalPersonCost(nonOptimalPersonCost);
        mRooms.add(r1);

        if (mRoomListModel != null)
        {
            // int tableRow = mRooms.size()-1;
            //          mRoomListModel.fireTableRowsInserted(tableRow, tableRow);
            mRoomListModel.fireTableDataChanged();
        }
    }

    private void trySimulatedAnnealing()
    {
        if (mSimAnneal == null)
        {
            mSimAnneal = new SimAnneal(mRooms, mGroups);
        }

        Runnable runnable = new Runnable()
        {
            public void run()
            {
                mSimAnneal.addListener(Prototype.this);
                displayStatusMessage("Working...");
                mMainWindow.setEnabledForRunningSearch(true);
                long startTime = System.currentTimeMillis();
                mSimAnneal.search();
                String message = "Total time: " + (System.currentTimeMillis() - startTime) + " ms";
                System.out.println(message);
                displayMessage(message);
            }
        };

        Thread workThread = new Thread(runnable);
        // Between min priority and normal. Closer to min priority.
        workThread.setPriority((2 * Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 3);
        workThread.start();
    }

    private void displayStatusMessage(final String text)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                mStatusMessage.setText(text);
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private void displayMessage(final String message)
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                if (mMessageBox != null)
                {
                    mMessageBox.append("\n" + message);
                    try
                    {
                        int lastLineNo = mMessageBox.getLineOfOffset(mMessageBox.getText().length());
                        int startOfLastLine = mMessageBox.getLineStartOffset(lastLineNo);
                        mMessageBox.setCaretPosition(startOfLastLine);
                    }
                    catch (BadLocationException e)
                    {
                        e.printStackTrace();  //To change body of catch statement use Options | File Templates.
                    }
                }
            }
        };
        SwingUtilities.invokeLater(runnable);
    }

    private void trySmartBruteForce()
    {
        SmartBruteForce brute = new SmartBruteForce(mRooms, mGroups);
        long startTime = System.currentTimeMillis();
        brute.search();
        System.out.println("Total time: " + (System.currentTimeMillis() - startTime) + " ms");
    }

    public void onFinished(PlacementWithCost solution,
                           String bestSolutionDescription, int bestCost)
    {
        // The solution should already be displayed since this should be the
        // same solution as the last reported in onNewBest
        if (mProgressBar != null)
        {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisible(false);
        }
        mSimAnneal.removeListener(this);
        displayStatusMessage("Klar");
        displayMessage("Sökning avslutad.");
        mMainWindow.setEnabledForRunningSearch(false);
    }

    public void onNewBest(PlacementWithCost solution, String solutionDescription, int cost)
    {
        displayMessage("Ny bästa - kostnad " + cost +
                       " lösning: " + solutionDescription);
        mRoomListModel.setSolution(solution.copy());
        mGroupListModel.setSolution(solution.copy());
    }

    public void onProgress(int progress, int max)
    {
        if (mProgressBar != null)
        {
            mProgressBar.setVisible(true);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setMaximum(max + 1);
            mProgressBar.setValue(progress + 1);
        }
    }

    private void clearSearch()
    {
        if (isSimAnnealSearchRunning())
        {
            throw new IllegalStateException("Can not clear search data while " +
                                            "the search is running.");
        }

        mSimAnneal = null;
        mGroupListModel.setSolution(null);
        mRoomListModel.setSolution(null);
    }

    private static ImageIcon getIcon(String name)
    {
        ImageIcon icon = null;
        try
        {
            ClassLoader loader = Prototype.class.getClassLoader();
            URL resource = loader.getResource(name);
            if (resource != null)
            {
                icon = new ImageIcon(resource);
            }
        }
        catch (RuntimeException re)
        {
              re.printStackTrace();
        }
        return icon;
    }

    public boolean isSimAnnealSearchRunning()
    {
        return mSimAnneal != null && mSimAnneal.isSearchRunning();
    }

    class LoadRoomsAction extends AbstractAction
    {
        public LoadRoomsAction(String text, ImageIcon icon,
                      String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!isSimAnnealSearchRunning())
            {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showOpenDialog(mMainWindow);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    loadRoomsFile(fileChooser.getSelectedFile());
                }
            }
        }
    }


    class SaveRoomsAction extends AbstractAction
    {
        public SaveRoomsAction(String text, ImageIcon icon,
                      String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!isSimAnnealSearchRunning())
            {
                JFileChooser fileChooser = new JFileChooser();
                int returnVal = fileChooser.showSaveDialog(mMainWindow);
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    saveRoomsFile(fileChooser.getSelectedFile());
                }
            }
        }
    }



    class StartSearchAction extends AbstractAction
    {
        public StartSearchAction(String text, ImageIcon icon,
                      String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!isSimAnnealSearchRunning())
            {
                clearSearch(); // Forget everything
                trySimulatedAnnealing();
            }
        }
    }

    class StopSearchAction extends AbstractAction
    {
        public StopSearchAction(String text, ImageIcon icon,
                      String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (isSimAnnealSearchRunning())
            {
                // Will block until search stopped
                mSimAnneal.stopSearch();
            }
        }
    }

    class ContinueSearchAction extends AbstractAction
    {
        public ContinueSearchAction(String text, ImageIcon icon,
                      String desc, Integer mnemonic)
        {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e)
        {
            if (!isSimAnnealSearchRunning())
            {
                trySimulatedAnnealing();
            }
        }
    }
}
