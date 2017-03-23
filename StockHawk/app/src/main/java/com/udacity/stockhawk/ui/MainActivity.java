package com.udacity.stockhawk.ui;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener,
        SearchView.OnQueryTextListener,
        StockAdapter.StockAdapterOnClickHandler
{

    /* variable stored in shared pref to track the status of newly added stock */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VALIDATING, INVALID , VALID, VALID_SUCCESS, ALREADY_EXISTS})
    public @interface validationStatus {};

    public final static int VALID = 1;
    public final static int INVALID = 0;
    public final static int VALIDATING = -1;
    public final static int VALID_SUCCESS = 2;
    public final static int ALREADY_EXISTS = 3;
    public static final int STOCK_LOADER = 0;
    private String filterText="";



    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.recycler_view)
    RecyclerView stockRecyclerView;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.fab)
    FloatingActionButton fab;


    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.coordinator)
    CoordinatorLayout coordinator;

    @BindView(R.id.myactionbar)
    Toolbar mytoolbar;


    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.myactionbar_textview)
    TextView toolbarText;

    private StockAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SearchView searchView = (SearchView) findViewById(R.id.search);

        setSearchViewAnimation(searchView);
        initializeSharedPref(defaultPreference);

        setSupportActionBar(mytoolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Typeface typeFace= Typeface.createFromAsset(getAssets(),getString(R.string.robotoLight));
        toolbarText.setTypeface(typeFace);

        defaultPreference.registerOnSharedPreferenceChangeListener(this);

        MaterialShowcaseView m1 = new MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .setDismissText(getString(R.string.demo_message_accept))
                .setContentText(getString(R.string.demo_message1))
                .setDelay(0).singleUse("2323653").build();

        MaterialShowcaseView m2 = new MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .setDismissText(getString(R.string.demo_message_accept))
                .setContentText(getString(R.string.demo_message_2))
                .setDelay(0).singleUse("2367233").build();

        MaterialShowcaseView m3 = new MaterialShowcaseView.Builder(this)
                .setTarget(fab)
                .setDismissText(getString(R.string.demo_message_accept))
                .setContentText(getString(R.string.demo_message_3))
                .setDelay(0).singleUse("213233").build();

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "3543543");

        sequence.addSequenceItem(m1);
        sequence.addSequenceItem(m2);
        sequence.addSequenceItem(m3);
        sequence.start();

        adapter = new StockAdapter(this, this);
        stockRecyclerView.setAdapter(adapter);
        DividerItemDecoration did = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        stockRecyclerView.addItemDecoration(did);
        stockRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        QuoteSyncJob.syncImmediately(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
            }
        }).attachToRecyclerView(stockRecyclerView);

    }

    public void setSearchViewAnimation(SearchView sv)
    {
        SearchManager sm = (SearchManager)getSystemService(SEARCH_SERVICE);
        sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
        sv.setOnQueryTextListener(this);
        sv.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus)
                {


                    ValueAnimator slideAnimator = ValueAnimator
                            .ofInt(findViewById(R.id.myactionbar_textview).getMinimumHeight(), 0)
                            .setDuration(150);

                    slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Integer value = (Integer) animation.getAnimatedValue();
                            toolbarText.getLayoutParams().height = value.intValue();
                            toolbarText.requestLayout();
                        }
                    });

                    AnimatorSet as = new AnimatorSet();
                    as.play(slideAnimator);
                    as.setInterpolator(new AccelerateDecelerateInterpolator());
                    as.start();

                }
                else
                {
                    ValueAnimator slideAnimator = ValueAnimator
                            .ofInt(0, findViewById(R.id.myactionbar_textview).getMinimumHeight())
                            .setDuration(150);

                    slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            Integer value = (Integer) animation.getAnimatedValue();
                            toolbarText.getLayoutParams().height = value.intValue();
                            toolbarText.requestLayout();
                        }
                    });

                    AnimatorSet as = new AnimatorSet();
                    as.play(slideAnimator);
                    as.setInterpolator(new AccelerateDecelerateInterpolator());
                    as.start();

                }

            }
        });

    }

    public void initializeSharedPref(SharedPreferences sp)
    {
        @MainActivity.validationStatus int status=  sp.getInt(getString(R.string.validation_status_key), VALID);
        String statusCheck = sp.getString(getString(R.string.checking_validity), null);
        SharedPreferences.Editor editor = sp.edit();

        if(sp.getBoolean(getString(R.string.is_first_time), true)) {

            PrefUtils.getStocks(this);
            editor.putInt(getString(R.string.validation_status_key), status).apply();
            editor.putString(getString(R.string.checking_validity), statusCheck).apply();
            QuoteSyncJob.initialize(this);
            editor.putBoolean(getString(R.string.is_first_time), false).apply();
        }
    }



    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

