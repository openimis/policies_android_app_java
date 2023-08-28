package org.openimis.imispolicies.usecase;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.openimis.imispolicies.GetFamilyQuery;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.domain.utils.IdUtils;
import org.openimis.imispolicies.domain.utils.PhotoUtils;
import org.openimis.imispolicies.network.request.GetFamilyGraphQLRequest;
import org.openimis.imispolicies.network.request.GetPhotoBytesRequest;
import org.openimis.imispolicies.network.util.Mapper;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.util.StringUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Objects;

public class FetchFamily {

    @NonNull
    private final GetFamilyGraphQLRequest getFamilyGraphQLRequest;

    public FetchFamily() {
        this(new GetFamilyGraphQLRequest());
    }

    public FetchFamily(@NonNull GetFamilyGraphQLRequest getFamilyGraphQLRequest) {
        this.getFamilyGraphQLRequest = getFamilyGraphQLRequest;
    }

    @WorkerThread
    @NonNull
    public Family execute(@NonNull String headChfId) throws Exception {
        GetFamilyQuery.Node node = getFamilyGraphQLRequest.get(headChfId);
        return new Family(
                /* headChfId = */ headChfId,
                /* id = */ -IdUtils.getIdFromGraphQLString(node.id()), // I have no idea why they add this '-'
                /* uuid = */ node.uuid(),
                /* sms = */ null,
                /* locationId = */ node.location() != null ? IdUtils.getIdFromGraphQLString(Objects.requireNonNull(node.location()).id()) : null,
                /* isPoor = */ node.poverty() != null ? Objects.requireNonNull(node.poverty()) : false,
                /* type = */ node.familyType() != null ? Objects.requireNonNull(node.familyType()).code() : null,
                /* address = */ node.address(),
                /* ethnicity = */ node.ethnicity(),
                /* confirmationNumber = */ node.confirmationNo(),
                /* confirmationType = */ node.confirmationType() != null ? Objects.requireNonNull(node.confirmationType()).code() : null,
                /* isOffline = */ node.isOffline() != null ? Objects.requireNonNull(node.isOffline()) : false,
                /* insurees = */ Mapper.map(node.members().edges(), (edge) -> toMember(edge, headChfId, node))
        );
    }

    @NonNull
    private Family.Member toMember(@NonNull GetFamilyQuery.Edge1 edge, @NonNull String headChfId, @NonNull GetFamilyQuery.Node family) {
        GetFamilyQuery.Node1 member = Objects.requireNonNull(edge.node());
        return new Family.Member(
                /* chfId = */ Objects.requireNonNull(member.chfId()),
                /* isHead = */ headChfId.equals(member.chfId()),
                /* id = */ -IdUtils.getIdFromGraphQLString(member.id()), // I have no idea why they add this '-'
                /* uuid = */ member.uuid(),
                /* familyId = */ -IdUtils.getIdFromGraphQLString(family.id()),
                /* familyUuid = */ family.uuid(),
                /* identificationNumber = */ member.passport(),
                /* lastName = */ member.lastName(),
                /* otherNames = */ member.otherNames(),
                /* dateOfBirth = */ member.dob(),
                /* gender = */ Objects.requireNonNull(member.gender()).code(),
                /* marital = */ member.marital(),
                /* phone = */ member.phone(),
                /* cardIssued = */ member.cardIssued(),
                /* relationship = */ member.relationship() != null ? Objects.requireNonNull(member.relationship()).id() : null,
                /* profession = */ member.profession() != null ? Objects.requireNonNull(member.profession()).id() : null,
                /* education = */ member.education() != null ? Objects.requireNonNull(member.education()).id() : null,
                /* email = */ member.email(),
                /* typeOfId = */ member.typeOfId() != null ? Objects.requireNonNull(member.typeOfId()).code() : null,
                /* healthFacilityId = */ member.healthFacility() != null ? IdUtils.getIdFromGraphQLString(Objects.requireNonNull(member.healthFacility()).id()) : null,
                /* currentAddress = */ member.currentAddress(),
                /* currentVillage = */ member.currentVillage() != null ? Objects.requireNonNull(member.currentVillage()).id() : null,
                /* geolocation = */ member.geolocation(),
                /* photoPath = */ downloadPhoto(member.photo()),
                /* isOffline = */ member.offline() != null ? Objects.requireNonNull(member.offline()) : false
        );
    }

    @Nullable
    private String downloadPhoto(@Nullable GetFamilyQuery.Photo photo) {
        String photoPath = getPhotoPath(photo);
        if (photoPath != null) {
            String[] photoPathSegments = photoPath.split("[\\\\/]");
            String photoName = photoPathSegments[photoPathSegments.length - 1];
            if (!StringUtils.isEmpty(photoName)) {
                byte[] photoBytes = getPhotoBytes(photo);
                String imagePath = Global.getGlobal().getImageFolder() + photoName;
                try (OutputStream imageOutputStream = new FileOutputStream(imagePath)) {
                    if (photoBytes != null) {
                        try {
                            Bitmap image = BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length);
                            image.compress(Bitmap.CompressFormat.JPEG, 90, imageOutputStream);
                        } catch (Exception e) {
                            Log.e("MODIFYFAMILY", "Error while processing Base64 image", e);
                        }
                    } else if (photoName.length() > 0) {
                        imageOutputStream.write(new GetPhotoBytesRequest(photoName).get());
                    }
                    return imagePath;
                } catch (Exception e) {
                    e.printStackTrace();
                    return photoPath;
                }
            }
        }
        return null;
    }

    @Nullable
    private String getPhotoPath(@Nullable GetFamilyQuery.Photo photo) {
        if (photo == null) {
            return null;
        }
        return PhotoUtils.getPhotoPath(photo.folder(), photo.filename());
    }

    @Nullable
    private byte[] getPhotoBytes(@Nullable GetFamilyQuery.Photo photo) {
        if (photo == null) {
            return null;
        }
        return PhotoUtils.getPhotoBytes(photo.photo());
    }
}
