package lol.graunephar.android.nfc;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ListFoundActivity extends ListActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // set the list adapter
        String[] entities = {"Users", "Books", "Orders", "States"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, entities);
        setListAdapter(adapter);

    }

}
