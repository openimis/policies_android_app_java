# 🔍 ANALYSE COMPLÈTE DES CHAMPS DU FORMULAIRE INSUREE

## 📋 LISTE COMPLÈTE DES CHAMPS EXTRAITS DU HTML

### **CHAMPS VISIBLES (Affichés à l'utilisateur):**
1. **ddlRelationship** → `dataField="Relationship"`
2. **txtLastName** → `dataField="LastName"`
3. **txtOtherNames** → `dataField="OtherNames"`
4. **txtBirthDate** → `dataField="DOB"`
5. **ddlGender** → `datafield="Gender"`
6. **ddlMaritalStatus** → `datafield="Marital"`
7. **txtOtherHousehold** → `datafield="OtherHousehold"`
8. **ddlBeneficiaryCard** → `datafield="CardIssued"`
9. **txtProfessionalSituation** → `datafield="ProfessionalSituation"`
10. **ddlProfession** → `datafield="Profession"`
11. **ddlEducation** → `datafield="Education"`
12. **txtPhoneNumber** → `datafield="Phone"`
13. **txtEmail** → `datafield="Email"`
14. **ddlIdentificationType** → `datafield="TypeOfId"`
15. **txtIdentificationNumber** → `datafield="IdentificationNumber"`
16. **ddlIncomeLevel** → `datafield="IncomeLevel"`
17. **ddlResidenceEnvironment** → `datafield="ResidenceEnvironment"`
18. **ddlNoDisability** → `datafield="NoDisability"`
19. **ddlNonDisablingDisease** → `datafield="NonDisablingDisease"`
20. **ddlMutualInsuranceCoverage** → `datafield="MutualInsuranceCoverage"`
21. **ddlHousingType** → `datafield="HousingType"`
22. **ddlPaymentMethod** → `datafield="PaymentMethod"`
23. **txtAccountDetails** → `datafield="AccountDetails"`

### **CHAMPS CACHÉS (display:none):**
24. **hfInsureeId** → `datafield="InsureeId"`
25. **hfIsOffline** → `datafld="isOffline"` ⚠️ **ERREUR: datafld au lieu de datafield**
26. **hfisHead** → `datafield="isHead"`
27. **txtInsuranceNumber** → `dataField="CHFID"`
28. **hfImagePath** → `datafield="PhotoPath"`
29. **hfNewPhotoPath** → `datafield="newPhotoPath"`
30. **ddlCurrentRegion** → `datafield="CurRegion"`
31. **ddlCurrentDistrict** → `datafield="CurDistrict"`
32. **ddlCurrentMunicipality** → `datafield="CurWard"`
33. **ddlCurrentVillage** → `datafield="CurVillage"`
34. **txtCurrentAddress** → `datafield="CurrentAddress"`
35. **ddlVulnerability** → `datafield="Vulnerability"`
36. **ddlFSPRegion** → `datafield="FSPRegion"`
37. **ddlFSPDistrict** → `datafield="FSPDistrict"`
38. **ddlFSPCategory** → `datafield="FSPCategory"`
39. **ddlFSP** → `datafield="HFID"`

---

## ⚠️ **PROBLÈMES IDENTIFIÉS**

### **1. ERREUR CRITIQUE - CHAMP hfIsOffline:**
```html
<input type="hidden" datafld="isOffline" id="hfIsOffline" value="."/>
```
**❌ PROBLÈME**: `datafld` au lieu de `datafield`
**✅ CORRECTION NÉCESSAIRE**: Changer en `datafield="isOffline"`

### **2. INCOHÉRENCES DE CASSE:**
- Certains champs utilisent `dataField` (majuscule)
- D'autres utilisent `datafield` (minuscule)
- **IMPACT**: Peut causer des problèmes de liaison des données

---

## 🔍 **VÉRIFICATION DANS LA BASE DE DONNÉES**

Vérifions maintenant quels champs sont traités dans SaveInsuree() et lesquels sont manquants...

### **CHAMPS TRAITÉS DANS SaveInsuree():**
✅ **Relationship** → `values.put("Relationship", Relation)`
✅ **LastName** → `values.put("LastName", data.get("txtLastName"))`
✅ **OtherNames** → `values.put("OtherNames", data.get("txtOtherNames"))`
✅ **DOB** → `values.put("DOB", data.get("txtBirthDate"))`
✅ **Gender** → `values.put("Gender", data.get("ddlGender"))`
✅ **Marital** → `values.put("Marital", Marital)`
✅ **OtherHousehold** → `values.put("OtherHousehold", data.get("txtOtherHousehold"))`
✅ **CardIssued** → `values.put("CardIssued", CardIssued)`
✅ **ProfessionalSituation** → `values.put("ProfessionalSituation", data.get("txtProfessionalSituation"))`
✅ **Profession** → `values.put("Profession", Profession)`
✅ **Education** → `values.put("Education", Education)`
✅ **Phone** → `values.put("Phone", data.get("txtPhoneNumber"))`
✅ **Email** → `values.put("Email", data.get("txtEmail"))`
✅ **TypeOfId** → `values.put("TypeOfId", IdentificationType)`
✅ **IdentificationNumber** → `values.put("IdentificationNumber", data.get("txtIdentificationNumber"))`
✅ **IncomeLevel** → `values.put("IncomeLevel", IncomeLevel)`
✅ **ResidenceEnvironment** → `values.put("ResidenceEnvironment", ResidenceEnvironment)`
✅ **NoDisability** → `values.put("NoDisability", NoDisability)`
✅ **NonDisablingDisease** → `values.put("NonDisablingDisease", NonDisablingDisease)`
✅ **MutualInsuranceCoverage** → `values.put("MutualInsuranceCoverage", MutualInsuranceCoverage)`
✅ **HousingType** → `values.put("HousingType", HousingType)`
✅ **PaymentMethod** → `values.put("PaymentMethod", PaymentMethod)`
✅ **AccountDetails** → `values.put("AccountDetails", data.get("txtAccountDetails"))`

