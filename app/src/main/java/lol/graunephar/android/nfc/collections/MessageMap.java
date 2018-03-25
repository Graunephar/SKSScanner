package lol.graunephar.android.nfc.collections;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import lol.graunephar.android.nfc.models.TagContentMessage;

/**
 * Created by daniel on 3/26/18.
 */

public class MessageMap {

    private TreeMap<Integer, TagContentMessage> messages;
    public MessageMap() {
        this.messages = new TreeMap<>();
    }

    public MessageMap(String json) {
        this.messages = fromJson(json);
    }


    public boolean add(TagContentMessage message) {
        int hash = message.hashCode();
        if(!messages.containsKey(hash)) {
            this.messages.put(hash, message);
            return true;
        } else {
            return false;
        }
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(messages);
    }

    private TreeMap fromJson(String json) {
        Type listType = new TypeToken<TreeMap<Integer, TagContentMessage>>(){}.getType();
        return new Gson().fromJson(json, listType);
    }


    public boolean contains(TagContentMessage message) {
        int hash = message.hashCode();
        return messages.containsKey(hash);
    }
}
