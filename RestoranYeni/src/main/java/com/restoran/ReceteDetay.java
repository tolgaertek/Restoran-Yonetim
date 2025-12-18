package com.restoran;

public class ReceteDetay {
    private String urunAdi;
    private String malzemeAdi;
    private double miktar;
    private String birim;

    public ReceteDetay(String urunAdi, String malzemeAdi, double miktar, String birim) {
        this.urunAdi = urunAdi;
        this.malzemeAdi = malzemeAdi;
        this.miktar = miktar;
        this.birim = birim;
    }

    public String getUrunAdi() { return urunAdi; }
    public String getMalzemeAdi() { return malzemeAdi; }
    public double getMiktar() { return miktar; }
    public String getBirim() { return birim; }
}