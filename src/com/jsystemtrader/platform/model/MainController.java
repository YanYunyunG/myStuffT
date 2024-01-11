package com.jsystemtrader.platform.model;

import static com.jsystemtrader.platform.preferences.JSTPreferences.LookAndFeelClassName;
import static com.jsystemtrader.platform.preferences.JSTPreferences.LookAndFeelMacStyle;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowHeight;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowWidth;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowX;
import static com.jsystemtrader.platform.preferences.JSTPreferences.MainWindowY;
import static com.jsystemtrader.platform.preferences.JSTPreferences.OptimizerHeight;
import static com.jsystemtrader.platform.preferences.JSTPreferences.OptimizerWidth;
import static com.jsystemtrader.platform.preferences.JSTPreferences.OptimizerX;
import static com.jsystemtrader.platform.preferences.JSTPreferences.OptimizerY;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

//import com.birosoft.liquid.LiquidLookAndFeel;
import com.birosoft.liquid.LiquidLookAndFeel;
import com.jsystemtrader.platform.backdata.BackDataDialog;
import com.jsystemtrader.platform.backtest.BackTestStrategyRunner;
import com.jsystemtrader.platform.chart.StrategyPerformanceChart;
import com.jsystemtrader.platform.dialog.MainFrame;
import com.jsystemtrader.platform.dialog.TradingModeDialog;
import com.jsystemtrader.platform.opentick.OTBackDataDialog;
import com.jsystemtrader.platform.optimizer.OptimizerDialog;
import com.jsystemtrader.platform.preferences.PreferencesDialog;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.preferences.StrategyPrefDlg;
import com.jsystemtrader.platform.startup.JSystemTrader;
import com.jsystemtrader.platform.strategy.Strategy;
import com.jsystemtrader.platform.strategy.StrategyRunner;
import com.jsystemtrader.platform.util.Browser;
import com.jsystemtrader.platform.util.MessageDialog;

/**
 * Acts as a controller in the Model-View-Controller pattern
 */
public class MainController {
	private final MainFrame mainFrame;
	private final TradingModeDialog tradingModeDialog;
	private final PreferencesHolder preferences = PreferencesHolder.getInstance();

	public MainController() throws JSystemTraderException, IOException {
		boolean lookAndFeelMacTitle = preferences.getBool(LookAndFeelMacStyle);
		if (lookAndFeelMacTitle)
			LiquidLookAndFeel.setLiquidDecorations(true, "mac");

		String lookAndFeelClassName = preferences.get(LookAndFeelClassName);
		_setLookAndFeel(lookAndFeelClassName);

		mainFrame = new MainFrame();
		tradingModeDialog = new TradingModeDialog(mainFrame);

		mainFrame.setSelectedLookAndFeel(lookAndFeelClassName);
		mainFrame.setMacWindowTitle(lookAndFeelMacTitle);

		assignListeners();
	}

