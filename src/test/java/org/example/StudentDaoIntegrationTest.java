package org.example;

import org.junit.jupiter.api.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*; // Для is(), notNullValue(), containsInAnyOrder()

import java.time.LocalDate;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Визначаємо порядок виконання тестів
class StudentDaoIntegrationTest {

    private StudentDao studentDao;

    @BeforeAll // Виконується один раз перед усіма тестами
    static void setupAll() {
        // Забезпечуємо, що Main не буде запущено під час тестів,
        // або будь-яка ініціалізація, яка може конфліктувати
        System.out.println("Starting integration tests for StudentDao...");
    }

    @BeforeEach // Виконується перед кожним тестовим методом
    void setUp() {
        // Ініціалізуємо StudentDao для кожного тесту, використовуючи тестовий persistence-unit
        // Це гарантує чисту базу даних для кожного тесту завдяки create-drop у persistence-test.xml
        studentDao = new StudentDao("hillel-test-persistence-unit");
    }

    @AfterEach // Виконується після кожного тестового методу
    void tearDown() {
        if (studentDao != null) {
            studentDao.close(); // Закриваємо EntityManagerFactory після кожного тесту
        }
    }

    @AfterAll // Виконується один раз після всіх тестів
    static void tearDownAll() {
        System.out.println("All integration tests for StudentDao finished.");
    }


    @Test
    @Order(1) // Виконається першим
    @DisplayName("Test A: Save a new Student and find by ID")
    void testSaveAndFindStudentById() {
        Student student = new Student("Test", "Student", "test.student@example.com");
        studentDao.save(student);

        assertThat("Student ID should not be null after saving", student.getId(), is(notNullValue()));

        Student foundStudent = studentDao.findById(student.getId());

        assertThat("Found student should not be null", foundStudent, is(notNullValue()));
        assertThat("Found student ID should match", foundStudent.getId(), is(student.getId()));
        assertThat("Found student first name should match", foundStudent.getFirstName(), is("Test"));
        assertThat("Found student email should match", foundStudent.getEmail(), is("test.student@example.com"));
    }

    @Test
    @Order(2)
    @DisplayName("Test B: Find Student by Email")
    void testFindByEmail() {
        Student student = new Student("Email", "Finder", "email.finder@example.com");
        studentDao.save(student);

        Student foundStudent = studentDao.findByEmail("email.finder@example.com");

        assertThat("Found student by email should not be null", foundStudent, is(notNullValue()));
        assertThat("Found student email should match", foundStudent.getEmail(), is("email.finder@example.com"));
    }

    @Test
    @Order(3)
    @DisplayName("Test C: Update Student details")
    void testUpdateStudent() {
        Student student = new Student("Original", "Name", "original.name@example.com");
        studentDao.save(student);

        student.setFirstName("Updated");
        student.setEmail("updated.email@example.com");
        Student updatedStudent = studentDao.update(student);

        assertThat("Updated student should not be null", updatedStudent, is(notNullValue()));
        assertThat("Updated student ID should match original", updatedStudent.getId(), is(student.getId()));
        assertThat("Student first name should be updated", updatedStudent.getFirstName(), is("Updated"));
        assertThat("Student email should be updated", updatedStudent.getEmail(), is("updated.email@example.com"));

        Student retrievedStudent = studentDao.findById(student.getId());
        assertThat("Retrieved student first name should be updated", retrievedStudent.getFirstName(), is("Updated"));
    }

    @Test
    @Order(4)
    @DisplayName("Test D: Save Student with Homeworks and retrieve with them")
    void testSaveStudentWithHomeworksAndFindByIdWithHomeworks() {
        Student student = new Student("Homework", "Lover", "homework.lover@example.com");
        studentDao.save(student);

        Homework hw1 = new Homework("Spring Basics", LocalDate.of(2025, 7, 1), 85, student);
        Homework hw2 = new Homework("Hibernate Deep Dive", LocalDate.of(2025, 7, 15), 92, student);

        student.addHomework(hw1);
        student.addHomework(hw2);

        studentDao.update(student); // Оновлюємо студента, що зберегти домашні завдання

        Student foundStudent = studentDao.findByIdWithHomeworks(student.getId());

        assertThat("Found student should not be null", foundStudent, is(notNullValue()));
        assertThat("Found student ID should match", foundStudent.getId(), is(student.getId()));
        assertThat("Homeworks collection should not be null", foundStudent.getHomeworks(), is(notNullValue()));
        assertThat("Homeworks collection size should be 2", foundStudent.getHomeworks().size(), is(2));

        // Перевіряємо, чи колекція містить очікувані домашні завдання
        assertThat(foundStudent.getHomeworks(), containsInAnyOrder(
                hasProperty("description", is("Spring Basics")),
                hasProperty("description", is("Hibernate Deep Dive"))
        ));
    }

    @Test
    @Order(5)
    @DisplayName("Test E: Retrieve all Students including Homeworks")
    void testFindAllWithHomeworks() {
        // Створюємо двох студентів з домашками
        Student student1 = new Student("All", "Student1", "all.student1@example.com");
        Student student2 = new Student("All", "Student2", "all.student2@example.com");

        studentDao.save(student1);
        studentDao.save(student2);

        student1.addHomework(new Homework("Task A", LocalDate.now(), 70, student1));
        student1.addHomework(new Homework("Task B", LocalDate.now(), 80, student1));
        student2.addHomework(new Homework("Task C", LocalDate.now(), 90, student2));

        studentDao.update(student1);
        studentDao.update(student2);

        List<Student> allStudents = studentDao.findAllWithHomeworks();

        assertThat("Should find at least 2 students", allStudents.size(), greaterThanOrEqualTo(2));

        Student s1 = allStudents.stream()
                .filter(s -> s.getEmail().equals("all.student1@example.com"))
                .findFirst()
                .orElse(null);

        assertThat("Student1 should be found", s1, is(notNullValue()));
        assertThat("Student1 should have 2 homeworks", s1.getHomeworks().size(), is(2));

        Student s2 = allStudents.stream()
                .filter(s -> s.getEmail().equals("all.student2@example.com"))
                .findFirst()
                .orElse(null);

        assertThat("Student2 should be found", s2, is(notNullValue()));
        assertThat("Student2 should have 1 homework", s2.getHomeworks().size(), is(1));
    }

    @Test
    @Order(6)
    @DisplayName("Test F: Delete Student by ID")
    void testDeleteStudentById() {
        Student student = new Student("Delete", "Me", "delete.me@example.com");
        studentDao.save(student);

        Long studentId = student.getId();
        assertThat("Student ID should not be null before deletion", studentId, is(notNullValue()));

        boolean deleted = studentDao.deleteById(studentId);
        assertThat("Deletion should be successful", deleted, is(true));

        Student foundStudent = studentDao.findById(studentId);
        assertThat("Student should not be found after deletion", foundStudent, is(nullValue()));

        // Перевіряємо, чи пов'язані домашні завдання також видалені (orphanRemoval)
        // Для цього потрібен HomeworkDao або прямий запит. Пропускаємо для цього тесту
        // або можна перевірити, що жоден Homework з таким student_id не існує
    }
}