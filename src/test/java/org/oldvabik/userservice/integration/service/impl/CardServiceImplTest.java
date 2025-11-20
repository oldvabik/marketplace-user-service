package org.oldvabik.userservice.integration.service.impl;

import org.junit.jupiter.api.Test;
import org.oldvabik.userservice.dto.*;
import org.oldvabik.userservice.exception.AlreadyExistsException;
import org.oldvabik.userservice.exception.NotFoundException;
import org.oldvabik.userservice.repository.CardRepository;
import org.oldvabik.userservice.service.CardService;
import org.oldvabik.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CardServiceImplTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("marketplace")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CardService cardService;

    @Autowired
    private UserService userService;

    @Autowired
    private CardRepository cardRepository;

    private final Authentication auth = mock(Authentication.class);

    {
        when(auth.getName()).thenReturn("testuser@example.com");
    }

    private UserDto createTestUser() {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("testuser@example.com");
        dto.setName("Test");
        dto.setSurname("User");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        return userService.createUser(dto);
    }

    private CardCreateDto cardCreateDto(Long userId, String number) {
        CardCreateDto dto = new CardCreateDto();
        dto.setUserId(userId);
        dto.setNumber(number);
        dto.setExpirationDate(LocalDate.now().plusYears(3));
        return dto;
    }

    private CardUpdateDto cardUpdateDto(String number) {
        CardUpdateDto dto = new CardUpdateDto();
        dto.setNumber(number);
        dto.setExpirationDate(LocalDate.now().plusYears(4));
        return dto;
    }

    @Test
    void createCard_success() {
        UserDto user = createTestUser();
        CardCreateDto dto = cardCreateDto(user.getId(), "1234567890123456");
        CardDto created = cardService.createCard(auth, dto);
        assertNotNull(created.getId());
        assertEquals("1234567890123456", created.getNumber());
        assertEquals("Test User", created.getHolder());
        assertNotNull(created.getExpirationDate());
    }

    @Test
    void createCard_userNotFound() {
        CardCreateDto dto = cardCreateDto(999L, "1234567890123456");
        assertThrows(NotFoundException.class, () -> cardService.createCard(auth, dto));
    }

    @Test
    void createCard_numberExists() {
        UserDto user = createTestUser();
        cardService.createCard(auth, cardCreateDto(user.getId(), "duplicate"));
        CardCreateDto dto2 = cardCreateDto(user.getId(), "duplicate");
        assertThrows(AlreadyExistsException.class, () -> cardService.createCard(auth, dto2));
    }

    @Test
    void getCardById_found() {
        UserDto user = createTestUser();
        CardDto created = cardService.createCard(auth, cardCreateDto(user.getId(), "get123"));
        CardDto fetched = cardService.getCardById(auth, created.getId());
        assertEquals("get123", fetched.getNumber());
    }

    @Test
    void getCardById_notFound() {
        assertThrows(NotFoundException.class, () -> cardService.getCardById(auth, 999L));
    }

    @Test
    void getAllCards_returnsList() {
        UserDto user = createTestUser();
        cardService.createCard(auth, cardCreateDto(user.getId(), "card1"));
        cardService.createCard(auth, cardCreateDto(user.getId(), "card2"));
        Page<CardDto> page = cardService.getAllCards(0, 10);
        assertEquals(2, page.getContent().size());
    }

    @Test
    void updateCard_success() {
        UserDto user = createTestUser();
        CardDto created = cardService.createCard(auth, cardCreateDto(user.getId(), "oldnum"));
        CardDto updated = cardService.updateCard(auth, created.getId(), cardUpdateDto("newnum"));
        assertEquals("newnum", updated.getNumber());
    }

    @Test
    void updateCard_notFound() {
        assertThrows(NotFoundException.class, () -> cardService.updateCard(auth, 999L, cardUpdateDto("any")));
    }

    @Test
    void updateCard_numberExists() {
        UserDto user = createTestUser();
        cardService.createCard(auth, cardCreateDto(user.getId(), "existing"));
        CardDto toUpdate = cardService.createCard(auth, cardCreateDto(user.getId(), "toupdate"));
        CardUpdateDto dto = cardUpdateDto("existing");
        assertThrows(AlreadyExistsException.class, () -> cardService.updateCard(auth, toUpdate.getId(), dto));
    }

    @Test
    void deleteCard_success() {
        UserDto user = createTestUser();
        CardDto created = cardService.createCard(auth, cardCreateDto(user.getId(), "todelete"));
        cardService.deleteCard(auth, created.getId());

        assertThrows(NotFoundException.class, () -> cardService.getCardById(auth, created.getId()));
    }


    @Test
    void deleteCard_notFound() {
        assertThrows(NotFoundException.class, () -> cardService.deleteCard(auth, 999L));
    }
}