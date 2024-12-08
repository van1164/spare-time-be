package com.van1164.resttimebe.user

import com.van1164.resttimebe.common.exception.ErrorCode.CATEGORY_NOT_FOUND
import com.van1164.resttimebe.common.exception.GlobalExceptions.NotFoundException
import com.van1164.resttimebe.fixture.UserFixture.Companion.createUser
import com.van1164.resttimebe.user.repository.UserRepository
import com.van1164.resttimebe.user.service.CategoryService
import com.van1164.resttimebe.util.DatabaseIdHelper.Companion.validateAndGetId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CategoryTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val categoryService: CategoryService
) {
    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
    }

    @Test
    fun `getCategoryList should return all categories successfully`() {
        val user = userRepository.save(createUser())
        val category1 = categoryService.addCategoryToUser(user.userId, "category1", "#FFFFFF")
        val category2 = categoryService.addCategoryToUser(user.userId, "category2", "#000000")

        val categories = categoryService.getCategoryList(user.userId)

        assertThat(categories).hasSize(2)
        assertThat(categories[0].categoryId).isEqualTo(category1.categoryId)
        assertThat(categories[1].categoryId).isEqualTo(category2.categoryId)
    }

    @Test
    fun `getCategoryById should return category successfully`() {
        val user = userRepository.save(createUser())
        val category = categoryService.addCategoryToUser(user.userId, "category1", "#FFFFFF")

        val foundCategory = categoryService.getCategoryById(user.userId, category.categoryId)

        assertThat(foundCategory.categoryId).isEqualTo(category.categoryId)
    }

    @Test
    fun `getCategoryById should throw exception when category not found`() {
        val user = userRepository.save(createUser())
        categoryService.addCategoryToUser(user.userId, "category1", "#FFFFFF")

        assertThatThrownBy {
            categoryService.getCategoryById(user.userId, "notFoundCategoryId")
        }.isInstanceOf(NotFoundException::class.java)
            .hasMessage(CATEGORY_NOT_FOUND.message)
    }

    @Test
    fun `addCategoryToUser should add category successfully`() {
        val user = userRepository.save(createUser())
        val category = categoryService.addCategoryToUser(user.userId, "category1", "#FFFFFF")

        assertThat(category.categoryName).isEqualTo("category1")
        assertThat(category.color).isEqualTo("#FFFFFF")
    }

    @Test
    fun `updateCategory should update category successfully`() {
        val user = userRepository.save(createUser())
        val category = categoryService.addCategoryToUser(user.userId, "category1", "#FFFFFF")

        val updatedCategory = categoryService.updateCategory(user.userId, category.categoryId, "category2", "#000000")

        assertThat(updatedCategory.categoryName).isEqualTo("category2")
        assertThat(updatedCategory.color).isEqualTo("#000000")
    }

    @Test
    fun `removeCategoryFromUser should remove category successfully`() {
        val user = userRepository.save(createUser())
        val category = categoryService.addCategoryToUser(user.userId, "category1", "#FFFFFF")

        val removedCategory = categoryService.removeCategoryFromUser(user.userId, category.categoryId)

        assertThat(removedCategory.categoryId).isEqualTo(category.categoryId)
    }
}