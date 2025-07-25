package org.openimis.imispolicies.domain.entity;

import androidx.annotation.Nullable;

/**
 * Modèle pour ResidenceEnvironment
 */
public class ResidenceEnvironment {
    private final Integer id;
    private final String firstLanguage;
    private final String secondLanguage;

    public ResidenceEnvironment(Integer id, @Nullable String firstLanguage, @Nullable String secondLanguage) {
        this.id = id;
        this.firstLanguage = firstLanguage;
        this.secondLanguage = secondLanguage;
    }

    public Integer getId() {
        return id;
    }

    @Nullable
    public String getFirstLanguage() {
        return firstLanguage;
    }

    @Nullable
    public String getSecondLanguage() {
        return secondLanguage;
    }
}
