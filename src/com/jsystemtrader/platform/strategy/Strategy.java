package com.jsystemtrader.platform.strategy;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.ib.client.Contract;
import com.jsystemtrader.indicator.ChartableIndicator;
import com.jsystemtrader.indicator.Indicator;
import com.jsystemtrader.platform.model.Dispatcher;
import com.jsystemtrader.platform.model.JSystemTraderException;
import com.jsystemtrader.platform.optimizer.StrategyParams;
import com.jsystemtrader.platform.position.Position;
import com.jsystemtrader.platform.position.PositionManager;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.quote.BarSize;
import com.jsystemtrader.platform.quote.PriceBar;
import com.jsystemtrader.platform.quote.QuoteHistory;
import com.jsystemtrader.platform.report.Report;
import com.jsystemtrader.platform.schedule.TradingInterval;
import com.jsystemtrader.platform.schedule.TradingSchedule;

/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy {

	public static final String SECOND_QH = "_second";

	private static final String NOT_APPLICABLE = "N/A";

	private List<String> strategyReportHeaders;
	private StrategyParams params;

	protected final QuoteHistory quoteHistory;
	protected final QuoteHistory secondQuoteHistory;

	private int Id;
	private final DecimalFormat nf2, nf5;
	private Report strategyReport;
	protected Report eventReport;
	private final List<Object> strategyReportColumns = new ArrayList<Object>();

	private boolean onlyRTHPriceBars, isActive;
	private Calendar backTestCalendar;
	private Contract contract;

	private BarSize barSize;
	private final TradingSchedule tradingSchedule;
	private final List<ChartableIndicator> indicators;
	private final PositionManager positionManager;
	private final String name;
	protected int position;
	private boolean hasValidIndicators;

	private boolean hasSecondValidIndicators;

	private int myCount = 0;

	// following added by yan.
	protected int shareNumber;
	protected boolean autoTrade = true;
	protected String tradingStartTime = "9:00";;
	protected String tradingEndTime = "23:56";

	/**
	 * Framework calls this method when a new strategy-specified bar becomes
	 * available.
	 */
	public abstract void onBar();

	/**
	 * Framework calls this method when a new 5-second bar becomes available.
	 */
	public void onMarketChange() {
	}

	/**
	 * Framework calls this method to obtain strategy parameter ranges.
	 */
	public abstract StrategyParams initParams();

	/**
	 * Framework calls this method to obtain strategy trading time interval
	 */
	public abstract TradingInterval initTradingInterval() throws JSystemTraderException;

	public Strategy() {
		strategyReportHeaders = new ArrayList<String>();
		strategyReportHeaders.add("Date");
		strategyReportHeaders.add("Last Bar");
		strategyReportHeaders.add("Position");
		strategyReportHeaders.add("Trades");
		strategyReportHeaders.add("Avg Fill Price");
		strategyReportHeaders.add("Trade P&L");
		strategyReportHeaders.add("Total P&L");
		strategyReportHeaders.add("Trades distribution");

		name = getClass().getSimpleName();
		tradingSchedule = new TradingSchedule(this);
		indicators = new ArrayList<ChartableIndicator>();
		params = new StrategyParams();
		positionManager = new PositionManager(this);
		quoteHistory = new QuoteHistory(name);
		secondQuoteHistory = new QuoteHistory(name + SECOND_QH);

		nf2 = (DecimalFormat) NumberFormat.getNumberInstance();
		nf2.setMaximumFractionDigits(2);
		nf5 = (DecimalFormat) NumberFormat.getNumberInstance();
		nf5.setMaximumFractionDigits(5);

		eventReport = Dispatcher.getReporter();
		isActive = true;

	}

	public void setContractorTicker(String ticker) {
		if (contract != null) {
			contract.m_symbol = ticker;
		}
	}

	public void setShareNumber(int shareNumber) {
		this.shareNumber = shareNumber;
	}

	public int getShareNumber() {
		return shareNumber;
	}

	public void setAutoTrade(boolean autoTrade) {
		this.autoTrade = autoTrade;
	}

	public void setReport(Report strategyReport) {
		this.strategyReport = strategyReport;
	}

	public List<String> getStrategyReportHeaders() {
		return strategyReportHeaders;
	}

	public boolean isActive() {
		return isActive;
	}

	public boolean hasValidIndicators() {
		return hasValidIndicators;
	}

	public int getPosition() {
		return position;
	}

	public void closeOpenPositions() {
		position = 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(" ").append(name);
		sb.append(" [");
		sb.append(contract.m_symbol).append("-");
		sb.append(contract.m_secType).append("-");
		sb.append(contract.m_exchange).append("-");
		sb.append(barSize.toString()).append("]");

		return sb.toString();
	}

	public void update() {
		PositionManager pm = getPositionManager();
		boolean hasTraded = pm.getHasTraded();

		strategyReportColumns.clear();
		strategyReportColumns.add(quoteHistory.getLastPriceBar().getClose());
		strategyReportColumns.add(positionManager.getPosition());
		strategyReportColumns.add(positionManager.getTrades());
		strategyReportColumns.add(hasTraded ? nf5.format(positionManager.getAvgFillPrice()) : NOT_APPLICABLE);
		strategyReportColumns.add(hasTraded ? nf5.format(positionManager.getProfitAndLoss()) : NOT_APPLICABLE);
		strategyReportColumns.add(nf5.format(positionManager.getTotalProfitAndLoss()));
		strategyReportColumns.add(hasTraded ? getTradeDistribution() : NOT_APPLICABLE);

		String fieldBreak = eventReport.getRenderer().getFieldBreak();
		String msg = getName() + ": state updated" + fieldBreak;
		msg += "Last bar:  " + quoteHistory.getLastPriceBar() + fieldBreak;

		for (ChartableIndicator chartableIndicator : indicators) {
			String formattedValue = NOT_APPLICABLE;
			if (!chartableIndicator.isEmpty()) {
				synchronized (nf2) {
					formattedValue = nf2.format(chartableIndicator.getIndicator().getValue());
				}
			}
			strategyReportColumns.add(formattedValue);
			msg += chartableIndicator.getName() + ": " + formattedValue + " ";
		}

		eventReport.report(msg);
		strategyReport.report(strategyReportColumns, getCalendar());
	}

	public String getTradeDistribution() {
		StringBuilder msg = new StringBuilder();
		double profittableTradeMeanValue = positionManager.getProfitableTradeMeanValue();
		double profitableStandardDeviation = positionManager.getProfitableTradeStandardDeviation();
		double unprofittableTradeMeanValue = positionManager.getUnprofitableTradeMeanValue();
		double unprofitableStandardDeviation = positionManager.getUnprofitableTradeStandardDeviation();

		msg.append(profittableTradeMeanValue > 0.1 ? nf2.format(profittableTradeMeanValue)
		        : nf5.format(profittableTradeMeanValue));
		msg.append(" ~ ");
		msg.append(profitableStandardDeviation > 0.1 ? nf2.format(profitableStandardDeviation)
		        : nf5.format(profitableStandardDeviation));
		msg.append(" (");
		msg.append(positionManager.getPercentProfitable());
		msg.append("%) ");
		msg.append(unprofittableTradeMeanValue < -0.1 ? nf2.format(unprofittableTradeMeanValue)
		        : nf5.format(unprofittableTradeMeanValue));
		msg.append(" ~ ");
		msg.append(unprofitableStandardDeviation > 0.1 ? nf2.format(unprofitableStandardDeviation)
		        : nf5.format(unprofitableStandardDeviation));

		return msg.toString();
	}

	public void setParams(StrategyParams params) {
		this.params = params;
	}

	public StrategyParams getParams() {
		return params;
	}

	public PositionManager getPositionManager() {
		return positionManager;
	}

	public TradingSchedule getTradingSchedule() {
		return tradingSchedule;
	}

	private void setTradingInterval(TradingInterval tradingInterval) throws JSystemTraderException {
		tradingSchedule.setTradingInterval(tradingInterval);
		backTestCalendar = Calendar.getInstance();
		TimeZone tz = getTradingSchedule().getTimeZone();
		backTestCalendar.setTimeZone(tz);
	}

	protected void addIndicator(String name, Indicator indicator, int chart) {
		ChartableIndicator chartableIndicator = new ChartableIndicator(name, indicator, chart);
		indicators.add(chartableIndicator);
		strategyReportHeaders.add(chartableIndicator.getName());
	}

	public List<ChartableIndicator> getIndicators() {
		return indicators;
	}

	public void setBarSize(BarSize barSize) {
		this.barSize = barSize;
	}

	protected void setStrategy(Contract contract, BarSize barSize, boolean onlyRTHPriceBars)
	        throws JSystemTraderException {
		this.contract = contract;
		this.barSize = barSize;
		this.onlyRTHPriceBars = onlyRTHPriceBars;

		String exchange = contract.m_exchange;
		boolean isForex = exchange.equalsIgnoreCase("IDEAL") || exchange.equalsIgnoreCase("IDEALPRO");
		quoteHistory.setIsForex(isForex);

		// setDefaultValuesFromPreferences();

		/*
		 * if ( tradingStartTime != null && tradingEndTime != null ) {
		 * setTradingInterval( new TradingInterval(tradingStartTime,tradingEndTime,
		 * "America/New_York", false)); }else
		 */
		{
			setTradingInterval(initTradingInterval());
		}
	}

	public QuoteHistory getQuoteHistory() {
		return quoteHistory;
	}

	public Calendar getCalendar() {
		boolean isTradeMode = (Dispatcher.getMode() == Dispatcher.Mode.TRADE);
		return isTradeMode ? tradingSchedule.getCalendar() : backTestCalendar;
	}

	public BarSize getBarSize() {
		return barSize;
	}

	public BarSize getSecondBarSize() {
		return null;
	}

	public QuoteHistory getSecondHQ() {
		return secondQuoteHistory;
	}

	public void setId(int Id) {
		this.Id = Id;
	}

	public int getId() {
		return Id;
	}

	public Contract getContract() {
		return contract;
	}

	public boolean getOnlyRTHPriceBars() {
		return onlyRTHPriceBars;
	}

	public String getName() {
		return name;
	}

	public PriceBar getLastPriceBar() {
		return quoteHistory.getLastPriceBar();
	}

	public void AddNewBarCount() {
		myCount++;
	}

	public void updateIndicators(boolean isForSecondQH) {
		for (ChartableIndicator chartableIndicator : indicators) {
			Indicator indicator = chartableIndicator.getIndicator();
			if (shouldCalculate(indicator, isForSecondQH)) {
				indicator.calculate();
				indicator.addToHistory(indicator.getDate(), indicator.getValue());
			}
		}
	}

	private boolean shouldCalculate(Indicator indicator, boolean isForSecondQH) {
		// we are updating indicator from history data
		if (myCount < 1) {
			return !(indicator.useSecondQH() ^ isForSecondQH);
		}

		if (indicator.useSecondQH()) {
			boolean test = myCount % 3 == 0;
			if (test) {
				System.out.println(" ===  MyCount = " + String.valueOf(myCount) + "calculated 15mint indicator");
			}
			return test;
		} else {
			return true;
		}

	}

	public void validateIndicators() {
		validateIndicators(false);
	}

	public void validateIndicators(boolean isForSecondQH) {
		hasValidIndicators = true;
		boolean isQuoteHistoryValid = quoteHistory.isValid();

		if (isQuoteHistoryValid) {
			try {
				updateIndicators(isForSecondQH);
			} catch (ArrayIndexOutOfBoundsException aie) {
				String message = "Quote history length is insufficient to calculate the indicator";
				quoteHistory.getValidationMessages().add(message);

				hasValidIndicators = false;
			} catch (Exception e) {
				hasValidIndicators = false;
				eventReport.report(e);
			}
		} else {
			String msg = name + ": PriceBar history is invalid: " + quoteHistory.getValidationMessages();
			eventReport.report(getName() + ": " + msg);
		}
	}

	int openHour = 9;
	int openMin = 30;
	int closeHour = 15;
	int closeMin = 56;

	public boolean isTimeToTrade() {
		// check to see the last pricebar's time is between my time.
		Date date = new Date(quoteHistory.getLastPriceBar().getDate());
		int hour = date.getHours();
		int min = date.getMinutes();

		boolean cantrade = false;
		if ((hour >= openHour) || (hour <= closeHour)) {
			cantrade = true;
			if (hour == openHour) {
				cantrade = min > openMin;
			} else if (hour == closeHour) {
				cantrade = min < closeMin;
			}
		}
		return cantrade;

	}

	public boolean canTrade() {
		boolean canTrade = true;

		boolean timeToClose = tradingSchedule.isTimeToClose();
		if (timeToClose) {
			System.out.println("beyond close time = " + new Date(quoteHistory.getLastPriceBar().getDate()).toString());

		}
		if (timeToClose && (positionManager.getPosition() != 0)) {
			position = 0;
			String msg = "End of trading interval. Closing current position.";
			eventReport.report(getName() + ": " + msg);
			canTrade = false;
		}

		if (!tradingSchedule.isTimeToTrade()) {
			canTrade = false;
		}

		return canTrade;
	}

	public long getTimeSinceLastPosition() {
		long secondsSinceLastPosition = 0;
		PositionManager positionManager = getPositionManager();
		List<Position> positionHistory = positionManager.getPositionsHistory();
		if (!positionHistory.isEmpty()) {
			Position lastPosition = positionHistory.get(positionHistory.size() - 1);
			long lastPositionTime = lastPosition.getDate();
			long timeNow = getCalendar().getTimeInMillis();
			secondsSinceLastPosition = (timeNow - lastPositionTime) / 1000L;
		}
		return secondsSinceLastPosition;
	}

	public void report(String message) {
		strategyReportColumns.clear();
		strategyReportColumns.add(message);
		strategyReport.report(strategyReportColumns, getCalendar());
	}

	public StrategyPreferences getStrategyPrefereces() {
		StrategyPreferences prefs = new StrategyPreferences();
		prefs.addPreference(new StrategyPreference(contract.m_symbol, shareNumber, tradingStartTime, tradingEndTime));
		return prefs;
	}

	public void saveStrategyPreferences(StrategyPreferences prefs) {
		PreferencesHolder.getInstance().setStrategyPreferences(preferecePrex + name, prefs.toString());
	}

	public void saveParamsToPreferences(StrategyParams sparams) {
		PreferencesHolder.getInstance().setStrategyPreferences(prefereceParamsPrex + name, sparams.toString());

	}

	private void setDefaultValuesFromPreferences() {

		try {
			PreferencesHolder preferences = PreferencesHolder.getInstance();
			String sprefsValue = preferences.getStrategyPreferences(preferecePrex + name);

			if (sprefsValue != null && sprefsValue.length() > 0) {
				StrategyPreferences sprefs = StrategyPreferences.create(sprefsValue);

				List<StrategyPreference> list = sprefs.getAll();

				// we only support one d per strategy at this time, will support multi in the
				// future.
				if (list != null && list.size() > 0) {
					StrategyPreference spref = list.get(0);
					tradingStartTime = spref.getTradeStartTime();
					shareNumber = spref.getTradeShareVolumn().intValue();
					tradingEndTime = spref.getTradeEndTime();
				}
			}

			String paramsValue = preferences.getStrategyPreferences(prefereceParamsPrex + name);

			if (paramsValue != null && paramsValue.length() > 0) {
				params = StrategyParams.create(paramsValue);
			}

		} catch (Exception _e) {
			;// ignore;
		}

	}

	private static String prefereceParamsPrex = "strategg.preferences.params.";
	private static String preferecePrex = "strategg.preferences.";

}