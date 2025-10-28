package br.edu.atitus.currencyservice.controllers;

import br.edu.atitus.currencyservice.clients.CurrencyBCClient;
import br.edu.atitus.currencyservice.clients.CurrencyBCResponse;
import br.edu.atitus.currencyservice.entities.CurrencyEntity;
import br.edu.atitus.currencyservice.repositories.CurrencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("currency")
public class CurrencyController {

    private final CurrencyRepository repository ;
    private final CurrencyBCClient currencyBCClient;
    private  final CacheManager cacheManager;

    @Value("${server.port}")
    private int serverPort;

    public CurrencyController(CurrencyRepository repository, CurrencyBCClient currencyBCClient, CacheManager cacheManager) {
        super ();
        this.repository = repository;
        this.currencyBCClient = currencyBCClient;

        this.cacheManager = cacheManager;
    }
    @GetMapping("/{value}/{source}/{target}" )
    public ResponseEntity<CurrencyEntity> getConvercion(
            @PathVariable Double value,
            @PathVariable String source,
            @PathVariable String target) throws Exception {
        source = source.toUpperCase();
        target = target.toUpperCase();
        String dataSource = "None";
        String nameCache = "Currency";
        String keyCache = source + target;
        CurrencyEntity currency = cacheManager.getCache(nameCache).get(keyCache, CurrencyEntity.class);
        if (currency != null) {
            dataSource = "Cache";
        } else {
            currency = new CurrencyEntity();
            currency.setSource(source);
            currency.setTarget(target);
            if (source.equals(target)) {currency.setConversionRate(1.0);}
            else {
                try {
                    double curSource = 1;
                    double curTarget = 1;
                    if (!source.equals("BRL")) {
                        CurrencyBCResponse resposta = currencyBCClient.getCurrencyBC(source);
                        if (resposta.getValue().isEmpty()) throw  new Exception("Nenhum source encontrado: "+ source);
                        curSource = resposta.getValue().get(0).getCotacaoVenda();}
                    if (!target.equals("BRL")) {
                        CurrencyBCResponse resposta = currencyBCClient.getCurrencyBC(target);
                        if (resposta.getValue().isEmpty()) throw  new Exception("Nenhum target encontrado: "+ target);
                        curTarget = resposta.getValue().get(0).getCotacaoVenda();}
                    currency.setConversionRate(curSource / curTarget);
                    dataSource = "API BCB";} catch (Exception e) {
                    currency = repository.findBySourceAndTarget(source,target).orElseThrow(()-> new Exception("Currency Unsupported")) ;
                    dataSource = "Local Database";
                }}
            cacheManager.getCache(nameCache).put(keyCache, currency);
        }
        currency.setConvertedValue(value * currency.getConversionRate());
        currency.setEnvironment("Currency running in port: " + serverPort +" | Data Source : "+ dataSource);
        return ResponseEntity.ok(currency);}
}