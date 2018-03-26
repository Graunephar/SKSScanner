package lol.graunephar.android.nfc.collections;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import lol.graunephar.android.nfc.R;
import lol.graunephar.android.nfc.models.TagContentMessage;

/**
 * Created by daniel on 3/26/18.
 */

public class MessageListAdapter extends BaseAdapter{

    private final ArrayList<TagContentMessage> messages;
    private final Context context;

    public MessageListAdapter(Context context, MessageMap messages) {
        this.messages = messages.toList();
        this.context = context;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout , parent, false);

        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(messages.get(position).getName());
        return rowView;
    }
}
