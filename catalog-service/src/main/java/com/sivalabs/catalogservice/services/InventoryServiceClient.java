package com.sivalabs.catalogservice.services;

import com.sivalabs.catalogservice.utils.MyThreadLocalsHolder;
import com.sivalabs.catalogservice.web.models.ProductInventoryResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class InventoryServiceClient {
    private final RestTemplate restTemplate;
    private final InventoryServiceFeignClient inventoryServiceFeignClient;
    //TODO; move this to config file
    private static final String INVENTORY_API_PATH = "http://inventory-service/api/";

    public InventoryServiceClient(RestTemplate restTemplate, InventoryServiceFeignClient inventoryServiceFeignClient) {
        this.restTemplate = restTemplate;
        this.inventoryServiceFeignClient = inventoryServiceFeignClient;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getDefaultProductInventoryLevels")
    public List<ProductInventoryResponse> getProductInventoryLevels() {
        return this.inventoryServiceFeignClient.getInventoryLevels();
    }

    @SuppressWarnings("unused")
    List<ProductInventoryResponse> getDefaultProductInventoryLevels(Throwable throwable) {
        log.info("Returning default product inventory levels");
        return new ArrayList<>();
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getDefaultProductInventoryByCode")
    public Optional<ProductInventoryResponse> getProductInventoryByCode(String productCode) {
        log.info("CorrelationID: " + MyThreadLocalsHolder.getCorrelationId());
        ResponseEntity<ProductInventoryResponse> itemResponseEntity =
            restTemplate.getForEntity(INVENTORY_API_PATH + "inventory/{code}",
                ProductInventoryResponse.class,
                productCode);

        if (itemResponseEntity.getStatusCode() == HttpStatus.OK) {
            Integer quantity = itemResponseEntity.getBody().getAvailableQuantity();
            log.info("Available quantity: " + quantity);
            return Optional.ofNullable(itemResponseEntity.getBody());
        } else {
            log.error("Unable to get inventory level for product_code: " + productCode + ", StatusCode: " + itemResponseEntity.getStatusCode());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unused")
    Optional<ProductInventoryResponse> getDefaultProductInventoryByCode(String productCode, Throwable throwable) {
        log.info("Returning default ProductInventoryByCode for productCode: " + productCode);
        log.info("CorrelationID: " + MyThreadLocalsHolder.getCorrelationId());
        ProductInventoryResponse response = new ProductInventoryResponse();
        response.setProductCode(productCode);
        response.setAvailableQuantity(50);
        return Optional.ofNullable(response);
    }
}
