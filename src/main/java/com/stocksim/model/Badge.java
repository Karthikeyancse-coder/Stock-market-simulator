package com.stocksim.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "badges")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String code;

    @Column(length = 60)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(length = 10)
    private String icon;

    @Column(name = "progress_type")
    private String progressType;

    @Column(name = "progress_target")
    private Integer progressTarget;
}
