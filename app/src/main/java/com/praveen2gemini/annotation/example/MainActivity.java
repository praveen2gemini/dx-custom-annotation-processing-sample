package com.praveen2gemini.annotation.example;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.praveen2gemini.binder.DXViewBinding;
import com.praveen2gemini.lib.annotations.BindView;
import com.praveen2gemini.lib.annotations.OnBroadcast;
import com.praveen2gemini.lib.annotations.OnClick;

/**
 * Forked from Mindork's Custom Annotation
 * <p>
 * During the enhancement, this project handling Broadcast receiver functions extra.
 *
 * @author Praveen Kumar Sugumaran
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_content)
    TextView actionTextView;

    private static final String ACTION_INTENT_TEST = "com.praveen2gemini.annotation.ACTION_INTENT_TEST";
    private static final String ACTION_SNACKBAR_TEST = "ACTION_SNACKBAR_TEST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DXViewBinding.bind(this);
    }

    @OnClick(R.id.testButtonOne)
    void testButtonOne(View v) {
        sendBroadcast(new Intent(ACTION_INTENT_TEST));
    }

    @OnClick(R.id.testButtonTwo)
    void testButtonTwo(View v) {
        sendBroadcast(new Intent(ACTION_SNACKBAR_TEST));
    }

    @OnBroadcast(ACTION_INTENT_TEST)
    void showToastOne(Intent intent) {
        actionTextView.setText("Clicked TestButtonOne\n");
        actionTextView.append(String.format("Action is %s", intent.getAction()));
        Toast.makeText(this, "Hi custom broadcast receiver for testButtonOne()  " + intent.getAction(), Toast.LENGTH_SHORT).show();
    }

    @OnBroadcast(ACTION_SNACKBAR_TEST)
    void showToastTwo(Intent intent) {
        actionTextView.setText("Clicked TestButtonTwo\n");
        actionTextView.append(String.format("Action is %s", intent.getAction()));
        Toast.makeText(this, "Hi custom broadcast receiver for testButtonTwo() " + intent.getAction(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        DXViewBinding.unbind(this);
        super.onDestroy();
    }
}
