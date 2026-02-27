package com.example.lms.domain.qna;

import jakarta.persistence.*;

@Entity
@Table(name = "qna_answers")
public class QnaAnswer {
    @Id
    private Long id;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "responder_name", nullable = false, length = 100)
    private String responderName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    protected QnaAnswer() {}

    public QnaAnswer(Long id, Long questionId, String responderName, String content) {
        this.id = id;
        this.questionId = questionId;
        this.responderName = responderName;
        this.content = content;
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public String getResponderName() { return responderName; }
    public String getContent() { return content; }
}