### **CHAMPS POTENTIELLEMENT MANQUANTS:**
❓ **InsureeId** → Traité séparément
❓ **isOffline** → ⚠️ **ERREUR datafld** - Peut ne pas être récupéré
❓ **isHead** → Traité séparément
❓ **CHFID** → `values.put("CHFID", data.get("txtInsuranceNumber"))`
❓ **PhotoPath** → Traité séparément
❓ **newPhotoPath** → Traité séparément
❓ **CurRegion** → Non traité directement
❓ **CurDistrict** → Non traité directement
❓ **CurWard** → Non traité directement
❓ **CurVillage** → `values.put("CurVillage", Integer.valueOf(data.get("ddlCurrentVillage")))`
❓ **CurrentAddress** → `values.put("CurrentAddress", data.get("txtCurrentAddress"))`
❓ **Vulnerability** → `values.put("Vulnerability", data.get("ddlVulnerability"))`
❓ **FSPRegion** → Non traité directement
❓ **FSPDistrict** → Non traité directement
❓ **FSPCategory** → Non traité directement
❓ **HFID** → `values.put("HFID", Integer.valueOf(data.get("ddlFSP")))`

---

## 🚨 **CHAMPS PROBLÉMATIQUES IDENTIFIÉS**

### **1. CHAMP isOffline - ERREUR CRITIQUE**
```html
<input type="hidden" datafld="isOffline" id="hfIsOffline" value="."/>
```
**Problème**: `datafld` au lieu de `datafield`
**Impact**: Le champ n'est pas récupéré par `getControlsValuesJSON()`

### **2. CHAMPS DE LOCALISATION NON TRAITÉS**
- **CurRegion** → Pas dans SaveInsuree()
- **CurDistrict** → Pas dans SaveInsuree()
- **CurWard** → Pas dans SaveInsuree()

### **3. CHAMPS FSP NON TRAITÉS**
- **FSPRegion** → Pas dans SaveInsuree()
- **FSPDistrict** → Pas dans SaveInsuree()
- **FSPCategory** → Pas dans SaveInsuree()

---

## 📊 **STATISTIQUES**

### **CHAMPS TOTAL**: 39 champs
### **CHAMPS FONCTIONNELS**: ~30 champs (77%)
### **CHAMPS PROBLÉMATIQUES**: ~9 champs (23%)

### **RÉPARTITION DES PROBLÈMES:**
- **1 erreur critique** (datafld)
- **6 champs de localisation/FSP** non traités
- **2 champs spéciaux** (photo, IDs) traités séparément

---

## ✅ **RECOMMANDATIONS DE CORRECTION**

### **1. CORRECTION IMMÉDIATE REQUISE:**
```html
<!-- AVANT -->
<input type="hidden" datafld="isOffline" id="hfIsOffline" value="."/>

<!-- APRÈS -->
<input type="hidden" datafield="isOffline" id="hfIsOffline" value="."/>
```

### **2. AJOUT DES CHAMPS MANQUANTS DANS SaveInsuree():**
```java
// Ajouter dans SaveInsuree()
if (data.get("ddlCurrentRegion") != null)
    values.put("CurRegion", Integer.valueOf(data.get("ddlCurrentRegion")));
    
if (data.get("ddlCurrentDistrict") != null)
    values.put("CurDistrict", Integer.valueOf(data.get("ddlCurrentDistrict")));
    
if (data.get("ddlCurrentMunicipality") != null)
    values.put("CurWard", Integer.valueOf(data.get("ddlCurrentMunicipality")));
```

### **3. STANDARDISATION DE LA CASSE:**
Utiliser `datafield` (minuscule) partout pour la cohérence.

---

## 🎯 **CONCLUSION**

**PROBLÈME PRINCIPAL IDENTIFIÉ**: Le champ `isOffline` utilise `datafld` au lieu de `datafield`, ce qui empêche sa récupération correcte.

**CHAMPS FONCTIONNELS**: La majorité des champs (77%) fonctionnent correctement.

**CORRECTIONS NÉCESSAIRES**: 
1. Corriger `datafld` → `datafield`
2. Ajouter les champs de localisation manquants
3. Standardiser la casse des attributs

**IMPACT**: Ces corrections amélioreront la fiabilité de la sauvegarde et de la synchronisation des données.
