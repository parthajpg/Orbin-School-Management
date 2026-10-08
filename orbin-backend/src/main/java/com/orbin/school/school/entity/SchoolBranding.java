package com.orbin.school.school.entity;

import com.orbin.school.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Per-school branding and public website customization.
 */
@Entity
@Table(name = "school_branding")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SchoolBranding extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false, unique = true)
    private School school;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(length = 200)
    private String motto;

    @Column(name = "primary_color", length = 10)
    @Builder.Default
    private String primaryColor = "#1E40AF";

    @Column(name = "secondary_color", length = 10)
    @Builder.Default
    private String secondaryColor = "#3B82F6";

    @Column(name = "accent_color", length = 10)
    @Builder.Default
    private String accentColor = "#F59E0B";

    @Column(name = "hero_banner_url", length = 500)
    private String heroBannerUrl;

    @Column(name = "hero_title", length = 200)
    private String heroTitle;

    @Column(name = "hero_subtitle", length = 300)
    private String heroSubtitle;

    @Column(name = "about_text", columnDefinition = "TEXT")
    private String aboutText;

    @Column(name = "admissions_text", columnDefinition = "TEXT")
    private String admissionsText;

    @Column(name = "established_year")
    private Integer establishedYear;

    @Column(name = "affiliation_board", length = 50)
    private String affiliationBoard;

    @Column(name = "whatsapp_number", length = 20)
    private String whatsappNumber;

    @Column(name = "google_maps_embed", columnDefinition = "TEXT")
    private String googleMapsEmbed;
}
