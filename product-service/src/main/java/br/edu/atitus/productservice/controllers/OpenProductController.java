package br.edu.atitus.productservice.controllers;


import br.edu.atitus.productservice.clients.CurrencyClient;
import br.edu.atitus.productservice.clients.CurrencyResponse;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("products")
public class OpenProductController {

    private final ProductRepository repository;
    private final CurrencyClient currencyClient;
    private final CacheManager cacheManager;

    public OpenProductController(ProductRepository repository, CurrencyClient currencyClient, CacheManager cacheManager) {
        super();
        this.repository = repository;
        this.currencyClient = currencyClient;
        this.cacheManager = cacheManager;}
    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/{idProduct}/{targetCurrency}")
    public ResponseEntity<ProductEntity> getProduct(
            @PathVariable Long idProduct,
            @PathVariable String targetCurrency)
            throws Exception {
        targetCurrency = targetCurrency.toUpperCase();
        String nameCache = "product";
        String keyCache = idProduct + targetCurrency;
        ProductEntity product = cacheManager.getCache(nameCache).get(keyCache, ProductEntity.class);

        if (product == null){
            product = repository.findById(idProduct).orElseThrow(() -> new Exception("Product not found"));
            product.setEnviroment("Product-Service running on port: " + serverPort);
            if (targetCurrency.equals(product.getCurrency())) {
                product.setConvertedPrice(product.getPrice());}
            else{
                CurrencyResponse currency = currencyClient.getCurrency(
                        product.getPrice(),
                        product.getCurrency(),
                        targetCurrency);
                if (currency != null) {
                    product.setConvertedPrice(currency.getConvertedValue());
                    product.setEnviroment(product.getEnviroment() + " | Currency-Service running on port: " + currency.getEnvironment());
                    cacheManager.getCache(nameCache).put(keyCache, product);}
                else {
                    product.setConvertedPrice(-1);
                    product.setEnviroment(product.getEnviroment() + " | Currency unavailable");
                }}}else {
            product.setEnviroment("Product-Service running on port: " + serverPort + " | DataSource: Cache");}
        return ResponseEntity.ok(product);}

    @GetMapping("/noconverter/{idProduct}")
    public  ResponseEntity<ProductEntity> getNoConverter(
            @PathVariable Long idProduct)  throws  Exception {
            var product = repository.findById(idProduct).orElseThrow(()-> new Exception("Produto nao encontrado")) ;
            product.setConvertedPrice(-1);
        product.setEnviroment("Product-Service running on port: " + serverPort);
            return ResponseEntity.ok(product);}

    @GetMapping("/{targetCurrency}")
    public ResponseEntity<Page<ProductEntity>> getAllProducts(
            @PathVariable String targetCurrency,
            @PageableDefault(page = 0, size = 5, sort = "description", direction = Sort.Direction.ASC) Pageable pageable
    ) throws Exception {
        Page<ProductEntity> products = repository.findAll(pageable);

        for (ProductEntity product : products) {
            CurrencyResponse currency = currencyClient.getCurrency(
                    product.getPrice(),
                    product.getCurrency(),
                    targetCurrency);
            product.setConvertedPrice(currency.getConvertedValue());
            product.setEnviroment("Currency-Service running on port: " + currency.getEnvironment());}
        return ResponseEntity.ok(products);}}