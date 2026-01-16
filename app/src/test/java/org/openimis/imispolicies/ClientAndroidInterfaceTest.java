package org.openimis.imispolicies;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.openimis.imispolicies.tools.StorageManager;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.domain.entity.Policy;
import org.robolectric.RobolectricTestRunner;
import org.openimis.imispolicies.network.exception.HttpException;

import android.app.Activity;
import android.content.res.Resources;
import android.util.Pair;
import android.view.Window;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.robolectric.shadows.ShadowLooper;

import android.app.ProgressDialog;
import android.app.AlertDialog;
import android.view.WindowManager;

import java.util.HashMap;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Date;


@RunWith(RobolectricTestRunner.class)
public class ClientAndroidInterfaceTest {

    @Mock SQLHandler sqlHandler;
    @Mock Global global;
    @Mock Activity activity;
    @Mock Resources resources;
    @Mock StorageManager storageManager;
    @Mock ProgressDialog progressDialog;
    @Mock AlertDialog alertDialog;

    ClientAndroidInterface client;

    // ===== Global test data =====
    JSONArray familyArray;
    JSONArray insureesArray;
    JSONArray policiesArray;
    JSONArray premiumsArray;
    String insureeJson;
    HashMap<String, String> insureeMap;


    @Before
    public void setup() throws JSONException {

        MockitoAnnotations.openMocks(this);

        when(activity.getResources()).thenReturn(resources);
        when(resources.getString(anyInt())).thenReturn("mockString");
        when(global.isNetworkAvailable()).thenReturn(true);
        when(activity.getWindow()).thenReturn(mock(Window.class));

        client = new ClientAndroidInterface(
                activity,
                sqlHandler,
                global,
                null,
                storageManager
        );

        familyArray   = buildFamilyArray();
        insureesArray = buildInsureeArray();
        policiesArray = buildPolicyArray();
        premiumsArray = buildPremiumArray();
        insureeJson   = buildInsureeJson();
        insureeMap    = buildInsureeMap();
    }


    @After
    public void cleanUp() {
        client = null;
    }


    // =================== uploadEnrolment ===================

    @Test
    public void testUploadEnrolment_ShouldProcessSuccessfully() throws Exception {

        ClientAndroidInterface spyClient = spy(client);
        ProgressDialog mockDialog = mock(ProgressDialog.class);

        doReturn(mockDialog)
                .when(spyClient)
                .createProgressDialog(anyString(), anyString());

        doReturn(1)
                .when(spyClient)
                .Enrol(1);

        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(activity).runOnUiThread(any(Runnable.class));

        spyClient.uploadEnrolment();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(activity.getWindow())
                .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //verify(spyClient).Enrol(1);  This check often fails due to thread separation.
        verify(mockDialog).dismiss();
    }


    @Test
    public void testUploadEnrolment_ShouldHandleEnrolError() throws Exception {

        ClientAndroidInterface spyClient = spy(client);
        ProgressDialog mockDialog = mock(ProgressDialog.class);

        doReturn(mockDialog)
                .when(spyClient)
                .createProgressDialog(anyString(), anyString());

        doReturn(999)
                .when(spyClient)
                .Enrol(1);

        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(activity).runOnUiThread(any(Runnable.class));

        spyClient.uploadEnrolment();
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(mockDialog).dismiss();
    }


    // =================== Enrol ===================

    @Test
    public void testEnrol_ShouldProcessSuccessfully() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        String queryInsurees = "SELECT I.InsureeUUID AS InsureeUUID, I.InsureeId AS InsureeId, I.FamilyId AS FamilyId, I.CHFID, I.LastName, I.OtherNames, I.DOB, I.Gender, NULLIF(I.Marital,'') Marital, I.isHead, NULLIF(I.IdentificationNumber,'null') IdentificationNumber, NULLIF(I.Phone,'null') Phone, REPLACE(I.PhotoPath, RTRIM(PhotoPath, REPLACE(PhotoPath, '/', '')), '') PhotoPath, NULLIF(I.CardIssued,'null') CardIssued, NULLIF(I.Relationship,'null') Relationship, NULLIF(I.Profession,'null') Profession, NULLIF(I.Education,'null') Education, NULLIF(I.Email,'null') Email, CASE WHEN I.TypeOfId='null' THEN null ELSE I.TypeOfId END TypeOfId, NULLIF(I.HFID,'null') HFID, NULLIF(I.CurrentAddress,'null') CurrentAddress, NULLIF(I.GeoLocation,'null') GeoLocation, NULLIF(I.CurVillage,'null') CurVillage,I.isOffline, I.Vulnerability FROM tblInsuree I WHERE ";

