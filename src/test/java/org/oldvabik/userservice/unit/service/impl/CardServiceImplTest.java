package org.oldvabik.userservice.unit.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oldvabik.userservice.dto.*;
import org.oldvabik.userservice.entity.*;
import org.oldvabik.userservice.exception.*;
import org.oldvabik.userservice.mapper.*;
import org.oldvabik.userservice.repository.*;
import org.oldvabik.userservice.security.AccessChecker;
import org.oldvabik.userservice.service.impl.CardServiceImpl;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AccessChecker accessChecker;
    @Mock
    private Authentication auth;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void createCard_success() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);
        dto.setNumber("1234");

        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setSurname("Doe");

        Card card = new Card();
        Card saved = new Card();
        saved.setId(1L);
        CardDto cardDto = new CardDto();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(true);
        when(cardRepository.findByNumber("1234")).thenReturn(Optional.empty());
        when(cardMapper.toEntity(dto)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(saved);
        when(cardMapper.toDto(saved)).thenReturn(cardDto);

        CardDto result = cardService.createCard(auth, dto);
        assertNotNull(result);
        verify(cardRepository).save(card);
    }

    @Test
    void createCard_userNotFound() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.createCard(auth, dto));
    }

    @Test
    void createCard_accessDenied() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);

        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.createCard(auth, dto));
    }

    @Test
    void createCard_numberExists() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);
        dto.setNumber("1234");

        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(true);
        when(cardRepository.findByNumber("1234")).thenReturn(Optional.of(new Card()));

        assertThrows(AlreadyExistsException.class, () -> cardService.createCard(auth, dto));
    }

    @Test
    void getCardById_success() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);

        CardDto cardDto = new CardDto();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(true);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getCardById(auth, 1L);
        assertNotNull(result);
    }

    @Test
    void getCardById_notFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.getCardById(auth, 1L));
    }

    @Test
    void getCardById_accessDenied() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.getCardById(auth, 1L));
    }

    @Test
    void getAllCards_returnsList() {
        when(cardRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Card())));
        when(cardMapper.toDto(any(Card.class))).thenReturn(new CardDto());

        var result = cardService.getAllCards(0, 10);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void updateCard_success() {
        Long id = 1L;
        CardUpdateDto dto = new CardUpdateDto();
        dto.setNumber("5678");

        Card card = new Card();
        User user = new User();
        card.setUser(user);
        Card saved = new Card();
        saved.setId(id);
        CardDto cardDto = new CardDto();

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(true);
        when(cardRepository.findByNumber("5678")).thenReturn(Optional.empty());
        when(cardRepository.save(card)).thenReturn(saved);
        when(cardMapper.toDto(saved)).thenReturn(cardDto);

        CardDto result = cardService.updateCard(auth, id, dto);
        assertNotNull(result);
    }

    @Test
    void updateCard_notFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.updateCard(auth, 999L, new CardUpdateDto()));
    }

    @Test
    void updateCard_numberExists() {
        Long id = 1L;
        CardUpdateDto dto = new CardUpdateDto();
        dto.setNumber("1234");

        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(true);
        when(cardRepository.findByNumber("1234")).thenReturn(Optional.of(new Card()));

        assertThrows(AlreadyExistsException.class, () -> cardService.updateCard(auth, id, dto));
    }

    @Test
    void updateCard_accessDenied() {
        Long id = 1L;
        CardUpdateDto dto = new CardUpdateDto();

        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findById(id)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.updateCard(auth, id, dto));
    }

    @Test
    void deleteCard_success() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(true);

        cardService.deleteCard(auth, 1L);
        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_notFound() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.deleteCard(auth, 1L));
    }

    @Test
    void deleteCard_accessDenied() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(auth, new UserDto())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.deleteCard(auth, 1L));
    }
}
