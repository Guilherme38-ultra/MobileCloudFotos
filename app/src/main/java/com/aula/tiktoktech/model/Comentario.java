package com.aula.tiktoktech.model;

public class Comentario {
    private String autor;
    private String texto;
    private long criadoEm;

    /** Construtor vazio exigido pelo Firestore. */
    public Comentario() {
    }

    public Comentario(String autor, String texto) {
        this.autor = autor;
        this.texto = texto;
        this.criadoEm = System.currentTimeMillis();
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public long getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(long criadoEm) {
        this.criadoEm = criadoEm;
    }

}
