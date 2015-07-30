package net.donky.app.test.styling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.donky.core.DonkyCore;
import net.donky.core.events.OnCreateEvent;
import net.donky.core.events.OnPauseEvent;
import net.donky.core.events.OnResumeEvent;
import net.donky.core.messaging.rich.inbox.ui.components.RichInboxAndMessageActivityWithToolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnCreateEvent(getIntent()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnResumeEvent());
    }

    @Override
    protected void onPause() {
        super.onPause();

        // For Analytic Module
        DonkyCore.publishLocalEvent(new OnPauseEvent());
    }

    public void openInbox(View view) {

        Intent intent = new Intent(this, RichInboxAndMessageActivityWithToolbar.class);
        startActivity(intent);
    }
}
