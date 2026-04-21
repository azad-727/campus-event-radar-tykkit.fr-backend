package com.tykkit.fr.main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@Document(collection = "vibes")

public class Vibe {
    @Id
    private String id;
    private String emoji;
    private String label;
    private int votes;
}
