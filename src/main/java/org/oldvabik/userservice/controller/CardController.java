package org.oldvabik.userservice.controller;

import jakarta.validation.Valid;
import org.oldvabik.userservice.dto.CardCreateDto;
import org.oldvabik.userservice.dto.CardDto;
import org.oldvabik.userservice.dto.CardUpdateDto;
import org.oldvabik.userservice.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<CardDto> createCard(Authentication auth,
                                              @Valid @RequestBody CardCreateDto dto) {
        CardDto createdCard = cardService.createCard(auth, dto);
        return new ResponseEntity<>(createdCard, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(Authentication auth,
                                               @PathVariable Long id) {
        CardDto card = cardService.getCardById(auth, id);
        return new ResponseEntity<>(card, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCards(@RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "5") Integer size) {
        Page<CardDto> cards = cardService.getAllCards(page, size);
        return new ResponseEntity<>(cards, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(Authentication auth,
                                              @PathVariable Long id,
                                              @Valid @RequestBody CardUpdateDto dto) {
        CardDto updatedCard = cardService.updateCard(auth, id, dto);
        return new ResponseEntity<>(updatedCard, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCard(Authentication auth,
                                           @PathVariable Long id) {
        cardService.deleteCard(auth, id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}