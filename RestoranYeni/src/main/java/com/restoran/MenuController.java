package com.restoran;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.*;

public class MenuController {

    //  ARAYÜZ ELEMANLARI
    @FXML private TableView<Urun> tblMenu;
    @FXML private TableColumn<Urun, String> colMenuAd;
    @FXML private TableColumn<Urun, Double> colMenuFiyat;
    @FXML private TextField txtAdet;

    @FXML private TableView<SepetUrunu> tblSepet;
    @FXML private TableColumn<SepetUrunu, String> colSepetAd;
    @FXML private TableColumn<SepetUrunu, Integer> colSepetAdet;
    @FXML private TableColumn<SepetUrunu, Double> colSepetTutar;
    @FXML private TableColumn<SepetUrunu, Void> colSil;

    @FXML private Label lblGenelToplam;

    // VERİLER
    private ObservableList<SepetUrunu> sepetListesi = FXCollections.observableArrayList();
    private double genelToplam = 0.0;

    @FXML
    public void initialize() {
        // 1. Menü Tablosunu Ayarla
        colMenuAd.setCellValueFactory(new PropertyValueFactory<>("ad"));
        colMenuFiyat.setCellValueFactory(new PropertyValueFactory<>("fiyat"));
        menuyuGetir(); // Veritabanından çek

        // 2. Sepet Tablosunu Ayarla
        colSepetAd.setCellValueFactory(new PropertyValueFactory<>("ad"));
        colSepetAdet.setCellValueFactory(new PropertyValueFactory<>("adet"));
        colSepetTutar.setCellValueFactory(new PropertyValueFactory<>("toplamTutar"));

        tblSepet.setItems(sepetListesi); // Sepet listesini tabloya bağla

    }

    // Veritabanından Menüyü Çekme
    private void menuyuGetir() {
        ObservableList<Urun> menuListesi = FXCollections.observableArrayList();
        try (Connection conn = DBHelper.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Urunler")) {

            while (rs.next()) {
                menuListesi.add(new Urun(rs.getInt("UrunID"), rs.getString("Ad"), rs.getDouble("SatisFiyati")));
            }
            tblMenu.setItems(menuListesi);
        } catch (Exception e) { e.printStackTrace(); }
    }


    @FXML
    public void sepeteEkle() {
        Urun secilenUrun = tblMenu.getSelectionModel().getSelectedItem();
        if (secilenUrun == null) {
            uyariVer("Lütfen menüden bir yemek seçin!");
            return;
        }

        try {
            int adet = Integer.parseInt(txtAdet.getText());
            if (adet <= 0) return;

            // Sepete ekle
            SepetUrunu yeniUrun = new SepetUrunu(secilenUrun.getId(), secilenUrun.getAd(), adet, secilenUrun.getFiyat());
            sepetListesi.add(yeniUrun);

            // Toplam tutarı güncelle
            genelToplam += yeniUrun.getToplamTutar();
            lblGenelToplam.setText("Toplam: " + genelToplam + " TL");

            // Adet kutusunu sıfırla (Kullanım kolaylığı için)
            txtAdet.setText("1");

        } catch (NumberFormatException e) {
            uyariVer("Lütfen geçerli bir sayı girin.");
        }
    }


    @FXML
    public void siparisiTamamla() {
        if (sepetListesi.isEmpty()) {
            uyariVer("Sepet boş! Sipariş verilemez.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBHelper.baglan();
            conn.setAutoCommit(false); // Transaction Başlat

            // 1. Siparişi Oluştur
            String sqlSiparis = "INSERT INTO Siparisler (ToplamTutar) VALUES (?) RETURNING SiparisID";
            PreparedStatement pstmt1 = conn.prepareStatement(sqlSiparis);
            pstmt1.setDouble(1, genelToplam);

            ResultSet rs = pstmt1.executeQuery();
            int yeniSiparisID = 0;
            if (rs.next()) {
                yeniSiparisID = rs.getInt("SiparisID");
            }

            // 2. Sipariş Detaylarını Ekle
            String sqlDetay = "INSERT INTO SiparisDetay (SiparisID, UrunID, Adet, BirimFiyat) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt2 = conn.prepareStatement(sqlDetay);

            for (SepetUrunu urun : sepetListesi) {
                pstmt2.setInt(1, yeniSiparisID);
                pstmt2.setInt(2, urun.getUrunId());
                pstmt2.setInt(3, urun.getAdet());
                pstmt2.setDouble(4, urun.getToplamTutar() / urun.getAdet());
                pstmt2.addBatch();
            }
            pstmt2.executeBatch();

            conn.commit(); // Onayla

            uyariVer("Siparişiniz Başarıyla Alındı! Fiş No: " + yeniSiparisID);

            // Temizlik
            sepetListesi.clear();
            genelToplam = 0;
            lblGenelToplam.setText("Toplam: 0.0 TL");

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            uyariVer("Hata oluştu: " + e.getMessage());
        }
    }


    private void uyariVer(String mesaj) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.showAndWait();
    }


}