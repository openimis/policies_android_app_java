package org.openimis.imispolicies.domain;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class PolicyRenewal implements Parcelable {

    private final int id;
    @NonNull
    private final String uuid;
    private final int policyId;
    private final int officerId;
    @NonNull
    private final String officerCode;
    @NonNull
    private final String chfId;
    @NonNull
    private final String lastName;
    @NonNull
    private final String otherNames;
    @NonNull
    private final String productCode;
    @Nullable
    private final String productName;
    @Nullable
    private final String villageName;
    @NonNull
    private final Date renewalPromptDate;
    @Nullable
    private final String phone;

    public PolicyRenewal(
            int id,
            @NonNull String uuid,
            int policyId,
            int officerId,
            @NonNull String officerCode,
            @NonNull String chfId,
            @NonNull String lastName,
            @NonNull String otherNames,
            @NonNull String productCode,
            @Nullable String productName,
            @Nullable String villageName,
            @NonNull Date renewalPromptDate,
            @Nullable String phone
    ) {
        this.id = id;
        this.uuid = uuid;
        this.policyId = policyId;
        this.officerId = officerId;
        this.officerCode = officerCode;
        this.chfId = chfId;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.productCode = productCode;
        this.productName = productName;
        this.villageName = villageName;
        this.renewalPromptDate = renewalPromptDate;
        this.phone = phone;
    }

    protected PolicyRenewal(Parcel in) {
        id = in.readInt();
        uuid = in.readString();
        policyId = in.readInt();
        officerId = in.readInt();
        officerCode = in.readString();
        chfId = in.readString();
        lastName = in.readString();
        otherNames = in.readString();
        productCode = in.readString();
        productName = in.readString();
        villageName = in.readString();
        renewalPromptDate = new Date(in.readLong());
        phone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(uuid);
        dest.writeInt(policyId);
        dest.writeInt(officerId);
        dest.writeString(officerCode);
        dest.writeString(chfId);
        dest.writeString(lastName);
        dest.writeString(otherNames);
        dest.writeString(productCode);
        dest.writeString(productName);
        dest.writeString(villageName);
        dest.writeLong(renewalPromptDate.getTime());
        dest.writeString(phone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    public int getPolicyId() {
        return policyId;
    }

    public int getOfficerId() {
        return officerId;
    }

    @NonNull
    public String getOfficerCode() {
        return officerCode;
    }

    @NonNull
    public String getChfId() {
        return chfId;
    }

    @NonNull
    public String getLastName() {
        return lastName;
    }

    @NonNull
    public String getOtherNames() {
        return otherNames;
    }

    @NonNull
    public String getProductCode() {
        return productCode;
    }

    @Nullable
    public String getProductName() {
        return productName;
    }

    @Nullable
    public String getVillageName() {
        return villageName;
    }

    @NonNull
    public Date getRenewalPromptDate() {
        return renewalPromptDate;
    }

    @Nullable
    public String getPhone() {
        return phone;
    }

    public static final Creator<PolicyRenewal> CREATOR = new Creator<>() {
        @Override
        public PolicyRenewal createFromParcel(Parcel in) {
            return new PolicyRenewal(in);
        }

        @Override
        public PolicyRenewal[] newArray(int size) {
            return new PolicyRenewal[size];
        }
    };
}
