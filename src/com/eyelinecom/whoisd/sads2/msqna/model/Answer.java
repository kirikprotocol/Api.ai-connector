package com.eyelinecom.whoisd.sads2.msqna.model;

/**
 * Created by jeck on 23/03/17.
 */
public class Answer {
    private String answer;
    private Integer score;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "answer='" + answer + '\'' +
                ", score=" + score +
                '}';
    }
}
