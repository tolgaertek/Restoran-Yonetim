package com.restoran;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class StokController {

    //SOL PANEL
    @FXML private TextField txtYeniAd;
    @FXML private TextField txtYeniStok;
    @FXML private ComboBox<String> cmbBirim;

    //SAĞ PANEL
    @FXML private TextField txtEklenecekMiktar;

    // Tablo Tanımları
    @FXML private TableView<Malzeme> tblMalzemeler;
    @FXML private TableColumn<Malzeme, Integer> colID;
    @FXML private TableColumn<Malzeme, String> colAd;
    @FXML private TableColumn<Malzeme, Double> colMiktar;
    @FXML private TableColumn<Malzeme, String> colBirim;

    @FXML
    public void initialize() {

        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAd.setCellValueFactory(new PropertyValueFactory<>("ad"));
        colMiktar.setCellValueFactory(new PropertyValueFactory<>("miktar"));
        colBirim.setCellValueFactory(new PropertyValueFactory<>("birim"));

        // Birim ComboBox'ı Doldur
        cmbBirim.setItems(FXCollections.observableArrayList("kg", "litre", "adet"));
        cmbBirim.getSelectionModel().selectFirst();

        tabloyuDoldur();
    }


    @FXML
    public void yeniMalzemeEkle() {
        String ad = txtYeniAd.getText();
        String miktarStr = txtYeniStok.getText();
        String birim = cmbBirim.getValue();

        if (ad.isEmpty() || miktarStr.isEmpty()) {
            uyariVer("Lütfen malzeme adı ve başlangıç stoğunu giriniz.");
            return;
        }

        try (Connection conn = DBHelper.baglan()) {

            String sql = "INSERT INTO Malzemeler (Ad, StokMiktari, Birim) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, ad);
            pstmt.setDouble(2, Double.parseDouble(miktarStr));
            pstmt.setString(3, birim);

            pstmt.executeUpdate();

            bilgiVer(ad + " başarıyla sisteme tanımlandı.");


            txtYeniAd.clear();
            txtYeniStok.setText("0");
            tabloyuDoldur(); // Listeyi yenile

        } catch (Exception e) {
            uyariVer("Hata: " + e.getMessage());
        }
    }


    @FXML
    public void stokArttir() {
        Malzeme secilen = tblMalzemeler.getSelectionModel().getSelectedItem();
        String miktarStr = txtEklenecekMiktar.getText();

        if (secilen == null) {
            uyariVer("Lütfen tablodan stok eklemek istediğiniz malzemeyi seçin.");
            return;
        }
        if (miktarStr.isEmpty()) {
            uyariVer("Lütfen eklenecek miktarı giriniz.");
            return;
        }

        try (Connection conn = DBHelper.baglan()) {
            // SQL ile güncelle
            String sql = "UPDATE Malzemeler SET StokMiktari = StokMiktari + ? WHERE MalzemeID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, Double.parseDouble(miktarStr));
            pstmt.setInt(2, secilen.getId());
            pstmt.executeUpdate();

            bilgiVer(secilen.getAd() + " stoğu güncellendi.");
            txtEklenecekMiktar.clear();
            tabloyuDoldur();

        } catch (Exception e) {
            uyariVer("Hata: " + e.getMessage());
        }
    }

    private void tabloyuDoldur() {
        ObservableList<Malzeme> malzemeler = FXCollections.observableArrayList();
        try (Connection conn = DBHelper.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Malzemeler ORDER BY MalzemeID ASC")) {

            while (rs.next()) {
                malzemeler.add(new Malzeme(
                        rs.getInt("MalzemeID"),
                        rs.getString("Ad"),
                        rs.getDouble("StokMiktari"), // DB sütun adı
                        rs.getString("Birim")
                ));
            }
            tblMalzemeler.setItems(malzemeler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void geriDon() {
        try {
            Stage stage = (Stage) tblMalzemeler.getScene().getWindow();
            FXMLLoader loader;

            if (Oturum.aktifRol.equals("Yonetici")) {
                loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
            } else {
                loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            }

            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void uyariVer(String m) { new Alert(Alert.AlertType.WARNING, m).show(); }
    private void bilgiVer(String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
}