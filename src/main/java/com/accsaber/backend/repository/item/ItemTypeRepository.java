package com.accsaber.backend.repository.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.accsaber.backend.model.entity.item.ItemType;

@Repository
public interface ItemTypeRepository extends JpaRepository<ItemType, UUID> {

    List<ItemType> findByActiveTrue();

    Optional<ItemType> findByIdAndActiveTrue(UUID id);

    Optional<ItemType> findByKey(String key);

    boolean existsByKey(String key);
}
