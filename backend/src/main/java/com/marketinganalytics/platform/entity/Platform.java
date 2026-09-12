package com.marketinganalytics.platform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Advertising/publishing channel (Instagram, Facebook, Google Ads, ...).
 * Modeled as an entity (rather than an enum) so new channels can be added
 * without a code deploy once real ad-platform integrations are wired in.
 */
@Entity
@Table(name = "platforms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(name = "color_hex", length = 7)
    private String colorHex;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
