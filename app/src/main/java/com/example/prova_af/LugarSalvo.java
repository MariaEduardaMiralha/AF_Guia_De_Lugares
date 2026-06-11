package com.example.prova_af;

public class LugarSalvo {

    private String id;
    private String nome;
    private String categoria;
    private double latitude;
    private double longitude;
    private String observacao;

    public LugarSalvo() {}

    public LugarSalvo(String nome, String categoria,
                      double latitude, double longitude,
                      String observacao) {

        this.nome = nome;
        this.categoria = categoria;
        this.latitude = latitude;
        this.longitude = longitude;
        this.observacao = observacao;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getObservacao() {
        return observacao;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}