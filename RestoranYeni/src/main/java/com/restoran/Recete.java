package com.restoran;

public class Recete {
    private int receteID;
    private String urunAdi;
    private String malzemeAdi;
    private double miktar;
    private String birim;

    public Recete(int receteID, String urunAdi, String malzemeAdi, double miktar, String birim) {
        this.receteID = receteID;
        this.urunAdi = urunAdi;
        this.malzemeAdi = malzemeAdi;
        this.miktar = miktar;
        this.birim = birim;
    }

    // Getter Metotları (TableView İçin Şart)
    public int getReceteID() { return receteID; }
    public String getUrunAdi() { return urunAdi; }
    public String getMalzemeAdi() { return malzemeAdi; }
    public double getMiktar() { return miktar; }
    public String getBirim() { return birim; }
}