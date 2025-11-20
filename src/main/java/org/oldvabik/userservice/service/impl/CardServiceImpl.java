package org.oldvabik.userservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.oldvabik.userservice.dto.CardCreateDto;
import org.oldvabik.userservice.dto.CardDto;
import org.oldvabik.userservice.dto.CardUpdateDto;
import org.oldvabik.userservice.entity.Card;
import org.oldvabik.userservice.entity.User;
import org.oldvabik.userservice.exception.AlreadyExistsException;
import org.oldvabik.userservice.exception.NotFoundException;
import org.oldvabik.userservice.mapper.CardMapper;
import org.oldvabik.userservice.mapper.UserMapper;
import org.oldvabik.userservice.repository.CardRepository;
import org.oldvabik.userservice.repository.UserRepository;
import org.oldvabik.userservice.security.AccessChecker;
import org.oldvabik.userservice.service.CardService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Slf4j
@Service
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final UserMapper userMapper;
    private final AccessChecker accessChecker;
    private final RedisTemplate<String, Object> redisTemplate;

    public CardServiceImpl(CardRepository cardRepository,
                           UserRepository userRepository,
                           CardMapper cardMapper,
                           UserMapper userMapper,
                           AccessChecker accessChecker,
                           RedisTemplate<String, Object> redisTemplate) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.userMapper = userMapper;
        this.accessChecker = accessChecker;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @Transactional
    public CardDto createCard(Authentication auth, CardCreateDto dto) {
        log.info("[CardService] createCard: userId={}, number={}", dto.getUserId(), dto.getNumber());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.warn("[CardService] createCard: user not found userId={}", dto.getUserId());
                    return new NotFoundException("user with id " + dto.getUserId() + " not found");
                });

        if (!accessChecker.canAccessUser(auth, userMapper.toDto(user))) {
            log.warn("[CardService] createCard: access denied for user {}", auth.getName());
            throw new AccessDeniedException("Access denied");
        }

        cardRepository.findByNumber(dto.getNumber()).ifPresent(c -> {
            log.warn("[CardService] createCard: card number={} already exists", dto.getNumber());
            throw new AlreadyExistsException("card with number " + dto.getNumber() + " already exists");
        });

        Card card = cardMapper.toEntity(dto);
        card.setUser(user);
        card.setHolder(user.getName() + " " + user.getSurname());
        Card saved = cardRepository.save(card);

        log.info("[CardService] createCard: created id={}", saved.getId());

        evictUserCacheCompletely(user.getId(), user.getEmail());

        return cardMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "cards", key = "#id + '_' + #auth.name")
    public CardDto getCardById(Authentication auth, Long id) {
        log.debug("[CardService] getCardById: id={}", id);

        Card card = cardRepository.findByIdWithUserWithCards(id)
                .orElseThrow(() -> {
                    log.warn("[CardService] getCardById: not found id={}", id);
                    return new NotFoundException("card with id " + id + " not found");
                });

        if (!accessChecker.canAccessUser(auth, userMapper.toDto(card.getUser()))) {
            log.warn("[CardService] getCardById: access denied for user {}", auth.getName());
            throw new AccessDeniedException("Access denied");
        }

        log.info("[CardService] getCardById: found id={}", id);
        return cardMapper.toDto(card);
    }

    @Override
    public Page<CardDto> getAllCards(Integer page, Integer size) {
        log.debug("[CardService] getAllCards: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards = cardRepository.findAll(pageable);

        log.info("[CardService] getAllCards: fetched {} cards", cards.getContent().size());
        return cards.map(cardMapper::toDto);
    }

    @Override
    @Transactional
    @CachePut(value = "cards", key = "#id + '_' + #auth.name")
    public CardDto updateCard(Authentication auth, Long id, CardUpdateDto dto) {
        log.info("[CardService] updateCard: id={}", id);

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[CardService] updateCard: not found id={}", id);
                    return new NotFoundException("card with id " + id + " not found");
                });

        if (!accessChecker.canAccessUser(auth, userMapper.toDto(card.getUser()))) {
            log.warn("[CardService] updateCard: access denied for user {}", auth.getName());
            throw new AccessDeniedException("Access denied");
        }

        if (dto.getNumber() != null && !dto.getNumber().equals(card.getNumber())) {
            cardRepository.findByNumber(dto.getNumber()).ifPresent(c -> {
                log.warn("[CardService] updateCard: number={} already exists", dto.getNumber());
                throw new AlreadyExistsException("card with number " + dto.getNumber() + " already exists");
            });
        }

        cardMapper.updateEntityFromDto(dto, card);
        Card saved = cardRepository.save(card);

        log.info("[CardService] updateCard: updated id={}", saved.getId());
        return cardMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteCard(Authentication auth, Long id) {
        log.info("[CardService] deleteCard: id={}", id);
        Card card = cardRepository.findByIdWithUserWithCards(id)
                .orElseThrow(() -> new NotFoundException("card with id " + id + " not found"));

        User user = card.getUser();

        if (!accessChecker.canAccessUser(auth, userMapper.toDto(user))) {
            throw new AccessDeniedException("Access denied");
        }

        cardRepository.delete(card);

//        user.removeCard(card);
//        userRepository.save(user);

        log.info("[CardService] deleteCard: deleted id={}", id);

        deleteCardCache(id);
        evictUserCacheCompletely(user.getId(), user.getEmail());
    }


    private void deleteCardCache(Long cardId) {
        String pattern = cardId + "_*";
        Set<String> keys = redisTemplate.keys("cards::" + pattern);
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[CardService] deleteCardCache: deleted {} cache keys for card id={}", keys.size(), cardId);
        }
    }

    private void evictUserCacheCompletely(Long userId, String email) {
        Set<String> keysId = redisTemplate.keys("users::" + userId + "_*");
        if (!keysId.isEmpty()) {
            redisTemplate.delete(keysId);
            log.info("Evicted {} keys for user id={}", keysId.size(), userId);
        }

        Set<String> keysEmail = redisTemplate.keys("users::" + email + "_*");
        if (!keysEmail.isEmpty()) {
            redisTemplate.delete(keysEmail);
            log.info("Evicted {} keys for user email={}", keysEmail.size(), email);
        }
    }
}
