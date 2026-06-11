package com.example.prova_af;

public class Local {

    private String id;
    private String nome;
    private String categoria;
    private double latitude;
    private double longitude;
    private double distancia;
    private String observacao;
    private String finalidade;

    public Local() {
    }

    public Local(String nome, String categoria, double latitude, double longitude, double distancia) {
        this.nome = nome;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distancia = distancia;
        this.observacao = "";
        this.finalidade = "";
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistancia() {
        return distancia;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }
}