package org.oldvabik.userservice.service;

import org.oldvabik.userservice.dto.CardCreateDto;
import org.oldvabik.userservice.dto.CardDto;
import org.oldvabik.userservice.dto.CardUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;

public interface CardService {
    CardDto createCard(Authentication auth, CardCreateDto dto);

    CardDto getCardById(Authentication auth, Long id);

    Page<CardDto> getAllCards(Integer page, Integer size);

    CardDto updateCard(Authentication auth, Long id, CardUpdateDto dto);

    void deleteCard(Authentication auth, Long id);
}
