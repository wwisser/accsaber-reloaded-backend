package com.accsaber.backend.service.item;

import com.accsaber.backend.model.dto.response.item.ItemModifierResponse;
import com.accsaber.backend.model.dto.response.item.ItemResponse;
import com.accsaber.backend.model.dto.response.item.ItemTypeResponse;
import com.accsaber.backend.model.dto.response.item.TradeResponse;
import com.accsaber.backend.model.dto.response.item.UserItemResponse;
import com.accsaber.backend.model.entity.item.Item;
import com.accsaber.backend.model.entity.item.ItemModifier;
import com.accsaber.backend.model.entity.item.ItemType;
import com.accsaber.backend.model.entity.item.UserItemLink;
import com.accsaber.backend.model.entity.item.UserItemTrade;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ItemMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ItemMapper() {
    }

    public static ItemTypeResponse toTypeResponse(ItemType type) {
        return ItemTypeResponse.builder()
                .id(type.getId())
                .parentTypeId(type.getParentType() != null ? type.getParentType().getId() : null)
                .key(type.getKey())
                .name(type.getName())
                .description(type.getDescription())
                .valueSchema(toObject(type.getValueSchema()))
                .active(type.isActive())
                .createdAt(type.getCreatedAt())
                .build();
    }

    public static ItemResponse toItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .typeId(item.getType().getId())
                .typeKey(item.getType().getKey())
                .name(item.getName())
                .description(item.getDescription())
                .iconUrl(item.getIconUrl())
                .value(toObject(item.getValue()))
                .rarity(item.getRarity().name())
                .tradeable(item.isTradeable())
                .visible(item.isVisible())
                .active(item.isActive())
                .deprecated(item.isDeprecated())
                .createdAt(item.getCreatedAt())
                .build();
    }

    public static UserItemResponse.ModifierRef toModifierRef(ItemModifier m) {
        if (m == null)
            return null;
        return UserItemResponse.ModifierRef.builder()
                .id(m.getId())
                .key(m.getKey())
                .name(m.getName())
                .build();
    }

    public static ItemModifierResponse toModifierResponse(ItemModifier m) {
        return ItemModifierResponse.builder()
                .id(m.getId())
                .key(m.getKey())
                .name(m.getName())
                .description(m.getDescription())
                .active(m.isActive())
                .createdAt(m.getCreatedAt())
                .build();
    }

    public static UserItemResponse toUserItemResponse(UserItemLink link) {
        return UserItemResponse.builder()
                .linkId(link.getId())
                .item(toItemResponse(link.getItem()))
                .modifier(toModifierRef(link.getModifier()))
                .serialNumber(link.getSerialNumber())
                .source(link.getSource().name())
                .sourceId(link.getSourceId())
                .awardedByStaffId(link.getAwardedBy() != null ? link.getAwardedBy().getId() : null)
                .reason(link.getReason())
                .awardedAt(link.getAwardedAt())
                .build();
    }

    public static TradeResponse toTradeResponse(UserItemTrade trade) {
        UserItemLink link = trade.getUserItemLink();
        return TradeResponse.builder()
                .id(trade.getId())
                .fromUserId(trade.getFromUser().getId())
                .toUserId(trade.getToUser().getId())
                .userItemLinkId(link.getId())
                .item(toItemResponse(link.getItem()))
                .modifier(toModifierRef(link.getModifier()))
                .serialNumber(link.getSerialNumber())
                .status(trade.getStatus().name())
                .message(trade.getMessage())
                .createdAt(trade.getCreatedAt())
                .resolvedAt(trade.getResolvedAt())
                .build();
    }

    private static Object toObject(JsonNode node) {
        if (node == null)
            return null;
        try {
            return MAPPER.treeToValue(node, Object.class);
        } catch (Exception e) {
            return null;
        }
    }
}
