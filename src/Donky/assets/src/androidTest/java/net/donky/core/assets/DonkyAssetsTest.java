package net.donky.core.assets;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.test.ApplicationTestCase;
import android.text.TextUtils;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.DonkyAccountController;
import net.donky.core.account.UserDetails;
import net.donky.core.assets.mock.MockDonkyEventListener;
import net.donky.core.assets.mock.MockDonkyListener;
import net.donky.core.assets.mock.MockDonkyResultListener;
import net.donky.core.assets.mock.MockImageLoader;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.core.network.assets.Asset;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Random;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class DonkyAssetsTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 30000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "test_" + Integer.valueOf(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));

    public DonkyAssetsTest() {
        super(Application.class);
    }


    @Before
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();

        UserDetails userDetails = new UserDetails();
        userDetails.setUserCountryCode("GBR").
                setUserId(initialUserId).
                setUserFirstName("John").
                setUserLastName("Smith").
                setUserMobileNumber("07555555555").
                setUserEmailAddress("j.s@me.com").
                setUserDisplayName("John");

        DeviceDetails deviceDetails = new DeviceDetails("phone2 my favorite", "phone2", null);

        MockDonkyEventListener mockDonkyEventListenerInitialised = new MockDonkyEventListener<>(CoreInitialisedSuccessfullyEvent.class);
        DonkyCore.subscribeToLocalEvent(mockDonkyEventListenerInitialised);

        MockDonkyListener listenerA = new MockDonkyListener();
        DonkyAssets.initialiseDonkyAssets(getApplication(), listenerA);

        synchronized (listenerA) {
            listenerA.wait(TIME_OUT);
        }

        DonkyException donkyException = listenerA.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listenerA.getValidationErrors();
        assertNull(validationErrors);

        MockDonkyListener listenerB = new MockDonkyListener();
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listenerB);

        synchronized (listenerB) {
            listenerB.wait(TIME_OUT);
        }

        assertEquals(DonkyAccountController.getInstance().isRegistered(), true);
    }

    @Test
    public void testUploadAvatar() throws InterruptedException, DonkyException {

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bmp = Bitmap.createBitmap(1, 1, conf);

        MockDonkyResultListener<String> mockDonkyResultListener = new MockDonkyResultListener<>();

        DonkyAssetController.getInstance().uploadAccountAvatar(bmp, mockDonkyResultListener);

        synchronized (mockDonkyResultListener) {
            mockDonkyResultListener.wait(TIME_OUT);
        }

        assertEquals(mockDonkyResultListener.getDonkyException(), null);
        assertEquals(mockDonkyResultListener.getValidationErrors(), null);

        String assetId = mockDonkyResultListener.getResult();

        assertEquals(false, TextUtils.isEmpty(assetId));

        Bitmap bitmap = DonkyAssetController.getInstance().downloadImageAsset(assetId);

        assertNotNull(bitmap);

        MockImageLoader mockImageLoader = new MockImageLoader(getContext());

        DonkyAssetController.getInstance().downloadImageAsset(assetId, mockImageLoader);

        synchronized (mockImageLoader) {
            mockImageLoader.wait(TIME_OUT*2);
        }

        assertNull(mockImageLoader.getException());
        assertNotNull(mockImageLoader.getResult());
    }

    @Test
    public void testUploadMessageAssetFile() throws InterruptedException, DonkyException, IOException {

        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile(), "test.txt");

        file.createNewFile();
        FileOutputStream fOut = new FileOutputStream(file);
        OutputStreamWriter myOutWriter =
                new OutputStreamWriter(fOut);
        myOutWriter.append("test");
        myOutWriter.close();
        fOut.close();

        MockDonkyResultListener<Asset> mockDonkyResultListener = new MockDonkyResultListener<>();

        DonkyAssetController.getInstance().uploadMessageAsset(file, mockDonkyResultListener);

        synchronized (mockDonkyResultListener) {
            mockDonkyResultListener.wait(TIME_OUT);
        }

        assertEquals(mockDonkyResultListener.getDonkyException(), null);
        assertEquals(mockDonkyResultListener.getValidationErrors(), null);

        String assetId = mockDonkyResultListener.getResult().getAssetId();

        assertEquals(false, TextUtils.isEmpty(assetId));

        assertEquals(true, file.delete());
    }

    @Test
    public void testUploadMessageAssetBytes() throws InterruptedException, DonkyException, IOException {

        MockDonkyResultListener<Asset> mockDonkyResultListener = new MockDonkyResultListener<>();

        DonkyAssetController.getInstance().uploadMessageAsset("test".getBytes(), "text/plain", null, mockDonkyResultListener);

        synchronized (mockDonkyResultListener) {
            mockDonkyResultListener.wait(TIME_OUT);
        }

        assertEquals(mockDonkyResultListener.getDonkyException(), null);
        assertEquals(mockDonkyResultListener.getValidationErrors(), null);

        String assetId = mockDonkyResultListener.getResult().getAssetId();

        assertEquals(false, TextUtils.isEmpty(assetId));

    }
}