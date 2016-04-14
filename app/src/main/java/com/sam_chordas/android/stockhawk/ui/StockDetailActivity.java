package com.sam_chordas.android.stockhawk.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.LineChartView;
import com.db.chart.view.Tooltip;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockDetailService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 12/04/2016.
 */
public class StockDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SYMBOL = "extra_symbol";

    private StockDetailService mStockDetailService;

    private Boolean mBound = false;

    private String mSymbol;

    private Cursor mCursor;

    private LineChartView mLineChartView;

    private Map<Integer, Long> mEntryTimeTracker = new HashMap<>();

    private static long MILLIS_TO_MIN_DIVISOR = 1000*60;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StockDetailService.StockDetailBinder binder = (StockDetailService.StockDetailBinder) service;
            mStockDetailService = binder.getService();
            mBound = true;
            if (mSymbol != null) {
                mCursor = mStockDetailService.queryBySymbol(mSymbol);
                if (mCursor != null) {
                    bind(mCursor);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mLineChartView = (LineChartView) findViewById(R.id.linechart);
        mSymbol = getIntent().getExtras().getString(EXTRA_SYMBOL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            init();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void init() {
        Intent i = new Intent(this, StockDetailService.class);
        bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void bind(Cursor cursor) {
        if (Utils.isDeviceConnected(this)) {
            if (cursor != null && cursor.getCount() > 0) {

                cursor.moveToFirst();
                LineSet set = new LineSet();
                int i = 0;
                Float minimumPrice = -1.0f;
                Float maximumPrice = -1.0f;
                boolean isLastHour = false;

                do {
                    // limit entries to 15
                    if (i>=15){
                        break;
                    }
                    Float price = Float.valueOf(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)).replace(",", "."));
                    String created = cursor.getString(cursor.getColumnIndex(QuoteColumns.CREATED));
                    if (created != null) {
                        // compare the time when the entry was created into database
                        // and check whether it is inside the last one hour period or not
                        isLastHour = Utils.isLastHour(Utils.fromStringToDate(created));
                    }

                    // track min and max price
                    if (minimumPrice == -1.0f && maximumPrice == -1.0f) {
                        minimumPrice = price;
                        maximumPrice = price;
                    } else if (price > maximumPrice) {
                        maximumPrice = price;
                    } else if (price < minimumPrice) {
                        minimumPrice = price;
                    }

                    Point point = new Point(String.valueOf(i), price);
                    point.setColor(getResources().getColor(R.color.material_blue_500));

                    if (isLastHour) {
                        set.addPoint(point);
                        // track entry time
                        if (mEntryTimeTracker==null){
                            mEntryTimeTracker = new HashMap<>();
                        }
                        mEntryTimeTracker.put(i, Utils.fromStringToDate(created).getTime());
                    }
                    i++;

                } while (cursor.moveToNext());

                // adjust axis border values based on min and max prices
                if (minimumPrice != -1.0f && maximumPrice != -1.0f) {
                    minimumPrice = minimumPrice - 1.0f;
                    maximumPrice = maximumPrice + 1.0f;
                    mLineChartView.setAxisBorderValues((minimumPrice).intValue(), maximumPrice.intValue(), 1);
                }

                mLineChartView.setAxisColor(getResources().getColor(android.R.color.white));
                set.setColor(getResources().getColor(android.R.color.white));
                Resources r = getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                set.setDotsRadius(px);
                mLineChartView.setLabelsColor(getResources().getColor(android.R.color.white));
                mLineChartView.setXAxis(false);
                mLineChartView.setXLabels(AxisController.LabelPosition.NONE);
                mLineChartView.setOnEntryClickListener(new OnEntryClickListener() {
                    @Override
                    public void onClick(int setIndex, int entryIndex, Rect rect) {
                        mLineChartView.dismissAllTooltips();
                        Tooltip tooltip = new Tooltip(StockDetailActivity.this, R.layout.tooltip, R.id.tooltip_value);
                        if (mEntryTimeTracker.containsKey(entryIndex)){
                            long leftTime = (System.currentTimeMillis() - mEntryTimeTracker.get(entryIndex))/MILLIS_TO_MIN_DIVISOR;
                            TextView tv = (TextView)tooltip.findViewById(R.id.tooltip_time);
                            tv.setText(getString(R.string.tooltip_time_suffix, leftTime));
                        }
                        Resources r = getResources();
                        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, r.getDisplayMetrics());
                        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 82, r.getDisplayMetrics());
                        tooltip.setDimensions((int)width,(int)height);
                        tooltip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
                        tooltip.prepare(rect, mLineChartView.getData().get(setIndex).getValue(entryIndex));
                        mLineChartView.setTooltips(tooltip);
                        mLineChartView.showTooltip(tooltip,true);
                    }
                });

                if (set.size() > 0) {
                    mLineChartView.addData(set);
                    mLineChartView.show();
                }
            } else {
                // show empty state
                TextView tv = (TextView) findViewById(R.id.error);
                tv.setVisibility(View.VISIBLE);
                tv.setText(R.string.graph_unavailable);
            }
        } else {
            // show connectivity error state
            TextView tv = (TextView) findViewById(R.id.error);
            tv.setVisibility(View.VISIBLE);
            tv.setText(R.string.no_connection);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
    }



}
