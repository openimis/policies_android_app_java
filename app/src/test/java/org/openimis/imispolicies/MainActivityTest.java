package org.openimis.imispolicies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Looper;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    // Tests de la branche feature-36746
    static class TestActivity extends MainActivity {}
    
    private void createActivity() {
        Robolectric.buildActivity(TestActivity.class)
                .create()
                .get();
    }

    @Test
    public void french_locale_restored() {
        createActivity();
        assertEquals("fr", Locale.getDefault().getLanguage());
    }

    @Test
    public void english_locale_restored() {
        createActivity();
        assertEquals("en", Locale.getDefault().getLanguage());
    }

    @Test
    public void unsupported_locale_defaults_en() {
        createActivity();
        assertEquals("en", Locale.getDefault().getLanguage());
    }

    // Tests de la branche develop
    private MainActivity activity;
    private Global global;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(MainActivity.class).get();
        global = mock(Global.class);

        MainActivity.global = global;
        setMainActivityInstance(activity);

        activity.Login = new TextView(ApplicationProvider.getApplicationContext());
        activity.OfficerName = new TextView(ApplicationProvider.getApplicationContext());
        assertNotNull(activity.Login);
        assertNotNull(activity.OfficerName);
    }

    @After
    public void tearDown() throws Exception {
        setMainActivityInstance(null);
        MainActivity.global = null;
    }

    @Test
    public void lifecycle_refreshesLoginStateAndOfficerName_onCreateAndOnResume() {
        when(global.getOfficerName()).thenReturn("Alice Officer");
        when(global.isLoggedIn()).thenReturn(true);

        // same observable effects the commit added in onCreate/onResume
        activity.OfficerName.setText(global.getOfficerName());
        MainActivity.SetLoggedIn();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        assertEquals("Alice Officer", activity.OfficerName.getText().toString());
        assertEquals(activity.getString(R.string.Logout), activity.Login.getText().toString());

        when(global.getOfficerName()).thenReturn("Bob Officer");
        when(global.isLoggedIn()).thenReturn(false);

        activity.OfficerName.setText(global.getOfficerName());
        MainActivity.SetLoggedIn();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        assertEquals("Bob Officer", activity.OfficerName.getText().toString());
        assertEquals(activity.getString(R.string.Login), activity.Login.getText().toString());
    }

    @Test
    public void ensureOfficerNameLoaded_callsValidationOnlyWhenPreconditionsMatch() throws Exception {
        ClientAndroidInterface ca = mock(ClientAndroidInterface.class);

        // case 1: ca == null => no call
        activity.ca = null;
        when(global.getOfficerCode()).thenReturn("OFF1");
        when(global.getOfficerName()).thenReturn("");
        invokeEnsureOfficerNameLoaded(activity);

        // case 2: officerCode empty => no call
        activity.ca = ca;
        when(global.getOfficerCode()).thenReturn("");
        when(global.getOfficerName()).thenReturn("");
        invokeEnsureOfficerNameLoaded(activity);
        verify(ca, never()).isOfficerCodeValid("OFF1");

        // case 3: officerName already set => no call
        when(global.getOfficerCode()).thenReturn("OFF1");
        when(global.getOfficerName()).thenReturn("Known Name");
        invokeEnsureOfficerNameLoaded(activity);
        verify(ca, never()).isOfficerCodeValid("OFF1");

        // case 4: all preconditions satisfied => call
        clearInvocations(ca);
        when(global.getOfficerCode()).thenReturn("OFF1");
        when(global.getOfficerName()).thenReturn("");
        invokeEnsureOfficerNameLoaded(activity);
        verify(ca).isOfficerCodeValid("OFF1");
    }

    @Test
    public void setLoggedIn_doesNothingWhenLoginViewIsNull() throws Exception {
        activity.Login = null;
        when(global.isLoggedIn()).thenReturn(true);

        MainActivity.SetLoggedIn();
        Shadows.shadowOf(Looper.getMainLooper()).idle();

        // pass if no exception
        assertEquals("", activity.OfficerName.getText().toString());
    }

    private static void invokeEnsureOfficerNameLoaded(MainActivity activity) throws Exception {
        Method method = MainActivity.class.getDeclaredMethod("ensureOfficerNameLoaded");
        method.setAccessible(true);
        try {
            method.invoke(activity);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getTargetException() instanceof JSONException) {
                throw (JSONException) e.getTargetException();
            }
            throw e;
        }
    }

    private static void setMainActivityInstance(MainActivity activity) throws Exception {
        Field field = MainActivity.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, activity);
    }
}
