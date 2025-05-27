package com.Email.Email.Response.Generator;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class EmailType {
    private String emailContent;    // The email content to process
    private String tone;     // The tone to use in the response
    private String model;

    public String getEmail() {
        return emailContent;
    }

    public void setEmail(String email) {
        this.emailContent = email;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}

