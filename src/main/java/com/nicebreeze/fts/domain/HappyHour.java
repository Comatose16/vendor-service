package com.nicebreeze.fts.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@DiscriminatorValue("HAPPY_HOUR")
public class HappyHour extends Event {

    @Column(name = "drink_specials_detail")
    private String drinkSpecialsDetail;

    @Column(name = "food_specials_detail")
    private String foodSpecialsDetail;

    public HappyHour() { super(); }

    public HappyHour(String title, String description, LocalDateTime startTime, LocalDateTime endTime,
                     Venue venue, String drinkSpecialsDetail, String foodSpecialsDetail) {
        super(title, description, startTime, endTime, venue);
        this.drinkSpecialsDetail = drinkSpecialsDetail;
        this.foodSpecialsDetail = foodSpecialsDetail;
    }

    public String getDrinkSpecialsDetail() { return drinkSpecialsDetail; }

    public void setDrinkSpecialsDetail(String detail) { this.drinkSpecialsDetail = detail; }

    public String getFoodSpecialsDetail() { return foodSpecialsDetail; }

    public void setFoodSpecialsDetail(String detail) { this.foodSpecialsDetail = detail; }
}
