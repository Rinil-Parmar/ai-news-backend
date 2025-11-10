package com.example.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_events")
public class UserEvent {

    @Id
    private String id;

    private String userId;

    private String articleId;

    private String action; // read | like | hide

    private Date timestamp = new Date();
}