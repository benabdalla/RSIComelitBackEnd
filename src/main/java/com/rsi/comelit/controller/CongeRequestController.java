package com.rsi.comelit.controller;

import com.rsi.comelit.dto.CongeRequestDTO;
import com.rsi.comelit.dto.CreateCongeRequestDTO;
import com.rsi.comelit.entity.CongeRequest;
import com.rsi.comelit.entity.CongeRequestStatus;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.mapper.CongeRequestMapper;
import com.rsi.comelit.service.CongeRequestService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/conge-requests")
public class CongeRequestController {
    private final CongeRequestService service;
    private final CongeRequestMapper mapper;

    public CongeRequestController(CongeRequestService service, CongeRequestMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    private static CongeRequest getCongeRequest(CreateCongeRequestDTO dto) {
        CongeRequest entity = new CongeRequest();
        User requester = new User();
        requester.setId(dto.getRequesterId());
        entity.setRequester(requester);
        User validator = new User();
        validator.setId(dto.getValidatorId());
        entity.setValidator(validator);
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setReason(dto.getReason());
        entity.setStatus(CongeRequestStatus.DRAFT);
        return entity;
    }

    @PostMapping("/draft")
    public CongeRequestDTO createDraft(@RequestBody CreateCongeRequestDTO dto) {
        CongeRequest entity = getCongeRequest(dto);
        CongeRequest saved = service.createDraft(entity);
        return mapper.toDto(saved);
    }

    @PutMapping("/draft/{id}")
    public CongeRequestDTO updateDraft(@PathVariable Long id, @RequestBody CreateCongeRequestDTO dto) {
        CongeRequest entity = getCongeRequest(dto);
        entity.setId(id);
        CongeRequest updated = service.updateDraft(id, entity);
        return mapper.toDto(updated);
    }

    @PostMapping("/send/{id}")
    public CongeRequestDTO sendRequest(@PathVariable Long id) {
        CongeRequest sent = service.sendRequest(id);
        return mapper.toDto(sent);
    }

    @PostMapping("/validate/{id}")
    public CongeRequestDTO validateRequest(@PathVariable Long id, @RequestParam Long validatorId, @RequestParam(required = false) String comment) {
        User validator = new User();
        validator.setId(validatorId);
        CongeRequest validated = service.validateRequest(id, validator, comment);
        return mapper.toDto(validated);
    }

    @PostMapping("/reject/{id}")
    public CongeRequestDTO rejectRequest(@PathVariable Long id, @RequestParam Long validatorId, @RequestParam(required = false) String comment) {
        User validator = new User();
        validator.setId(validatorId);
        CongeRequest rejected = service.rejectRequest(id, validator, comment);
        return mapper.toDto(rejected);
    }

    @GetMapping("/mine")
    public List<CongeRequestDTO> getMyRequests(@RequestParam Long requesterId) {
        User requester = new User();
        requester.setId(requesterId);
        return service.getMyRequests(requester).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/to-validate")
    public List<CongeRequestDTO> getRequestsToValidate(@RequestParam Long validatorId) {
        User validator = new User();
        validator.setId(validatorId);
        return service.getRequestsToValidate(validator).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}
