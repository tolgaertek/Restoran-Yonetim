package com.restoran;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GirisController {

    @FXML private TextField txtKullanici;
    @FXML private PasswordField txtSifre;
    @FXML private Label lblHata;

    @FXML
    public void girisYap() {
        String kAdi = txtKullanici.getText();
        String sifre = txtSifre.getText();

        String sql = "SELECT * FROM Kullanicilar WHERE KullaniciAdi = ? AND Sifre = ?";

        try (Connection conn = DBHelper.baglan();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kAdi);
            pstmt.setString(2, sifre);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 1. Giriş Başarılı: Rolü Hafızaya Kaydet
                Oturum.aktifRol = rs.getString("Rol");
                Oturum.aktifKullaniciAdi = rs.getString("KullaniciAdi");

                // 2. Ana Menüye Geç
                ekranaYonlendir();
            } else {
                // Hatalı Giriş
                lblHata.setText("Hatalı kullanıcı adı veya şifre!");
                lblHata.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            lblHata.setText("Veritabanı hatası!");
            lblHata.setVisible(true);
        }
    }


    private void ekranaYonlendir() {
        try {
            Stage stage = (Stage) txtKullanici.getScene().getWindow();
            FXMLLoader fxmlLoader;


            if (Oturum.aktifRol.equals("Yonetici")) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/admin.fxml"));
            }

            else {
                fxmlLoader = new FXMLLoader(getClass().getResource("/menu.fxml"));
            }

            Scene scene = new Scene(fxmlLoader.load());


            if (Oturum.aktifRol.equals("Yonetici")) {
                stage.setWidth(1000);
                stage.setHeight(750);
                stage.setTitle("Yönetici Paneli");
            } else {
                stage.setWidth(1000);
                stage.setHeight(700);
                stage.setTitle("Sipariş Ekranı - ");
            }

            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}