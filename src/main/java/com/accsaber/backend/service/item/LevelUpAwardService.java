package com.accsaber.backend.service.item;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accsaber.backend.model.entity.item.ItemSource;
import com.accsaber.backend.repository.milestone.LevelThresholdRepository;
import com.accsaber.backend.repository.user.UserRepository;
import com.accsaber.backend.service.milestone.LevelService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LevelUpAwardService {

    private final LevelService levelService;
    private final LevelThresholdRepository levelThresholdRepository;
    private final ItemService itemService;
    private final UserRepository userRepository;

    @Transactional
    public void addXp(Long userId, BigDecimal delta) {
        if (delta == null)
            return;
        BigDecimal oldXp = userRepository.findTotalXpById(userId).orElse(BigDecimal.ZERO);
        userRepository.addXp(userId, delta);
        processLevelUps(userId, oldXp, delta);
    }

    @Transactional
    public void processLevelUps(Long userId, BigDecimal oldXp, BigDecimal xpDelta) {
        if (xpDelta == null || xpDelta.signum() <= 0)
            return;
        BigDecimal previous = oldXp == null ? BigDecimal.ZERO : oldXp;
        BigDecimal next = previous.add(xpDelta);

        int oldLevel = levelService.calculateLevel(previous).getLevel();
        int newLevel = levelService.calculateLevel(next).getLevel();
        if (newLevel <= oldLevel)
            return;

        for (int level = oldLevel + 1; level <= newLevel; level++) {
            int crossed = level;
            levelThresholdRepository.findById(crossed)
                    .filter(t -> t.getAwardsItem() != null)
                    .ifPresent(t -> itemService.awardSystem(
                            userId,
                            t.getAwardsItem().getId(),
                            ItemSource.level,
                            String.valueOf(crossed),
                            "Reached level " + crossed));
        }
    }
}
