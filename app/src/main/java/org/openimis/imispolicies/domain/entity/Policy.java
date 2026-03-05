package org.openimis.imispolicies.domain.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Objects;

public class Policy implements Parcelable {

    @NonNull
    private final String code;
    @NonNull
    private final String name;
    @Nullable
    private final Double value;
    @Nullable
    private final Date expiryDate;
    @NonNull
    private final Status status;
    @Nullable
    private final Double deductibleType;
    @Nullable
    private final Double deductibleIp;
    @Nullable
    private final Double deductibleOp;
    @Nullable
    private final Double ceilingIp;
    @Nullable
    private final Double ceilingOp;
    @Nullable
    private final Double antenatalAmountLeft;
    @Nullable
    private final Double consultationAmountLeft;
    @Nullable
    private final Double deliveryAmountLeft;
    @Nullable
    private final Double hospitalizationAmountLeft;
    @Nullable
    private final Double surgeryAmountLeft;
    @Nullable
    private final Integer totalAdmissionsLeft;
    @Nullable
    private final Integer totalAntenatalLeft;
    @Nullable
    private final Integer totalConsultationsLeft;
    @Nullable
    private final Integer totalDeliveriesLeft;
    @Nullable
    private final Integer totalSurgeriesLeft;
    @Nullable
    private final Integer totalVisitsLeft;

    public Policy(
            @NonNull String code,
            @NonNull String name,
            @Nullable Double value,
            @Nullable Date expiryDate,
            @NonNull Status status,
            @Nullable Double deductibleType,
            @Nullable Double deductibleIp,
            @Nullable Double deductibleOp,
            @Nullable Double ceilingIp,
            @Nullable Double ceilingOp,
            @Nullable Double antenatalAmountLeft,
            @Nullable Double consultationAmountLeft,
            @Nullable Double deliveryAmountLeft,
            @Nullable Double hospitalizationAmountLeft,
            @Nullable Double surgeryAmountLeft,
            @Nullable Integer totalAdmissionsLeft,
            @Nullable Integer totalAntenatalLeft,
            @Nullable Integer totalConsultationsLeft,
            @Nullable Integer totalDeliveriesLeft,
            @Nullable Integer totalSurgeriesLeft,
            @Nullable Integer totalVisitsLeft
    ) {
        this.code = code;
        this.name = name;
        this.value = value;
        this.expiryDate = expiryDate;
        this.status = status;
        this.deductibleType = deductibleType;
        this.deductibleIp = deductibleIp;
        this.deductibleOp = deductibleOp;
        this.ceilingIp = ceilingIp;
        this.ceilingOp = ceilingOp;
        this.antenatalAmountLeft = antenatalAmountLeft;
        this.consultationAmountLeft = consultationAmountLeft;
        this.deliveryAmountLeft = deliveryAmountLeft;
        this.hospitalizationAmountLeft = hospitalizationAmountLeft;
        this.surgeryAmountLeft = surgeryAmountLeft;
        this.totalAdmissionsLeft = totalAdmissionsLeft;
        this.totalAntenatalLeft = totalAntenatalLeft;
        this.totalConsultationsLeft = totalConsultationsLeft;
        this.totalDeliveriesLeft = totalDeliveriesLeft;
        this.totalSurgeriesLeft = totalSurgeriesLeft;
        this.totalVisitsLeft = totalVisitsLeft;
    }

