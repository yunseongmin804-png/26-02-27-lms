package com.example.lms.domain.submission;

import jakarta.persistence.*;

@Entity
@Table(name = "submissions")
public class Submission {
    @Id
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "attachment_path", length = 300)
    private String attachmentPath;

    @Column
    private Integer score;

    @Column(length = 500)
    private String feedback;

    protected Submission() {}

    public Submission(Long id, Long assignmentId, Long studentId, String content, String attachmentPath, Integer score, String feedback) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.studentId = studentId;
        this.content = content;
        this.attachmentPath = attachmentPath;
        this.score = score;
        this.feedback = feedback;
    }

    public Long getId() { return id; }
    public Long getAssignmentId() { return assignmentId; }
    public Long getStudentId() { return studentId; }
    public String getContent() { return content; }
    public String getAttachmentPath() { return attachmentPath; }
    public Integer getScore() { return score; }
    public String getFeedback() { return feedback; }
}
