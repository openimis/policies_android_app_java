package org.openimis.imispolicies.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

public class PolicyRenewalRequest implements Parcelable {

    private final int renewalId;
    private final int officerId;
    @NonNull
    private final String officerCode;
    @NonNull
    private final String chfId;
    @NonNull
    private final String receiptNumber;
    @NonNull
    private final String productCode;
    private final double amount;
    @NonNull
    private final Date date;
    private final boolean discontinued;
    private final int payerId;
    @NonNull
    private final String payType;

    public PolicyRenewalRequest(
            int renewalId,
            int officerId,
            @NonNull String officerCode,
            @NonNull String chfId,
            @NonNull String receiptNumber,
            @NonNull String productCode,
            double amount,
            @NonNull Date date,
            boolean discontinued,
            int payerId,
            @NonNull String payType
    ) {
        this.renewalId = renewalId;
        this.officerId = officerId;
        this.officerCode = officerCode;
        this.chfId = chfId;
        this.receiptNumber = receiptNumber;
        this.productCode = productCode;
        this.amount = amount;
        this.date = date;
        this.discontinued = discontinued;
        this.payerId = payerId;
        this.payType = payType;
    }

    protected PolicyRenewalRequest(Parcel in) {
        renewalId = in.readInt();
        officerId = in.readInt();
        officerCode = in.readString();
        chfId = in.readString();
        receiptNumber = in.readString();
        productCode = in.readString();
        amount = in.readDouble();
        date = new Date(in.readLong());
        discontinued = in.readByte() != 0;
        payerId = in.readInt();
        payType = in.readString();
    }

    public int getRenewalId() {
        return renewalId;
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
    public String getReceiptNumber() {
        return receiptNumber;
    }

    @NonNull
    public String getProductCode() {
        return productCode;
    }

    public double getAmount() {
        return amount;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public boolean isDiscontinued() {
        return discontinued;
    }

    @Nullable
    public Integer getPayerId() {
        if (payerId == 0) {
            return null;
        }
        return payerId;
    }

    @NonNull
    public String getPayType() {
        return payType;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(renewalId);
        dest.writeInt(officerId);
        dest.writeString(officerCode);
        dest.writeString(chfId);
        dest.writeString(receiptNumber);
        dest.writeString(productCode);
        dest.writeDouble(amount);
        dest.writeLong(date.getTime());
        dest.writeByte((byte) (discontinued ? 1 : 0));
        dest.writeInt(payerId);
        dest.writeString(payType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PolicyRenewalRequest> CREATOR = new Creator<>() {
        @Override
        public PolicyRenewalRequest createFromParcel(Parcel in) {
            return new PolicyRenewalRequest(in);
        }

        @Override
        public PolicyRenewalRequest[] newArray(int size) {
            return new PolicyRenewalRequest[size];
        }
    };
}