	private void assignListeners() {
		mainFrame.IBHistoricalDataAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Dispatcher.setTradingMode();
					new BackDataDialog(mainFrame);
				} catch (Throwable t) {
					Dispatcher.getReporter().report(t);
					MessageDialog.showError(mainFrame, t.getMessage());
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

		mainFrame.OTHistoricalDataAction(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					new OTBackDataDialog(mainFrame);
				} catch (Throwable t) {
					Dispatcher.getReporter().report(t);
					MessageDialog.showError(mainFrame, t.getMessage());
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

		tradingModeDialog.selectFileAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser(JSystemTrader.getAppPath());
				fileChooser.setDialogTitle("Select Historical Data File");

				String filename = tradingModeDialog.getFileName();
				if (filename.length() != 0) {
					fileChooser.setSelectedFile(new File(filename));
				}

				if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					tradingModeDialog.setFileName(file.getAbsolutePath());
				}
			}
		});

		mainFrame.runStrategiesAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					ArrayList<Strategy> selectedStrategies = mainFrame.getTradingTableModel().getSelectedStrategies();
					if (selectedStrategies.size() == 0) {
						MessageDialog.showError(mainFrame, "At least one strategy must be selected to run.");
						return;
					}

					tradingModeDialog.setVisible(true);
					if (tradingModeDialog.getAction() == JOptionPane.CANCEL_OPTION) {
						return;
					}

					mainFrame.getTradingTableModel().saveStrategyStatus();
					mainFrame.getTradingTableModel().reset();

					Dispatcher.setActiveStrategies(selectedStrategies.size());
					Dispatcher.Mode mode = tradingModeDialog.getMode();
					if (mode == Dispatcher.Mode.BACK_TEST) {
						Dispatcher.setBackTestingMode(tradingModeDialog);
						for (Strategy strategy : selectedStrategies) {
							new BackTestStrategyRunner(strategy).start();
						}
					}

					if (mode == Dispatcher.Mode.TRADE) {
						Dispatcher.setTradingMode();
						for (Strategy strategy : selectedStrategies) {
							new Thread(new StrategyRunner(strategy)).start();
						}
					}
				} catch (Throwable t) {
					Dispatcher.getReporter().report(t);
					MessageDialog.showError(mainFrame, t.toString());
					Dispatcher.fireModelChanged(ModelListener.Event.STRATEGIES_END, null);
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

		mainFrame.optimizerAction(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					OptimizerDialog optimizerDialog = new OptimizerDialog(mainFrame);
					optimizerDialog.addWindowListener(new WindowAdapter() {

						@Override
						public void windowClosed(WindowEvent arg0) {
							saveOptimizerDimensions(arg0);
						}

						@Override
						public void windowClosing(WindowEvent arg0) {
							saveOptimizerDimensions(arg0);
						}

						private void saveOptimizerDimensions(WindowEvent arg0) {
							preferences.set(OptimizerHeight, arg0.getWindow().getHeight());
							preferences.set(OptimizerWidth, arg0.getWindow().getWidth());
							preferences.set(OptimizerX, arg0.getWindow().getX());
							preferences.set(OptimizerY, arg0.getWindow().getY());
						}
					});
				} catch (Throwable t) {
					Dispatcher.getReporter().report(t);
					MessageDialog.showError(mainFrame, t.getMessage());
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

		mainFrame.exitAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});

		mainFrame.exitAction(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exit();
			}
		});

		mainFrame.strategyChartAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					TradingTableModel ttm = mainFrame.getTradingTableModel();
					int selectedRow = mainFrame.getTradingTable().getSelectedRow();
					if (selectedRow < 0) {
						String message = "No strategy is selected.";
						MessageDialog.showError(mainFrame, message);
						return;
					}
					Strategy strategy = ttm.getStrategyForRow(mainFrame.getTradingTable().getSelectedRow());
					StrategyPerformanceChart spChart = new StrategyPerformanceChart(strategy);
					JFrame chartFrame = spChart.getChartFrame(mainFrame);

					chartFrame.setVisible(true);
				} catch (Exception ex) {
					Dispatcher.getReporter().report(ex);
					MessageDialog.showError(mainFrame, ex.toString());
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}

			}
		});

		mainFrame.doubleClickTableAction(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					try {
						mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						TradingTableModel ttm = mainFrame.getTradingTableModel();
						int selectedRow = mainFrame.getTradingTable().getSelectedRow();
						if (selectedRow < 0) {
							return;
						}
						Strategy strategy = ttm.getStrategyForRow(mainFrame.getTradingTable().getSelectedRow());
						StrategyPerformanceChart spChart = new StrategyPerformanceChart(strategy);
						JFrame chartFrame = spChart.getChartFrame(mainFrame);

						chartFrame.setVisible(true);
					} catch (Exception ex) {
						Dispatcher.getReporter().report(ex);
						MessageDialog.showError(mainFrame, ex.toString());
					} finally {
						mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}
				}
			}
		});

		mainFrame.lookAndFeelAction(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String newLook = mainFrame.getSelectedLookAndFeel();
					preferences.set(LookAndFeelClassName, newLook);
					_setLookAndFeel(newLook);
				}
			}
		});

		mainFrame.macWindowTitleAction(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				preferences.set(LookAndFeelMacStyle, e.getStateChange() == ItemEvent.SELECTED);
				MessageDialog.showMessage(mainFrame, "Liquid mac decoration will take effect after restarting JST");
			}
		});

		mainFrame.preferencesAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					JDialog preferencesDialog = new PreferencesDialog(mainFrame);
					preferencesDialog.setVisible(true);
				} catch (Throwable t) {
					MessageDialog.showError(mainFrame, t.getMessage());
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

		mainFrame.strageyPreferencesAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					JDialog strategyPreferencesDialog = new StrategyPrefDlg(mainFrame);
					strategyPreferencesDialog.setVisible(true);
				} catch (Throwable t) {
					MessageDialog.showError(mainFrame, t.getMessage());
				} finally {
					mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});

	}

	private void _setLookAndFeel(String lookAndFeelName) {
		try {
			UIManager.setLookAndFeel(lookAndFeelName);
		} catch (Throwable t) {
			MessageDialog.showMessage(null,
			        t.getMessage() + ": Unable to set custom look & feel. The default L&F will be used.");
		}

		// Set the color scheme explicitly
		ColorUIResource color = new ColorUIResource(102, 102, 153);
		UIManager.put("Label.foreground", color);
		UIManager.put("TitledBorder.titleColor", color);

		if (mainFrame != null) {
			SwingUtilities.updateComponentTreeUI(mainFrame);
			mainFrame.pack();
		}

		if (tradingModeDialog != null) {
			SwingUtilities.updateComponentTreeUI(tradingModeDialog);
			tradingModeDialog.pack();
		}
	}

	private void exit() {
		preferences.set(MainWindowHeight, mainFrame.getSize().height);
		preferences.set(MainWindowWidth, mainFrame.getSize().width);
		preferences.set(MainWindowX, mainFrame.getX());
		preferences.set(MainWindowY, mainFrame.getY());
		Dispatcher.exit();
	}

}
