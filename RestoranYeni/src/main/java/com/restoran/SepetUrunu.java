package com.restoran;

public class SepetUrunu {
    private int urunId;
    private String ad;
    private int adet;
    private double birimFiyat;
    private double toplamTutar;

    public SepetUrunu(int urunId, String ad, int adet, double birimFiyat) {
        this.urunId = urunId;
        this.ad = ad;
        this.adet = adet;
        this.birimFiyat = birimFiyat;
        this.toplamTutar = adet * birimFiyat; // Otomatik hesapla
    }


    public String getAd() { return ad; }
    public int getAdet() { return adet; }
    public double getToplamTutar() { return toplamTutar; }
    public int getUrunId() { return urunId; }

    public double getBirimFiyat() {
        return birimFiyat;
    }
}