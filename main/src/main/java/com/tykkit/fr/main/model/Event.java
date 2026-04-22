package com.tykkit.fr.main.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "events")
public class Event {
    @Id
    private String id;
    @Indexed(unique = true)
    private String eventId;

    private String title;
    private String type;
    private String venueId;
    private Instant eventDate;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    private int maxSeats;
    private int registeredCount;

    private List<EventAttribute> attributes;


    @Data
    @NoArgsConstructor
    public static class EventAttribute{
        private String k;
        private String v;
    }
}
