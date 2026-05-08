package com.accsaber.backend.repository.item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.accsaber.backend.model.entity.item.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {

    List<Item> findByActiveTrue();

    List<Item> findByActiveTrueAndVisibleTrue();

    List<Item> findByType_IdAndActiveTrue(UUID typeId);

    List<Item> findByType_IdAndActiveTrueAndVisibleTrue(UUID typeId);

    List<Item> findByType_Id(UUID typeId);

    Optional<Item> findByIdAndActiveTrue(UUID id);
}
