package com.restoran;

public class Urun {

    private int id;          // urunID değil, sadece 'id'
    private String ad;
    private double fiyat;    // satisFiyati değil, sadece 'fiyat'
    private String kategoriAdi; // Admin paneli için eklendi

    // ----------------------------------------------------
    // CONSTRUCTORLAR (Kurucu Metotlar)
    // ----------------------------------------------------

    // 1. ESKİ YAPI (MenuController ve Sipariş Ekranı için)
    // Sadece 3 bilgi gelir, kategori adı boştur.
    public Urun(int id, String ad, double fiyat) {
        this.id = id;
        this.ad = ad;
        this.fiyat = fiyat;
        this.kategoriAdi = "";
    }

    // 2. YENİ YAPI (AdminController ve Listeleme için)
    // 4 bilgi gelir, kategori adı dahildir.
    public Urun(int id, String ad, double fiyat, String kategoriAdi) {
        this.id = id;
        this.ad = ad;
        this.fiyat = fiyat;
        this.kategoriAdi = kategoriAdi;
    }

    // ----------------------------------------------------
    // GETTER ve SETTER METOTLARI
    // (TableView'de PropertyValueFactory ile eşleşmesi için bu isimler şart)
    // ----------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public double getFiyat() {
        return fiyat;
    }

    public void setFiyat(double fiyat) {
        this.fiyat = fiyat;
    }

    public String getKategoriAdi() {
        return kategoriAdi;
    }

    public void setKategoriAdi(String kategoriAdi) {
        this.kategoriAdi = kategoriAdi;
    }
}