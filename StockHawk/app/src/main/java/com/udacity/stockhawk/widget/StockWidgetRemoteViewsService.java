package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.CpuUsageInfo;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by siddharth.thakrey on 05-02-2017.
 */

public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Cursor c =null;
            public final String SymbolNameKey = "SYMBOL_NAME";


            private final String[] StockProjection = {
                    Contract.Quote._ID,
                    Contract.Quote.COLUMN_SYMBOL,
                    Contract.Quote.COLUMN_PRICE,
                    Contract.Quote.COLUMN_HISTORY,
                    Contract.Quote.COLUMN_ABSOLUTE_CHANGE,
                    Contract.Quote.COLUMN_PERCENTAGE_CHANGE
            };

            static final int ID = 0;
            static final int SYMBOL =1;
            static final int PRICE=2;
            static final int HISTORY=3;
            static final int ABSOLUTE_CHANGE = 4;
            static final int PERCENTAGE_CHANGE = 5;
            DecimalFormat dollarFormatWithPlus ;
            DecimalFormat dollarFormat;
            DecimalFormat percentageFormat;

            @Override
            public void onCreate() {

                dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

            }

            @Override
            public void onDataSetChanged() {

                Uri query = Contract.Quote.URI;
                final long identityToken = Binder.clearCallingIdentity();
                c = getContentResolver().query(query, StockProjection, null, null, Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                return c.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                RemoteViews rv = null;
                if(c == null || c.moveToFirst()==false)
                    return null;
                c.moveToPosition(position);
                rv = new RemoteViews(getPackageName(), R.layout.stock_widget_layout_item);
                rv.setTextViewText(R.id.symbol, c.getString(SYMBOL));
                rv.setTextViewText(R.id.price,dollarFormat.format(c.getFloat(Contract.Quote.POSITION_PRICE)));
                Double rawAbsoluteChange = c.getDouble(ABSOLUTE_CHANGE);
                Double percentageChange  = c.getDouble(PERCENTAGE_CHANGE);
                if (rawAbsoluteChange > 0) {
                    rv.setTextColor(R.id.change, Color.GREEN);
                } else {
                    rv.setTextColor(R.id.change, Color.RED);
                }

                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);

                if (PrefUtils.getDisplayMode(getBaseContext())
                        .equals(getBaseContext().getString(R.string.pref_display_mode_absolute_key))) {
                    rv.setTextViewText(R.id.change, change);
                } else {
                    rv.setTextViewText(R.id.change, percentage);
                }




                Intent fillInIntent = new Intent();
                fillInIntent.putExtra(SymbolNameKey, c.getString(SYMBOL));
                rv.setOnClickFillInIntent(R.id.widget_handler, fillInIntent);
                return rv;
            }


            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
