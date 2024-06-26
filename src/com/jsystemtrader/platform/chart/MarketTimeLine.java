package com.jsystemtrader.platform.chart;

import org.jfree.chart.axis.SegmentedTimeLine;

import com.jsystemtrader.platform.quote.PriceBar;
import com.jsystemtrader.platform.quote.QuoteHistory;


public class MarketTimeLine {
    /**
     * Gaps less than MAX_GAP will be ignored, gaps greater than MAX_GAP will be removed
     */
    private static final long MAX_GAP = 12 * 60 * 60 * 1000;// 12 hours
    private static final long SEGMENT_SIZE = SegmentedTimeLine.FIFTEEN_MINUTE_SEGMENT_SIZE;
    private static final long GAP_BUFFER = SEGMENT_SIZE;
    private final QuoteHistory qh;

    public MarketTimeLine(QuoteHistory qh) {
        this.qh = qh;
    }

    public SegmentedTimeLine getNormalHours() {
        SegmentedTimeLine timeline = new SegmentedTimeLine(SEGMENT_SIZE, 1, 0);
        long previousTime = qh.getFirstPriceBar().getDate();

        for (PriceBar bar : qh.getAll()) {
            long barTime = bar.getDate();
            long difference = barTime - previousTime;
            if (difference > MAX_GAP) {
                timeline.addException(previousTime + GAP_BUFFER, barTime - GAP_BUFFER);
            }
            previousTime = barTime;
        }

        return timeline;
    }

    public SegmentedTimeLine getAllHours() {
        return new SegmentedTimeLine(SegmentedTimeLine.DAY_SEGMENT_SIZE, 7, 0);
    }
}
