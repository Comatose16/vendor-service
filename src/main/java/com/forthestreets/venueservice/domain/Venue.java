package com.forthestreets.venueservice.domain;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String address;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();

    public Venue() {}

    public Venue(String name, String address, Point location) {
        this.name = name;
        this.address = address;
        this.location = location;
    }

    public void addEvent(Event event) {
        this.events.add(event);
        event.setVenue(this);
    }

    public void removeEvent(Event event) {
        this.events.remove(event);
        event.setVenue(null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Point getLocation() { return location; }
    public void setLocation(Point location) { this.location = location; }

    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = events; }
}