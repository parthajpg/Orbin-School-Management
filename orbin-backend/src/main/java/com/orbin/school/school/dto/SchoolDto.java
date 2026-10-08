package com.orbin.school.school.dto;

import java.util.Map;

public record SchoolDto(
    String id,
    String name,
    String shortName,
    String slug,
    String board,
    String city,
    String motto,
    String phone,
    String email,
    String address,
    SchoolBrandingDto branding,
    Map<String, Boolean> activeModules
) {
    public record SchoolBrandingDto(
        String primaryColor,
        String secondaryColor,
        String accentColor,
        String logoUrl,
        String faviconUrl
    ) {}
}
