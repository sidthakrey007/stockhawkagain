package com.udacity.stockhawk.ui;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 * Use the {@link ThreeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThreeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private  static String Symbol = null;
    Cursor mydata;
    static LineChartView d = null;


    @BindView(R.id.symbol_detail3)
    TextView SymbolDetail;

    @BindView(R.id.minprice_detail3)
    TextView MinPriceDetail;

    @BindView(R.id.maxprice_detail3)
    TextView MaxPriceDetail;



    public static final int DETAILS_LOADER = 2;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


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

    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        boolean isodd = true;
        data.moveToFirst();
        //TextView tv = (TextView)getActivity().findViewById(R.id.symbol1);
        //tv.setText(data.getString(Contract.Quote.POSITION_SYMBOL));
        String History = data.getString(Contract.Quote.POSITION_HISTORY);
        String[] weeklyHistory = History.split("\n");
        String[] Label = new String[181];
        Symbol = data.getString(Contract.Quote.POSITION_SYMBOL);

        d = (LineChartView) getActivity().findViewById(R.id.chart3);
        final LineSet ls = new LineSet();

        for(int i=104; i>=0 ; i--)
        {

            String[] Point  = weeklyHistory[i].split(", ");
            Calendar c = Calendar.getInstance();
            long x = Long.valueOf(Point[0]);
            c.setTimeInMillis(x);
            int Month = c.get(Calendar.MONTH);

            long difference = Math.abs(System.currentTimeMillis() - x);
            long differenceDates = difference / (24 * 60 * 60 * 1000);
            if(differenceDates > 180)
                continue;

            String[] Point2 = weeklyHistory[i+1].split(", ");
            long x2 = Long.valueOf(Point2[0]);
            c.setTimeInMillis(x2);
            int prevMonth = c.get(Calendar.MONTH);
            SimpleDateFormat sp = new SimpleDateFormat("MMM yyyy");
            String xstr = sp.format(x);

            if(prevMonth != Month)
            {
              if(isodd) {
                  ls.addPoint(new Point(xstr, Float.valueOf(Point[1])));
                  isodd = false;
              }
                else {
                  ls.addPoint(new Point("", Float.valueOf(Point[1])));
                  isodd = true;
              }
            }
            else
                ls.addPoint(new Point("", Float.valueOf(Point[1])));
            sp = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            xstr = sp.format(x);
            Label[i] = xstr;

        }
        if(d==null)
            return;
        d.reset();

        ls.setSmooth(false);
        d.setAxisThickness(6);
            ls.setDotsRadius(7);
            ls.setDotsStrokeThickness(3);
            d.setClickablePointRadius(7);
        ls.setDotsColor(Color.BLACK);
        ls.setDotsStrokeColor(getResources().getColor(R.color.colorPrimary));
        ls.setThickness(4);
        ls.setSmooth(true);
        d.addData(ls);

        int min = (int)ls.getMin().getValue();
        int max = (int)ls.getMax().getValue()+1;
        if((max-min)%3==1)
            max = max+2;
        else
        {
            if((max - min)%3==2)
                max = max+1;
        }
        int step = (max - min)/3;
        d.setStep(step);


        double minValue  = ls.getMin().getValue();
        double maxValue = ls.getMax().getValue();
        SymbolDetail.setText(Symbol);
        MinPriceDetail.setText(String.format("%.2f", minValue) + " "+getString(R.string.usd));
        MaxPriceDetail.setText(String.format("%.2f", maxValue)+ " "+getString(R.string.usd));

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

        d.setAxisBorderValues(min, max);
        d.show();

        this.mydata = data;
        return;

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getSupportLoaderManager().initLoader(DETAILS_LOADER, null, this);

    }

    public ThreeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlankFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ThreeFragment newInstance(String param1, String param2) {
        ThreeFragment fragment = new ThreeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View v = inflater.inflate(R.layout.fragment_three, container, false);
        ButterKnife.bind(this, v);
        return v;

    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

}
