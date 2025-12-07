package org.example.booking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "events")
public class Event extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal ticketPrice;

    @Column(nullable = false)
    private Long totalTickets;

    @Column(nullable = false)
    private Long availableTickets;

    // Helper to check stock
    public boolean hasTickets(long count){
        return availableTickets >= count;
    }
}
