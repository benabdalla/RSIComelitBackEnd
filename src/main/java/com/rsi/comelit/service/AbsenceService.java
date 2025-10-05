package com.rsi.comelit.service;
import com.rsi.comelit.dto.AbsencePageResponseDto;
import com.rsi.comelit.dto.AbsenceRequestDto;
import com.rsi.comelit.dto.AbsenceResponseDto;
import com.rsi.comelit.dto.JustificationUpdateDto;
import com.rsi.comelit.entity.Absence;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.enumeration.NotificationType;
import com.rsi.comelit.exception.DuplicateAbsenceException;
import com.rsi.comelit.exception.ResourceNotFoundException;
import com.rsi.comelit.repository.AbsenceRepository;
import com.rsi.comelit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AbsenceService {

    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * Create a new absence
     */


    public AbsenceResponseDto createAbsence(AbsenceRequestDto request) {
        try {
            log.info("Creating absence for user {} on date {}", request.getUserId(), request.getDate());

            // Find user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

            // Check if absence already exists
            if (absenceRepository.existsByUserIdAndDate(request.getUserId(), request.getDate())) {
                throw new DuplicateAbsenceException("Absence already recorded for user " + user.getFirstName() + " " + user.getLastName() + " on date " + request.getDate());
            }

            // Create absence entity
            Absence absence = new Absence();
            absence.setUser(user);
            absence.setDate(request.getDate());

            // Handle justification - IGNORE "Not Justified" text
            String justificationText = request.getJustificationText();

            if (isValidJustification(justificationText)) {
                absence.setJustificationText(justificationText);
                absence.setStatus(Absence.AbsenceStatus.JUSTIFIED);
            } else {
                absence.setJustificationText(null);
                absence.setStatus(Absence.AbsenceStatus.NOT_JUSTIFIED);
            }

            // Handle justification file
            String justificationFile = request.getJustificationFile();
            if (isValidJustificationFile(justificationFile)) {
                absence.setJustificationFile(justificationFile);
                if (absence.getStatus() == Absence.AbsenceStatus.NOT_JUSTIFIED) {
                    absence.setStatus(Absence.AbsenceStatus.JUSTIFIED);
                }
            }

            log.info("Saving absence with status: {}", absence.getStatus());
            Absence savedAbsence = absenceRepository.save(absence);
            log.info("Absence created successfully with id: {}", savedAbsence.getId());
            notificationService.sendNotification(
                    user,
                    userService.getAuthenticatedUser(),
                    null,
                    null,
                    NotificationType.NOT_JUSTIFIED_ABSENCE
            );
            return convertToDto(savedAbsence);

        } catch (Exception e) {
            log.error("Error creating absence: ", e);
            throw new RuntimeException("Failed to create absence", e);
        }
    }

    // Helper method to validate justification text
    private boolean isValidJustification(String justificationText) {
        return justificationText != null &&
                !justificationText.trim().isEmpty() &&
                !"Not Justified".equalsIgnoreCase(justificationText.trim()) &&
                !"null".equalsIgnoreCase(justificationText.trim());
    }

    // Helper method to validate justification file
    private boolean isValidJustificationFile(String justificationFile) {
        return justificationFile != null &&
                !justificationFile.trim().isEmpty() &&
                !"null".equalsIgnoreCase(justificationFile.trim());
    }
//public AbsenceResponseDto createAbsence(AbsenceRequestDto request) {
//    log.info("Creating absence for user {} on date {}", request.getUserId(), request.getDate());
//
//    // Check if user exists
//    User user = userRepository.findById(request.getUserId())
//            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));
//
//    // Check if absence already exists for this date
//    if (absenceRepository.existsByUserIdAndDate(request.getUserId(), request.getDate())) {
//        throw new DuplicateAbsenceException("Absence already recorded for user " + request.getUserId() + " on date " + request.getDate());
//    }
//
//    // Create absence entity
//    Absence absence = new Absence();
//    absence.setUser(user); // Set the User object instead of userId
//    absence.setDate(request.getDate());
//    absence.setJustificationText(request.getJustificationText());
//    absence.setJustificationFile(request.getJustificationFile());
//
//    // Set status based on justification
//    if (request.getJustificationText() != null || request.getJustificationFile() != null) {
//        absence.setStatus(Absence.AbsenceStatus.JUSTIFIED);
//    } else {
//        absence.setStatus(Absence.AbsenceStatus.NOT_JUSTIFIED);
//    }
//
//    Absence savedAbsence = absenceRepository.save(absence);
//    log.info("Absence created successfully with id: {}", savedAbsence.getId());
//
//    return convertToDto(savedAbsence, user);
//}

    public AbsenceResponseDto createTodayAbsence(Long userId) {
        AbsenceRequestDto request = new AbsenceRequestDto();
        request.setUserId(userId);
        request.setDate(LocalDate.now());
        return createAbsence(request);
    }

    /**
     * Get all absences with pagination and filters
     */
    @Transactional(readOnly = true)
    public AbsencePageResponseDto getAbsences(int page, int limit, String search, String status, LocalDate date) {
        log.info("Fetching absences - page: {}, limit: {}, search: {}, status: {}, date: {}",
                page, limit, search, status, date);

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("date").descending().and(Sort.by("createdAt").descending()));

        Absence.AbsenceStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Absence.AbsenceStatus.valueOf(status.toUpperCase().replace(" ", "_"));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
            }
        }

        Page<Absence> absencePage = absenceRepository.findAbsencesWithFilters(search, statusEnum, date, pageable);

        List<AbsenceResponseDto> absenceDtos = absencePage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new AbsencePageResponseDto(
                absenceDtos,
                absencePage.getTotalElements(),
                page,
                limit,
                absencePage.getTotalPages()
        );
    }

    /**
     * Get all absences with pagination and filters
     */
    @Transactional(readOnly = true)
    public AbsencePageResponseDto getMyAbsences(int page, int limit) {
        log.info("Fetching absences - page: {}, limit: {}",
                page, limit);

        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Absence> absencePage = absenceRepository.findByStatusAndUser(Absence.AbsenceStatus.NOT_JUSTIFIED, userService.getAuthenticatedUser(), pageable);

        List<AbsenceResponseDto> absenceDtos = absencePage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new AbsencePageResponseDto(
                absenceDtos,
                absencePage.getTotalElements(),
                page,
                limit,
                absencePage.getTotalPages()
        );
    }

    /**
     * Get today's absences
     */
    @Transactional(readOnly = true)
    public List<AbsenceResponseDto> getTodayAbsences() {
        LocalDate today = LocalDate.now();
        List<Absence> absences = absenceRepository.findByDate(today);

        return absences.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get absence by ID
     */
    @Transactional(readOnly = true)
    public AbsenceResponseDto getAbsenceById(Long id) {
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + id));

        return convertToDto(absence);
    }

    /**
     * Update absence justification
     */
    public AbsenceResponseDto updateJustification(Long id, JustificationUpdateDto request) {
        log.info("Updating justification for absence id: {}", id);

        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + id));

        if (request.getJustificationText() != null) {
            absence.setJustificationText(request.getJustificationText());
        }
        if (request.getJustificationFile() != null) {
            absence.setJustificationFile(request.getJustificationFile());
        }

        // Update status to justified if justification is provided
        if (absence.getJustificationText() != null || absence.getJustificationFile() != null) {
            absence.setStatus(Absence.AbsenceStatus.JUSTIFIED);
        }

        Absence updatedAbsence = absenceRepository.save(absence);
        log.info("Justification updated successfully for absence id: {}", id);

        return convertToDto(updatedAbsence);
    }

    /**
     * Validate absence (change status to Valid)
     */
    public AbsenceResponseDto validateAbsence(Long id) {
        log.info("Validating absence id: {}", id);

        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Absence not found with id: " + id));

        if (absence.getStatus() != Absence.AbsenceStatus.JUSTIFIED) {
            throw new IllegalStateException("Only justified absences can be validated");
        }

        absence.setStatus(Absence.AbsenceStatus.VALID);
        Absence validatedAbsence = absenceRepository.save(absence);

        log.info("Absence validated successfully with id: {}", id);
        return convertToDto(validatedAbsence);
    }

    /**
     * Delete absence
     */
    public void deleteAbsence(Long id) {
        log.info("Deleting absence id: {}", id);

        if (!absenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Absence not found with id: " + id);
        }

        absenceRepository.deleteById(id);
        log.info("Absence deleted successfully with id: {}", id);
    }

    /**
     * Convert Absence entity to DTO
     */
private AbsenceResponseDto convertToDto(Absence absence) {
    User user = absence.getUser();
    return convertToDto(absence, user);
}
    /**
     * Convert Absence entity to DTO with user info
     */
    private AbsenceResponseDto convertToDto(Absence absence, User user) {
        AbsenceResponseDto.UserDto userDto = null;
        if (user != null) {
            userDto = new AbsenceResponseDto.UserDto();
            userDto.setId(user.getId());
            userDto.setFirstName(user.getFirstName());
            userDto.setLastName(user.getLastName());
            userDto.setEmail(user.getEmail());
            userDto.setDepartment(user.getSite());
        }

        return AbsenceResponseDto.fromEntity(absence, userDto);
    }
}