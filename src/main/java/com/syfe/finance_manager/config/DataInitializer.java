package com.syfe.finance_manager.config;

import com.syfe.finance_manager.entity.Category;
import com.syfe.finance_manager.entity.CategoryType;
import com.syfe.finance_manager.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize default categories
        List<Category> defaultCategories = Arrays.asList(
                // INCOME
                new Category("Salary", CategoryType.INCOME, false, null),
                
                // EXPENSE
                new Category("Food", CategoryType.EXPENSE, false, null),
                new Category("Rent", CategoryType.EXPENSE, false, null),
                new Category("Transportation", CategoryType.EXPENSE, false, null),
                new Category("Entertainment", CategoryType.EXPENSE, false, null),
                new Category("Healthcare", CategoryType.EXPENSE, false, null),
                new Category("Utilities", CategoryType.EXPENSE, false, null)
        );

        for (Category cat : defaultCategories) {
            if (!categoryRepository.existsByNameAndUserIsNull(cat.getName())) {
                categoryRepository.save(cat);
            }
        }
    }
}
