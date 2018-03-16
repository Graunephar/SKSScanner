package lol.graunephar.android.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import lol.graunephar.android.nfc.models.TagContentMessage;
import lol.graunephar.android.nfc.utilities.nfc.NFCReader;


public class ScanActivity extends AppCompatActivity implements MessageCloser {

    private static final String TAG = ScanActivity.class.toString();
    Tag detectedTag;
    NfcAdapter nfcAdapter;
    IntentFilter[] readTagFilters;
    PendingIntent pendingIntent;
    private NFCReader mReader;
    private android.support.v4.app.FragmentManager mManager;
    private long MESSAGE_SHOW_DELAY = 7000;
    private MessageFragment mFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        detectedTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
        mReader = new NFCReader(this);

        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(this, getClass()).
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        readTagFilters = new IntentFilter[]{tagDetected, filter2};
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
            showMessage(message);
        } catch (NFCReader.EmptytagException e) {
            tellUser(getString(R.string.empty_tag_messaage));
        } catch (NFCReader.NotSupportedContentException e) {
            tellUser(getString(R.string.undopported_tag_message));
        } catch (IOException e) {
            tellUser(getString(R.string.reader_unable_read_message));
        }
    }

    private void showMessage(TagContentMessage message) {

        if (mManager == null) mManager = getSupportFragmentManager();

        android.support.v4.app.FragmentTransaction transaction = mManager.beginTransaction();

        if (mFragment != null) closeMessage();

        if (mFragment == null) {
            mFragment = new MessageFragment();
            mFragment.setCloser(this);
            mFragment.addContent(message);
            transaction.add(R.id.fragment_layout, mFragment);
            transaction.commit();
            startAutoClose();
        }

    }


    @Override
    public void closeMessage() {
        if (mFragment != null) {
            Log.d(TAG, "CLosing fragment");
            if (mManager == null) mManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = mManager.beginTransaction();
            transaction.remove(mFragment);
            transaction.commit();
            mFragment = null;
        }
    }

    private void startAutoClose() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
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

        //checkIfTag(getIntent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkIfTag(getIntent());
    }



}
