package org.openimis.imispolicies;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
public class MainActivityTest {

    static class TestActivity extends MainActivity {}
    private void createActivity() {
        Robolectric.buildActivity(TestActivity.class)
                .create()
                .get();
    }

    @Test
    @Config(qualifiers = "fr")
    public void french_locale_restored() {
        createActivity();
        assertEquals("fr", Locale.getDefault().getLanguage());
    }

    @Test
    @Config(qualifiers = "en")
    public void english_locale_restored() {
        createActivity();
        assertEquals("en", Locale.getDefault().getLanguage());
    }

    @Test
    @Config(qualifiers = "es")
    public void unsupported_locale_defaults_en() {
        createActivity();
        assertEquals("en", Locale.getDefault().getLanguage());
    }
}