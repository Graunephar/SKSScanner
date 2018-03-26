package lol.graunephar.android.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import lol.graunephar.android.nfc.collections.MessageMap;
import lol.graunephar.android.nfc.models.TagContentMessage;
import lol.graunephar.android.nfc.utilities.nfc.NFCReader;

import static lol.graunephar.android.nfc.SettingsActivity.SETTINGS_KEY_NUM;


public class ScanActivity extends AppCompatActivity implements MessageCloser {

    private static final String TAG = ScanActivity.class.toString();
    private static final int REQUESTCODE_SETTINGS = 4557;
    private static final int DEFAULT_TOKEN_NR = 80;

    //NFC
    Tag detectedTag;
    NfcAdapter nfcAdapter;
    IntentFilter[] readTagFilters;
    PendingIntent pendingIntent;
    private NFCReader mReader;

    // Tag UI
    private android.support.v4.app.FragmentManager mManager;
    private long MESSAGE_SHOW_DELAY = 7000;
    private MessageFragment mFragment;
    private Handler mHandler;

    //Data
    private int mfound = 0;
    private int mNrOfTags = DEFAULT_TOKEN_NR;
    private int mPoints = 0;
    private MessageMap mMessages;

    @BindView(R.id.chart)
    PieChart mChart;
    @BindView(R.id.scan_points_label_txt)
    TextView mPointLabelTxt;
    @BindView(R.id.scan_points_txt)
    TextView mPointTxt;
    @BindView(R.id.scan_count_label_txt)
    TextView mCountLabelTxt;


    //Preferences
    private String KEY_FOUND = "key_found";
    private String KEY_POINTS = "key_points";
    private String KEY_NUMBER_OF_TOKENS = "key_number_of_tokens";
    private static final String KEY_MESSAGES_MAP = "key_messages_map";

    public ScanActivity() {
        this.mMessages = new MessageMap();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        ButterKnife.bind(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        detectedTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        mReader = new NFCReader(this);

        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(this, getClass()).
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        readTagFilters = new IntentFilter[]{tagDetected, filter2};

        drawChart();
        drawUI();

    }

    private void drawUI() {
        mPointLabelTxt.setText(R.string.point_text);

        updatePoints(); //Draws labels with numbers

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, REQUESTCODE_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUESTCODE_SETTINGS) {
            int number = data.getIntExtra(SETTINGS_KEY_NUM, 80);

            mNrOfTags = number;
            writeToSharedPreferences(number);
            updatePoints();
        }
    }

    private void writeToSharedPreferences(int number) {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_NUMBER_OF_TOKENS, number);
        editor.commit();
    }

    private void drawChart() {

        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);

        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);

        updateChartData();

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    private void saveStatsToPreferences() {

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(KEY_POINTS, mPoints);
        editor.putInt(KEY_FOUND, mfound);
        editor.putString(KEY_MESSAGES_MAP, mMessages.toJson());
        editor.commit();

    }


    private void updateChartData() {

        PieDataSet dataset = updateData();

        dataset.setDrawIcons(false);

        dataset.setSliceSpace(0f);
        dataset.setIconsOffset(new MPPointF(0, 40));
        dataset.setSelectionShift(5f);

        int foundcolor = this.getResources().getColor(R.color.chart_found);
        int notfoundcolor = this.getResources().getColor(R.color.chart_not_found);

        // add a lot of colors
        dataset.setColors(foundcolor, notfoundcolor);

        PieData data = new PieData(dataset);
        data.setValueFormatter(new PercentFormatter(new DecimalFormat("0")));
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        //data.setValueTypeface(mTfLight);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private PieDataSet updateData() {

        ArrayList<PieEntry> entries = new ArrayList<>();

        int alltags = mNrOfTags;
        int found = mfound;
        int notfound;
        if (found > alltags) {
            notfound = 0;
        } else {
            notfound = alltags - found;
        }

        entries.add(new PieEntry(found, getString(R.string.found_label)));
        entries.add(new PieEntry(notfound, getString(R.string.not_found_label)));

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of

        // the chart.

        return new PieDataSet(entries, "");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkIfTag(intent);

    }

    private void checkIfTag(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            detectedTag = tag;
            readNFCData();
        }
    }

    private void readNFCData() {
        try {
            TagContentMessage message = mReader.readFromTag(getIntent(), detectedTag);
            checkSaveAndShow(message);
        } catch (NFCReader.EmptytagException e) {
            tellUser(getString(R.string.empty_tag_messaage));
        } catch (NFCReader.NotSupportedContentException e) {
            tellUser(getString(R.string.undopported_tag_message));
        } catch (IOException e) {
            tellUser(getString(R.string.reader_unable_read_message));
        }
    }

    private void checkSaveAndShow(TagContentMessage message) {
        loadFromPreferences();
        if(!mMessages.contains(message)) {
            mMessages.add(message);
            updateStats(message);
            showMessage(message, true);
        } else {
            showMessage(message, false);
        }

    }

    private void showMessage(TagContentMessage message, boolean newtag) {
        mChart.setVisibility(View.INVISIBLE);

        if (mManager == null) mManager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction transaction = mManager.beginTransaction();

        if (mFragment != null) clearFragments();

        if (mFragment == null) {
            mFragment = new MessageFragment();
            mFragment.setCloser(this);
            mFragment.addContent(message, newtag);
            transaction.add(R.id.fragment_layout, mFragment);
            transaction.commit();
            startAutoClose();
        }

    }

    private void updateStats(TagContentMessage message) {
        mfound++; // We found a tag increment
        mPoints += message.getPoints();
        updateChartData(); //redraaw pie chart
        updatePoints();
    }

    private void clearFragments() {

        //Remove all fragments
        for (android.support.v4.app.Fragment fragment : getSupportFragmentManager().getFragments()) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }

        mFragment = null;

    }

    @Override
    public void closeMessage() {
        if (mFragment != null) {

            if (mManager == null) mManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = mManager.beginTransaction();
            transaction.remove(mFragment);
            transaction.commit();
            mFragment = null;
        }

        Log.d(TAG, "CLosing fragment");

        mChart.setVisibility(View.VISIBLE);
    }

    private void startAutoClose() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                closeMessage();
            }
        }, MESSAGE_SHOW_DELAY);
    }


    private void tellUser(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        saveStatsToPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfTag(getIntent());
        loadFromPreferences();
    }

    private void updatePoints() {

        String points = String.valueOf(mPoints);
        mPointTxt.setText(points);

        String found = String.valueOf(mfound);
        String countres = getString(R.string.counnt_text) + " " + found + " " + getString(R.string.tags) + " " +  getString(R.string.tokens_of_text) + " " + mNrOfTags;
        mCountLabelTxt.setText(countres);

        updateChartData();

    }

    private void loadFromPreferences() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        mfound = sharedPref.getInt(KEY_FOUND, 0);
        mPoints = sharedPref.getInt(KEY_POINTS, 0);
        mNrOfTags = sharedPref.getInt(KEY_NUMBER_OF_TOKENS, DEFAULT_TOKEN_NR);
        String messages = sharedPref.getString(KEY_MESSAGES_MAP, "{}");
        mMessages = new MessageMap(messages);
        updatePoints();
    }


}
