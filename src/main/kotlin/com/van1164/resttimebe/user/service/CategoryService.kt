package com.van1164.resttimebe.user.service

import com.van1164.resttimebe.common.exception.ErrorCode.CATEGORY_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions
import com.van1164.resttimebe.domain.Category
import com.van1164.resttimebe.user.repository.UserRepository
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
            ?: throw GlobalExceptions.NotFoundException(CATEGORY_NOT_FOUND)
    }

    fun addCategoryToUser(userId: String, categoryName: String, color: String): Category {
        val user = userReadService.getById(userId)
        val newCategory = Category(
            userId = userId,
            categoryName = categoryName,
            color = color
        )

        val updatedCategories = user.categories + newCategory
        val updatedUser = user.copy(categories = updatedCategories)

        userRepository.save(updatedUser)
        return newCategory
    }

    fun updateCategory(userId: String, categoryId: String, categoryName: String, color: String): Category {
        val user = userReadService.getById(userId)
        val category = getCategoryById(userId, categoryId)
        val updatedCategory = category.copy(
            categoryName = categoryName,
            color = color
        )

        val updatedCategories = user.categories.map { if (it.categoryId == categoryId) updatedCategory else it }
        val updatedUser = user.copy(categories = updatedCategories)

        userRepository.save(updatedUser)
        return updatedCategory
    }

    fun removeCategoryFromUser(userId: String, categoryId: String): Category {
        val user = userReadService.getById(userId)
        val category = getCategoryById(userId, categoryId)

        val updatedCategories = user.categories.filter { it.categoryId != categoryId }
        val updatedUser = user.copy(categories = updatedCategories)

        userRepository.save(updatedUser)
        return category
    }
}
