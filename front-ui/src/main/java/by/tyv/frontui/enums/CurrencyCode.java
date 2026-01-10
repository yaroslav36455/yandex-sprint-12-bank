package by.tyv.frontui.enums;

import lombok.Getter;

@Getter
public enum CurrencyCode {
    RUB("Российский Рубль"),
    BYN("Белорусский Рубль"),
    IRR("Иранский Риал"),
    CNY("Китайский Юань"),
    INR("Индийская Рупия");

    private final String title;

    CurrencyCode(String title) {
        this.title = title;
    }
}
