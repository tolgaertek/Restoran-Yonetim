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

public class AdminController {

    @FXML private TextField txtAd;
    @FXML private TextField txtFiyat;
    @FXML private ComboBox<String> cmbKategori;

    @FXML private TableView<Urun> tblUrunler;
    @FXML private TableColumn<Urun, Integer> colID;
    @FXML private TableColumn<Urun, String> colAd;
    @FXML private TableColumn<Urun, Double> colFiyat;
    @FXML private TableColumn<Urun, String> colKategori;

    private int secilenUrunID = 0;

    private ObservableList<String> kategoriListesi = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAd.setCellValueFactory(new PropertyValueFactory<>("ad"));
        colFiyat.setCellValueFactory(new PropertyValueFactory<>("fiyat"));
        colKategori.setCellValueFactory(new PropertyValueFactory<>("kategoriAdi"));

        kategorileriYukle();
        tabloyuDoldur();


        tblUrunler.getSelectionModel().selectedItemProperty().addListener((obs, eskiSecim, yeniSecim) -> {
            if (yeniSecim != null) {
                secilenUrunID = yeniSecim.getId();
                txtAd.setText(yeniSecim.getAd());
                txtFiyat.setText(String.valueOf(yeniSecim.getFiyat()));
                cmbKategori.setValue(yeniSecim.getKategoriAdi());
            }
        });
    }



    @FXML
    public void urunGuncelle() {
        if (secilenUrunID == 0) {
            uyariVer("Lütfen tablodan güncellenecek bir ürün seçiniz!");
            return;
        }

        try (Connection conn = DBHelper.baglan()) {
            // 1. Kategori ID bul
            int katID = getKategoriID(conn, cmbKategori.getValue());

            // 2. SQL UPDATE
            String sql = "UPDATE Urunler SET Ad=?, SatisFiyati=?, KategoriID=? WHERE UrunID=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtAd.getText());
            pstmt.setDouble(2, Double.parseDouble(txtFiyat.getText()));
            pstmt.setInt(3, katID);
            pstmt.setInt(4, secilenUrunID);

            pstmt.executeUpdate();

            bilgiVer("Ürün başarıyla güncellendi!");
            formuTemizle();
            tabloyuDoldur();

        } catch (Exception e) {
            uyariVer("Hata: " + e.getMessage());
        }
    }

    public void urunSil() {
        if (secilenUrunID == 0) {
            uyariVer("Lütfen silinecek bir ürün seçiniz!");
            return;
        }

        // Onay kutusu sor
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Silme Onayı");
        alert.setHeaderText("Bu ürünü ve geçmiş tüm kayıtlarını silmek üzeresiniz.");
        alert.setContentText("Ürün: " + txtAd.getText() + "\nBu işlem geri alınamaz!");

        if (alert.showAndWait().get() == ButtonType.OK) {
            Connection conn = null;
            try {
                conn = DBHelper.baglan();
                conn.setAutoCommit(false); // Transaction

                // 1. ADIM: Önce bu ürünün REÇETESİNİ temizle
                String sqlRecete = "DELETE FROM Receteler WHERE UrunID = ?";
                PreparedStatement pstmt1 = conn.prepareStatement(sqlRecete);
                pstmt1.setInt(1, secilenUrunID);
                pstmt1.executeUpdate();

                // 2. ADIM: Bu ürünün geçtiği SİPARİŞ DETAYLARINI temizle
                String sqlSiparis = "DELETE FROM SiparisDetay WHERE UrunID = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(sqlSiparis);
                pstmt2.setInt(1, secilenUrunID);
                pstmt2.executeUpdate();

                // 3. ADIM: Artık ürünün kendisini silebiliriz
                String sqlUrun = "DELETE FROM Urunler WHERE UrunID = ?";
                PreparedStatement pstmt3 = conn.prepareStatement(sqlUrun);
                pstmt3.setInt(1, secilenUrunID);
                pstmt3.executeUpdate();

                conn.commit(); // Hepsini onayla

                bilgiVer("Ürün ve ilgili tüm geçmiş kayıtları başarıyla silindi.");
                formuTemizle();
                tabloyuDoldur();

            } catch (Exception e) {
                try { if (conn != null) conn.rollback(); } catch (Exception ex) {} // Hata varsa geri al
                e.printStackTrace();
                uyariVer("Silme işlemi başarısız oldu:\n" + e.getMessage());
            } finally {
                try { if (conn != null) conn.close(); } catch (Exception e) {}
            }
        }
    }

    @FXML
    public void formuTemizle() {
        txtAd.clear();
        txtFiyat.clear();
        cmbKategori.getSelectionModel().clearSelection();
        secilenUrunID = 0;
        tblUrunler.getSelectionModel().clearSelection();
    }



    @FXML
    public void urunEkle() {


        try (Connection conn = DBHelper.baglan()) {
            int katID = getKategoriID(conn, cmbKategori.getValue());
            String sql = "INSERT INTO Urunler (Ad, SatisFiyati, KategoriID) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, txtAd.getText());
            pstmt.setDouble(2, Double.parseDouble(txtFiyat.getText()));
            pstmt.setInt(3, katID);
            pstmt.executeUpdate();
            bilgiVer("Eklendi");
            formuTemizle();
            tabloyuDoldur();
        } catch (Exception e) { e.printStackTrace(); }
    }

    //Kategori Adından ID bulma
    private int getKategoriID(Connection conn, String katAdi) throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT KategoriID FROM Kategoriler WHERE Ad = ?");
        ps.setString(1, katAdi);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getInt("KategoriID");
        return 0;
    }

    private void kategorileriYukle() {
        kategoriListesi.clear();
        try (Connection conn = DBHelper.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Ad FROM Kategoriler")) {
            while (rs.next()) kategoriListesi.add(rs.getString("Ad"));
            cmbKategori.setItems(kategoriListesi);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void tabloyuDoldur() {
        ObservableList<Urun> urunler = FXCollections.observableArrayList();
        String sql = "SELECT U.UrunID, U.Ad, U.SatisFiyati, K.Ad as KategoriAdi FROM Urunler U JOIN Kategoriler K ON U.KategoriID = K.KategoriID ORDER BY U.UrunID DESC";
        try (Connection conn = DBHelper.baglan();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                urunler.add(new Urun(
                        rs.getInt("UrunID"),
                        rs.getString("Ad"),
                        rs.getDouble("SatisFiyati"),
                        rs.getString("KategoriAdi")
                ));
            }
            tblUrunler.setItems(urunler);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    public void raporSayfasinaGit() {
        sayfayaGit("/raporlar.fxml", "Raporlar");
    }


    @FXML public void stokSayfasinaGit() { sayfayaGit("/stok.fxml", "Stok Yönetimi"); }
    @FXML public void receteSayfasinaGit() { sayfayaGit("/recete.fxml", "Reçete Yönetimi"); }
    @FXML public void cikisYap() { sayfayaGit("/giris.fxml", "Giriş"); }

    private void sayfayaGit(String fxml, String title) {
        try {
            Stage stage = (Stage) txtAd.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load());
            stage.setTitle(title);
            stage.setScene(scene);
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void uyariVer(String m) { new Alert(Alert.AlertType.WARNING, m).show(); }
    private void bilgiVer(String m) { new Alert(Alert.AlertType.INFORMATION, m).show(); }
}