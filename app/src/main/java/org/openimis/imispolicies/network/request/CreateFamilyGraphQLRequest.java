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
        Log.d("GRAPHQL_DEBUG", "Début de la création de la famille - CreateFamilyGraphQLRequest.create()");
        try {
            Family.Member head = family.getHead();
            if (head == null) {
                throw new IllegalArgumentException("Family must have a head member");
            }

            // Log des valeurs actuelles
            Log.d("GRAPHQL_DEBUG", "Résidence: " + head.getResidenceEnvironment());
            Log.d("GRAPHQL_DEBUG", "Type de logement: " + head.getHousingType());
            Log.d("GRAPHQL_DEBUG", "Couverture mutuelle: " + head.getMutualInsuranceCoverage());
            Log.d("GRAPHQL_DEBUG", "Pas de handicap: " + head.getNoDisability());
            Log.d("GRAPHQL_DEBUG", "Maladie non invalidante: " + head.getNonDisablingDisease());
            
            // Log indiquant qu'aucune valeur par défaut ne sera utilisée
            Log.d("GRAPHQL_DEBUG", "Aucune valeur par défaut ne sera utilisée. Seules les valeurs saisies par l'utilisateur seront envoyées.");
            
            // Log des valeurs du chef de famille
            Log.d("GRAPHQL_DEBUG", "Détails du chef de famille:");
            Log.d("GRAPHQL_DEBUG", "- CHF ID: " + head.getChfId());
            Log.d("GRAPHQL_DEBUG", "- Date de naissance: " + head.getDateOfBirth());
            Log.d("GRAPHQL_DEBUG", "- Genre: " + head.getGender());

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
                    .clientMutationId("Create family '" + family.getHeadChfId() + "'")
                    .locationId(family.getLocationId())
                    .poverty(family.isPoor())
                    .familyTypeId(family.getType() != null ? family.getType() : "H")
                    .address(family.getAddress())
                    .ethnicity(family.getEthnicity())
                    .confirmationNo(family.getConfirmationNumber())
                    .confirmationTypeId(family.getConfirmationType())
                    .isOffline(family.isOffline())
                    .attachments(
                            family.getAttachments() != null ? Mapper.map(family.getAttachments(), dto -> toAttachment(dto, officerId)) : new ArrayList<>()
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
            
            Log.d("GRAPHQL_REQUEST", "Mutation: " + mutation.toString());
            // Afficher la requête GraphQL complète
            // Afficher les détails de la mutation
            Log.d("GRAPHQL_DEBUG", "=== Détails de la mutation ===");
            Log.d("GRAPHQL_DEBUG", "- Location ID: " + family.getLocationId());
            Log.d("GRAPHQL_DEBUG", "- Pauvre: " + family.isPoor());
            Log.d("GRAPHQL_DEBUG", "- Type de famille: " + (family.getType() != null ? family.getType() : "H"));
            Log.d("GRAPHQL_DEBUG", "- Adresse: " + family.getAddress());
            Log.d("GRAPHQL_DEBUG", "- Ethnicité: " + family.getEthnicity());
            Log.d("GRAPHQL_DEBUG", "- Numéro de confirmation: " + family.getConfirmationNumber());
            Log.d("GRAPHQL_DEBUG", "- Type de confirmation: " + family.getConfirmationType());
            Log.d("GRAPHQL_DEBUG", "- Hors ligne: " + family.isOffline());
            
            // Afficher les pièces jointes
            if (family.getAttachments() != null) {
                Log.d("GRAPHQL_DEBUG", "- Nombre de pièces jointes: " + family.getAttachments().size());
            } else {
                Log.d("GRAPHQL_DEBUG", "- Aucune pièce jointe");
            }
            
            // Afficher la requête GraphQL
            String queryString = mutation.queryDocument().toString();
            Log.d("GRAPHQL_DEBUG", "=== Requête GraphQL ===\n" + queryString);
            
            Log.d("GRAPHQL_DEBUG", "=== Exécution de la mutation ===");
            Response<CreateFamilyMutation.Data> response = makeSynchronous(mutation);
            if (response != null) {
                Log.d("GRAPHQL_RESPONSE", "Code de statut: " + (response.getData() != null ? "200" : "N/A"));
                if (response.hasErrors()) {
                    for (com.apollographql.apollo.api.Error error : response.errors()) {
                        Log.e("GRAPHQL_RESPONSE", "Erreur: " + error.message());
                    }
                }
                if (response.getData() != null && response.getData().createFamily() != null) {
                    Log.d("GRAPHQL_RESPONSE", "Réponse: " + response.getData().createFamily().toString());
                }
            } else {
                Log.e("GRAPHQL_RESPONSE", "Réponse nulle");
            }
            
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
            Log.e("GRAPHQL_EXCEPTION", "Erreur lors de la création de la famille", e);
            throw e;
        }
    }

    private FamilyAttachmentInputType toAttachment(
            @NonNull Family.Attachment dto, int officerId
    ){
        java.sql.Date date = new java.sql.Date(System.currentTimeMillis());
        return FamilyAttachmentInputType.builder()
                .title(dto.getTitle())
                .filename(dto.getFilename())
                .document(dto.getContent())
                .build();
    }
    
    private Integer safeGetResidenceEnvironment(Family.Member head) {
        try {
            // Vérifier d'abord si la valeur est null - retourner null si pas défini
            if (head.getResidenceEnvironment() == null) {
                Log.d("GRAPHQL_DEBUG", "ResidenceEnvironment: null (optionnel)");
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getResidenceEnvironment());
            if (TextUtils.isEmpty(value)) {
                Log.d("GRAPHQL_DEBUG", "ResidenceEnvironment: null (valeur vide)");
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Vérifier que le code existe dans la table de référence
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblResidenceEnvironment + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code d'environnement de résidence invalide: " + value + ", retour null");
                return null;
            }
            
            Log.d("GRAPHQL_DEBUG", "ResidenceEnvironment: " + intValue);
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
            // Vérifier d'abord si la valeur est null - retourner null si pas défini
            if (head.getNoDisability() == null) {
                Log.d("GRAPHQL_DEBUG", "NoDisability: null (optionnel)");
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getNoDisability());
            if (TextUtils.isEmpty(value)) {
                Log.d("GRAPHQL_DEBUG", "NoDisability: null (valeur vide)");
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Vérifier que le code existe dans la table de référence
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblNoDisability + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code de type d'incapacité invalide: " + value + ", retour null");
                return null;
            }
            
            Log.d("GRAPHQL_DEBUG", "NoDisability: " + intValue);
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
            // Vérifier d'abord si la valeur est null - retourner null si pas défini
            if (head.getMutualInsuranceCoverage() == null) {
                Log.d("GRAPHQL_DEBUG", "MutualInsuranceCoverage: null (optionnel)");
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getMutualInsuranceCoverage());
            if (TextUtils.isEmpty(value)) {
                Log.d("GRAPHQL_DEBUG", "MutualInsuranceCoverage: null (valeur vide)");
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Vérifier que le code existe dans la table de référence
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblMutualInsuranceCoverage + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code de couverture d'assurance mutuelle invalide: " + value + ", retour null");
                return null;
            }
            
            Log.d("GRAPHQL_DEBUG", "MutualInsuranceCoverage: " + intValue);
            return intValue;
        } catch (NumberFormatException e) {
            Log.e("GRAPHQL_DEBUG", "Format de couverture d'assurance mutuelle invalide", e);
            return null;
        } catch (Exception e) {
            Log.e("GRAPHQL_DEBUG", "Erreur lors de la validation de la couverture d'assurance mutuelle", e);
            return null;
        }
    }
    
    private Integer safeGetHousingType(Family.Member head) {
        try {
            // Vérifier d'abord si la valeur est null - retourner null si pas défini
            if (head.getHousingType() == null) {
                Log.d("GRAPHQL_DEBUG", "HousingType: null (optionnel)");
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getHousingType());
            if (TextUtils.isEmpty(value)) {
                Log.d("GRAPHQL_DEBUG", "HousingType: null (valeur vide)");
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Vérifier que le code existe dans la table de référence
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblHousingType + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code de type de logement invalide: " + value + ", retour null");
                return null;
            }
            
            Log.d("GRAPHQL_DEBUG", "HousingType: " + intValue);
            return intValue;
        } catch (NumberFormatException e) {
            Log.e("GRAPHQL_DEBUG", "Format de type de logement invalide", e);
            return null;
        } catch (Exception e) {
            Log.e("GRAPHQL_DEBUG", "Erreur lors de la validation du type de logement", e);
            return null;
        }
    }
    
    private Integer safeGetNonDisablingDisease(Family.Member head) {
        try {
            // Vérifier d'abord si la valeur est null - retourner null si pas défini
            if (head.getNonDisablingDisease() == null) {
                Log.d("GRAPHQL_DEBUG", "NonDisablingDisease: null (optionnel)");
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getNonDisablingDisease());
            if (TextUtils.isEmpty(value)) {
                Log.d("GRAPHQL_DEBUG", "NonDisablingDisease: null (valeur vide)");
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Vérifier que le code existe dans la table de référence
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblNonDisablingDisease + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code d'état de maladie invalide: " + value + ", retour null");
                return null;
            }
            
            Log.d("GRAPHQL_DEBUG", "NonDisablingDisease: " + intValue);
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
            // Vérifier d'abord si la valeur est null - retourner null si pas défini
            if (head.getIncomeLevel() == null) {
                Log.d("GRAPHQL_DEBUG", "IncomeLevel: null (optionnel)");
                return null;
            }
            
            // Convertir en String puis en int
            String value = String.valueOf(head.getIncomeLevel());
            if (TextUtils.isEmpty(value)) {
                Log.d("GRAPHQL_DEBUG", "IncomeLevel: null (valeur vide)");
                return null;
            }
            
            int intValue = Integer.parseInt(value);
            
            // Vérifier que le code existe dans la table de référence
            SQLHandler sqlHandler = new SQLHandler(context);
            JSONArray result = sqlHandler.getResult("SELECT Code FROM " + SQLHandler.tblIncomeLevel + " WHERE Code = ?", 
                    new String[]{value});
            
            if (result == null || result.length() == 0) {
                Log.w("GRAPHQL_DEBUG", "Code de niveau de revenu invalide: " + value + ", retour null");
                return null;
            }
            
            Log.d("GRAPHQL_DEBUG", "IncomeLevel: " + intValue);
            return intValue;
        } catch (NumberFormatException e) {
            Log.e("GRAPHQL_DEBUG", "Format de niveau de revenu invalide", e);
            return null;
        } catch (Exception e) {
            Log.e("GRAPHQL_DEBUG", "Erreur lors de la validation du niveau de revenu", e);
            return null;
        }
    }
}
