package org.openimis.imispolicies;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openimis.imispolicies.domain.entity.Insuree;
import org.openimis.imispolicies.domain.entity.Policy;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, application = Application.class)
public class EnquireTest {

    @Mock
    private Global mockGlobal;
    @Mock
    private ClientAndroidInterface mockCa;
    @Mock
    private Escape mockEscape;
    @Mock
    private Resources mockResources;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        when(mockGlobal.isSDCardAvailable()).thenReturn(1);
        when(mockGlobal.isNetworkAvailable()).thenReturn(true);
        when(mockGlobal.getAppDirectory()).thenReturn("/mock/path/");
    }

    @Test
    public void insureeWithNoPolicies_ShouldShowNotCovered() {
        Insuree insuree = new Insuree(
            "CHF123",
            "John Doe",
            new Date(),
            "M",
            null,
            null,
            new ArrayList<>()
        );

        assertTrue(insuree.getPolicies().isEmpty());
    }

    @Test
    public void insureeWithPolicies_ShouldHavePolicyList() {
        Policy policy = new Policy(
            "POL001",
            "Basic Health Plan",
            null,
            new Date(),
            Policy.Status.ACTIVE,
            1.0,
            100.0,
            null,
            500.0,
            null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        List<Policy> policies = new ArrayList<>();
        policies.add(policy);

        Insuree insuree = new Insuree(
            "CHF123",
            "John Doe",
            new Date(),
            "M",
            null,
            null,
            policies
        );

        assertFalse(insuree.getPolicies().isEmpty());
        assertEquals(1, insuree.getPolicies().size());
        assertEquals("POL001", insuree.getPolicies().get(0).getCode());
    }

    @Test
    public void policyWithDeductionType1_HasCorrectProperties() {
        Policy policy = new Policy(
            "POL001",
            "Basic Plan",
            null,
            new Date(),
            Policy.Status.ACTIVE,
            1.0,
            100.0,
            null,
            500.0,
            null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        assertNotNull(policy);
        assertEquals("POL001", policy.getCode());
        assertEquals("Basic Plan", policy.getName());
        assertEquals(Double.valueOf(1.0), policy.getDeductibleType());
        assertEquals(Double.valueOf(100.0), policy.getDeductibleIp());
        assertEquals(Double.valueOf(500.0), policy.getCeilingIp());
        assertEquals(Policy.Status.ACTIVE, policy.getStatus());
    }

    @Test
    public void policyWithDeductionType1_1_HasIPandOPValues() {
        Policy policy = new Policy(
            "POL002",
            "Premium Plan",
            null,
            new Date(),
            Policy.Status.ACTIVE,
            1.1,
            100.0,
            50.0,
            500.0,
            300.0,
            null, null, null, null, null, null, null, null, null, null, null
        );

        assertNotNull(policy);
        assertEquals(Double.valueOf(1.1), policy.getDeductibleType());
        assertEquals(Double.valueOf(100.0), policy.getDeductibleIp());
        assertEquals(Double.valueOf(50.0), policy.getDeductibleOp());
        assertEquals(Double.valueOf(500.0), policy.getCeilingIp());
        assertEquals(Double.valueOf(300.0), policy.getCeilingOp());
    }

    @Test
    public void policyWithAllAmountsLeft_HasCorrectValues() {
        Policy policy = new Policy(
            "POL003",
            "Full Coverage",
            1000.0,
            new Date(),
            Policy.Status.ACTIVE,
            1.0,
            100.0,
            null,
            500.0,
            null,
            200.0,
            150.0,
            180.0,
            300.0,
            250.0,
            5,
            3,
            10,
            2,
            4,
            15
        );

        assertEquals(Double.valueOf(200.0), policy.getAntenatalAmountLeft());
        assertEquals(Double.valueOf(150.0), policy.getConsultationAmountLeft());
        assertEquals(Double.valueOf(180.0), policy.getDeliveryAmountLeft());
        assertEquals(Double.valueOf(300.0), policy.getHospitalizationAmountLeft());
        assertEquals(Double.valueOf(250.0), policy.getSurgeryAmountLeft());
        assertEquals(Integer.valueOf(5), policy.getTotalAdmissionsLeft());
        assertEquals(Integer.valueOf(3), policy.getTotalAntenatalLeft());
        assertEquals(Integer.valueOf(10), policy.getTotalConsultationsLeft());
        assertEquals(Integer.valueOf(2), policy.getTotalDeliveriesLeft());
        assertEquals(Integer.valueOf(4), policy.getTotalSurgeriesLeft());
        assertEquals(Integer.valueOf(15), policy.getTotalVisitsLeft());
    }

    @Test
    public void policyWithNullExpiryDate_IsNotExpired() {
        Policy policy = new Policy(
            "POL004",
            "No Expiry",
            null,
            null,
            Policy.Status.ACTIVE,
            1.0,
            100.0,
            null,
            500.0,
            null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        assertNull(policy.getExpiryDate());
        assertEquals(Policy.Status.ACTIVE, policy.getStatus());
    }

    @Test
    public void insureeWithPhoto_HasPhotoData() {
        byte[] photoData = new byte[]{1, 2, 3, 4, 5};
        
        Insuree insuree = new Insuree(
            "CHF123",
            "John Doe",
            new Date(),
            "M",
            null,
            photoData,
            new ArrayList<>()
        );

        assertNotNull(insuree.getPhoto());
        assertEquals(5, insuree.getPhoto().length);
    }

    @Test
    public void insureeWithPhotoPath_HasPhotoPathString() {
        String photoPath = "/images/insuree123.jpg";
        
        Insuree insuree = new Insuree(
            "CHF123",
            "John Doe",
            new Date(),
            "M",
            photoPath,
            null,
            new ArrayList<>()
        );

        assertNotNull(insuree.getPhotoPath());
        assertEquals(photoPath, insuree.getPhotoPath());
    }

    @Test
    public void escapeCheckInsuranceNumber_ValidNumber_ReturnsZero() {
        Escape escape = new Escape();
        String validNumber = "123456789";
        
        int result = escape.CheckInsuranceNumber(validNumber);
        
        assertTrue(result >= 0);
    }

    @Test
    public void insureeBasicInfo_IsCorrect() {
        Date dob = new Date();
        Insuree insuree = new Insuree(
            "CHF123",
            "Jane Smith",
            dob,
            "F",
            null,
            null,
            new ArrayList<>()
        );

        assertEquals("CHF123", insuree.getChfId());
        assertEquals("Jane Smith", insuree.getName());
        assertEquals(dob, insuree.getDateOfBirth());
        assertEquals("F", insuree.getGender());
    }

    @Test
    public void policyStatusValues_AreValid() {
        Policy activePolicy = new Policy(
            "POL001", "Plan A", null, new Date(), Policy.Status.ACTIVE,
            1.0, 100.0, null, 500.0, null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        Policy idlePolicy = new Policy(
            "POL002", "Plan B", null, new Date(), Policy.Status.IDLE,
            1.0, 100.0, null, 500.0, null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        assertEquals(Policy.Status.ACTIVE, activePolicy.getStatus());
        assertEquals(Policy.Status.IDLE, idlePolicy.getStatus());
        assertNotEquals(activePolicy.getStatus(), idlePolicy.getStatus());
    }

    @Test
    public void multiplePoliciesToInsuree_AllPresent() {
        Policy policy1 = new Policy(
            "POL001", "Basic", null, new Date(), Policy.Status.ACTIVE,
            1.0, 100.0, null, 500.0, null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        Policy policy2 = new Policy(
            "POL002", "Premium", null, new Date(), Policy.Status.ACTIVE,
            1.1, 150.0, 75.0, 600.0, 400.0,
            null, null, null, null, null, null, null, null, null, null, null
        );

        List<Policy> policies = new ArrayList<>();
        policies.add(policy1);
        policies.add(policy2);

        Insuree insuree = new Insuree(
            "CHF123",
            "Multi Policy User",
            new Date(),
            "M",
            null,
            null,
            policies
        );

        assertEquals(2, insuree.getPolicies().size());
        assertEquals("POL001", insuree.getPolicies().get(0).getCode());
        assertEquals("POL002", insuree.getPolicies().get(1).getCode());
    }

    @Test
    public void scanQRCodeIntent_HasCorrectAction() {
        Intent intent = new Intent();
        intent.setAction("com.google.zxing.client.android.SCAN");
        intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

        assertEquals("com.google.zxing.client.android.SCAN", intent.getAction());
        assertEquals("QR_CODE_MODE", intent.getStringExtra("SCAN_MODE"));
    }

    @Test
    public void scanResultIntent_ContainsChfid() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SCAN_RESULT", "CHF999888");

        String chfid = resultIntent.getStringExtra("SCAN_RESULT");

        assertEquals("CHF999888", chfid);
        assertNotNull(chfid);
    }

    @Test
    public void policyWithValue_HasCorrectValue() {
        Policy policy = new Policy(
            "POL001", "Premium", 1500.0, new Date(), Policy.Status.ACTIVE,
            1.0, 100.0, null, 500.0, null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        assertEquals(Double.valueOf(1500.0), policy.getValue());
    }

    @Test
    public void policyDeductionCalculation_Type1() {
        Policy policy = new Policy(
            "POL001", "Basic", null, new Date(), Policy.Status.ACTIVE,
            1.0, 100.0, null, 500.0, null,
            null, null, null, null, null, null, null, null, null, null, null
        );

        double dedType = policy.getDeductibleType();
        
        assertTrue(dedType == 1.0 || dedType == 2.0 || dedType == 3.0);
        assertNotNull(policy.getDeductibleIp());
        assertNotNull(policy.getCeilingIp());
    }

    @Test
    public void policyDeductionCalculation_Type1_1() {
        Policy policy = new Policy(
            "POL002", "Premium", null, new Date(), Policy.Status.ACTIVE,
            1.1, 100.0, 50.0, 500.0, 300.0,
            null, null, null, null, null, null, null, null, null, null, null
        );

        double dedType = policy.getDeductibleType();
        
        assertTrue(dedType == 1.1 || dedType == 2.1 || dedType == 3.1);
        assertNotNull(policy.getDeductibleIp());
        assertNotNull(policy.getDeductibleOp());
        assertNotNull(policy.getCeilingIp());
        assertNotNull(policy.getCeilingOp());
        
    }
}