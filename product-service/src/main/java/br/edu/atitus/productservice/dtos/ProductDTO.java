package br.edu.atitus.productservice.dtos;

public record ProductDTO(
        String description,
        String brand,
        String model,
        String currency,
        Double price,
        String imageUrl) {
}