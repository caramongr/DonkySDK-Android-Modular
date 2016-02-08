package net.donky.sample.rich.inbox;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.donky.core.DonkyException;
import net.donky.core.DonkyResultListener;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.messaging.logic.DonkyMessaging;
import net.donky.core.messaging.rich.logic.database.RichMsgContentProvider;
import net.donky.core.messaging.rich.logic.model.DatabaseSQLContract;
import net.donky.core.messaging.rich.logic.model.RichMessage;
import net.donky.core.messaging.rich.logic.model.RichMessageDataController;

import java.util.Map;

/**
 * Activity that will host list of received Rich Messages.
 */
public class ActivityInbox extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 1;

    private ListView listview;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        listview = (ListView) findViewById(R.id.list_view);
        initLoader();

        // Set the user external id as title for the Activity. You can use that id to target Rich Messages to that particular user.
        UserDetails user = DonkyAccountController.getInstance().getCurrentDeviceUser();
        if (user != null)
            setTitle(user.getUserId());
    }

    @Override
    public void onLoadFinished(
            android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                // The asynchronous load is complete and the data is now available for use.
                adapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(
            android.support.v4.content.Loader<Cursor> loader) {
        // Previously created loader is being reset, and thus making its data unavailable.
        adapter.swapCursor(null);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(
            int id, Bundle args) {
        // Instantiate and return a new Loader
        // Include _id - mandatory to create Loader
        // Include internalId to be able to identify rich message with clicked row
        // Include Sender display name to display that as an title for the list row
        // Include Description of rich message to give context to the message represented by list row
        return new CursorLoader(this,
                RichMsgContentProvider.getContentUri(getApplicationContext()),
                new String[] {"_id",
                        DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId,
                        DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName,
                        DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description},
                null,
                null,
                null);
    }

    /**
     * Initialise the loader and set click listeners
     */
    private void initLoader() {

        // This table columns will be used to populate list row layout elements defined as 'to' array
        String[] from = new String[] {DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_senderDisplayName, DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_description};

        // This views will be populated with columns content defined in 'from' array
        int[] to = new int[] {
                R.id.sender,
                R.id.description
        };

        // Create simple adapter for list view
        adapter = new SimpleCursorAdapter(this,
                R.layout.row, null, from, to, 0) {

            @Override
            public View getView (final int position, View convertView, ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);

                // Set the button clicked listener for deleting messages
                v.findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buttonDeleteClicked(position);
                    }
                });
                return v;
            }
        };

        // Bind adapter with list view
        listview = (ListView) findViewById(R.id.list_view);
        listview.setAdapter(adapter);

        // Set action when list element will be clicked
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemClicked(position);
            }
        });

        // Initialise loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    /**
     * List element has been clicked. This will load associated Rich Message and start Activity that will display the message body in a WebView
     * @param position List item position in list view
     */
    private void itemClicked(int position) {
        RichMessageDataController.getInstance().getRichMessagesDAO().getRichMessage(getClickedMessageInternalId(position), new DonkyResultListener<RichMessage>() {
            @Override
            public void success(RichMessage result) {
                startActivity(getRichMessageIntent(result));
            }
            @Override
            public void error(DonkyException donkyException, Map<String, String> validationErrors) {

            }
        });
    }

    /**
     * Delete message on button clicked
     * @param position Position in the list view.
     */
    private void buttonDeleteClicked(int position) {
        RichMessageDataController.getInstance().getRichMessagesDAO().removeRichMessage(getClickedMessageInternalId(position), null);
    }

    /**
     * Obtain internalId for a message represented by list row element on given position.
     * @param position List item position in list view
     * @return Rich Message internalId
     */
    private String getClickedMessageInternalId(int position) {
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(position);
        return cursor.getString(cursor.getColumnIndex(DatabaseSQLContract.RichMessageEntry.COLUMN_NAME_internalId));
    }

    /**
     * Gets Intent to start Activity that will display Rich Message body.
     * @param richMessage Rich Message to be displayed.
     * @return Intent to start Activity that will display Rich Message body.
     */
    private Intent getRichMessageIntent(RichMessage richMessage) {
        if (richMessage != null) {
            Intent intent = new Intent(ActivityInbox.this, ActivityMessage.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(DonkyMessaging.KEY_INTENT_BUNDLE_RICH_MESSAGE, richMessage);
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            return intent;
        } else {
            return null;
        }
    }
}