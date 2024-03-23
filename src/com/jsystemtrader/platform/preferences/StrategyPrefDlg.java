package com.jsystemtrader.platform.preferences;

import static com.jsystemtrader.platform.preferences.JSTPreferences.OptimizerStrategyName;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;

import com.jsystemtrader.platform.dialog.MainFrame;
import com.jsystemtrader.platform.model.JSystemTraderException;
import com.jsystemtrader.platform.optimizer.ParamTableModel;
import com.jsystemtrader.platform.strategy.Strategy;
import com.jsystemtrader.platform.strategy.StrategyPreference;
import com.jsystemtrader.platform.strategy.StrategyPreferences;
import com.jsystemtrader.platform.util.DoubleRenderer;
import com.jsystemtrader.platform.util.MessageDialog;
import com.jsystemtrader.platform.util.SpringUtilities;
import com.jsystemtrader.platform.util.TitledSeparator;

@SuppressWarnings("serial") // Same-version serialization only
public class StrategyPrefDlg extends JDialog {

	private static final Dimension MIN_SIZE = new Dimension(800, 600);// minimum frame size

	private JButton cancelButton;
	private JButton okButton;
	private JButton resetButton;
	private JComboBox<Strategy> strategyCombo;

	private ParamTableModel paramTableModel;
	private StrategyTickerTableModel tickerPrefTableModel;

	private JTable strategyPreferenceTable;
	private ArrayList<Strategy> strategyList;

	public StrategyPrefDlg(JFrame parent) throws JSystemTraderException {
		super(parent);
		if (parent instanceof MainFrame) {
			strategyList = ((MainFrame) parent).getTradingTableModel().getAllStrategies();
		}
		init();
		initParams();
	
		pack();
		assignListeners();
		setLocationRelativeTo(null);
	}

	private void assignListeners() {

		addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				strategyCombo.requestFocus();
			}
		});

		strategyCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initParams();
			}
		});

		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTickerPreferences();
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

//		addTicker_bt.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				updateTickerTable(true);
//			}
//		});

//		deleteTicker_bt.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				updateTickerTable(false);
//			}
//		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	private void saveTickerPreferences() {
		((Strategy) strategyCombo.getSelectedItem()).saveStrategyPreferences(tickerPrefTableModel.getPreferences());
		((Strategy) strategyCombo.getSelectedItem()).saveParamsToPreferences(paramTableModel.getParams());
	}

	private void init() throws JSystemTraderException {

		setModal(true);
	
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Strategy Preferences");

		getContentPane().setLayout(new BorderLayout());

		JPanel northPanel = new JPanel(new SpringLayout());
		JPanel centerPanel = new JPanel(new SpringLayout());
		JPanel southPanel = new JPanel(new BorderLayout());

		// strategy panel and its components
		JPanel strategyPanel = new JPanel(new SpringLayout());
		JLabel strategyLabel = new JLabel("Strategy Name:", JLabel.TRAILING);

		strategyCombo = new JComboBox<Strategy>();
		if (strategyList != null) {
			for (int idx = 0; idx < strategyList.size(); idx++) {
				strategyCombo.addItem(strategyList.get(idx));
			}
		}

		Dimension size = new Dimension(250, 20);
		strategyCombo.setPreferredSize(size);
		strategyCombo.setMaximumSize(size);

		String lastStrategyName = PreferencesHolder.getInstance().get(OptimizerStrategyName);
		if (lastStrategyName.length() > 0) {
			strategyCombo.setSelectedItem(lastStrategyName);
		}

		strategyPanel.add(strategyLabel);
		strategyPanel.add(strategyCombo);

		strategyLabel.setLabelFor(strategyCombo);
		SpringUtilities.makeOneLineGrid(strategyPanel, 2);

		// strategy parametrs panel and its components
		JPanel tickerPreferencePanel = new JPanel(new SpringLayout());

		JScrollPane tickerPrefScrollPane = new JScrollPane();

		tickerPrefTableModel = new StrategyTickerTableModel();
		strategyPreferenceTable = new JTable(tickerPrefTableModel);
		tickerPrefScrollPane.getViewport().add(strategyPreferenceTable);
		tickerPrefScrollPane.setPreferredSize(new Dimension(100, 100));

		tickerPreferencePanel.add(tickerPrefScrollPane);
		SpringUtilities.makeOneLineGrid(tickerPreferencePanel, 1);

		northPanel.add(new TitledSeparator(new JLabel("Strategy definition & tickers")));
		northPanel.add(strategyPanel);
		northPanel.add(tickerPreferencePanel);
		SpringUtilities.makeCompactGrid(northPanel, 3, 1, 5, 12, 8, 8);

		////////////////////////// Center pane ////////
		// strategy parametrs panel and its components
		JPanel strategyParamPanel = new JPanel(new SpringLayout());
		JScrollPane paramScrollPane = new JScrollPane();

		paramTableModel = new ParamTableModel();
		JTable paramTable = new JTable(paramTableModel);
		paramTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		paramTable.setDefaultRenderer(Double.class, new DoubleRenderer());
		paramScrollPane.getViewport().add(paramTable);
		paramScrollPane.setPreferredSize(new Dimension(100, 100));

		strategyParamPanel.add(paramScrollPane);
		SpringUtilities.makeOneLineGrid(strategyParamPanel, 1);

		centerPanel.add(new TitledSeparator(new JLabel("Custom Preference Setttings")));
		centerPanel.add(strategyParamPanel);
		SpringUtilities.makeCompactGrid(centerPanel, 2, 1, 5, 12, 8, 8);

		/////////////////////////////////////// southPanel//////////////////
		cancelButton = new JButton("Cancel");
		cancelButton.setMnemonic('C');

		resetButton = new JButton("Rest Initial Values");
		resetButton.setMnemonic('R');
		
		okButton = new JButton("OK");
		okButton.setMnemonic('O');

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(resetButton);

		southPanel.add(buttonsPanel, BorderLayout.SOUTH);

		////////////////////////////////////////////////////////////////////////////////
		getContentPane().add(northPanel, BorderLayout.NORTH);
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

		getRootPane().setDefaultButton(okButton);
		getContentPane().setPreferredSize(MIN_SIZE);
		getContentPane().setMinimumSize(getContentPane().getPreferredSize());
	}

	private void initParams() {
		try {
			int itemCount = strategyCombo.getItemCount();

			// init ticker table
			if (itemCount == 0) {
				throw new JSystemTraderException("No strategies found.");
			}
			Strategy strategy = (Strategy) strategyCombo.getSelectedItem();
			StrategyPreferences pref = strategy.getStrategyPrefereces();
			tickerPrefTableModel.setTickersParams(pref);

			/*
			 * //set drop-down list as editor for timeZone column; TableColumnModel
			 * columnModel = strategyPreferenceTable.getColumnModel(); JComboBox box = new
			 * JComboBox( TradingInterval.TIME_ZONES);; box.setEditable( false );
			 * 
			 * DefaultCellEditor cellEditor = new DefaultCellEditor( box);
			 * columnModel.getColumn(5).setCellEditor(cellEditor);
			 */

			// init parameters
			paramTableModel.setParams(strategy.initParams());

		} catch (Exception e) {
			MessageDialog.showError(this, e.getMessage());
		}
	}

}