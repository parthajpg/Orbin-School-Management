package com.orbin.school.school.service;

import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.dto.SchoolDto;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.entity.SchoolBranding;
import com.orbin.school.school.entity.SchoolModule;
import com.orbin.school.school.repository.SchoolModuleRepository;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolModuleRepository schoolModuleRepository;

    @Transactional(readOnly = true)
    public List<SchoolDto> getAllActiveSchools() {
        return schoolRepository.findAll().stream()
                .filter(s -> s.getStatus() == School.SchoolStatus.ACTIVE)
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SchoolDto getSchoolById(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", id));
        return toDto(school);
    }

    @Transactional(readOnly = true)
    public SchoolDto getCurrentSchool() {
        Long schoolId = TenantContext.getSchoolId();
        if (schoolId == null) {
            // If no school context, return the first active school or throw
            List<School> schools = schoolRepository.findAll();
            if (schools.isEmpty()) {
                throw new ResourceNotFoundException("School", "tenant", "No schools configured");
            }
            return toDto(schools.get(0));
        }
        return getSchoolById(schoolId);
    }

    private SchoolDto toDto(School school) {
        SchoolBranding b = school.getBranding();
        SchoolDto.SchoolBrandingDto brandingDto = new SchoolDto.SchoolBrandingDto(
                b != null ? b.getPrimaryColor() : "#2563eb",
                b != null ? b.getSecondaryColor() : "#1d4ed8",
                b != null ? b.getAccentColor() : "#38bdf8",
                b != null ? b.getLogoUrl() : null,
                b != null ? b.getFaviconUrl() : null
        );

        List<SchoolModule> modules = schoolModuleRepository.findBySchoolId(school.getId());
        Map<String, Boolean> moduleMap = new HashMap<>();
        // Default standard modules
        moduleMap.put("attendance", true);
        moduleMap.put("fees", true);
        moduleMap.put("syllabus", true);
        moduleMap.put("exams", true);
        moduleMap.put("whatsapp", true);
        moduleMap.put("cms", true);

        for (SchoolModule m : modules) {
            moduleMap.put(m.getModuleName(), m.isEnabled());
        }

        return new SchoolDto(
                String.valueOf(school.getId()),
                school.getName(),
                school.getShortName(),
                school.getSlug(),
                b != null && b.getAffiliationBoard() != null ? b.getAffiliationBoard() : "CBSE",
                school.getCity(),
                b != null ? b.getMotto() : null,
                school.getPhone(),
                school.getEmail(),
                school.getAddress(),
                brandingDto,
                moduleMap
        );
    }
}
