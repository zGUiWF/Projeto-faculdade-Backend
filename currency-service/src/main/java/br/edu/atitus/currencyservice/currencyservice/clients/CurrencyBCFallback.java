package br.edu.atitus.currencyservice.currencyservice.clients;

import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CurrencyBCFallback implements CurrencyBCClient{
    @Override
    public CurrencyBCResponse getCurrency(String moeda) {
        CurrencyBCResponse fallback = new CurrencyBCResponse();
        fallback.setValue(Collections.emptyList());
        return fallback;
    }
}
