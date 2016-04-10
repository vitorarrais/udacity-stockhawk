package com.sam_chordas.android.stockhawk.service.receiver;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by User on 09/04/2016.
 */
@SuppressLint("ParcelCreator")
public class StockIntentServiceReceiver extends ResultReceiver {

    public static final String STOCK_INTENT_RECEIVER = "stock_itent_receiver";

    private Receiver mReceiver;

    public StockIntentServiceReceiver(Handler handler) {

        super(handler);
        // TODO Auto-generated constructor stub
    }

    public void setReceiver(Receiver receiver) {

        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

    public interface Receiver {

        /**
         * On receive result.
         *
         * @param resultCode the result code
         * @param resultData the result data
         */
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

}
