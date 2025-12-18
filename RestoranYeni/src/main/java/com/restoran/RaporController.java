package com.restoran;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class RaporController {

    @FXML private BarChart<String, Number> barChartSatis;
    @FXML private PieChart pieChartKategori;

    @FXML private Label lblToplamCiro;
    @FXML private Label lblToplamSiparis;
    @FXML private Label lblToplamUrun;

    @FXML
    public void initialize() {
        verileriYenile();
    }

    @FXML
    public void verileriYenile() {
        grafikleriDoldur();
        ozetBilgileriGetir();
    }

    private void grafikleriDoldur() {
        // 1. BAR CHART: En Çok Satanlar
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Satış Adedi");

        try (Connection conn = DBHelper.baglan(); Statement stmt = conn.createStatement()) {
            // SQL SORGUSU 1: Ürün bazlı satış adetlerini çoktan aza sıralar
            String sql = "SELECT " +
                    "    U.Ad, " +
                    "    SUM(D.Adet) AS Toplam " +
                    "FROM " +
                    "    SiparisDetay D " +
                    "JOIN " +
                    "    Urunler U ON D.UrunID = U.UrunID " +
                    "GROUP BY " +
                    "    U.Ad " +
                    "ORDER BY " +
                    "    Toplam DESC " +
                    "LIMIT 5";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("Ad"), rs.getInt("Toplam")));
            }
        } catch (Exception e) { e.printStackTrace(); }

        barChartSatis.getData().clear();
        barChartSatis.getData().add(series);

        // 2. PIE CHART: Kategori Bazlı Ciro
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        try (Connection conn = DBHelper.baglan(); Statement stmt = conn.createStatement()) {
            // SQL SORGUSU 2: Kategorilere göre toplam ciroyu hesaplar
            String sql = "SELECT " +
                    "    K.Ad, " +
                    "    SUM(D.Adet * D.BirimFiyat) AS Ciro " +
                    "FROM " +
                    "    SiparisDetay D " +
                    "JOIN " +
                    "    Urunler U ON D.UrunID = U.UrunID " +
                    "JOIN " +
                    "    Kategoriler K ON U.KategoriID = K.KategoriID " +
                    "GROUP BY " +
                    "    K.Ad";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                pieData.add(new PieChart.Data(rs.getString("Ad"), rs.getDouble("Ciro")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        pieChartKategori.setData(pieData);
    }

    private void ozetBilgileriGetir() {
        try (Connection conn = DBHelper.baglan(); Statement stmt = conn.createStatement()) {

            // 1. Toplam Ciro ve Toplam Sipariş (Adisyon) Sayısı
            // SQL SORGUSU 3: Genel ciro ve fiş sayısını çeker
            String sql1 = "SELECT " +
                    "    SUM(ToplamTutar) AS ToplamCiro, " +
                    "    COUNT(*) AS SiparisSayisi " +
                    "FROM " +
                    "    Siparisler";

            ResultSet rs1 = stmt.executeQuery(sql1);

            if (rs1.next()) {
                double ciro = rs1.getDouble("ToplamCiro");
                int siparis = rs1.getInt("SiparisSayisi");

                lblToplamCiro.setText(String.format("%.2f TL", ciro));
                lblToplamSiparis.setText(String.valueOf(siparis));
            }

            // 2. Toplam Satılan Ürün Sayısı
            // SQL SORGUSU 4: Tek tek satılan tüm ürünlerin toplam adedini bulur
            String sql2 = "SELECT " +
                    "    SUM(Adet) AS ToplamUrun " +
                    "FROM " +
                    "    SiparisDetay";

            ResultSet rs2 = stmt.executeQuery(sql2);

            if (rs2.next()) {
                int urunSayisi = rs2.getInt("ToplamUrun");
                lblToplamUrun.setText(String.valueOf(urunSayisi));
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblToplamCiro.setText("Hata");
        }
    }

    @FXML
    public void geriDon() {
        try {
            Stage stage = (Stage) lblToplamCiro.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }
}