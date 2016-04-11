package com.sam_chordas.android.stockhawk.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by User on 11/04/2016.
 */
public class StockWidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Intent mIntent;
    private Cursor mData;

    public StockWidgetRemoteViewsFactory(Context mContext, Intent mIntent) {
        this.mContext = mContext;
        this.mIntent = mIntent;
    }


    @Override
    public void onCreate() {
        mData = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{"Distinct " + QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.CHANGE},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"}, null);
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
        DatabaseUtils.dumpCursor(mData);
        mData.moveToPosition(position);
        views.setTextViewText(R.id.stock_symbol, mData.getString(mData.getColumnIndex(QuoteColumns.SYMBOL)));
        views.setTextViewText(R.id.bid_price, mData.getString(mData.getColumnIndex(QuoteColumns.BIDPRICE)));
        views.setTextViewText(R.id.change, mData.getString(mData.getColumnIndex(QuoteColumns.CHANGE)));

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.list_item_quote);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
