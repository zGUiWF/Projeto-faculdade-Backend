package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.dtos.ProductDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ws/products")
public class WsProductController {

    private final ProductRepository repository;
    public WsProductController(ProductRepository repository) {
        this.repository = repository;
    }

    private ProductEntity convertDto2Entity(ProductDTO dto) {
        var product = new ProductEntity();
        BeanUtils.copyProperties(dto, product);
        return product;}

    @PostMapping
    public ResponseEntity<ProductEntity> post(
            @RequestBody ProductDTO dto,
            @RequestHeader ("x-User-Id") Long userId,
            @RequestHeader ("x-User-Email") String userEmail,
            @RequestHeader ("x-User-Type") Integer userType
            )
            throws Exception{
        // Apenas Admin
        if (userType != 0 ) {
            throw new AuthenticationException("User without permission");}
        var product = convertDto2Entity(dto);
        product.setStock(10);
        repository.save(product);
        return ResponseEntity.status(201).body(product);}

    @PutMapping("/{idProduct}")
    public ResponseEntity<ProductEntity> put(
            @PathVariable Long idProduct,
            @RequestBody ProductDTO dto,
            @RequestHeader ("x-User-Id") Long userId,
            @RequestHeader ("x-User-Email") String userEmail,
            @RequestHeader ("x-User-Type") Integer userType)
            throws Exception{
        // Apenas Admin
        if (userType != 0 ) {
            throw new AuthenticationException("User without permission");}
        var product = convertDto2Entity(dto);
        product.setId(idProduct);
        product.setStock(10);
        repository.save(product);
        return ResponseEntity.ok().body(product);}

    @DeleteMapping ("/{idProduct}")
    public ResponseEntity<String> delete(
            @PathVariable Long idProduct,
            @RequestHeader ("x-User-Id") Long userId,
            @RequestHeader ("x-User-Email") String userEmail,
            @RequestHeader ("x-User-Type") Integer userType)
            throws Exception{
        // Apenas Admin
        if (userType != 0 ) {
            throw new AuthenticationException("User without permission");}
        repository.deleteById(idProduct);
        return ResponseEntity.ok(" Excluido : "+idProduct);}

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuth(AuthenticationException e) {
        String message = e.getMessage().replaceAll("\\r\\n","");
        return ResponseEntity.status(403).body(message);
    }}
