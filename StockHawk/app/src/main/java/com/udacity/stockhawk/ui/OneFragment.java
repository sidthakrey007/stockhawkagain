package com.udacity.stockhawk.ui;

import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.renderer.AxisRenderer;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OneFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static String Symbol ;
    Cursor mydata;
    static LineChartView d = null;

// Binding Vuew vua BetterKnife
    @BindView(R.id.stockHeading)
    TextView StockHeading;

    @BindView(R.id.symbol_detail)
    TextView SymbolDetail;

    @BindView(R.id.minprice_detail)
    TextView MinPriceDetail;

    @BindView(R.id.maxprice_detail)
    TextView MaxPriceDetail;


    public static  final int DETAILS_LOADER = 1;


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String SymbolNameKey = getString(R.string.symbol_name_key);
        if(getArguments().getString(SymbolNameKey) != null)
        Symbol = getArguments().getString(SymbolNameKey);
        Uri detailUri = Contract.Quote.makeUriForStock(Symbol);
        CursorLoader loader = new CursorLoader(getActivity(), detailUri, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,null,null);
        return loader;
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        this.mydata = null;

    }


    @Override
    @TargetApi(11)

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        d = (LineChartView) getActivity().findViewById(R.id.chart1);



        data.moveToFirst();
        if(data==null || data.getCount()==0)
            return ;
        String History = data.getString(Contract.Quote.POSITION_HISTORY);
        String[] weeklyHistory = History.split("\n");
        String[] Label = new String[35];
        final LineSet ls = new LineSet();
        for(int i=29; i>=0 ; i--)
        {
            String[] Point  = weeklyHistory[i].split(", ");
            Calendar c = Calendar.getInstance();

            long x = Long.valueOf(Point[0]);
            c.setTimeInMillis(x);
            String xstr;
            SimpleDateFormat sp = new SimpleDateFormat("dd MMM", Locale.US);
            xstr = sp.format(x);
            if(i%8==0)
                ls.addPoint(new Point(xstr, Float.valueOf(Point[1])));
            else
                ls.addPoint(new Point(getString(R.string.empty_string), Float.valueOf(Point[1])));

            sp = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            xstr = sp.format(x);
            Label[i] = xstr;

        }
        if(d==null)
            return;

        d.dismiss();

        d.setAxisLabelsSpacing(20);
        ls.setSmooth(false);
            ls.setDotsRadius(16);
            ls.setDotsStrokeThickness(3);
            d.setClickablePointRadius(16);

        d.setLabelsColor(Color.BLACK);
        ls.setDotsColor(Color.BLACK);

        ls.setDotsStrokeColor(getResources().getColor(R.color.colorPrimary));
        ls.setThickness(4);
        ls.setSmooth(true);
        final Tooltip mTip = new Tooltip(getActivity(), R.layout.linechart_three_tooltip, R.id.tooltip_value);
        mTip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
        mTip.setDimensions((int) Tools.fromDpToPx(58), (int) Tools.fromDpToPx(25));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

            mTip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f)).setDuration(200);

            mTip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 0),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0f)).setDuration(200);

            mTip.setPivotX(Tools.fromDpToPx(65) / 2);
            mTip.setPivotY(Tools.fromDpToPx(25));
        }

        d.setYLabels(AxisRenderer.LabelPosition.OUTSIDE);
        d.setLabelsFormat(new DecimalFormat());
        d.setDrawingCacheEnabled(true);
        int min = (int)ls.getMin().getValue();
        int max = (int)ls.getMax().getValue()+1;
        if((max-min)%2==1)
            max = max+1;
        double minValue = ls.getMin().getValue();
        double maxValue = ls.getMax().getValue();

        if((max-min)%3==1)
            max = max+2;
        else
        {
            if((max - min)%3==2)
                max = max+1;
        }
        int step = (max - min)/3;
        d.setStep(step);
        d.setAxisBorderValues(min, max);
        d.addData(ls);


        d.setClickablePointRadius(16);
        d.setClickable(true);


        SymbolDetail.setText(Symbol);
        MinPriceDetail.setText(String.format("%.2f", minValue) + " "+getString(R.string.usd));
        MaxPriceDetail.setText(String.format("%.2f", maxValue) + " "+getString(R.string.usd));
        this.mydata = data;


        d.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                d.dismissAllTooltips();
                mTip.prepare(rect, ls.getValue(entryIndex));
                mTip.announceForAccessibility(getString(R.string.accessibility_msg_1)+" "+ ls.getLabel(entryIndex)+ getString(R.string.accessibility_msg_2)+
                        " "+ ls.getValue(entryIndex)+ " "+getString(R.string.accessibility_msg_3));
                d.showTooltip(mTip, true);
            }

        });

        d.show();
        return;

    }



    public OneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_one, container, false);
        ButterKnife.bind(this, v);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getActivity().getSupportLoaderManager().initLoader(DETAILS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
}
