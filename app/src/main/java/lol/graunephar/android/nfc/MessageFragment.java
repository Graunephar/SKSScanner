package lol.graunephar.android.nfc;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import lol.graunephar.android.nfc.models.TagContentMessage;


public class MessageFragment extends Fragment {

    private MessageCloser mCloser;
    private Button exitBtn;
    private TextView headlineText;
    private TagContentMessage mContent;
    private TextView nameTxt;
    private TextView descriptionTxt;
    private TextView pointTxt;

    public void setCloser(MessageCloser closer) {
        this.mCloser = closer;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        unbinder = ButterKnife.bind(this, view);
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    private Unbinder unbinder;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStart() {
        super.onStart();

        updateUI();
        setFullscreen(getActivity());

    }

    private void updateUI() {
        exitBtn = getView().findViewById(R.id.exit_message_fragment_btn);
        exitBtn.setText(R.string.exit_button_text);
        headlineText = getView().findViewById(R.id.message_headline_txtView);
        headlineText.setText(getString(R.string.message_headline_text));

        loadFrontContent();

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCloser.closeMessage();
            }
        });

        setBackroundColor();
    }

    private void loadFrontContent() {
        if(mContent == null) return;
        nameTxt = getView().findViewById(R.id.message_name_txtView);
        descriptionTxt = getView().findViewById(R.id.message_description_txtView);
        pointTxt = getView().findViewById(R.id.message_point_txtView);

        nameTxt.setText(mContent.getName());
        descriptionTxt.setText(mContent.getFact());
        //pointTxt.setText(mContent.getPoints());
    }

    private void setBackroundColor() {
        getView().setBackgroundColor(Color.GREEN);
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    /**
     * https://gist.github.com/gelldur/cf7ff0d0d026dbd743d0
     *
     * @param activity
     */
    public void setFullscreen(Activity activity) {
        if (Build.VERSION.SDK_INT > 10) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (isImmersiveAvailable()) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        } else {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static boolean isImmersiveAvailable() {
        return android.os.Build.VERSION.SDK_INT >= 19;
    }

    public void addContent(TagContentMessage message) {
        mContent = message;
    }
}
