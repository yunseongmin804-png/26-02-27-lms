package com.example.lms.domain.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "instructor_name", nullable = false, length = 100)
    private String instructorName;

    @Column(nullable = false)
    private Integer capacity;

    protected Course() {}

    public Course(Long id, String title, String category, String instructorName, Integer capacity) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.instructorName = instructorName;
        this.capacity = capacity;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getInstructorName() { return instructorName; }
    public Integer getCapacity() { return capacity; }
}
