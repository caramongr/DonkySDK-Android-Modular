package net.donky.location;

import android.app.Application;
import android.location.Location;
import android.os.Bundle;
import android.test.ApplicationTestCase;

import com.google.android.gms.location.LocationRequest;

import net.donky.core.DonkyCore;
import net.donky.core.DonkyException;
import net.donky.core.account.DeviceDetails;
import net.donky.core.account.UserDetails;
import net.donky.core.events.CoreInitialisedSuccessfullyEvent;
import net.donky.location.internal.LocationUpdatesCallback;
import net.donky.location.mock.MockDonkyEventListener;
import net.donky.location.mock.MockDonkyListener;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class LocationApplicationTest extends ApplicationTestCase<Application> {

    private static int TIME_OUT = 30000;

    private static String apiKey = "PUT_YOUR_API_KEY_HERE";

    private static String initialUserId = "test_" + new Integer(Math.abs(new Random().nextInt(Integer.MAX_VALUE)));




    public LocationApplicationTest() {
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
        DonkyLocation.initialiseDonkyLocation(getApplication(), listenerA);

        synchronized (listenerA) {
            listenerA.wait(TIME_OUT);
        }

        DonkyException donkyException = listenerA.getDonkyException();
        assertNull(donkyException);

        Map<String, String> validationErrors = listenerA.getValidationErrors();
        assertNull(validationErrors);

        MockDonkyListener listenerB = new MockDonkyListener();
        DonkyCore.initialiseDonkySDK(getApplication(), apiKey, userDetails, deviceDetails, "1.0.0.0", listenerB);

        synchronized (mockDonkyEventListenerInitialised) {
            mockDonkyEventListenerInitialised.wait(TIME_OUT);
        }
    }

    @Test
    public void testLastKnownLocation() {
        Location location = DonkyLocationController.getInstance().getLastLocation();
        assertNotNull("Last location not allowed to be null", location);
    }

    @Test
    public void testLocationUpdates() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        DonkyLocationController.getInstance().registerLocationListener(new LocationUpdatesCallback() {
            @Override
            public void onConnected(Bundle bundle) {

            }

            @Override
            public void onLocationChanged(Location location) {
                assertNotNull("Last location not allowed to be null", location);
                lock.countDown();
            }
        });
        lock.await(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testCustomLocationUpdates() throws Exception {
        final CountDownLatch lock = new CountDownLatch(1);
        LocationRequest request = new LocationRequest();
        request.setInterval(300);
        request.setFastestInterval(300);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(1);

        assertNotNull("request not allowed to be null", request);

        DonkyLocationController.getInstance().registerLocationListener(new LocationUpdatesCallback() {
            @Override
            public void onConnected(Bundle bundle) {

            }

            @Override
            public void onLocationChanged(Location location) {
                assertNotNull("Last location not allowed to be null", location);
                lock.countDown();
            }
        }, request);
        lock.await(2000, TimeUnit.MILLISECONDS);
    }

}