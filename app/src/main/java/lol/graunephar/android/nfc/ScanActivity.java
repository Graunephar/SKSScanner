package lol.graunephar.android.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import lol.graunephar.android.nfc.models.TagContentMessage;
import lol.graunephar.android.nfc.utilities.nfc.NFCReader;


public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.toString();
    Tag detectedTag;
    NfcAdapter nfcAdapter;
    IntentFilter[] readTagFilters;
    PendingIntent pendingIntent;
    private NFCReader mReader;

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
        if (getIntent().getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            readNFCData();
        }
    }

    private void readNFCData() {
        try {
            TagContentMessage message = mReader.readFromTag(getIntent(), detectedTag);
            tellUser(message.getName());
        } catch (NFCReader.EmptytagException e) {
            tellUser(getString(R.string.empty_tag_messaage));

        } catch (NFCReader.NotSupportedContentException e) {
            tellUser(getString(R.string.undopported_tag_message));
        } catch (IOException e) {
            tellUser(getString(R.string.reader_unable_read_message));
        }


    }

    private void tellUser(String string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {

        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, readTagFilters, null);
    }
}
