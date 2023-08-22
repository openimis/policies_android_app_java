package org.openimis.imispolicies.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

public class PendingFeedback implements Parcelable {

    @NonNull
    private final String claimUUID;
    @NonNull
    private final String chfId;
    private final boolean careRendered;
    private final boolean paymentAsked;
    private final boolean drugPrescribed;
    private final boolean drugReceived;
    private final int assessment;

    public PendingFeedback(
            @NonNull String claimUUID,
            @NonNull String chfId,
            boolean careRendered,
            boolean paymentAsked,
            boolean drugPrescribed,
            boolean drugReceived,
            @IntRange(from = 0, to = 5) int assessment
    ) {
        this.claimUUID = claimUUID;
        this.chfId = chfId;
        this.careRendered = careRendered;
        this.paymentAsked = paymentAsked;
        this.drugPrescribed = drugPrescribed;
        this.drugReceived = drugReceived;
        this.assessment = assessment;
    }

    protected PendingFeedback(Parcel in) {
        claimUUID = in.readString();
        chfId = in.readString();
        careRendered = in.readByte() != 0;
        paymentAsked = in.readByte() != 0;
        drugPrescribed = in.readByte() != 0;
        drugReceived = in.readByte() != 0;
        assessment = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(claimUUID);
        dest.writeString(chfId);
        dest.writeByte((byte) (careRendered ? 1 : 0));
        dest.writeByte((byte) (paymentAsked ? 1 : 0));
        dest.writeByte((byte) (drugPrescribed ? 1 : 0));
        dest.writeByte((byte) (drugReceived ? 1 : 0));
        dest.writeInt(assessment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getClaimUUID() {
        return claimUUID;
    }

    @NonNull
    public String getChfId() {
        return chfId;
    }

    public boolean wasCareRendered() {
        return careRendered;
    }

    public boolean wasPaymentAsked() {
        return paymentAsked;
    }

    public boolean wasDrugPrescribed() {
        return drugPrescribed;
    }

    public boolean wasDrugReceived() {
        return drugReceived;
    }

    @IntRange(from = 0, to = 5)
    public int getAssessment() {
        return assessment;
    }

    public static final Creator<PendingFeedback> CREATOR = new Creator<>() {
        @Override
        public PendingFeedback createFromParcel(Parcel in) {
            return new PendingFeedback(in);
        }

        @Override
        public PendingFeedback[] newArray(int size) {
            return new PendingFeedback[size];
        }
    };

}
