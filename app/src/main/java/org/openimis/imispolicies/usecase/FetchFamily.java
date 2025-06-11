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
import java.util.Collections;
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
    public Family execute(@NonNull String headChfId, @NonNull String parentUuid) throws Exception {
        GetFamilyQuery.Node node = getFamilyGraphQLRequest.get(headChfId, parentUuid);
        return new Family(
                /* headChfId = */ node.headInsuree().chfId(),
                /* id = */ IdUtils.getIdFromGraphQLString(node.id()),
                /* uuid = */ node.uuid(),
                /* sms = */ null,
                /* locationId = */ node.location() != null ? IdUtils.getIdFromGraphQLString(Objects.requireNonNull(node.location()).id()) : null,
                /* isPoor = */ node.poverty() != null ? Objects.requireNonNull(node.poverty()) : false,
                /* type = */ node.familyType() != null ? Objects.requireNonNull(node.familyType()).code() : null,
                /* address = */ node.address() != null ? node.address() : "",
                /* ethnicity = */ node.ethnicity(),
                /* confirmationNumber = */ node.confirmationNo() != null ? node.confirmationNo() : "",
                /* confirmationType = */ node.confirmationType() != null ? Objects.requireNonNull(node.confirmationType()).code() : null,
                /* isOffline = */ node.isOffline() != null ? Objects.requireNonNull(node.isOffline()) : false,
                /* parentId = */ node.parent() != null ? IdUtils.getIdFromGraphQLString(node.parent().id()) : null,
                /* parentUuid = */ node.parent() != null ? Objects.requireNonNull(node.parent()).uuid()  : null,
                /* insurees = */ Mapper.map(node.members().edges(), (edge) -> toMember(edge, node)),
                /* attachments = */ Mapper.map(Objects.requireNonNull(node.attachments()), (attachment) -> toAttachment(attachment)),
                null
        );
    }

    @NonNull
    private Family.Member toMember(@NonNull GetFamilyQuery.Edge1 edge, @NonNull GetFamilyQuery.Node family) {
        GetFamilyQuery.Node1 member = Objects.requireNonNull(edge.node());
        return new Family.Member(
                /* chfId = */ Objects.requireNonNull(member.chfId()),
                /* isHead = */ member.head(),
                /* id = */ IdUtils.getIdFromGraphQLString(member.id()),
                /* uuid = */ member.uuid(),
                /* familyId = */ IdUtils.getIdFromGraphQLString(family.id()),
                /* familyUuid = */ family.uuid(),
                /* identificationNumber = */ member.passport(),
                /* lastName = */ member.lastName(),
                /* otherNames = */ member.otherNames(),
                /* dateOfBirth = */ member.dob(),
                /* gender = */ Objects.requireNonNull(member.gender()).code(),
                /* marital = */ member.marital(),
                /* phone = */ member.phone() != null ? member.phone() : "",
                /* cardIssued = */ member.cardIssued(),
                /* relationship = */ member.relationship() != null ? Objects.requireNonNull(member.relationship()).id() : null,
                /* profession = */ member.profession() != null ? Objects.requireNonNull(member.profession()).id() : null,
                /* education = */ member.education() != null ? Objects.requireNonNull(member.education()).id() : null,
                /* email = */ member.email() != null ? member.email() : "",
                /* typeOfId = */ member.typeOfId() != null ? Objects.requireNonNull(member.typeOfId()).code() : null,
                /* healthFacilityId = */ member.healthFacility() != null ? IdUtils.getIdFromGraphQLString(Objects.requireNonNull(member.healthFacility()).id()) : null,
                /* currentAddress = */ member.currentAddress(),
                /* currentVillage = */ member.currentVillage() != null ? IdUtils.getIdFromGraphQLString(Objects.requireNonNull(member.currentVillage()).id()) : null,
                /* geolocation = */ member.geolocation(),
                /* professional situation = */ member.professionalSituation(),
                /* incomeLevel = */ member.incomeLevel() != null ? IdUtils.getIdFromGraphQLString(Objects.requireNonNull(member.incomeLevel()).id()) : null,
                /* payment method = */ member.preferredPaymentMethod(),
                /* other household = */ member.coordinates() != null ? member.coordinates() : "",
                /* account details = */ member.bankCoordinates() != null ? member.bankCoordinates() : "",
                /* photoPath = */ downloadPhoto(member.photo()),
                /* photoBytes = */ null, // We already saved them on disk, no need to pass them here.
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

    @NonNull
    private Family.Attachment toAttachment(@NonNull GetFamilyQuery.Attachment attachment){
        return new Family.Attachment(
                /* id */ Integer.parseInt(attachment.idAttachment()),
                /* tittle */ attachment.title(),
                /* mime */ attachment.mime(),
                /* filename */ attachment.filename(),
                /* content */ attachment.document()
        );
    }
}
