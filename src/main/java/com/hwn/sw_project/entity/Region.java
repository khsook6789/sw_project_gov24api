package com.hwn.sw_project.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "region")
public class Region {

    @Id
    @Column(name = "region_code",length = 10)
    private String regionCode;

    @Column(nullable = false,length = 100)
    private String name;
}
