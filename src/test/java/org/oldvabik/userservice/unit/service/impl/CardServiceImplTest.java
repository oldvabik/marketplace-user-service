package org.oldvabik.userservice.unit.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oldvabik.userservice.dto.*;
import org.oldvabik.userservice.entity.Card;
import org.oldvabik.userservice.entity.User;
import org.oldvabik.userservice.exception.AlreadyExistsException;
import org.oldvabik.userservice.exception.NotFoundException;
import org.oldvabik.userservice.mapper.CardMapper;
import org.oldvabik.userservice.mapper.UserMapper;
import org.oldvabik.userservice.repository.CardRepository;
import org.oldvabik.userservice.repository.UserRepository;
import org.oldvabik.userservice.security.AccessChecker;
import org.oldvabik.userservice.service.impl.CardServiceImpl;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

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
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

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

        Card cardFromMapper = new Card();
        Card savedCard = new Card();
        savedCard.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(true);
        when(cardRepository.findByNumber("1234")).thenReturn(Optional.empty());
        when(cardMapper.toEntity(dto)).thenReturn(cardFromMapper);
        when(cardRepository.save(cardFromMapper)).thenReturn(savedCard);
        when(cardMapper.toDto(savedCard)).thenReturn(new CardDto());
        when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());

        assertNotNull(cardService.createCard(auth, dto));

        verify(cardRepository).save(cardFromMapper);
        verify(redisTemplate, atLeastOnce()).keys(anyString());
    }

    @Test
    void createCard_userNotFound() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.createCard(auth, dto));
    }

    @Test
    void createCard_accessDenied() {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(1L);

        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(false);

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
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(true);
        when(cardRepository.findByNumber("1234")).thenReturn(Optional.of(new Card()));

        assertThrows(AlreadyExistsException.class, () -> cardService.createCard(auth, dto));
    }

    @Test
    void getCardById_success() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);
        CardDto cardDto = new CardDto();

        when(cardRepository.findByIdWithUserWithCards(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(true);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getCardById(auth, 1L);
        assertNotNull(result);
    }

    @Test
    void getCardById_notFound() {
        when(cardRepository.findByIdWithUserWithCards(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.getCardById(auth, 1L));
    }

    @Test
    void getCardById_accessDenied() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findByIdWithUserWithCards(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(false);

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
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(true);
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
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(true);
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
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.updateCard(auth, id, dto));
    }

    @Test
    void deleteCard_success() {
        Card card = new Card();
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        card.setUser(user);

        when(cardRepository.findByIdWithUserWithCards(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(true);
        when(redisTemplate.keys(anyString())).thenReturn(Collections.emptySet());

        cardService.deleteCard(auth, 1L);

        verify(cardRepository).delete(card);

        verify(redisTemplate, atLeastOnce()).keys(anyString());
    }

    @Test
    void deleteCard_notFound() {
        when(cardRepository.findByIdWithUserWithCards(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cardService.deleteCard(auth, 1L));
    }

    @Test
    void deleteCard_accessDenied() {
        Card card = new Card();
        User user = new User();
        card.setUser(user);

        when(cardRepository.findByIdWithUserWithCards(1L)).thenReturn(Optional.of(card));
        when(userMapper.toDto(user)).thenReturn(new UserDto());
        when(accessChecker.canAccessUser(eq(auth), any(UserDto.class))).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> cardService.deleteCard(auth, 1L));
    }
}