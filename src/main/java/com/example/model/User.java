package com.example.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String name;

    private String email;

    private String password; // hashed

    private List<String> preferences = new ArrayList<>(); // topics

    private List<String> likedArticles = new ArrayList<>(); // article IDs

    private List<String> hiddenArticles = new ArrayList<>(); // article IDs

    private Date createdAt = new Date();

    private Date updatedAt = new Date();
}