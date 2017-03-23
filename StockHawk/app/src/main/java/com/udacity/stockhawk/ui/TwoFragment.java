package com.udacity.stockhawk.ui;

import android.animation.PropertyValuesHolder;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.transition.Visibility;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.tooltip.Tooltip;
import com.db.chart.view.LineChartView;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class TwoFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static String Symbol ;
    Cursor mydata;
    static LineChartView d = null;


    public static final int DETAILS_LOADER = 2;

    // TODO: Rename and change types of parameters

    @BindView(R.id.symbol_detail2)
    TextView SymbolDetail;

    @BindView(R.id.minprice_detail2)
    TextView MinPriceDetail;

    @BindView(R.id.maxprice_detail2)
    TextView MaxPriceDetail;


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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        d = (LineChartView) getActivity().findViewById(R.id.chart2);
        if(data==null || data.getCount()==0)
            return ;

        data.moveToFirst();
        String[] Label = new String[92];
        String History = data.getString(Contract.Quote.POSITION_HISTORY);
        String[] weeklyHistory = History.split("\n");

        final LineSet ls = new LineSet();

        for(int i=89; i>=0; i--)
        {


            String[] Point  = weeklyHistory[i].split(", ");
            Calendar c = Calendar.getInstance();
            long x = Long.valueOf(Point[0]);
            c.setTimeInMillis(x);
            int Month = c.get(Calendar.MONTH);

            long difference = Math.abs(System.currentTimeMillis() - x);
            long differenceDates = difference / (24 * 60 * 60 * 1000);
            if(differenceDates > 90)
                continue;

            String[] Point2 = weeklyHistory[i+1].split(", ");
            long x2 = Long.valueOf(Point2[0]);
            c.setTimeInMillis(x2);
            int prevMonth = c.get(Calendar.MONTH);
            SimpleDateFormat sp = new SimpleDateFormat("MMM yyyy");
            String xstr = sp.format(x);

            if(prevMonth != Month)
            {
                ls.addPoint(new Point(xstr, Float.valueOf(Point[1])));

            }
            else
            ls.addPoint(new Point("", Float.valueOf(Point[1])));
            sp = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            xstr = sp.format(x);
            Label[i] = xstr;

        }

        if(d!=null)
        d.reset();

        ls.setSmooth(false);
        d.setAxisThickness(6);
        ls.setDotsColor(Color.BLACK);
        ls.setDotsRadius(10);
        ls.setDotsStrokeThickness(6);
        ls.setDotsStrokeColor(getResources().getColor(R.color.colorPrimary));
        d.setClickablePointRadius(10);
        ls.setThickness(4);

            ls.setDotsRadius(10);
            ls.setDotsStrokeThickness(3);
            d.setClickablePointRadius(10);

        d.addData(ls);

        int min = (int)ls.getMin().getValue();
        int max = (int)ls.getMax().getValue()+1;

        double minValue  = ls.getMin().getValue();
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

        SymbolDetail.setText(Symbol);
        MinPriceDetail.setText(String.format("%.2f", minValue) + " " + getString(R.string.usd));
        MaxPriceDetail.setText(String.format("%.2f", maxValue)+ " " + getString(R.string.usd));
        d.setAxisBorderValues(min, max);

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

        this.mydata = data;
        return;

    }

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getSupportLoaderManager().initLoader(DETAILS_LOADER, null, this);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View v = inflater.inflate(R.layout.fragment_two, container, false);
        ButterKnife.bind(this , v);
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

}