//    @Override
//    public void onRefresh() {
//
//        //QuoteSyncJob.syncImmediately(this);
//        PrefUtils.getStocks(this);
//        if (!networkUp() && adapter.getItemCount() == 0) {
//            //swipeRefreshLayout.setRefreshing(false);
//            error.setText(getString(R.string.error_no_network));
//            error.setVisibility(View.VISIBLE);
//        } else if (!networkUp()) {
//            //swipeRefreshLayout.setRefreshing(false);
//            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
//        } else if (PrefUtils.getStocks(this).size() == 0) {
//            //swipeRefreshLayout.setRefreshing(false);
//            error.setText(getString(R.string.error_no_stocks));
//            error.setVisibility(View.VISIBLE);
//        } else {
//            error.setVisibility(GONE);
//        }
//    }

    @Override
    public void onClick(String symbol) {

    }

    public void button(@SuppressWarnings("UnusedParameters") View view) {
        new AddStockDialog().show(getFragmentManager(), getString(R.string.stock_dialog_fragment));
    }

    void addStock(String symbol) {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> quotes = PrefUtils.getStocks(this);
        if(quotes.contains(symbol)) {
            defaultSharedPreferences.edit().putInt(getString(R.string.validation_status_key), MainActivity.ALREADY_EXISTS).commit();
            return;
        }
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp())
            {defaultSharedPreferences.edit().putString(getString(R.string.checking_validity), symbol).commit();
                QuoteSyncJob.addImmediately(this, symbol);
            }
            else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String wildCharacter = getString(R.string.wild_char);
        return new CursorLoader(this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                Contract.Quote.COLUMN_SYMBOL+" " +getString(R.string.query_like), new String[]{wildCharacter +filterText+ wildCharacter},
                Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() != 0) {
            error.setVisibility(GONE);
            adapter.setCursor(data);
        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //adapter.setCursor(null);
    }


    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this).
                equals(getString(R.string.pref_display_mode_absolute_key)))
            item.setIcon(R.drawable.ic_percentage);
        else item.setIcon(R.drawable.ic_dollar);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(getString(R.string.validation_status_key)))
        {
            @validationStatus int status =  sharedPreferences.getInt(key, VALID);
            if (status == INVALID)
            {
                final Toast toast = Toast.makeText(this, getString(R.string.snackbar_invalid_stock_msg), Toast.LENGTH_SHORT);

                toast.show();
                sharedPreferences.edit().putInt(getString(R.string.validation_status_key), MainActivity.VALID).commit();
                sharedPreferences.edit().putString(getString(R.string.checking_validity), null).commit();
                return;
            }
            if(status == VALID_SUCCESS)
            {
                final Toast snackBar = Toast.makeText(this ,getString(R.string.snackbar_add_success_msg), Toast.LENGTH_SHORT);
                snackBar.show();
                sharedPreferences.edit().putInt(getString(R.string.validation_status_key), MainActivity.VALID).commit();
                return;


            }
            if(status == ALREADY_EXISTS)
            {

                final Toast snackBar = Toast.makeText(this, getString(R.string.snackbar_alreay_exists), Toast.LENGTH_SHORT);
                snackBar.show();
                sharedPreferences.edit().putInt(getString(R.string.validation_status_key), MainActivity.VALID).commit();
                sharedPreferences.edit().putString(getString(R.string.checking_validity), null).commit();
                return;

            }

        }
    }




    @Override
    public boolean onQueryTextChange(String newText) {
        filterText = newText;
        getSupportLoaderManager().restartLoader(STOCK_LOADER, null, this);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    protected void onPause() {
        super.onPause();

        SearchView searchView = (SearchView) findViewById(R.id.search);
        searchView.clearFocus();
        searchView.setQuery(getString(R.string.empty_string), true);
        searchView.onActionViewCollapsed();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState = new Bundle();
        super.onSaveInstanceState(outState, outPersistentState);
    }
}
