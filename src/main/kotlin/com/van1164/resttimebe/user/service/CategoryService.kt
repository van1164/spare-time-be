package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.domain.Category
import com.van1164.resttimebe.user.UserRepository
import org.springframework.stereotype.Service

@Service
class CategoryService (
    private val userReadService: UserReadService,
    private val userRepository: UserRepository
) {
    fun getCategoryList(userId: String): List<Category> {
        return userReadService.getById(userId).categories
    }

    fun getCategoryById(userId: String, categoryId: String): Category {
        return userReadService.getById(userId).categories.find { it.categoryId == categoryId }
            ?: throw RuntimeException("Category not found")
    }

    fun addCategoryToUser(userId: String, categoryName: String, color: String): Category {
        val user = userReadService.getById(userId)
        val newCategory = Category(
            userId = user.id,
            categoryName = categoryName,
            color = color
        )

        val updatedCategories = user.categories + newCategory
        val updatedUser = user.copy(categories = updatedCategories)

        userRepository.save(updatedUser)

        return newCategory
    }
}
