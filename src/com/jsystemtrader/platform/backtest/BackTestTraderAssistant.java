package com.jsystemtrader.platform.backtest;

import static com.jsystemtrader.platform.preferences.JSTPreferences.BacktestShowNumberOfBar;

import java.util.List;

import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.jsystemtrader.platform.model.JSystemTraderException;
import com.jsystemtrader.platform.position.OpenOrder;
import com.jsystemtrader.platform.preferences.PreferencesHolder;
import com.jsystemtrader.platform.quote.PriceBar;
import com.jsystemtrader.platform.strategy.Strategy;
import com.jsystemtrader.platform.trader.Trader;
import com.jsystemtrader.platform.trader.TraderAssistant;
import com.jsystemtrader.platform.util.MessageDialog;

/**
 * Extends regular trader assistant for backtesting purposes
 */
public class BackTestTraderAssistant extends TraderAssistant {
    public List<PriceBar> priceBars;
    private final double spread, slippage, commission;

    public BackTestTraderAssistant(Trader trader, String fileName) throws JSystemTraderException {
        super(trader);
        PreferencesHolder properties = PreferencesHolder.getInstance();

        eventReport.report("Reading back data file");
        BackTestFileReader reader = new BackTestFileReader(fileName);
        priceBars = reader.getPriceBars();
        spread = reader.getBidAskSpread();
        slippage = reader.getSlippage();
        commission = reader.getCommission();

        boolean showNumberOfBars = properties.getBool(BacktestShowNumberOfBar);
        if (showNumberOfBars) {
            String msg = priceBars.size() + " bars have been read successfully.";
            MessageDialog.showMessage(null, msg);
        }
        eventReport.report("Connected to back test");
    }

    @Override
    public synchronized void placeOrder(Contract contract, Order order, Strategy strategy) {
        orderID++;
        openOrders.put(orderID, new OpenOrder(orderID, order, strategy));
        String msg = strategy.getName() + ": Placed order " + orderID;
        eventReport.report(msg);

        double price = strategy.getLastPriceBar().getClose();
        Execution execution = new Execution();
        execution.m_shares = order.m_totalQuantity;

        double aveTransactionCost = slippage + spread / 2d + commission;

        if (order.m_action.equalsIgnoreCase("BUY")) {
            execution.m_price = price + aveTransactionCost;
        }

        if (order.m_action.equalsIgnoreCase("SELL")) {
            execution.m_price = price - aveTransactionCost;
        }

        trader.execDetails(orderID, contract, execution);
    }
}
