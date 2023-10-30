package org.openimis.imispolicies.domain.entity;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Family implements Parcelable {

    @NonNull
    private final String headChfId;
    private final int id;
    @NonNull
    private final String uuid;
    @Nullable
    private final SMS sms;
    @Nullable
    private final Integer locationId;
    private final boolean isPoor;
    @Nullable
    private final String type;
    @Nullable
    private final String address;
    @Nullable
    private final String ethnicity;
    @Nullable
    private final String confirmationNumber;
    @Nullable
    private final String confirmationType;
    private final boolean isOffline;
    @NonNull
    private final List<Member> members;

    @Nullable
    private Member head = null;

    public Family(
            @NonNull String headChfId,
            int id,
            @NonNull String uuid,
            @Nullable SMS sms,
            @Nullable Integer locationId,
            boolean isPoor,
            @Nullable String type,
            @Nullable String address,
            @Nullable String ethnicity,
            @Nullable String confirmationNumber,
            @Nullable String confirmationType,
            boolean isOffline,
            @NonNull List<Member> members
    ) {
        this.headChfId = headChfId;
        this.id = id;
        this.uuid = uuid;
        this.sms = sms;
        this.locationId = locationId;
        this.isPoor = isPoor;
        this.type = type;
        this.address = address;
        this.ethnicity = ethnicity;
        this.confirmationNumber = confirmationNumber;
        this.confirmationType = confirmationType;
        this.isOffline = isOffline;
        this.members = members;
    }

    protected Family(Parcel in) {
        headChfId = Objects.requireNonNull(in.readString());
        id = in.readInt();
        uuid = Objects.requireNonNull(in.readString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            sms = in.readParcelable(SMS.class.getClassLoader(), SMS.class);
        } else {
            sms = in.readParcelable(SMS.class.getClassLoader());
        }
        int lId = in.readInt();
        locationId = lId != -1 ? lId : null;
        isPoor = in.readByte() != 0;
        type = in.readString();
        address = in.readString();
        ethnicity = in.readString();
        confirmationNumber = in.readString();
        confirmationType = in.readString();
        isOffline = in.readByte() != 0;
        members = Objects.requireNonNull(in.createTypedArrayList(Member.CREATOR));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(headChfId);
        dest.writeInt(id);
        dest.writeString(uuid);
        dest.writeParcelable(sms, flags);
        dest.writeInt(locationId != null ? locationId : -1);
        dest.writeByte((byte) (isPoor ? 1 : 0));
        dest.writeString(type);
        dest.writeString(address);
        dest.writeString(ethnicity);
        dest.writeString(confirmationNumber);
        dest.writeString(confirmationType);
        dest.writeByte((byte) (isOffline ? 1 : 0));
        dest.writeTypedList(members);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public Member getHead() {
        if (head != null) {
            return head;
        }
        for (Member member : members) {
            if (headChfId.equals(member.getChfId())) {
                head = member;
                return member;
            }
        }
        throw new IllegalStateException("The members list (size: '" + members.size() + "') didn't contain an insuree with the head chfId: '" + headChfId + "'");
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getUuid() {
        return uuid;
    }

    @Nullable
    public SMS getSms() {
        return sms;
    }

    @Nullable
    public Integer getLocationId() {
        return locationId;
    }

    public boolean isPoor() {
        return isPoor;
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getAddress() {
        return address;
    }

    @Nullable
    public String getEthnicity() {
        return ethnicity;
    }

    @Nullable
    public String getConfirmationNumber() {
        return confirmationNumber;
    }

    @Nullable
    public String getConfirmationType() {
        return confirmationType;
    }

    public boolean isOffline() {
        return isOffline;
    }

    @NonNull
    public List<Member> getMembers() {
        return members;
    }

    public static final Creator<Family> CREATOR = new Creator<>() {
        @Override
        public Family createFromParcel(Parcel in) {
            return new Family(in);
        }

        @Override
        public Family[] newArray(int size) {
            return new Family[size];
        }
    };

    public static class SMS implements Parcelable {

        private final boolean approval;
        @NonNull
        private final String language;

        public SMS(
                boolean approval,
                @NonNull String language
        ) {
            this.approval = approval;
            this.language = language;
        }


        protected SMS(Parcel in) {
            approval = in.readByte() != 0;
            language = Objects.requireNonNull(in.readString());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) (approval ? 1 : 0));
            dest.writeString(language);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public boolean isApproval() {
            return approval;
        }

        @NonNull
        public String getLanguage() {
            return language;
        }

        public static final Creator<SMS> CREATOR = new Creator<>() {
            @Override
            public SMS createFromParcel(Parcel in) {
                return new SMS(in);
            }

            @Override
            public SMS[] newArray(int size) {
                return new SMS[size];
            }
        };
    }

    public static class Member implements Parcelable {

        @NonNull
        private final String chfId;
        private final boolean isHead;
        private final int id;
        @Nullable
        private final String uuid;
        private final int familyId;
        @NonNull
        private final String familyUuid;
        @Nullable
        private final String identificationNumber;
        @NonNull
        private final String lastName;
        @NonNull
        private final String otherNames;
        @NonNull
        private final Date dateOfBirth;
        @NonNull
        private final String gender;
        @Nullable
        private final String marital;
        @Nullable
        private final String phone;
        private final boolean cardIssued;
        @Nullable
        private final Integer relationship;
        @Nullable
        private final Integer profession;
        @Nullable
        private final Integer education;
        @Nullable
        private final String email;
        @Nullable
        private final String typeOfId;
        @Nullable
        private final Integer healthFacilityId;
        @Nullable
        private final String currentAddress;
        @Nullable
        private final Integer currentVillage;
        @Nullable
        private final String geolocation;
        @Nullable
        private final String photoPath;
        @Nullable
        private final byte[] photoBytes;
        private final boolean isOffline;

        public Member(
                @NonNull String chfId,
                boolean isHead,
                int id,
                @Nullable String uuid,
                int familyId,
                @NonNull String familyUuid,
                @Nullable String identificationNumber,
                @NonNull String lastName,
                @NonNull String otherNames,
                @NonNull Date dateOfBirth,
                @NonNull String gender,
                @Nullable String marital,
                @Nullable String phone,
                boolean cardIssued,
                @Nullable Integer relationship,
                @Nullable Integer profession,
                @Nullable Integer education,
                @Nullable String email,
                @Nullable String typeOfId,
                @Nullable Integer healthFacilityId,
                @Nullable String currentAddress,
                @Nullable Integer currentVillage,
                @Nullable String geolocation,
                @Nullable String photoPath,
                @Nullable byte[] photoBytes,
                boolean isOffline
        ) {
            this.chfId = chfId;
            this.isHead = isHead;
            this.id = id;
            this.uuid = uuid;
            this.familyId = familyId;
            this.familyUuid = familyUuid;
            this.identificationNumber = identificationNumber;
            this.lastName = lastName;
            this.otherNames = otherNames;
            this.dateOfBirth = dateOfBirth;
            this.gender = gender;
            this.marital = marital;
            this.phone = phone;
            this.cardIssued = cardIssued;
            this.relationship = relationship;
            this.profession = profession;
            this.education = education;
            this.email = email;
            this.typeOfId = typeOfId;
            this.healthFacilityId = healthFacilityId;
            this.currentAddress = currentAddress;
            this.currentVillage = currentVillage;
            this.geolocation = geolocation;
            this.photoPath = photoPath;
            this.photoBytes = photoBytes;
            this.isOffline = isOffline;
        }

        protected Member(Parcel in) {
            chfId = Objects.requireNonNull(in.readString());
            isHead = in.readByte() != 0;
            id = in.readInt();
            uuid = Objects.requireNonNull(in.readString());
            familyId = in.readInt();
            familyUuid = Objects.requireNonNull(in.readString());
            identificationNumber = in.readString();
            lastName = Objects.requireNonNull(in.readString());
            otherNames = Objects.requireNonNull(in.readString());
            dateOfBirth = new Date(in.readLong());
            gender = Objects.requireNonNull(in.readString());
            marital = in.readString();
            phone = in.readString();
            cardIssued = in.readByte() != 0;
            if (in.readByte() == 0) {
                relationship = null;
            } else {
                relationship = in.readInt();
            }
            if (in.readByte() == 0) {
                profession = null;
            } else {
                profession = in.readInt();
            }
            if (in.readByte() == 0) {
                education = null;
            } else {
                education = in.readInt();
            }
            email = in.readString();
            typeOfId = in.readString();
            int hfId = in.readInt();
            healthFacilityId = hfId != -1 ? hfId : null;
            currentAddress = in.readString();
            int villageId = in.readInt();
            currentVillage = villageId != -1 ? villageId : null;
            geolocation = in.readString();
            photoPath = in.readString();
            int size = in.readInt();
            if (size >= 0) {
                photoBytes = new byte[size];
                in.readByteArray(photoBytes);
            } else {
                photoBytes = null;
            }
            isOffline = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(chfId);
            dest.writeByte((byte) (isHead ? 1 : 0));
            dest.writeInt(id);
            dest.writeString(uuid);
            dest.writeInt(familyId);
            dest.writeString(familyUuid);
            dest.writeString(identificationNumber);
            dest.writeString(lastName);
            dest.writeString(otherNames);
            dest.writeLong(dateOfBirth.getTime());
            dest.writeString(gender);
            dest.writeString(marital);
            dest.writeString(phone);
            dest.writeByte((byte) (cardIssued ? 1 : 0));
            if (relationship == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeInt(relationship);
            }
            if (profession == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeInt(profession);
            }
            if (education == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeInt(education);
            }
            dest.writeString(email);
            dest.writeString(typeOfId);
            dest.writeInt(healthFacilityId != null ? healthFacilityId : -1);
            dest.writeString(currentAddress);
            dest.writeInt(currentVillage != null ? currentVillage : -1);
            dest.writeString(geolocation);
            dest.writeString(photoPath);
            if (photoBytes != null) {
                dest.writeInt(photoBytes.length);
                dest.writeByteArray(photoBytes);
            } else {
                dest.writeInt(-1);
            }
            dest.writeByte((byte) (isOffline ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @NonNull
        public String getChfId() {
            return chfId;
        }

        public boolean isHead() {
            return isHead;
        }

        public int getId() {
            return id;
        }

        @Nullable
        public String getUuid() {
            return uuid;
        }

        public int getFamilyId() {
            return familyId;
        }

        @NonNull
        public String getFamilyUuid() {
            return familyUuid;
        }

        @Nullable
        public String getIdentificationNumber() {
            return identificationNumber;
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
        public Date getDateOfBirth() {
            return dateOfBirth;
        }

        @NonNull
        public String getGender() {
            return gender;
        }

        @Nullable
        public String getMarital() {
            return marital;
        }

        @Nullable
        public String getPhone() {
            return phone;
        }

        public boolean isCardIssued() {
            return cardIssued;
        }

        @Nullable
        public Integer getRelationship() {
            return relationship;
        }

        @Nullable
        public Integer getProfession() {
            return profession;
        }

        @Nullable
        public Integer getEducation() {
            return education;
        }

        @Nullable
        public String getEmail() {
            return email;
        }

        @Nullable
        public String getTypeOfId() {
            return typeOfId;
        }

        @Nullable
        public Integer getHealthFacilityId() {
            return healthFacilityId;
        }

        @Nullable
        public String getCurrentAddress() {
            return currentAddress;
        }

        @Nullable
        public Integer getCurrentVillage() {
            return currentVillage;
        }

        @Nullable
        public String getGeolocation() {
            return geolocation;
        }

        @Nullable
        public String getPhotoPath() {
            return photoPath;
        }

        @Nullable
        public byte[] getPhotoBytes() {
            return photoBytes;
        }

        public boolean isOffline() {
            return isOffline;
        }

        public static final Creator<Member> CREATOR = new Creator<>() {
            @Override
            public Member createFromParcel(Parcel in) {
                return new Member(in);
            }

            @Override
            public Member[] newArray(int size) {
                return new Member[size];
            }
        };
    }

    public static class Policy implements Parcelable {
        private final int id;
        @NonNull
        private final String uuid;
        private final int familyId;
        @NonNull
        private final String familyUUID;
        @NonNull
        private final Date enrollDate;
        @NonNull
        private final Date startDate;
        @Nullable
        private final Date effectiveDate;
        @NonNull
        private final Date expiryDate;
        @Nullable
        private final String status;
        @Nullable
        private final Double value;
        @Nullable
        private final Integer productId;
        private final int officerId;
        @Nullable
        private final String policyStage;
        private final boolean isOffline;
        @Nullable
        private final String controlNumber;
        @NonNull
        private final List<Premium> premiums;

        public Policy(
                int id,
                @NonNull String uuid,
                int familyId,
                @NonNull String familyUUID,
                @NonNull Date enrollDate,
                @NonNull Date startDate,
                @Nullable Date effectiveDate,
                @NonNull Date expiryDate,
                @Nullable String status,
                @Nullable Double value,
                @Nullable Integer productId,
                int officerId,
                @Nullable String policyStage,
                boolean isOffline,
                @Nullable String controlNumber,
                @NonNull List<Premium> premiums
        ) {
            this.id = id;
            this.uuid = uuid;
            this.familyId = familyId;
            this.familyUUID = familyUUID;
            this.enrollDate = enrollDate;
            this.startDate = startDate;
            this.effectiveDate = effectiveDate;
            this.expiryDate = expiryDate;
            this.status = status;
            this.value = value;
            this.productId = productId;
            this.officerId = officerId;
            this.policyStage = policyStage;
            this.isOffline = isOffline;
            this.controlNumber = controlNumber;
            this.premiums = premiums;
        }

        protected Policy(Parcel in) {
            id = in.readInt();
            uuid = in.readString();
            familyId = in.readInt();
            familyUUID = in.readString();
            enrollDate = new Date(in.readLong());
            startDate = new Date(in.readLong());
            long effectiveDateLong = in.readLong();
            effectiveDate = effectiveDateLong != -1 ? new Date(effectiveDateLong) : null;
            expiryDate = new Date(in.readLong());
            status = in.readString();
            if (in.readByte() == 0) {
                value = null;
            } else {
                value = in.readDouble();
            }
            if (in.readByte() == 0) {
                productId = null;
            } else {
                productId = in.readInt();
            }
            officerId = in.readInt();
            policyStage = in.readString();
            isOffline = in.readByte() != 0;
            controlNumber = in.readString();
            premiums = in.createTypedArrayList(Premium.CREATOR);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(uuid);
            dest.writeInt(familyId);
            dest.writeString(familyUUID);
            dest.writeLong(enrollDate.getTime());
            dest.writeLong(startDate.getTime());
            dest.writeLong(effectiveDate != null ? effectiveDate.getTime() :-1);
            dest.writeLong(expiryDate.getTime());
            dest.writeString(status);
            if (value == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeDouble(value);
            }
            if (productId == null) {
                dest.writeByte((byte) 0);
            } else {
                dest.writeByte((byte) 1);
                dest.writeInt(productId);
            }
            dest.writeInt(officerId);
            dest.writeString(policyStage);
            dest.writeByte((byte) (isOffline ? 1 : 0));
            dest.writeString(controlNumber);
            dest.writeTypedList(premiums);
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

        public int getFamilyId() {
            return familyId;
        }

        @NonNull
        public String getFamilyUUID() {
            return familyUUID;
        }

        @NonNull
        public Date getEnrollDate() {
            return enrollDate;
        }

        @NonNull
        public Date getStartDate() {
            return startDate;
        }

        @Nullable
        public Date getEffectiveDate() {
            return effectiveDate;
        }

        @NonNull
        public Date getExpiryDate() {
            return expiryDate;
        }

        @Nullable
        public String getStatus() {
            return status;
        }

        @Nullable
        public Double getValue() {
            return value;
        }

        @Nullable
        public Integer getProductId() {
            return productId;
        }

        public int getOfficerId() {
            return officerId;
        }

        @Nullable
        public String getPolicyStage() {
            return policyStage;
        }

        public boolean isOffline() {
            return isOffline;
        }

        @Nullable
        public String getControlNumber() {
            return controlNumber;
        }

        @NonNull
        public List<Premium> getPremiums() {
            return premiums;
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

        public static class Premium implements Parcelable {
            private final int id;
            private final int policyId;
            @NonNull
            private final String policyUuid;
            @Nullable
            private final Integer payerId;
            @Nullable
            private final Double amount;
            @Nullable
            private final String receipt;
            @Nullable
            private final Date payDate;
            @Nullable
            private final String payType;
            private final boolean isPhotoFee;
            private final boolean isOffline;

            public Premium(
                    int id,
                    int policyId,
                    @NonNull String policyUuid,
                    @Nullable Integer payerId,
                    @Nullable Double amount,
                    @Nullable String receipt,
                    @Nullable Date payDate,
                    @Nullable String payType,
                    boolean isPhotoFee,
                    boolean isOffline
            ) {
                this.id = id;
                this.policyId = policyId;
                this.policyUuid = policyUuid;
                this.payerId = payerId;
                this.amount = amount;
                this.receipt = receipt;
                this.payDate = payDate;
                this.payType = payType;
                this.isPhotoFee = isPhotoFee;
                this.isOffline = isOffline;
            }

            protected Premium(Parcel in) {
                id = in.readInt();
                policyId = in.readInt();
                policyUuid = in.readString();
                if (in.readByte() == 0) {
                    payerId = null;
                } else {
                    payerId = in.readInt();
                }
                if (in.readByte() == 0) {
                    amount = null;
                } else {
                    amount = in.readDouble();
                }
                receipt = in.readString();
                long payDateLong = in.readLong();
                payDate = payDateLong != -1 ? new Date(payDateLong) : null;
                payType = in.readString();
                isPhotoFee = in.readByte() != 0;
                isOffline = in.readByte() != 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(id);
                dest.writeInt(policyId);
                dest.writeString(policyUuid);
                if (payerId == null) {
                    dest.writeByte((byte) 0);
                } else {
                    dest.writeByte((byte) 1);
                    dest.writeInt(payerId);
                }
                if (amount == null) {
                    dest.writeByte((byte) 0);
                } else {
                    dest.writeByte((byte) 1);
                    dest.writeDouble(amount);
                }
                dest.writeString(receipt);
                dest.writeLong(payDate != null ? payDate.getTime() : -1);
                dest.writeString(payType);
                dest.writeByte((byte) (isPhotoFee ? 1 : 0));
                dest.writeByte((byte) (isOffline ? 1 : 0));
            }

            @Override
            public int describeContents() {
                return 0;
            }

            public int getId() {
                return id;
            }

            public int getPolicyId() {
                return policyId;
            }

            @NonNull
            public String getPolicyUuid() {
                return policyUuid;
            }

            @Nullable
            public Integer getPayerId() {
                return payerId;
            }

            @Nullable
            public Double getAmount() {
                return amount;
            }

            @Nullable
            public String getReceipt() {
                return receipt;
            }

            @Nullable
            public Date getPayDate() {
                return payDate;
            }

            @Nullable
            public String getPayType() {
                return payType;
            }

            public boolean isPhotoFee() {
                return isPhotoFee;
            }

            public boolean isOffline() {
                return isOffline;
            }

            public static final Creator<Premium> CREATOR = new Creator<>() {
                @Override
                public Premium createFromParcel(Parcel in) {
                    return new Premium(in);
                }

                @Override
                public Premium[] newArray(int size) {
                    return new Premium[size];
                }
            };
        }
    }
}
