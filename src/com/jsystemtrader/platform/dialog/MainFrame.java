package com.jsystemtrader.platform.dialog;

import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowHeight;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowWidth;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowX;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowY;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import com.jsystemtrader.platform.model.Dispatcher;
import com.jsystemtrader.platform.model.JSystemTraderException;
import com.jsystemtrader.platform.model.ModelListener;
import com.jsystemtrader.platform.model.TradingTableModel;
import com.jsystemtrader.platform.optimizer.StrategyParams;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.startup.JSystemTrader;
import com.jsystemtrader.platform.strategy.Strategy;
import com.jsystemtrader.platform.util.AutofitTableColumns;
import com.jsystemtrader.platform.util.ClassFinder;
import com.jsystemtrader.platform.util.DoubleRenderer;

/**
 * Main application window.
 */
public class MainFrame extends JFrame implements ModelListener {
	private JMenuItem optimizerMenuItem, exitMenuItem, IBDataMenuItem, OTDataMenuItem, preferencesMenuItem, resizeTableMenuItem;

	private JMenuItem strategyPreferencesMenuItem;
	private JButton runButton, viewChartButton;
	private TradingTableModel tradingTableModel;
	private JTable tradingTable;
	private final String fileSep = System.getProperty("file.separator");
	private final PreferencesHolder preferences = PreferencesHolder.getInstance();
	private int limitTableResize;

	public MainFrame() throws JSystemTraderException {
		Dispatcher.addListener(this);
		init();
		populateStrategies();
		pack();
		setLocationRelativeTo(null);

		int lastHeight = preferences.getInt(MainWindowHeight);
		int lastWidth = preferences.getInt(MainWindowWidth);
		int lastX = preferences.getInt(MainWindowX);
		int lastY = preferences.getInt(MainWindowY);

		if (lastHeight > 0 && lastWidth > 0)
			setBounds(lastX, lastY, lastWidth, lastHeight);

		setVisible(true);
		limitTableResize = 0;
	}

	public void modelChanged(ModelListener.Event event, Object value) {
		switch (event) {
		case STRATEGY_UPDATE:
			Strategy strategy = (Strategy) value;
			tradingTableModel.updateStrategy(strategy);
			if (limitTableResize == 0) {
				AutofitTableColumns.autoResizeTable(tradingTable, true);
				limitTableResize = 1;
			}
			break;
		case STRATEGIES_START:
			runButton.setEnabled(false);
			tradingTableModel.setCanEditor(false);
			break;
		case STRATEGIES_END:
			runButton.setEnabled(true);
			tradingTableModel.setCanEditor(true);
			AutofitTableColumns.autoResizeTable(tradingTable, true);
			break;
		}
	}

	public void runStrategiesAction(ActionListener action) {
		runButton.addActionListener(action);
	}

	public void optimizerAction(ActionListener action) {
		optimizerMenuItem.addActionListener(action);
	}

	public void IBHistoricalDataAction(ActionListener action) {
		IBDataMenuItem.addActionListener(action);
	}

	public void OTHistoricalDataAction(ActionListener action) {
		OTDataMenuItem.addActionListener(action);
	}

	public void exitAction(ActionListener action) {
		exitMenuItem.addActionListener(action);
	}

	public void exitAction(WindowAdapter action) {
		addWindowListener(action);
	}

	public void strategyChartAction(ActionListener action) {
		viewChartButton.addActionListener(action);
	}

	public void doubleClickTableAction(MouseAdapter action) {
		tradingTable.addMouseListener(action);
	}

	public void strageyPreferencesAction(ActionListener action) {
		strategyPreferencesMenuItem.addActionListener(action);
	}

	public void preferencesAction(ActionListener action) {
		preferencesMenuItem.addActionListener(action);
	}

	private URL getImageURL(String imageFileName) throws JSystemTraderException {
		URL imgURL = ClassLoader.getSystemResource(imageFileName);
		if (imgURL == null) {
			String msg = "Could not locate " + imageFileName
			        + ". Make sure that JSystemTrader directory is in the classpath.";
			throw new JSystemTraderException(msg);
		}
		return imgURL;
	}

	private void populateStrategies() throws JSystemTraderException {
		try {
			ClassFinder classFinder = new ClassFinder();
			List<Class<?>> strategies = classFinder.getClasses("com.jsystemtrader.strategy",
			        "com.jsystemtrader.platform.strategy.Strategy");

			for (Class<?> strategyClass : strategies) {
				Constructor<?> constructor = strategyClass.getConstructor(StrategyParams.class);
				Strategy strategy = (Strategy) constructor.newInstance(new StrategyParams());
				tradingTableModel.addStrategy(strategy);
			}
			AutofitTableColumns.autoResizeTable(tradingTable, true);
		} catch (Exception e) {
			throw new JSystemTraderException("Could not populate strategies: " + e.getMessage());
		}
	}

	public TradingTableModel getTradingTableModel() {
		return tradingTableModel;
	}

