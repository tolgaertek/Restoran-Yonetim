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

public class ReceteController {

    @FXML private ComboBox<String> cmbUrunler;
    @FXML private ComboBox<String> cmbMalzemeler;
    @FXML private TextField txtMiktar;
    @FXML private Label lblBirim;

    @FXML private TableView<Recete> tblReceteler;
    @FXML private TableColumn<Recete, String> colUrun;
    @FXML private TableColumn<Recete, String> colMalzeme;
    @FXML private TableColumn<Recete, Double> colMiktar;
    @FXML private TableColumn<Recete, String> colBirim;

    @FXML
    public void initialize() {
        // Tablo Ayarları
        colUrun.setCellValueFactory(new PropertyValueFactory<>("urunAdi"));
        colMalzeme.setCellValueFactory(new PropertyValueFactory<>("malzemeAdi"));
        colMiktar.setCellValueFactory(new PropertyValueFactory<>("miktar"));
        colBirim.setCellValueFactory(new PropertyValueFactory<>("birim"));

        verileriYukle();

        // Malzeme seçilince birimini otomatik getir (Görsellik için)
        cmbMalzemeler.getSelectionModel().selectedItemProperty().addListener((obs, eski, yeni) -> {
            if (yeni != null) birimGetir(yeni);
        });
    }

    private void verileriYukle() {
        // 1. Ürünleri Combobox'a Doldur
        try (Connection conn = DBHelper.baglan(); Statement stmt = conn.createStatement()) {
            ObservableList<String> urunList = FXCollections.observableArrayList();
            ResultSet rs = stmt.executeQuery("SELECT Ad FROM Urunler");
            while (rs.next()) urunList.add(rs.getString("Ad"));
            cmbUrunler.setItems(urunList);
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Malzemeleri Combobox'a Doldur
        try (Connection conn = DBHelper.baglan(); Statement stmt = conn.createStatement()) {
            ObservableList<String> malzList = FXCollections.observableArrayList();
            ResultSet rs = stmt.executeQuery("SELECT Ad FROM Malzemeler");
            while (rs.next()) malzList.add(rs.getString("Ad"));
            cmbMalzemeler.setItems(malzList);
        } catch (Exception e) { e.printStackTrace(); }

        // 3. Tabloyu Doldur (JOIN Sorgusu)
        tabloyuGuncelle();
    }

    private void tabloyuGuncelle() {
        ObservableList<Recete> receteler = FXCollections.observableArrayList();
        String sql = "SELECT R.ReceteID, U.Ad AS UrunAdi, M.Ad AS MalzemeAdi, R.Miktar, M.Birim " +
                "FROM Receteler R " +
                "JOIN Urunler U ON R.UrunID = U.UrunID " +
                "JOIN Malzemeler M ON R.MalzemeID = M.MalzemeID " +
                "ORDER BY U.Ad";

        try (Connection conn = DBHelper.baglan(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                receteler.add(new Recete(
                        rs.getInt("ReceteID"),
                        rs.getString("UrunAdi"),
                        rs.getString("MalzemeAdi"),
                        rs.getDouble("Miktar"),
                        rs.getString("Birim")
                ));
            }
            tblReceteler.setItems(receteler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    //REÇETE EKLEME
    @FXML
    public void receteEkle() {
        String yemek = cmbUrunler.getValue();
        String malzeme = cmbMalzemeler.getValue();
        String miktarStr = txtMiktar.getText();

        if (yemek == null || malzeme == null || miktarStr.isEmpty()) {
            uyariVer("Lütfen tüm alanları doldurunuz!");
            return;
        }

        try (Connection conn = DBHelper.baglan()) {
            // İsimlerden ID'leri bul
            int urunID = getID(conn, "Urunler", "UrunID", yemek);
            int malzemeID = getID(conn, "Malzemeler", "MalzemeID", malzeme);

            // Veritabanına Ekle
            String sql = "INSERT INTO Receteler (UrunID, MalzemeID, Miktar) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, urunID);
            pstmt.setInt(2, malzemeID);
            pstmt.setDouble(3, Double.parseDouble(miktarStr));

            pstmt.executeUpdate();

            bilgiVer("Reçete başarıyla eklendi!");
            tabloyuGuncelle();
            txtMiktar.clear();

        } catch (Exception e) {
            uyariVer("Hata: " + e.getMessage());
        }
    }

    //  REÇETE SİLME
    @FXML
    public void receteSil() {
        Recete secilen = tblReceteler.getSelectionModel().getSelectedItem();
        if (secilen == null) {
            uyariVer("Lütfen silinecek satırı seçiniz.");
            return;
        }

        try (Connection conn = DBHelper.baglan()) {
            String sql = "DELETE FROM Receteler WHERE ReceteID = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, secilen.getReceteID());
            pstmt.executeUpdate();

            bilgiVer("Silindi.");
            tabloyuGuncelle();
        } catch (Exception e) { e.printStackTrace(); }
    }



    // İsmi verip ID'sini döndüren metot
    private int getID(Connection conn, String tablo, String idKolon, String ad) throws Exception {
        String sql = "SELECT " + idKolon + " FROM " + tablo + " WHERE Ad = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, ad);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
        return 0;
    }


    // Seçilen malzemenin birimini bulup ekrana yazar
    private void birimGetir(String malzemeAdi) {
        try (Connection conn = DBHelper.baglan()) {
            PreparedStatement ps = conn.prepareStatement("SELECT Birim FROM Malzemeler WHERE Ad = ?");
            ps.setString(1, malzemeAdi);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) lblBirim.setText(rs.getString("Birim"));
        } catch (Exception e) {}
    }

    @FXML
    public void geriDon() {
        try {
            Stage stage = (Stage) tblReceteler.getScene().getWindow();
            FXMLLoader loader;
            if (Oturum.aktifRol.equals("Yonetici")) loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
            else loader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void uyariVer(String m) { new Alert(Alert.AlertType.WARNING, m).show(); }
    private void bilgiVer(String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
}