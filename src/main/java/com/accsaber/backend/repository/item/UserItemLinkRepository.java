package com.accsaber.backend.repository.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.accsaber.backend.model.entity.item.ItemSource;
import com.accsaber.backend.model.entity.item.UserItemLink;

@Repository
public interface UserItemLinkRepository extends JpaRepository<UserItemLink, UUID> {

        List<UserItemLink> findByUser_Id(Long userId);

        List<UserItemLink> findByUser_IdAndItem_Type_Key(Long userId, String typeKey);

        Page<UserItemLink> findByUser_Id(Long userId, Pageable pageable);

        Page<UserItemLink> findByUser_IdAndItem_Type_Key(Long userId, String typeKey, Pageable pageable);

        Optional<UserItemLink> findByIdAndUser_Id(UUID id, Long userId);

        boolean existsByUser_IdAndItem_Id(Long userId, UUID itemId);

        boolean existsByUser_IdAndItem_IdAndSourceAndSourceId(Long userId, UUID itemId, ItemSource source,
                        String sourceId);

        @org.springframework.data.jpa.repository.Modifying
        @org.springframework.data.jpa.repository.Query("UPDATE UserItemLink l SET l.modifier = :modifier WHERE l.item.id = :itemId")
        int reassignModifierByItem(
                        @org.springframework.data.repository.query.Param("itemId") UUID itemId,
                        @org.springframework.data.repository.query.Param("modifier") com.accsaber.backend.model.entity.item.ItemModifier modifier);
}
