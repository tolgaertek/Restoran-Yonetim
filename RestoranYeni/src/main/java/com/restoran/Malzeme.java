package com.restoran;

public class Malzeme {
    private int id;
    private String ad;
    private double miktar;
    private String birim;

    public Malzeme(int id, String ad, double miktar, String birim) {
        this.id = id;
        this.ad = ad;
        this.miktar = miktar;
        this.birim = birim;
    }

    public int getId() { return id; }
    public String getAd() { return ad; }
    public double getMiktar() { return miktar; }
    public String getBirim() { return birim; }
}