	public JTable getTradingTable() {
		return tradingTable;
	}

	private void init() throws JSystemTraderException {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());

		int menuKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
		// file menu //////////////////////////////////////////////////////////
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setMnemonic('X');
		exitMenuItem.setAccelerator(KeyStroke.getKeyStroke('W', menuKeyMask));
		fileMenu.add(exitMenuItem);

		// tools menu /////////////////////////////////////////////////////////
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic('T');
		JMenu historicalDataMenu = new JMenu("Historical Data");
		historicalDataMenu.setMnemonic('H');
		IBDataMenuItem = new JMenuItem("Interactive Brokers...");
		OTDataMenuItem = new JMenuItem("Open Tick...");
		IBDataMenuItem.setAccelerator(KeyStroke.getKeyStroke('I', menuKeyMask));
		OTDataMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', menuKeyMask));
		historicalDataMenu.add(IBDataMenuItem);
		historicalDataMenu.add(OTDataMenuItem);
		optimizerMenuItem = new JMenuItem("Strategy Optimizer...");
		optimizerMenuItem.setMnemonic('S');
		optimizerMenuItem.setAccelerator(KeyStroke.getKeyStroke('O', menuKeyMask));
		toolsMenu.add(historicalDataMenu);
		// toolsMenu.addSeparator();
		toolsMenu.add(optimizerMenuItem);
		toolsMenu.addSeparator();
		resizeTableMenuItem = new JMenuItem("Resize Columns");
		resizeTableMenuItem.setMnemonic('R');
		resizeTableMenuItem.setAccelerator(KeyStroke.getKeyStroke('L', menuKeyMask));
		toolsMenu.add(resizeTableMenuItem);
		preferencesMenuItem = new JMenuItem("Preferences");
		preferencesMenuItem.setMnemonic('P');
		preferencesMenuItem.setAccelerator(KeyStroke.getKeyStroke('P', menuKeyMask));
		toolsMenu.add(preferencesMenuItem);

		strategyPreferencesMenuItem = new JMenuItem("Strategy Preferences");
		strategyPreferencesMenuItem.setMnemonic('T');
		strategyPreferencesMenuItem.setAccelerator(KeyStroke.getKeyStroke('T', menuKeyMask));
		toolsMenu.add(strategyPreferencesMenuItem);

		resizeTableMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AutofitTableColumns.autoResizeTable(tradingTable, true);
			}
		});

		// menu bar ///////////////////////////////////////////////////////////
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(toolsMenu);
		setJMenuBar(menuBar);

		// buttons panel //////////////////////////////////////////////////////
		JPanel buttonsPanel = new JPanel();
		viewChartButton = new JButton("Chart");
		viewChartButton.setMnemonic('C');
		runButton = new JButton("Run");
		runButton.setMnemonic('R');
		runButton.requestFocusInWindow();
		buttonsPanel.add(runButton);
		buttonsPanel.add(viewChartButton);

		JScrollPane tradingScroll = new JScrollPane();
		tradingScroll.setAutoscrolls(true);
		JPanel tradingPanel = new JPanel(new BorderLayout());
		tradingPanel.add(tradingScroll, BorderLayout.CENTER);

		tradingTableModel = new TradingTableModel();
		tradingTable = new JTable(tradingTableModel);
		tradingTable.setDefaultRenderer(Double.class, new DoubleRenderer());
		tradingTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Make some columns wider than the rest, so that the info fits in.
		TableColumnModel columnModel = tradingTable.getColumnModel();

		columnModel.getColumn(0).setPreferredWidth(20); // Activation column
		columnModel.getColumn(1).setPreferredWidth(150); // strategy name column
		columnModel.getColumn(2).setPreferredWidth(30); // ticker column
		columnModel.getColumn(3).setPreferredWidth(40); // bar size column
		columnModel.getColumn(4).setPreferredWidth(120); // last bar time column
		columnModel.getColumn(5).setPreferredWidth(60); // last bar close column
		columnModel.getColumn(6).setPreferredWidth(30); // Position
		columnModel.getColumn(7).setPreferredWidth(30); // Trades
		columnModel.getColumn(8).setPreferredWidth(30); // P&L
		columnModel.getColumn(9).setPreferredWidth(30); // Max DD
		columnModel.getColumn(10).setPreferredWidth(30); // PF
		columnModel.getColumn(11).setPreferredWidth(30); // Kelly
		columnModel.getColumn(12).setPreferredWidth(120); // Trade distribution

		tradingScroll.getViewport().add(tradingTable);

		Image appIcon = Toolkit.getDefaultToolkit().getImage(getImageURL("resources" + fileSep + "JSystemTrader.jpg"));
		setIconImage(appIcon);

		getContentPane().add(tradingPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		getContentPane().setPreferredSize(new Dimension(800, 300));
		setTitle(JSystemTrader.APP_NAME);
		getRootPane().setDefaultButton(runButton);
		pack();
	}

}
