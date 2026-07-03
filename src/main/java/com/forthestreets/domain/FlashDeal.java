package com.forthestreets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("FLASH_DEAL")
public class FlashDeal extends Event {

    @Column(name = "is_active")
    private boolean isActive = true;

    public FlashDeal() { super(); }

    public FlashDeal(String title, String description, LocalDateTime startTime, LocalDateTime endTime,
                     Venue venue, boolean isActive) {
        super(title, description, startTime, endTime, venue);
        this.isActive = isActive;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
}