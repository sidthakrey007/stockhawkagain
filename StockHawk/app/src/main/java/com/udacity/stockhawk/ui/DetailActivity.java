package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * Created by siddharth.thakrey on 05-02-2017.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{


    @BindView(R.id.viewpager)
    ViewPager viewpager;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    Cursor mydata =null;
    int DETAILS_LOADER = 1;
    static String Symbol = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);
        ButterKnife.bind(this);
        setUpViewPagerAdapter(viewpager);
        changeTabsFont();
        tabLayout.setupWithViewPager(viewpager);

        getLoaderManager().initLoader(DETAILS_LOADER, null, this);

    }


    private void setUpViewPagerAdapter(final ViewPager viewPager)
    {

        String SymbolNameKey = getString(R.string.symbol_name_key);

        final ViewPager viewPagerFinal = viewPager;
        ViewPagerAdapter vpa1 = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle dataBundle = new Bundle();
        dataBundle.putString(SymbolNameKey, getIntent().getStringExtra(SymbolNameKey));
        OneFragment threemonthFragment = new OneFragment();
        threemonthFragment.setArguments(dataBundle);

        vpa1.addFragment(threemonthFragment, getString(R.string.tab_onemonth));
        TwoFragment sixmonthFragment = new TwoFragment();
        sixmonthFragment.setArguments(dataBundle);
        vpa1.addFragment(sixmonthFragment, getString(R.string.tab_threemonths));
        ThreeFragment oneYearFragment = new ThreeFragment();
        oneYearFragment.setArguments(dataBundle);
        vpa1.addFragment(oneYearFragment, getString(R.string.tab_sixmonths));
        viewPager.setAdapter(vpa1);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {


                viewPagerFinal.setCurrentItem(tab.getPosition());

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String SymbolNameKey = getString(R.string.symbol_name_key);
        Symbol = getIntent().getStringExtra(SymbolNameKey);
        Uri detailUri = Contract.Quote.makeUriForStock(Symbol);
        CursorLoader loader = new CursorLoader(this, detailUri, Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,null,null);
        return loader;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        data.moveToFirst();
        this.mydata = data;
        return;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }


    private void changeTabsFont() {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        Typeface typeFace= Typeface.createFromAsset(getAssets(),getString(R.string.robotoRegular));

        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTextColor(Color.WHITE);
                    ((TextView) tabViewChild).setTypeface(typeFace);
                    ((TextView) tabViewChild).setTextSize(22);


                }
            }
        }
    }
}


class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
}