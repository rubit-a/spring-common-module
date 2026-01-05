package rubit.coredata.entity

import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@DisplayName("BaseEntity 계층 구조 테스트")
class BaseEntityAnnotationTest {

    @Test
    @DisplayName("BaseTimeEntity는 MappedSuperclass이며 AuditingEntityListener를 사용한다")
    fun baseTimeEntityHasAuditingAnnotations() {
        val mappedSuperclass = BaseTimeEntity::class.java.getAnnotation(MappedSuperclass::class.java)
        val entityListeners = BaseTimeEntity::class.java.getAnnotation(EntityListeners::class.java)

        assertNotNull(mappedSuperclass)
        assertNotNull(entityListeners)
        val listenerTypes = entityListeners.value.map { it.java }
        assertTrue(listenerTypes.contains(AuditingEntityListener::class.java))
    }

    @Test
    @DisplayName("BaseEntity는 id와 version에 JPA 어노테이션이 붙는다")
    fun baseEntityHasIdAndVersionAnnotations() {
        val mappedSuperclass = BaseEntity::class.java.getAnnotation(MappedSuperclass::class.java)
        val idField = BaseEntity::class.java.getDeclaredField("id")
        val versionField = BaseEntity::class.java.getDeclaredField("version")

        assertNotNull(mappedSuperclass)
        assertNotNull(idField.getAnnotation(Id::class.java))
        assertNotNull(versionField.getAnnotation(Version::class.java))
    }

    @Test
    @DisplayName("BaseAuditEntity는 createdBy/updatedBy 감사 필드를 가진다")
    fun baseAuditEntityHasAuditFields() {
        val mappedSuperclass = BaseAuditEntity::class.java.getAnnotation(MappedSuperclass::class.java)
        val createdByField = BaseAuditEntity::class.java.getDeclaredField("createdBy")
        val updatedByField = BaseAuditEntity::class.java.getDeclaredField("updatedBy")

        assertNotNull(mappedSuperclass)
        assertTrue(hasAnnotationOnFieldOrGetter(createdByField, BaseAuditEntity::class.java, "getCreatedBy", CreatedBy::class.java))
        assertTrue(hasAnnotationOnFieldOrGetter(updatedByField, BaseAuditEntity::class.java, "getUpdatedBy", LastModifiedBy::class.java))
    }

    private fun hasAnnotationOnFieldOrGetter(
        field: java.lang.reflect.Field,
        owner: Class<*>,
        getterName: String,
        annotation: Class<out Annotation>
    ): Boolean {
        if (field.isAnnotationPresent(annotation)) {
            return true
        }
        val getter = owner.methods.firstOrNull { it.name == getterName && it.parameterCount == 0 }
        return getter?.isAnnotationPresent(annotation) ?: false
    }
}
