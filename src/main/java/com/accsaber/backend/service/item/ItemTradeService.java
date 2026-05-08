package com.accsaber.backend.service.item;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accsaber.backend.exception.ResourceNotFoundException;
import com.accsaber.backend.exception.ValidationException;
import com.accsaber.backend.model.entity.item.TradeStatus;
import com.accsaber.backend.model.entity.item.UserItemLink;
import com.accsaber.backend.model.entity.item.UserItemTrade;
import com.accsaber.backend.repository.item.UserItemLinkRepository;
import com.accsaber.backend.repository.item.UserItemTradeRepository;
import com.accsaber.backend.repository.user.UserRepository;
import com.accsaber.backend.service.player.DuplicateUserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemTradeService {

    private final UserItemTradeRepository tradeRepository;
    private final UserItemLinkRepository userItemLinkRepository;
    private final UserRepository userRepository;
    private final DuplicateUserService duplicateUserService;
    private final ItemService itemService;

    public List<UserItemTrade> listIncomingPending(Long toUserId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(toUserId);
        return tradeRepository.findByToUser_IdAndStatus(resolved, TradeStatus.pending);
    }

    public List<UserItemTrade> listOutgoingPending(Long fromUserId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(fromUserId);
        return tradeRepository.findByFromUser_IdAndStatus(resolved, TradeStatus.pending);
    }

    public Page<UserItemTrade> listForUser(Long userId, String direction, Collection<TradeStatus> statuses,
            Pageable pageable) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        boolean incoming = direction == null || direction.equalsIgnoreCase("incoming")
                || direction.equalsIgnoreCase("both");
        boolean outgoing = direction == null || direction.equalsIgnoreCase("outgoing")
                || direction.equalsIgnoreCase("both");
        Collection<TradeStatus> effective = (statuses == null || statuses.isEmpty())
                ? Arrays.asList(TradeStatus.values())
                : statuses;
        return tradeRepository.findByDirectionAndStatusIn(resolved, incoming, outgoing, effective, pageable);
    }

    public UserItemTrade findById(UUID id) {
        return tradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserItemTrade", id));
    }

    @Transactional
    public UserItemTrade create(Long fromUserId, Long toUserId, UUID userItemLinkId, String message) {
        Long resolvedFrom = duplicateUserService.resolvePrimaryUserId(fromUserId);
        Long resolvedTo = duplicateUserService.resolvePrimaryUserId(toUserId);

        if (resolvedFrom.equals(resolvedTo)) {
            throw new ValidationException("toUserId", "cannot trade with yourself");
        }
        if (!userRepository.existsById(resolvedTo)) {
            throw new ResourceNotFoundException("User", toUserId);
        }

        UserItemLink link = userItemLinkRepository.findByIdAndUser_Id(userItemLinkId, resolvedFrom)
                .orElseThrow(() -> new ValidationException("userItemLinkId",
                        "you do not own this item or it does not exist"));

        if (!link.getItem().isTradeable()) {
            throw new ValidationException("userItemLinkId", "this item is not tradeable");
        }
        if (tradeRepository.findByUserItemLink_IdAndStatus(userItemLinkId, TradeStatus.pending).isPresent()) {
            throw new ValidationException("userItemLinkId", "a pending trade already exists for this item");
        }

        UserItemTrade trade = UserItemTrade.builder()
                .fromUser(userRepository.getReferenceById(resolvedFrom))
                .toUser(userRepository.getReferenceById(resolvedTo))
                .userItemLink(link)
                .status(TradeStatus.pending)
                .message(message)
                .build();
        return tradeRepository.save(trade);
    }

    @Transactional
    public UserItemTrade accept(UUID tradeId, Long actingUserId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(actingUserId);
        UserItemTrade trade = findById(tradeId);
        ensurePending(trade);
        if (!trade.getToUser().getId().equals(resolved)) {
            throw new ValidationException("tradeId", "only the recipient can accept this trade");
        }

        Long senderId = trade.getFromUser().getId();
        UserItemLink link = trade.getUserItemLink();
        link.setUser(userRepository.getReferenceById(resolved));
        userItemLinkRepository.saveAndFlush(link);
        itemService.clearEquippedIfNoLongerOwned(senderId, link.getItem());

        trade.setStatus(TradeStatus.accepted);
        trade.setResolvedAt(Instant.now());
        return tradeRepository.save(trade);
    }

    @Transactional
    public UserItemTrade decline(UUID tradeId, Long actingUserId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(actingUserId);
        UserItemTrade trade = findById(tradeId);
        ensurePending(trade);
        if (!trade.getToUser().getId().equals(resolved)) {
            throw new ValidationException("tradeId", "only the recipient can decline this trade");
        }
        trade.setStatus(TradeStatus.declined);
        trade.setResolvedAt(Instant.now());
        return tradeRepository.save(trade);
    }

    @Transactional
    public UserItemTrade cancel(UUID tradeId, Long actingUserId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(actingUserId);
        UserItemTrade trade = findById(tradeId);
        ensurePending(trade);
        if (!trade.getFromUser().getId().equals(resolved)) {
            throw new ValidationException("tradeId", "only the sender can cancel this trade");
        }
        trade.setStatus(TradeStatus.cancelled);
        trade.setResolvedAt(Instant.now());
        return tradeRepository.save(trade);
    }

    @Transactional
    public int expireOlderThan(Instant cutoff) {
        return tradeRepository.expirePending(cutoff, Instant.now());
    }

    private void ensurePending(UserItemTrade trade) {
        if (trade.getStatus() != TradeStatus.pending) {
            throw new ValidationException("tradeId", "trade is not pending (status: " + trade.getStatus() + ")");
        }
    }
}
