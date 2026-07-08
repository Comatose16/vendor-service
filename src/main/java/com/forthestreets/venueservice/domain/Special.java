package com.forthestreets.venueservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("SPECIAL")
public class Special extends Event {

    @Column(name = "condition_details")
    private String conditionDetails;

    public Special() { super(); }

    public Special(String title, String description, LocalDateTime startTime, LocalDateTime endTime,
                   Venue venue, String conditionDetails) {
        super(title, description, startTime, endTime, venue);
        this.conditionDetails = conditionDetails;
    }

    public String getConditionDetails() { return conditionDetails; }
    public void setConditionDetails(String conditionDetails) { this.conditionDetails = conditionDetails; }
}