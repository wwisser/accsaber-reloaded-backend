package com.accsaber.backend.service.item;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.accsaber.backend.exception.ResourceNotFoundException;
import com.accsaber.backend.exception.ValidationException;
import com.accsaber.backend.model.dto.response.item.ItemResponse;
import com.accsaber.backend.model.entity.item.Item;
import com.accsaber.backend.model.entity.item.ItemSource;
import com.accsaber.backend.model.entity.item.ItemType;
import com.accsaber.backend.model.entity.item.UserItemLink;
import com.accsaber.backend.model.entity.staff.StaffUser;
import com.accsaber.backend.model.entity.user.UserSettingKey;
import com.accsaber.backend.repository.item.ItemRepository;
import com.accsaber.backend.repository.item.UserItemLinkRepository;
import com.accsaber.backend.repository.user.UserRepository;
import com.accsaber.backend.service.player.DuplicateUserService;
import com.accsaber.backend.service.player.UserSettingsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ItemRepository itemRepository;
    private final UserItemLinkRepository userItemLinkRepository;
    private final UserRepository userRepository;
    private final DuplicateUserService duplicateUserService;
    private final ItemTypeService itemTypeService;
    private final UserSettingsService userSettingsService;

    public List<Item> findAllVisible() {
        return itemRepository.findByActiveTrueAndVisibleTrue();
    }

    public List<Item> findAllForStaff(boolean includeInactive) {
        return includeInactive
                ? itemRepository.findAll()
                : itemRepository.findByActiveTrue();
    }

    public List<Item> findByType(UUID typeId, boolean includeHidden) {
        return includeHidden
                ? itemRepository.findByType_IdAndActiveTrue(typeId)
                : itemRepository.findByType_IdAndActiveTrueAndVisibleTrue(typeId);
    }

    public List<Item> findByTypeForStaff(UUID typeId, boolean includeInactive) {
        return includeInactive
                ? itemRepository.findByType_Id(typeId)
                : itemRepository.findByType_IdAndActiveTrue(typeId);
    }

    public Item findById(UUID id) {
        return itemRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
    }

    public List<UserItemLink> findUserCollection(Long userId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        return userItemLinkRepository.findByUser_Id(resolved);
    }

    public List<UserItemLink> findUserCollectionByType(Long userId, String typeKey) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        return userItemLinkRepository.findByUser_IdAndItem_Type_Key(resolved, typeKey);
    }

    public Page<UserItemLink> findInventory(Long userId, String typeKey, Pageable pageable) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        return typeKey == null
                ? userItemLinkRepository.findByUser_Id(resolved, pageable)
                : userItemLinkRepository.findByUser_IdAndItem_Type_Key(resolved, typeKey, pageable);
    }

    public Map<String, ItemResponse> findEquippedHydrated(Long userId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);

        Map<String, Object> rawSettings = userSettingsService.getGroup(resolved, UserSettingKey.GROUP_EQUIPPED);

        Map<String, UUID> slotToItemId = new LinkedHashMap<>();
        for (UserSettingKey key : UserSettingKey.values()) {
            String typeKey = key.equippedTypeKey().orElse(null);
            if (typeKey == null)
                continue;
            Object raw = rawSettings.get(key.key());
            UUID id = raw == null ? null : UUID.fromString(raw.toString());
            slotToItemId.put(typeKey, id);
        }

        List<UUID> nonNullIds = slotToItemId.values().stream().filter(Objects::nonNull).toList();
        Map<UUID, Item> itemsById = nonNullIds.isEmpty()
                ? Map.of()
                : itemRepository.findAllById(nonNullIds).stream()
                        .collect(Collectors.toMap(Item::getId, Function.identity()));

        Map<String, ItemResponse> result = new LinkedHashMap<>();
        slotToItemId.forEach((typeKey, itemId) -> {
            Item item = itemId != null ? itemsById.get(itemId) : null;
            result.put(typeKey, item != null ? ItemMapper.toItemResponse(item) : null);
        });
        return result;
    }

    @Transactional
    public Item create(UUID typeId, String name, String description, String iconUrl,
            Object value, boolean tradeable, boolean visible) {
        ItemType type = itemTypeService.findByIdActive(typeId);
        Item item = Item.builder()
                .type(type)
                .name(name)
                .description(description)
                .iconUrl(iconUrl)
                .value(toJsonNode(value))
                .tradeable(tradeable)
                .visible(visible)
                .build();
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(UUID id, String name, String description, String iconUrl,
            Object value, Boolean tradeable, Boolean visible) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        if (name != null)
            item.setName(name);
        if (description != null)
            item.setDescription(description);
        if (iconUrl != null)
            item.setIconUrl(iconUrl);
        if (value != null)
            item.setValue(toJsonNode(value));
        if (tradeable != null)
            item.setTradeable(tradeable);
        if (visible != null)
            item.setVisible(visible);
        return itemRepository.save(item);
    }

    @Transactional
    public void deactivate(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        item.setActive(false);
        itemRepository.save(item);
    }

    @Transactional
    public Item reactivate(UUID id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        item.setActive(true);
        return itemRepository.save(item);
    }

    @Transactional
    public void revokeAward(UUID linkId) {
        UserItemLink link = userItemLinkRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException("UserItemLink", linkId));
        Long ownerId = link.getUser().getId();
        Item item = link.getItem();
        userItemLinkRepository.delete(link);
        userItemLinkRepository.flush();
        clearEquippedIfNoLongerOwned(ownerId, item);
    }

    @Transactional
    public void awardSystem(Long userId, UUID itemId, ItemSource source, String sourceId, String reason) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        if (!userRepository.existsById(resolved))
            return;

        Item item = itemRepository.findByIdAndActiveTrue(itemId).orElse(null);
        if (item == null)
            return;

        if (userItemLinkRepository.existsByUser_IdAndItem_IdAndSourceAndSourceId(resolved, itemId, source, sourceId)) {
            return;
        }

        UserItemLink link = UserItemLink.builder()
                .user(userRepository.getReferenceById(resolved))
                .item(item)
                .source(source)
                .sourceId(sourceId)
                .reason(reason)
                .build();
        userItemLinkRepository.save(link);
    }

    @Transactional
    public UserItemLink awardManual(Long userId, UUID itemId, StaffUser staff, String reason) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        if (!userRepository.existsById(resolved)) {
            throw new ResourceNotFoundException("User", userId);
        }
        Item item = itemRepository.findByIdAndActiveTrue(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", itemId));

        UserItemLink link = UserItemLink.builder()
                .user(userRepository.getReferenceById(resolved))
                .item(item)
                .source(ItemSource.manual)
                .sourceId(null)
                .awardedBy(staff)
                .reason(reason)
                .build();
        return userItemLinkRepository.save(link);
    }

    @Transactional
    public void equip(Long userId, UUID itemId) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        Item item = findById(itemId);
        if (!userItemLinkRepository.existsByUser_IdAndItem_Id(resolved, itemId)) {
            throw new ValidationException("itemId", "user does not own this item");
        }
        UserSettingKey slot = UserSettingKey.forEquippedItemType(item.getType().getKey())
                .orElseThrow(() -> new ValidationException(
                        "itemId", "items of type '" + item.getType().getKey() + "' are not equippable"));
        userSettingsService.set(resolved, slot, itemId);
    }

    @Transactional
    public void unequip(Long userId, String typeKey) {
        Long resolved = duplicateUserService.resolvePrimaryUserId(userId);
        UserSettingKey slot = UserSettingKey.forEquippedItemType(typeKey)
                .orElseThrow(() -> new ValidationException(
                        "typeKey", "items of type '" + typeKey + "' are not equippable"));
        userSettingsService.clear(resolved, slot);
    }

    @Transactional
    public void clearEquippedIfNoLongerOwned(Long userId, Item item) {
        Optional<UserSettingKey> slot = UserSettingKey.forEquippedItemType(item.getType().getKey());
        if (slot.isEmpty())
            return;
        UUID equipped = userSettingsService.get(userId, slot.get(), UUID.class);
        if (!item.getId().equals(equipped))
            return;
        if (!userItemLinkRepository.existsByUser_IdAndItem_Id(userId, item.getId())) {
            userSettingsService.clear(userId, slot.get());
        }
    }

    private JsonNode toJsonNode(Object value) {
        if (value == null)
            return null;
        return MAPPER.valueToTree(value);
    }
}
