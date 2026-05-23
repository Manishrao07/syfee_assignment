package com.syfe.finance_manager.dto;

import java.util.List;

public class CategoriesListResponse {
    private List<CategoryResponse> categories;

    public CategoriesListResponse() {}

    public CategoriesListResponse(List<CategoryResponse> categories) {
        this.categories = categories;
    }

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
