package com.sam_chordas.android.stockhawk.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.service.StockDetailService;

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


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StockDetailService.StockDetailBinder binder = (StockDetailService.StockDetailBinder) service;
            mStockDetailService = binder.getService();
            mBound = true;
            if (mSymbol != null) {
                mCursor = mStockDetailService.queryBySymbol(mSymbol);
                if (mCursor!=null){
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
        mLineChartView = (LineChartView)findViewById(R.id.linechart);
        mSymbol = getIntent().getExtras().getString(EXTRA_SYMBOL);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBound) {
            init();
        }
    }

    protected void init() {
        Intent i = new Intent(this, StockDetailService.class);
        bindService(i, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void bind(Cursor cursor){
        if (cursor!=null){
            cursor.moveToFirst();
            LineSet set = new LineSet();
            int i=0;
            while (cursor.moveToNext()){
                Float price = Float.valueOf(cursor.getString(cursor.getColumnIndex(QuoteColumns.BIDPRICE)).replace(",", "."));
                set.addPoint(new Point(String.valueOf(i++), price));
            }
            mLineChartView.addData(set);
            mLineChartView.show();
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