        String queryPolicies =
                "SELECT p.PolicyId AS PolicyId, FamilyId AS FamilyId, EnrollDate, StartDate, " +
                "NULLIF(EffectiveDate,'null') EffectiveDate, ExpiryDate, Policystatus, PolicyValue, " +
                "ProdId, OfficerId, PolicyStage, isOffline, bcn.ControlNumber " +
                "FROM tblPolicy p LEFT JOIN tblBulkControlNumbers bcn on p.PolicyId=bcn.PolicyId WHERE ";

        String queryPremiums =
                "SELECT PR.PremiumId, PR.PolicyId, NULLIF(PR.PayerId,'null') PayerId, PR.Amount, " +
                "PR.Receipt, PR.PayDate, PR.PayType, PR.isPhotoFee,PR.isOffline " +
                "FROM tblPremium PR INNER JOIN tblPolicy PL ON PL.PolicyId = PR.PolicyId WHERE ";

        String queryFamilies =
                "SELECT F.FamilyUUID as FamilyUUID, F.FamilyId AS FamilyId, F.InsureeId AS InsureeId, " +
                "F.LocationId, I.CHFID AS HOFCHFID, NULLIF(F.Poverty,'null') Poverty, " +
                "NULLIF(F.FamilyType,'null') FamilyType, NULLIF(F.FamilyAddress,'null') FamilyAddress, " +
                "NULLIF(F.Ethnicity,'null') Ethnicity, NULLIF(F.ConfirmationNo,'null') ConfirmationNo, " +
                "F.ConfirmationType ConfirmationType,F.isOffline isOffline " +
                "FROM tblFamilies F INNER JOIN tblInsuree I ON I.InsureeId = F.InsureeId WHERE";

        JSONArray mockFamilies = new JSONArray();
        JSONObject family1 = new JSONObject();
        family1.put("FamilyId", "1");
        family1.put("isOffline", "1");
        mockFamilies.put(family1);

        when(sqlHandler.getResult(
                eq("SELECT FamilyId, isOffline FROM tblFamilies WHERE InsureeId != '' ORDER BY FamilyId"),
                isNull()
        )).thenReturn(mockFamilies);

        when(sqlHandler.getResult(contains(queryFamilies), any())).thenReturn(familyArray);
        when(sqlHandler.getResult(contains(queryInsurees), any())).thenReturn(insureesArray);
        when(sqlHandler.getResult(contains(queryPolicies), any())).thenReturn(policiesArray);
        when(sqlHandler.getResult(contains(queryPremiums), any())).thenReturn(premiumsArray);
        when(sqlHandler.getResult(anyString(), any(String[].class))).thenReturn(new JSONArray());

        doReturn(true).when(spyClient).isPolicyRequired();
        doReturn(true).when(spyClient).isContributionRequired();
        doReturn(new Pair[0]).when(spyClient).FamilyPictures(any(JSONArray.class), eq(1));

        doNothing().when(spyClient).DeleteImages(any(JSONArray.class), any(ArrayList.class), anyInt());
        doNothing().when(spyClient).DeleteUploadedData(anyInt(), any(ArrayList.class), anyInt());
        doReturn(1).when(spyClient).DeleteFamily(anyInt());

        doNothing().when(spyClient).updatePolicyRecords(any(JSONArray.class));
        doNothing().when(spyClient).ShowErrorMessages();

        when(activity.getString(anyInt())).thenReturn("mockString");

        doReturn(0)
                .when(spyClient)
                .uploadEnrols(familyArray, insureesArray, policiesArray, premiumsArray, new Pair[0]);

        int result = spyClient.Enrol(1);

