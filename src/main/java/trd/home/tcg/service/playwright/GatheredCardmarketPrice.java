package trd.home.tcg.service.playwright;

import lombok.NonNull;
import trd.home.tcg.dao.CardmarketCardPrice;

public record GatheredCardmarketPrice(
        @NonNull String cardId, @NonNull CardmarketCardPrice price) {}
