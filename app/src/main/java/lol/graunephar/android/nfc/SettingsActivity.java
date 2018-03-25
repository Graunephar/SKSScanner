package lol.graunephar.android.nfc;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingsActivity extends AppCompatActivity {

    public static final String SETTINGS_KEY_NUM = "settings_key_num";
    @BindView(R.id.settings_save_btn)
    Button mSaveButton;
    @BindView(R.id.settings_nr_of_tokens_txt_nr)
    EditText mNumberOfTokens;
    @BindView(R.id.settings_nr_of_tokens_label)
    TextView mNrLabel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        mNrLabel.setText(getString(R.string.settings_number_of_tokens));
        mSaveButton.setText(R.string.settings_save_button_text);

    }

    @OnClick(R.id.settings_save_btn)
    public void save() {
        int nr = Integer.parseInt(mNumberOfTokens.getText().toString());

        Bundle result = new Bundle();
        result.putInt(SETTINGS_KEY_NUM, nr);
        Intent intent = new Intent();
        intent.putExtras(result);
        setResult(RESULT_OK, intent);
        finish();

    }
}