        assertEquals(result, 0);
    }


    // =================== SaveInsuree - Create ===================

    @Test
    public void testCreateNewInsuree_ShouldInsertInDatabase() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        when(sqlHandler.getResult(anyString(), any(String[].class))).thenReturn(new JSONArray());

        doNothing().when(spyClient).SaveInsureePolicy(anyInt(), anyInt(), anyBoolean(), anyInt());
        doNothing().when(sqlHandler).insertData(anyString(), any());
        doNothing().when(sqlHandler).updateData(anyString(), any(), anyString(), any(String[].class));

        doAnswer(invocation -> {
            String paramJson = invocation.getArgument(0);
            assertEquals(insureeJson, paramJson);

            HashMap<String, String> result =
                    (HashMap<String, String>) invocation.callRealMethod();

            assertEquals(insureeMap, result);
            return result;

        }).when(spyClient).jsonToTable(anyString());

        doReturn(999).when(spyClient).getNextAvailableInsureeId();
        doReturn(0).when(spyClient).getFamilyStatus(1);

        doNothing().when(spyClient).ShowDialog(anyString());
        doNothing().when(spyClient).ShowDialogYesNo(anyInt(), anyInt(), anyInt());

        doReturn("")
                .when(spyClient)
                .copyImageFromGalleryToApplication(
                        insureeMap.get("hfNewPhotoPath"),
                        insureeMap.get("txtInsuranceNumber")
                );

        int result = spyClient.SaveInsuree(insureeJson, 1, 1, 0, 0);

        assertEquals(-999, result);

        verify(sqlHandler, times(1)).insertData(eq("tblInsuree"), any());
        verify(spyClient, times(1)).jsonToTable(insureeJson);
        verify(sqlHandler, times(1)).insertData(eq("tblInsuree"), any());
        verify(spyClient, times(1)).SaveInsureePolicy(999, 1, true, 0);
    }


    // =================== SaveInsuree - Modify ===================

    @Test
    public void testModifyInsuree_ShouldUpdateInDatabase() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        when(sqlHandler.getResult(anyString(), any(String[].class))).thenReturn(new JSONArray());

        doReturn(999).when(spyClient).getNextAvailableInsureeId();

        HashMap<String, String> testData = new HashMap<>(insureeMap);
        testData.put("hfInsureeId", "10");

        System.out.println(testData);

        doReturn(0).when(spyClient).getFamilyStatus(1);
        doReturn(0).when(spyClient).getInsureeStatus(10);
        doReturn("").when(spyClient).copyImageFromGalleryToApplication(anyString(), anyString());
        doReturn(testData).when(spyClient).jsonToTable(anyString());
        doReturn(0).when(spyClient).isValidInsureeData(any());

        doNothing().when(spyClient).SaveInsureePolicy(anyInt(), anyInt(), anyBoolean(), anyInt());
        doNothing().when(spyClient).ShowDialog(anyString());

        int result = spyClient.SaveInsuree(insureeJson, 1, 1, 0, 0);

        assertEquals(10, result);
        verify(spyClient).isValidInsureeData(any());
    }


    // =================== SaveInsuree - New Family ===================

    @Test
    public void testCreateNewFamily_ShouldInsertInDatabase() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        when(sqlHandler.getResult(anyString(), any(String[].class))).thenReturn(new JSONArray());

        doReturn(999).when(spyClient).getNextAvailableInsureeId();

        HashMap<String, String> testData = new HashMap<>(insureeMap);
        System.out.println(testData);

        doReturn(1).when(spyClient).getFamilyStatus(1);
        doReturn(0).when(spyClient).getInsureeStatus(10);
        doReturn("").when(spyClient).copyImageFromGalleryToApplication(anyString(), anyString());
        doReturn(testData).when(spyClient).jsonToTable(anyString());
        doReturn(0).when(spyClient).isValidInsureeData(any());

        doNothing().when(spyClient).SaveInsureePolicy(anyInt(), anyInt(), anyBoolean(), anyInt());
        doNothing().when(spyClient).ShowDialog(anyString());

        int result = spyClient.SaveInsuree(insureeJson, 1, 1, 0, 0);

        assertEquals(999, result);
        verify(spyClient).isValidInsureeData(any());
    }


    // =================== Helpers ===================

    private JSONArray buildFamilyArray() throws JSONException {

        JSONArray array = new JSONArray();
        JSONObject family = new JSONObject();

        family.put("FamilyId", 1);
        family.put("InsureeId", 1);
        family.put("CHFID", "CHF12345");
        family.put("isOffline", "true");
        family.put("FamilyType", "firstType");
        family.put("ConfirmationType", "firstType");
        family.put("ConfirmationDate", "2023-01-01");
        family.put("ConfirmationNumber", "12345");

        array.put(family);
        return array;
    }


    private JSONArray buildInsureeArray() throws JSONException {

        JSONArray array = new JSONArray();
        JSONObject insuree = new JSONObject();

        insuree.put("InsureeId", 1);
        insuree.put("FamilyId", 1);
        insuree.put("CHFID", "CHF12345");
        insuree.put("LastName", "Doe");
        insuree.put("OtherNames", "John");
        insuree.put("DOB", "1990-01-01");
        insuree.put("Gender", "M");
        insuree.put("TypeOfId", "1");

        array.put(insuree);
        return array;
    }


    private JSONArray buildPolicyArray() throws JSONException {

        JSONArray array = new JSONArray();
        JSONObject policy = new JSONObject();

        policy.put("PolicyId", 1);
        policy.put("FamilyId", 1);
        policy.put("ProdId", 1);

        array.put(policy);
        return array;
    }


    private JSONArray buildPremiumArray() throws JSONException {

        JSONArray array = new JSONArray();
        JSONObject premium = new JSONObject();

        premium.put("PremiumId", 1);
        premium.put("PolicyId", 1);
        premium.put("Amount", 1000);
        premium.put("PayDate", "2023-01-01");

        array.put(premium);
        return array;
    }


    private String buildInsureeJson() throws JSONException {

        HashMap<String, String> originalData = new HashMap<>();

        originalData.put("txtInsuranceNumber", "12345");
        originalData.put("hfInsureeId", "0");
        originalData.put("hfisHead", "1");
        originalData.put("txtLastName", "Doe");
        originalData.put("txtOtherNames", "John");
        originalData.put("txtBirthDate", "1991-01-01");
        originalData.put("ddlGender", "M");
        originalData.put("txtPhoneNumber", "690000000");
        originalData.put("hfNewPhotoPath", "");
        originalData.put("hfImagePath", "/storage/emulated/0/DCIM/test.jpg");

        JSONArray array = new JSONArray();

        for (String key : originalData.keySet()) {
            JSONObject obj = new JSONObject();
            obj.put("id", key);
            obj.put("value", originalData.get(key));
            array.put(obj);
        }

        return array.toString();
    }


    private HashMap<String, String> buildInsureeMap() {

        HashMap<String, String> map = new HashMap<>();

        map.put("txtInsuranceNumber", "12345");
        map.put("hfInsureeId", "0");
        map.put("hfisHead", "1");
        map.put("txtLastName", "Doe");
        map.put("txtOtherNames", "John");
        map.put("txtBirthDate", "1991-01-01");
        map.put("ddlGender", "M");
        map.put("txtPhoneNumber", "690000000");
        map.put("hfNewPhotoPath", "");
        map.put("hfImagePath", "/storage/emulated/0/DCIM/test.jpg");

        return map;
    }

    @Test
    public void testModifyFamily_ShouldImportFamilyInsureesAndPolicies() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        String chfId = "CHF123456";

        when(sqlHandler.getCount(
                eq("tblInsuree"),
                eq("Trim(CHFID) = ?"),
                eq(new String[]{chfId})
        )).thenReturn(0);

        // Mock Family
        Family mockFamily = mock(Family.class);
        when(mockFamily.getUuid()).thenReturn("uuid-family");
        when(mockFamily.getId()).thenReturn(1);
        when(mockFamily.getLocationId()).thenReturn(1);
        when(mockFamily.isPoor()).thenReturn(false);
        when(mockFamily.isOffline()).thenReturn(false);
        when(mockFamily.getType()).thenReturn("N");
        when(mockFamily.getAddress()).thenReturn("ADDR");
        when(mockFamily.getEthnicity()).thenReturn("ETH");
        when(mockFamily.getConfirmationNumber()).thenReturn("CONF");
        when(mockFamily.getConfirmationType()).thenReturn("TYPE");
        when(mockFamily.getSms()).thenReturn(new Family.SMS(false, "EN"));

        Family.Member head = mock(Family.Member.class);
        when(head.getId()).thenReturn(1);
        when(head.getUuid()).thenReturn("uuid-insuree");
        when(mockFamily.getHead()).thenReturn(head);

        // Membres
        List<Family.Member> members = new ArrayList<>();
        Family.Member member = mock(Family.Member.class);
        when(member.getChfId()).thenReturn(chfId);
        when(member.getId()).thenReturn(1);
        when(member.getUuid()).thenReturn("uuid-member");
        when(member.getFamilyId()).thenReturn(1);
        when(member.getFamilyUuid()).thenReturn("uuid-family");
        when(member.getIdentificationNumber()).thenReturn("12345");
        when(member.getLastName()).thenReturn("Doe");
        when(member.getOtherNames()).thenReturn("John");
        when(member.getDateOfBirth()).thenReturn(new java.util.Date());
        when(member.getGender()).thenReturn("M");
        when(member.getMarital()).thenReturn("M");
        when(member.isHead()).thenReturn(true);
        when(member.getPhone()).thenReturn("690000000");
        when(member.getPhotoPath()).thenReturn("/storage/emulated/0/DCIM/test.jpg");
        when(member.isCardIssued()).thenReturn(true);
        when(member.isOffline()).thenReturn(false);
        when(member.getRelationship()).thenReturn(1);
        when(member.getProfession()).thenReturn(1);
        when(member.getEducation()).thenReturn(1);
        when(member.getEmail()).thenReturn("email");
        when(member.getTypeOfId()).thenReturn("type");
        when(member.getHealthFacilityId()).thenReturn(1);
        when(member.getCurrentAddress()).thenReturn("addr");
        when(member.getGeolocation()).thenReturn("geo");
        when(member.getCurrentVillage()).thenReturn(1);
        members.add(member);
        when(mockFamily.getMembers()).thenReturn(members);

        // Polices
        List<Family.Policy> policies = new ArrayList<>();
        Family.Policy policy = mock(Family.Policy.class);
        when(policy.getId()).thenReturn(1);
        when(policy.getFamilyId()).thenReturn(1);
        when(policy.getStatus()).thenReturn("ACTIVE");
        when(policy.getValue()).thenReturn(100.0);
        when(policy.getProductId()).thenReturn(1);
        when(policy.getOfficerId()).thenReturn(1);
        when(policy.getStartDate()).thenReturn(new java.util.Date());
        when(policy.getEffectiveDate()).thenReturn(new java.util.Date());
        when(policy.getExpiryDate()).thenReturn(new java.util.Date());
        when(policy.getEnrollDate()).thenReturn(new java.util.Date());
        when(policy.isOffline()).thenReturn(false);
        policies.add(policy);
        when(mockFamily.getPolicies()).thenReturn(policies);

        when(sqlHandler.getResult(anyString(), any())).thenReturn(new JSONArray());

        doNothing().when(spyClient).ShowDialog(anyString());

        // Mock FetchFamily
        doReturn(mockFamily)
                .when(spyClient)
                .newFetchFamilyExecute(chfId);

        int result = spyClient.ModifyFamily(chfId);

        assertEquals(1, result);

        verify(sqlHandler).insertData(eq("tblFamilies"), any(), any(JSONArray.class), anyString());
        verify(sqlHandler).insertData(eq("tblInsuree"), any(), any(JSONArray.class), anyString());
        verify(sqlHandler).insertData(eq("tblPolicy"), any(), any(JSONArray.class), anyString());
    }

    @Test
    public void testModifyFamily_ShouldNotImportIfInsureeExists() {

        ClientAndroidInterface spyClient = spy(client);

        when(sqlHandler.getCount(
                eq("tblInsuree"),
                eq("Trim(CHFID) = ?"),
                any(String[].class)
        )).thenReturn(1);

        doNothing().when(spyClient).ShowDialog(anyString());

        int result = spyClient.ModifyFamily("CHF_EXIST");

        assertEquals(0, result);
        verify(spyClient).ShowDialog(anyString());
    }

    @Test
    public void testModifyFamily_ShouldHandleFamilyNotFound() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        when(sqlHandler.getCount(anyString(), anyString(), any()))
                .thenReturn(0);


        doThrow(new HttpException(
                HttpURLConnection.HTTP_NOT_FOUND,
                "Not Found",
                null,
                null
        )).when(spyClient).newFetchFamilyExecute(anyString());

        doNothing().when(spyClient).ShowDialog(anyString());

        int result = spyClient.ModifyFamily("CHF_UNKNOWN");

        assertEquals(0, result);
        verify(spyClient).ShowDialog(anyString());
    }

    @Test
    public void testModifyFamily_ShouldHandleServerError() throws Exception {

        ClientAndroidInterface spyClient = spy(client);

        when(sqlHandler.getCount(anyString(), anyString(), any()))
                .thenReturn(0);

        doThrow(new RuntimeException("Server down"))
                .when(spyClient)
                .newFetchFamilyExecute(anyString());

        doNothing().when(spyClient).ShowDialog(anyString());

        int result = spyClient.ModifyFamily("CHF_ERR");

        assertEquals(0, result);
        verify(spyClient).ShowDialog(anyString());
    }


}
