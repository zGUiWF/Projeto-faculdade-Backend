CREATE TABLE tb_product (
    id SERIAL not null,
    description VARCHAR(100) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    price float(53) NOT NULL,
    stock integer NOT NULL,
    primary key (id)
);