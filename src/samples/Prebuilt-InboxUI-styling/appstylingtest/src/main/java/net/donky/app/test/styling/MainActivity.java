package net.donky.app.test.styling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.donky.core.messaging.rich.inbox.ui.components.RichInboxAndMessageActivityWithToolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void openInbox(View view) {

        Intent intent = new Intent(this, RichInboxAndMessageActivityWithToolbar.class);
        startActivity(intent);
    }
}
