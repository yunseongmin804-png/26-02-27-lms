package com.example.lms.domain.qna;

import jakarta.persistence.*;

@Entity
@Table(name = "qna_questions")
public class QnaQuestion {
    @Id
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected QnaQuestion() {}

    public QnaQuestion(Long id, Long studentId, String title, String content) {
        this.id = id;
        this.studentId = studentId;
        this.title = title;
        this.content = content;
    }

    public Long getId() { return id; }
    public Long getStudentId() { return studentId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
}
