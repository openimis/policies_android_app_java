package org.openimis.imispolicies;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;
import androidx.test.core.app.ApplicationProvider;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openimis.imispolicies.tools.ImageManager;
import org.openimis.imispolicies.tools.StorageManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Global.class)
public class AcquireTest {

    private Acquire activity;
    private Acquire spy;

    @Mock Global mockGlobal;
    @Mock ClientAndroidInterface mockCa;
    @Mock SQLHandler mockSql;
    @Mock Bitmap mockBitmap;
    @Mock
    RequestCreator mockRequestCreator;

    private EditText et;
    private ImageView iv;
    private Button submit;
    private ImageButton scan;
    private ImageButton photo;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        ActivityController<Acquire> controller =
                Robolectric.buildActivity(Acquire.class);

        activity = controller.create().start().resume().get();
        ImageManager mockImageManager = mock(ImageManager.class);

        activity.sqlHandler = mockSql;
        activity.ca = mockCa;
        activity.global = mockGlobal;
        activity.picasso = mock(Picasso.class);
        activity.storageManager = mock(StorageManager.class);
        activity.global = mockGlobal;
        activity.ca = mockCa;
        activity.sqlHandler = mockSql;
        activity.tempPhotoUri = mock(Uri.class);
        activity.imageManager = mockImageManager;
        spy = spy(activity);

        et = activity.findViewById(R.id.etCHFID);
        iv = activity.findViewById(R.id.imageView);
        submit = activity.findViewById(R.id.btnSubmit);
        scan = activity.findViewById(R.id.btnScan);
        photo = activity.findViewById(R.id.btnTakePhoto);

        when(mockGlobal.getOfficerCode()).thenReturn("OFF123");
        when(mockGlobal.getIntKey(anyString(), anyInt())).thenReturn(400);
        when(mockGlobal.getSubdirectory("Images"))
                .thenReturn(RuntimeEnvironment.getApplication().getFilesDir().getAbsolutePath());
        doReturn("imageFolder").when(mockGlobal).getImageFolder();
        doReturn(new File("/tmp/fake.jpg")).when(mockImageManager).getNewestInsureeImage(any());
        doReturn(new File[]{ new File("/tmp/fake.jpg") })
                .when(mockImageManager)
                .getInsureeImages(any());

        doReturn(mockRequestCreator)
                .when(activity.picasso)
                .load(any(File.class));

        doReturn(mockRequestCreator)
                .when(mockRequestCreator)
                .placeholder(anyInt());

        doReturn(mockRequestCreator)
                .when(mockRequestCreator)
                .error(anyInt());

        doNothing().when(mockRequestCreator).into(any(ImageView.class));
    }

    @Test
    public void clickingScan_shouldLaunchZXing() {
        scan.performClick();
        Intent started = shadowOf(activity).getNextStartedActivity();
        assertEquals("com.google.zxing.client.android.SCAN", started.getAction());
        assertEquals("QR_CODE_MODE", started.getStringExtra("SCAN_MODE"));
    }

    @Test
    public void clickingTakePhoto_withCHF_shouldLaunchCamera() {
        et.setText("123456789");
        photo.performClick();
        Intent started = shadowOf(activity).getNextStartedActivity();
        assertEquals(MediaStore.ACTION_IMAGE_CAPTURE, started.getAction());
        assertNotNull(started.getParcelableExtra(MediaStore.EXTRA_OUTPUT));
    }

    @Test
    public void onActivityResult_scan_shouldFillCHFID() {
        Intent data = new Intent();
        data.putExtra("SCAN_RESULT", "CHF999");
        activity.onActivityResult(0, Activity.RESULT_OK, data);
        assertEquals("CHF999", et.getText().toString());
    }

    @Test
    public void isValidate_missingImage_returnsFalse() {
        et.setText("123456789");
        activity.theImage = null;
        assertFalse(activity.isValidate());
    }

    @Test
    public void isValidate_valid_returnsTrue() {
        et.setText("123456789");
        activity.theImage = mockBitmap;
        assertTrue(activity.isValidate());
    }

    @Test
    public void menuStatistics_withoutInternet_returnsFalse() {
        when(mockGlobal.isNetworkAvailable()).thenReturn(false);
        MenuItem item = mock(MenuItem.class);
        when(item.getItemId()).thenReturn(R.id.mnuStatistics);
        assertFalse(activity.onOptionsItemSelected(item));
    }

    @Test
    public void submitData_shouldCreateFile_andUpdateSql() throws Exception {
        et.setText("123456789");
        activity.theImage = mockBitmap;

        when(mockBitmap.compress(any(), anyInt(), any()))
                .thenAnswer(invocation -> {
                    FileOutputStream fos = (FileOutputStream) invocation.getArgument(2);
                    fos.write(new byte[]{1,2,3});
                    return true;
                });

        Method m = Acquire.class.getDeclaredMethod("SubmitData");
        m.setAccessible(true);
        int result = (int) m.invoke(activity);

        assertEquals(1, result);

        verify(mockSql).updateData(
                eq("tblInsuree"),
                any(ContentValues.class),
                eq("CHFID = ?"),
                any(String[].class),
                eq(false)
        );
    }

    @Test
    public void submitData_emptyFile_returnsZero() throws Exception {
        et.setText("123456789");
        activity.theImage = mockBitmap;

        when(mockBitmap.compress(any(), anyInt(), any())).thenReturn(true);

        Method m = Acquire.class.getDeclaredMethod("SubmitData");
        m.setAccessible(true);
        int result = (int) m.invoke(activity);

        assertEquals(0, result);
        verify(mockSql, never()).updateData(any(), any(), any(), any(), anyBoolean());
    }
}
