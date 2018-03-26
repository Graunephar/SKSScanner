package lol.graunephar.android.nfc;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

import lol.graunephar.android.nfc.collections.MessageListAdapter;
import lol.graunephar.android.nfc.collections.MessageMap;
import lol.graunephar.android.nfc.models.TagContentMessage;

import static lol.graunephar.android.nfc.ScanActivity.KEY_MESSAGES_MAP;

public class ListFoundActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String json = getIntent().getStringExtra(KEY_MESSAGES_MAP);

        MessageMap map = new MessageMap(json); //Get mao from json parsed in intent
        ArrayList<TagContentMessage> messages = map.toList(); // Convert map to list

        MessageListAdapter adapter = new MessageListAdapter(this, map);

        setListAdapter(adapter);
    }
}
