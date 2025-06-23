package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import java.util.List;

public class StudentDao implements GenericDao<Student, Long> {

    private final EntityManagerFactory entityManagerFactory;

    public StudentDao(String persistenceUnitName) {
        this.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    }

    public StudentDao() {
        this("hillel-persistence-unit");
    }

    @Override
    public void save(Student student) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            if (student.getId() == null) {
                entityManager.persist(student);
            } else {
                entityManager.merge(student);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving student: " + e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Student findById(Long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.find(Student.class, id);
        }
    }

    public Student findByIdWithHomeworks(Long id) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

            return entityManager.createQuery(
                            "SELECT s FROM Student s LEFT JOIN FETCH s.homeworks WHERE s.id = :id", Student.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Student findByEmail(String email) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Student> findAll() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("SELECT s FROM Student s", Student.class).getResultList();
        }
    }

    public List<Student> findAllWithHomeworks() {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {

            return entityManager.createQuery("SELECT s FROM Student s LEFT JOIN FETCH s.homeworks", Student.class).getResultList();
        }
    }

    @Override
    public Student update(Student student) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {

            Student updatedStudent = entityManager.merge(student);
            entityManager.getTransaction().commit();
            return updatedStudent;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Error updating student: " + e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public boolean deleteById(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Student student = entityManager.find(Student.class, id);
            if (student != null) {
                entityManager.remove(student);
                entityManager.getTransaction().commit();
                return true;
            }
            entityManager.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting student: " + e.getMessage(), e);
        } finally {
            entityManager.close();
        }
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}