package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by siddharth.thakrey on 19-02-2017.
 */

public class AddQuoteIntentService extends IntentService {

    public AddQuoteIntentService()
    {
        super(AddQuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String symbol = intent.getStringExtra("Symbol");
        QuoteSyncJob.getNewQuote(this, symbol);


    }
}
