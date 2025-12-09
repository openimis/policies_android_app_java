package org.openimis.imispolicies;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import com.squareup.picasso.Picasso;
import org.openimis.imispolicies.tools.StorageManager;
import org.robolectric.RobolectricTestRunner;

import android.app.Activity;
import android.content.res.Resources;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

@RunWith(RobolectricTestRunner.class)
public class ClientAndroidInterfaceTest {

    @Mock
    SQLHandler sqlHandler;

    @Mock
    Global global;

    @Mock
    Activity activity;

    @Mock
    Resources resources;

    @Mock
    StorageManager storageManager;

    ClientAndroidInterface client;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(activity.getResources()).thenReturn(resources);
        when(resources.getString(anyInt())).thenReturn("mockString");

        client = new ClientAndroidInterface(activity, sqlHandler, global, null, storageManager);

        when(global.isNetworkAvailable()).thenReturn(true);
    }

    @Test
    public void testCreateNewInsuree_ShouldInsertInDatabase() throws Exception {

        String insureeJson = "{"
                + "\"txtInsuranceNumber\":\"12345\","
                + "\"hfInsureeId\":\"0\","
                + "\"hfisHead\":\"1\","
                + "\"txtLastName\":\"Doe\","
                + "\"txtOtherNames\":\"John\","
                + "\"txtBirthDate\":\"1991-01-01\","
                + "\"ddlGender\":\"M\","
                + "\"txtPhoneNumber\":\"690000000\""
                + "\"hfNewPhotoPath\":\"\""
                + "\"hfImagePath\":\"/storage/emulated/0/DCIM/test.jpg\""
                + "}";

        ClientAndroidInterface spyClient = spy(client);

        HashMap<String, String> mockData = new HashMap<>();
        mockData.put("txtInsuranceNumber", "12345");
        mockData.put("hfInsureeId", "0");
        mockData.put("hfisHead", "1");
        mockData.put("txtLastName", "Doe");
        mockData.put("txtOtherNames", "John");
        mockData.put("txtBirthDate", "1991-01-01");
        mockData.put("ddlGender", "M");
        mockData.put("txtPhoneNumber", "690000000");
        mockData.put("hfNewPhotoPath", "");
        mockData.put("hfImagePath", "/storage/emulated/0/DCIM/test.jpg");

        when(sqlHandler.getResult(anyString(), any(String[].class))).thenReturn(new JSONArray());
        doNothing().when(spyClient).SaveInsureePolicy(anyInt(), anyInt(), anyBoolean(), anyInt());
        doNothing().when(sqlHandler).insertData(anyString(), any());
        doNothing().when(sqlHandler).updateData(anyString(), any(), anyString(), any(String[].class));
        doReturn(mockData).when(spyClient).jsonToTable(anyString());
        doReturn(0).when(spyClient).isValidInsureeData(mockData);
        doReturn(999).when(spyClient).getNextAvailableInsureeId();
        doReturn(0).when(spyClient).getFamilyStatus(anyInt());
        doReturn(0).when(spyClient).getInsureeStatus(anyInt());
        doNothing().when(spyClient).ShowDialog(anyString());
        doNothing().when(spyClient).ShowDialogYesNo(anyInt(), anyInt(), anyInt());
        doReturn("").when(spyClient).copyImageFromGalleryToApplication(anyString(), anyString());

        doNothing().when(sqlHandler).insertData(anyString(), any());

        int result = spyClient.SaveInsuree(
                insureeJson,
                1,  // FamilyId
                1,  // isHead
                0,  // ExceedThreshold
                0   // PolicyId
        );

        assertEquals(-999, result);

        verify(sqlHandler, times(1)).insertData(eq("tblInsuree"), any());
    }
}
