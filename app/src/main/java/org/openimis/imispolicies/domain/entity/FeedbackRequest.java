package org.openimis.imispolicies.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Objects;

@SuppressWarnings("unused")
public class FeedbackRequest implements Parcelable {

    @NonNull
    private final String chfId;
    private final int officeId;
    @NonNull
    private final String officerCode;
    @NonNull
    private final String lastName;
    @NonNull
    private final String otherNames;
    @NonNull
    private final String hfCode;
    @NonNull
    private final String hfName;
    @NonNull
    private final String claimCode;
    @NonNull
    private final Date promptDate;
    @NonNull
    private final Date fromDate;
    @Nullable
    private final Date toDate;
    @NonNull
    private final String claimUUID;
    @Nullable
    private final String phone;

    public FeedbackRequest(
            @NonNull String chfId,
            int officeId,
            @NonNull String officerCode,
            @NonNull String lastName,
            @NonNull String otherNames,
            @NonNull String hfCode,
            @NonNull String hfName,
            @NonNull String claimCode,
            @NonNull String claimUUID,
            @NonNull Date promptDate,
            @NonNull Date fromDate,
            @Nullable Date toDate,
            @Nullable String phone
    ) {
        this.chfId = chfId;
        this.officeId = officeId;
        this.officerCode = officerCode;
        this.lastName = lastName;
        this.otherNames = otherNames;
        this.hfCode = hfCode;
        this.hfName = hfName;
        this.claimCode = claimCode;
        this.claimUUID = claimUUID;
        this.promptDate = promptDate;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.phone = phone;
    }

    protected FeedbackRequest(Parcel in) {
        chfId = Objects.requireNonNull(in.readString());
        officeId = in.readInt();
        officerCode = Objects.requireNonNull(in.readString());
        lastName = Objects.requireNonNull(in.readString());
        otherNames = Objects.requireNonNull(in.readString());
        hfCode = Objects.requireNonNull(in.readString());
        hfName = Objects.requireNonNull(in.readString());
        claimCode = Objects.requireNonNull(in.readString());
        claimUUID = Objects.requireNonNull(in.readString());
        promptDate = new Date(in.readLong());
        fromDate = new Date(in.readLong());
        long toDateLong = in.readLong();
        toDate = toDateLong == -1 ? null : new Date(toDateLong);
        phone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chfId);
        dest.writeInt(officeId);
        dest.writeString(officerCode);
        dest.writeString(lastName);
        dest.writeString(otherNames);
        dest.writeString(hfCode);
        dest.writeString(hfName);
        dest.writeString(claimCode);
        dest.writeString(claimUUID);
        dest.writeLong(promptDate.getTime());
        dest.writeLong(fromDate.getTime());
        dest.writeLong(toDate == null ? -1L : toDate.getTime());
        dest.writeString(phone);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getChfId() {
        return chfId;
    }

    public int getOfficeId() {
        return officeId;
    }

    @NonNull
    public String getOfficerCode() {
        return officerCode;
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
    public String getFullName() {
        return lastName + " " + otherNames;
    }

    @NonNull
    public String getHfCode() {
        return hfCode;
    }

    @NonNull
    public String getHfName() {
        return hfName;
    }

    @NonNull
    public String getClaimCode() {
        return claimCode;
    }

    @NonNull
    public Date getPromptDate() {
        return promptDate;
    }

    @NonNull
    public Date getFromDate() {
        return fromDate;
    }

    @Nullable
    public Date getToDate() {
        return toDate;
    }

    @NonNull
    public String getClaimUUID() {
        return claimUUID;
    }

    @NonNull
    public String getPhone() {
        // This is needed to insert data with SQLHandler
        return Objects.requireNonNullElse(phone, "");
    }

    public static final Creator<FeedbackRequest> CREATOR = new Creator<>() {
        @Override
        public FeedbackRequest createFromParcel(Parcel in) {
            return new FeedbackRequest(in);
        }

        @Override
        public FeedbackRequest[] newArray(int size) {
            return new FeedbackRequest[size];
        }
    };
}
