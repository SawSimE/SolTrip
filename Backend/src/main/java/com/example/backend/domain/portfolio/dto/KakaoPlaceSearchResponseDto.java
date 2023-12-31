package com.example.backend.domain.portfolio.dto;

import lombok.Getter;

import java.util.List;


public class KakaoPlaceSearchResponseDto {
    private List<Document> documents;

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    @Getter
    public static class Document {
        private String place_name;
        private String address_name;
        private double x; // longitude
        private double y; // latitude
        private String category_name;

        // Getter and Setter methods
    }
}
