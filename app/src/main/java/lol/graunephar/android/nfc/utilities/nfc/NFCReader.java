package lol.graunephar.android.nfc.utilities.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;

import lol.graunephar.android.nfc.models.TagContentMessage;

/**
 * Created by daniel on 3/16/18.
 * https://stackoverflow.com/questions/12453658/reading-data-from-nfc-tag
 */


public class NFCReader {

    private static final String TAG = NFCReader.class.toString();
    private final Context mContext;
    private Gson gson = new Gson();

    public NFCReader(Context context) {
        this.mContext = context;
    }


    public TagContentMessage readFromTag(Intent intent, Tag tag) throws IOException {

        Ndef ndef = Ndef.get(tag);

        if(ndef == null){
            //As ndef is null, this is the only place were we fo not have to close the connection
            throw new NotSupportedContentException("This is a strange tag");
        }

        ndef.connect();

        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (messages == null) {
            ndef.close(); //Always close the connection
            Log.d(TAG, "Empty tag");
            throw new EmptytagException("The tag is Empty");
        }

        TagContentMessage res = getContent(messages);

        ndef.close();

        return res;

    }


    private TagContentMessage getContent(Parcelable[] messages) throws NotSupportedContentException {
        NdefMessage[] ndefMessages = new NdefMessage[messages.length];
        for (int i = 0; i < messages.length; i++) {
            ndefMessages[i] = (NdefMessage) messages[i];
        }

        if (!isThisOurTag(ndefMessages)) {
            Log.d(TAG, "Not our tag");
            throw new NotSupportedContentException("Tag is not out type");

        }

        NdefRecord record = ndefMessages[0].getRecords()[0];
        byte[] payload = record.getPayload();
        String jsonstring = new String(payload);
        Log.d(TAG, "IS ourtag");

        TagContentMessage res = gson.fromJson(jsonstring, TagContentMessage.class);

        return res;
    }

    private boolean isThisOurTag(NdefMessage[] ndefMessages) {

        NdefRecord[] records = ndefMessages[0].getRecords();

        String packagename = mContext.getPackageName();
        for (int i = 0; i < records.length; i++) {

            NdefRecord type = records[i];
            byte[] aar = type.getPayload();
            String aarcontent = new String(aar);
            if (aarcontent.equals((packagename))) return true;
        }

        return false;
    }

    class NFCReaderException extends IOException {
        private final String message;

        public NFCReaderException(String messsage) {
            this.message = messsage;
        }
    }

    public class NotSupportedContentException extends NFCReaderException {

        public NotSupportedContentException(String messsage) {
            super(messsage);
        }
    }

    public class EmptytagException extends NFCReaderException {

        public EmptytagException(String messsage) {
            super(messsage);
        }
    }


}