    protected Policy(Parcel in) {
        code = Objects.requireNonNull(in.readString());
        name = Objects.requireNonNull(in.readString());
        if (in.readByte() == 0) {
            value = null;
        } else {
            value = in.readDouble();
        }
        if (in.readByte() == 0) {
            expiryDate = null;
        } else {
            expiryDate = new Date(in.readLong());
        }
        status = Status.valueOf(in.readString());
        if (in.readByte() == 0) {
            deductibleType = null;
        } else {
            deductibleType = in.readDouble();
        }
        if (in.readByte() == 0) {
            deductibleIp = null;
        } else {
            deductibleIp = in.readDouble();
        }
        if (in.readByte() == 0) {
            deductibleOp = null;
        } else {
            deductibleOp = in.readDouble();
        }
        if (in.readByte() == 0) {
            ceilingIp = null;
        } else {
            ceilingIp = in.readDouble();
        }
        if (in.readByte() == 0) {
            ceilingOp = null;
        } else {
            ceilingOp = in.readDouble();
        }
        if (in.readByte() == 0) {
            antenatalAmountLeft = null;
        } else {
            antenatalAmountLeft = in.readDouble();
        }
        if (in.readByte() == 0) {
            consultationAmountLeft = null;
        } else {
            consultationAmountLeft = in.readDouble();
        }
        if (in.readByte() == 0) {
            deliveryAmountLeft = null;
        } else {
            deliveryAmountLeft = in.readDouble();
        }
        if (in.readByte() == 0) {
            hospitalizationAmountLeft = null;
        } else {
            hospitalizationAmountLeft = in.readDouble();
        }
        if (in.readByte() == 0) {
            surgeryAmountLeft = null;
        } else {
            surgeryAmountLeft = in.readDouble();
        }
        if (in.readByte() == 0) {
            totalAdmissionsLeft = null;
        } else {
            totalAdmissionsLeft = in.readInt();
        }
        if (in.readByte() == 0) {
            totalAntenatalLeft = null;
        } else {
            totalAntenatalLeft = in.readInt();
        }
        if (in.readByte() == 0) {
            totalConsultationsLeft = null;
        } else {
            totalConsultationsLeft = in.readInt();
        }
        if (in.readByte() == 0) {
            totalDeliveriesLeft = null;
        } else {
            totalDeliveriesLeft = in.readInt();
        }
        if (in.readByte() == 0) {
            totalSurgeriesLeft = null;
        } else {
            totalSurgeriesLeft = in.readInt();
        }
        if (in.readByte() == 0) {
            totalVisitsLeft = null;
        } else {
            totalVisitsLeft = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(name);
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(value);
        }
        if (expiryDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(expiryDate.getTime());
        }
        dest.writeString(status.name());
        if (deductibleType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(deductibleType);
        }
        if (deductibleIp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(deductibleIp);
        }
        if (deductibleOp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(deductibleOp);
        }
        if (ceilingIp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(ceilingIp);
        }
        if (ceilingOp == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(ceilingOp);
        }
        if (antenatalAmountLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(antenatalAmountLeft);
        }
        if (consultationAmountLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(consultationAmountLeft);
        }
        if (deliveryAmountLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(deliveryAmountLeft);
        }
        if (hospitalizationAmountLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(hospitalizationAmountLeft);
        }
        if (surgeryAmountLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(surgeryAmountLeft);
        }
        if (totalAdmissionsLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalAdmissionsLeft);
        }
        if (totalAntenatalLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalAntenatalLeft);
        }
        if (totalConsultationsLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalConsultationsLeft);
        }
        if (totalDeliveriesLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalDeliveriesLeft);
        }
        if (totalSurgeriesLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalSurgeriesLeft);
        }
        if (totalVisitsLeft == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(totalVisitsLeft);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public String getCode() {
        return code;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public Double getValue() {
        return value;
    }

    @Nullable
    public Date getExpiryDate() {
        return expiryDate;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public Double getDeductibleType() {
        return deductibleType;
    }

    @Nullable
    public Double getDeductibleIp() {
        return deductibleIp;
    }

    @Nullable
    public Double getDeductibleOp() {
        return deductibleOp;
    }

    @Nullable
    public Double getCeilingIp() {
        return ceilingIp;
    }

    @Nullable
    public Double getCeilingOp() {
        return ceilingOp;
    }

    @Nullable
    public Double getAntenatalAmountLeft() {
        return antenatalAmountLeft;
    }

    @Nullable
    public Double getConsultationAmountLeft() {
        return consultationAmountLeft;
    }

    @Nullable
    public Double getDeliveryAmountLeft() {
        return deliveryAmountLeft;
    }

    @Nullable
    public Double getHospitalizationAmountLeft() {
        return hospitalizationAmountLeft;
    }

    @Nullable
    public Double getSurgeryAmountLeft() {
        return surgeryAmountLeft;
    }

    @Nullable
    public Integer getTotalAdmissionsLeft() {
        return totalAdmissionsLeft;
    }

    @Nullable
    public Integer getTotalAntenatalLeft() {
        return totalAntenatalLeft;
    }

    @Nullable
    public Integer getTotalConsultationsLeft() {
        return totalConsultationsLeft;
    }

    @Nullable
    public Integer getTotalDeliveriesLeft() {
        return totalDeliveriesLeft;
    }

    @Nullable
    public Integer getTotalSurgeriesLeft() {
        return totalSurgeriesLeft;
    }

    @Nullable
    public Integer getTotalVisitsLeft() {
        return totalVisitsLeft;
    }

    public enum Status {
        IDLE, ACTIVE, SUSPENDED, EXPIRED, READY
    }

    public static final Creator<Policy> CREATOR = new Creator<>() {
        @Override
        public Policy createFromParcel(Parcel in) {
            return new Policy(in);
        }

        @Override
        public Policy[] newArray(int size) {
            return new Policy[size];
        }
    };
}
