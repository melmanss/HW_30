package org.example;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        StudentDao studentDao = new StudentDao();

        try {
            String student1Email = "jon.doe@example.com";
            Student student1;
            Student existingStudent1 = studentDao.findByEmail(student1Email);
            if (existingStudent1 == null) {
                student1 = new Student("Jon", "Doe", student1Email);
                studentDao.save(student1);
                System.out.println("Saved new student: " + student1);
            } else {
                student1 = existingStudent1;
                System.out.println("Student with email " + student1.getEmail() + " already exists. Using existing student.");
            }

            String student2Email = "jane.smith@example.com";
            Student student2;
            Student existingStudent2 = studentDao.findByEmail(student2Email);
            if (existingStudent2 == null) {
                student2 = new Student("Jane", "Smith", student2Email);
                studentDao.save(student2);
                System.out.println("Saved new student: " + student2);
            } else {
                student2 = existingStudent2;
                System.out.println("Student with email " + student2.getEmail() + " already exists. Using existing student.");
            }

            Student student1Managed = studentDao.findByIdWithHomeworks(student1.getId());
            Student student2Managed = studentDao.findByIdWithHomeworks(student2.getId());

            Homework homework1 = new Homework("Math homework", LocalDate.of(2025, 6, 25), 90, student1Managed);
            Homework homework2 = new Homework("History essay", LocalDate.of(2025, 7, 10), 85, student1Managed);
            Homework homework3 = new Homework("Physics lab", LocalDate.of(2025, 7, 1), 95, student2Managed);

            if (student1Managed != null && student1Managed.getHomeworks().stream().noneMatch(h -> h.getDescription().equals(homework1.getDescription()))) {
                student1Managed.addHomework(homework1);
            }
            if (student1Managed != null && student1Managed.getHomeworks().stream().noneMatch(h -> h.getDescription().equals(homework2.getDescription()))) {
                student1Managed.addHomework(homework2);
            }
            if (student2Managed != null && student2Managed.getHomeworks().stream().noneMatch(h -> h.getDescription().equals(homework3.getDescription()))) {
                student2Managed.addHomework(homework3);
            }

            studentDao.update(student1Managed);
            studentDao.update(student2Managed);
            System.out.println("Students updated with homeworks.");

            Student finalStudent1 = studentDao.findByIdWithHomeworks(student1.getId());
            System.out.println("\nFound student with homeworks by ID: " + finalStudent1);
            if (finalStudent1 != null) {
                System.out.println("Homeworks for " + finalStudent1.getFirstName() + ":");
                finalStudent1.getHomeworks().forEach(System.out::println);
            }

            Student studentByEmail = studentDao.findByEmail(student2Email);
            System.out.println("\nStudent by email: " + studentByEmail);

            List<Student> allStudents = studentDao.findAllWithHomeworks();
            System.out.println("\nAll students with their homeworks:");
            allStudents.forEach(s -> {
                System.out.println(s);
                s.getHomeworks().forEach(h -> System.out.println("  - " + h));
            });

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            studentDao.close();
            System.out.println("\nApplication finished. EntityManagerFactory closed.");
        }
    }
}