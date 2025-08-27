package org.openimis.imispolicies.network.request;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.apollographql.apollo.api.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imispolicies.SQLHandler;
import org.openimis.imispolicies.domain.entity.Family;
import org.openimis.imispolicies.network.request.BaseGraphQLRequest;
import org.openimis.imispolicies.network.util.Mapper;
import org.openimis.imispolicies.tools.Log;
import org.openimis.imispolicies.CreateFamilyMutation;
import org.openimis.imispolicies.Global;
import org.openimis.imispolicies.type.CreateFamilyMutationInput;
import org.openimis.imispolicies.type.FamilyAttachmentInputType;
import org.openimis.imispolicies.type.FamilyHeadInsureeInputType;
import org.openimis.imispolicies.type.PhotoInputType;
import org.openimis.imispolicies.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CreateFamilyGraphQLRequest extends BaseGraphQLRequest {
    private final Context context;

    public CreateFamilyGraphQLRequest(Context context) {
        this.context = context.getApplicationContext();
    }

    @WorkerThread
    @NonNull
    public String create(@NonNull Family family, int officerId) throws Exception {

        try {
            Family.Member head = family.getHead();
            if (head == null) {
                throw new IllegalArgumentException("Family must have a head member");
            }
            PhotoInputType photoInput = null;
            if (head.getPhotoBytes() != null) {
                photoInput = PhotoInputType.builder()
                        .filename(head.getPhotoPath())
                        .photo(Base64.encodeToString(head.getPhotoBytes(), Base64.DEFAULT))
                        .date(new java.sql.Date(System.currentTimeMillis()))
                        .officerId(officerId)
                        .build();
            }
            CreateFamilyMutation mutation = new CreateFamilyMutation(
                CreateFamilyMutationInput.builder()
                    .clientMutationId(UUID.randomUUID().toString())
                    .locationId(family.getLocationId())
                    .poverty(family.isPoor())
                    .familyTypeId(family.getType() != null ? family.getType() : "H")
                    .address(family.getAddress())
                    .ethnicity(family.getEthnicity())
                    .confirmationNo(family.getConfirmationNumber())
                    .confirmationTypeId(family.getConfirmationType())
                    .isOffline(family.isOffline())
                    .attachments(
                            family.getAttachments() != null ? Mapper.map(family.getAttachments(), dto -> toAttachment(dto)) : new ArrayList<>()
                    )
                    .parentId(family.getParentId() != null && family.getParentId() != 0 ? family.getParentId() : null)
                    .headInsuree(
                            FamilyHeadInsureeInputType.builder()
                                    .lastName(head.getLastName())
                                    .otherNames(head.getOtherNames())
                                    .genderId(head.getGender())
                                    .dob(head.getDateOfBirth())
                                    .passport(head.getIdentificationNumber())
                                    .cardIssued(head.isCardIssued())
                                    .typeOfIdId(head.getTypeOfId())
                                    .marital(head.getMarital())
                                    .phone(head.getPhone())
                                    .email(head.getEmail())
                                    .professionId(head.getProfession())
                                    .educationId(head.getEducation() == 0 ? null : head.getEducation())
                                    .professionalSituation(head.getProfessionalSituation())
                                    .incomeLevelId(safeGetIncomeLevel(head))
                                    .preferredPaymentMethod(head.getPaymentMethod())
                                    .coordinates(head.getOtherHousehold())
                                    .bankCoordinates(head.getAccountDetails())
                                    .residenceEnvironmentId(safeGetResidenceEnvironment(head))
                                    .housingTypeId(safeGetHousingType(head))
                                    .mutualInsuranceCoverageId(safeGetMutualInsuranceCoverage(head))
                                    .noDisabilityId(safeGetNoDisability(head))
                                    .nonDisablingDiseaseId(safeGetNonDisablingDisease(head))
                                    .photo(photoInput)
                                    .build()
                    )
                    .build()
            );
            

            Response<CreateFamilyMutation.Data> response = makeSynchronous(mutation);

            
            if (response != null && response.hasErrors() && response.errors() != null) {
                String errorMessage = "Erreur GraphQL: ";
                for (com.apollographql.apollo.api.Error error : response.errors()) {
                    errorMessage += error.message() + "; ";
                    Log.e("GRAPHQL_DEBUG", "Erreur GraphQL: " + error.message());
                }
                throw new Exception(errorMessage);
            }
            
            if (response == null || response.getData() == null || response.getData().createFamily() == null) {
                Log.e("GRAPHQL_DEBUG", "Réponse invalide du serveur");
                throw new Exception("Réponse invalide du serveur: données manquantes dans la réponse");
            }
            
            return Objects.requireNonNull(
                    response.getData().createFamily().clientMutationId(), 
                    "client mutation id is null"
            );
            
        } catch (Exception e) {
            throw e;
        }
    }

    private FamilyAttachmentInputType toAttachment(
            @NonNull Family.Attachment dto
    ){
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        return FamilyAttachmentInputType.builder()
                .title(dto.getTitle())
                .mime(dto.getMime())
                .date(date)
                .filename(dto.getFilename())
                .document(dto.getContent())
                .build();
    }
    
    private Integer safeGetResidenceEnvironment(Family.Member head) {
        try {
            // First check if the value is null - return null if not defined
            if (head.getResidenceEnvironment() == null) {

                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getResidenceEnvironment());
            if (TextUtils.isEmpty(value)) {

                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Verify that the code exists in the reference table
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblResidenceEnvironment + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code d'environnement de résidence invalide: " + value + ", retour null");
                return null;
            }
            

            return intValue;
        } catch (NumberFormatException e) {
            Log.e("GRAPHQL_DEBUG", "Format d'environnement de résidence invalide", e);
            return null;
        } catch (Exception e) {
            Log.e("GRAPHQL_DEBUG", "Erreur lors de la validation de l'environnement de résidence", e);
            return null;
        }
    }
    
    private Integer safeGetNoDisability(Family.Member head) {
        try {
            // First check if the value is null - return null if not defined
            if (head.getNoDisability() == null) {

                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getNoDisability());
            if (TextUtils.isEmpty(value)) {

                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Verify that the code exists in the reference table
             SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblNoDisability + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code de type d'incapacité invalide: " + value + ", retour null");
                return null;
            }
            

            return intValue;
        } catch (NumberFormatException e) {
            Log.e("GRAPHQL_DEBUG", "Format de type d'incapacité invalide", e);
            return null;
        } catch (Exception e) {
            Log.e("GRAPHQL_DEBUG", "Erreur lors de la validation du type d'incapacité", e);
            return null;
        }
    }
    
    private Integer safeGetMutualInsuranceCoverage(Family.Member head) {
        try {
            // First check if the value is null - return null if not defined
            if (head.getMutualInsuranceCoverage() == null) {

                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getMutualInsuranceCoverage());
            if (TextUtils.isEmpty(value)) {

                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Verify that the code exists in the reference table
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblMutualInsuranceCoverage + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {

                return null;
            }
            

            return intValue;
        } catch (NumberFormatException e) {

            return null;
        } catch (Exception e) {

            return null;
        }
    }
    
    private Integer safeGetHousingType(Family.Member head) {
        try {
            // First check if the value is null - return null if not defined
            if (head.getHousingType() == null) {

                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getHousingType());
            if (TextUtils.isEmpty(value)) {

                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Verify that the code exists in the reference table
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblHousingType + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {

                return null;
            }
            

            return intValue;
        } catch (NumberFormatException e) {

            return null;
        } catch (Exception e) {

            return null;
        }
    }
    
    private Integer safeGetNonDisablingDisease(Family.Member head) {
        try {
            // First check if the value is null - return null if not defined
            if (head.getNonDisablingDisease() == null) {

                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getNonDisablingDisease());
            if (TextUtils.isEmpty(value)) {

                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Verify that the code exists in the reference table
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblNonDisablingDisease + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code d'état de maladie invalide: " + value + ", retour null");
                return null;
            }
            

            return intValue;
        } catch (NumberFormatException e) {
            Log.e("GRAPHQL_DEBUG", "Format d'état de maladie invalide", e);
            return null;
        } catch (Exception e) {
            Log.e("GRAPHQL_DEBUG", "Erreur lors de la validation de l'état de maladie", e);
            return null;
        }
    }
    
    private Integer safeGetIncomeLevel(Family.Member head) {
        try {
            // First check if the value is null - return null if not defined
             if (head.getIncomeLevel() == null) {
                // IncomeLevel not defined
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getIncomeLevel());
            if (TextUtils.isEmpty(value)) {
                // Valeur vide
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Verify that the code exists in the reference table
            SQLHandler sqlHandler = new SQLHandler(context);
            // The local schema defines columns: Id, FirstLanguage, SecondLanguage
            // Use Id for validation instead of Code
            JSONArray result = sqlHandler.getResult("SELECT Id FROM " + SQLHandler.tblIncomeLevel + " WHERE Id = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                // Id de niveau de revenu introuvable
                return null;
            }
            
            return intValue;
        } catch (NumberFormatException e) {
            // Format de niveau de revenu invalide
            return null;
        } catch (Exception e) {
            // Erreur lors de la validation du niveau de revenu
            return null;
        }
    }
}
