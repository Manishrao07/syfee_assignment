package com.syfe.finance_manager.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.syfe.finance_manager.entity.CategoryType;

public class CategoryResponse {
    private String name;
    private CategoryType type;
    
    @JsonProperty("isCustom")
    private boolean isCustom;

    public CategoryResponse() {}

    public CategoryResponse(String name, CategoryType type, boolean isCustom) {
        this.name = name;
        this.type = type;
        this.isCustom = isCustom;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CategoryType getType() {
        return type;
    }

    public void setType(CategoryType type) {
        this.type = type;
    }

    public boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(boolean custom) {
        isCustom = custom;
    }

    // Include custom getter for "custom" property as required by E2E test script
    @JsonProperty("custom")
    public boolean getCustom() {
        return isCustom;
    }
}
