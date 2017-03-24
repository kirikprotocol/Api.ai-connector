package com.eyelinecom.whoisd.sads2.msqna.model;

/**
 * Created by jeck on 23/03/17.
 */
public class Request {
    String question;
    Integer top;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }
}
