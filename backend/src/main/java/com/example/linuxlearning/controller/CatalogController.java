package com.example.linuxlearning.controller;

import com.example.linuxlearning.common.ApiResponse;
import com.example.linuxlearning.dto.CatalogResponse;
import com.example.linuxlearning.service.CatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ApiResponse<CatalogResponse> catalog() {
        return ApiResponse.ok(catalogService.catalog());
    }
}
