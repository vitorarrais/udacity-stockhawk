package com.sam_chordas.android.stockhawk.service;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by User on 12/04/2016.
 */
public class StockDetailService extends Service {

    private final StockDetailBinder mBinder = new StockDetailBinder();

    private Cursor mCursor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public Cursor queryBySymbol(String symbol){
        mCursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.CHANGE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{symbol}, null);
        return mCursor;
    }

    public class StockDetailBinder extends Binder {
        public StockDetailService getService() {
            return StockDetailService.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCursor!=null){
            mCursor.close();
        }
    }
}
