package br.edu.atitus.currencyservice.clients;

import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CurrencyBCFallback implements CurrencyBCClient {

    @Override
    public CurrencyBCResponse getCurrencyBC(String moeda) {
        CurrencyBCResponse fallback = new CurrencyBCResponse();
        fallback.setValue(Collections.emptyList());
        return fallback;
    }
}
