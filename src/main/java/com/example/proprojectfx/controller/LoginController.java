package com.example.proprojectfx.controller;

import com.example.proprojectfx.models.User.Membre;
import com.example.proprojectfx.models.User.Administrateur;
import com.example.proprojectfx.models.User.chefprojet;
import com.example.proprojectfx.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;


public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private Button loginButton;


    @FXML
    public void LoginHandle() {
        String email = emailField.getText().trim();
        String mdp = passwordField.getText();
        String typeSelectionne = typeComboBox.getValue();


        if (email.isEmpty() || mdp.isEmpty() || typeSelectionne == null) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants",
                    "Veuillez remplir tous les champs.");
            return;
        }

        // Convertir le type sélectionné en type BD
        String typeBD = switch (typeSelectionne) {
            case "Chef de projet" -> "CHEF_PROJET";
            case "Administrateur" -> "ADMINISTRATEUR";
            case "Membre" -> "MEMBRE";
            default -> "";
        };

        // Vérifier dans la base de données
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            // Requête pour vérifier l'utilisateur
            String sql = "SELECT * FROM user_tab WHERE email = ? AND mdp = ? AND type = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, mdp);
            pstmt.setString(3, typeBD);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {

                String id = rs.getString("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String role = rs.getString("role");

                ouvrirDashboard(typeSelectionne, id, nom, prenom, email, mdp, role, typeBD);

            } else {
                // Utilisateur non trouvé
                showAlert(Alert.AlertType.ERROR, "Échec de connexion",
                        "Email, mot de passe ou type de compte incorrect.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Erreur lors de la vérification : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void ouvrirDashboard(String typeSelectionne, String id, String nom, String prenom,
                                 String email, String mdp, String role, String typeBD) {
        try {
            String fxmlFile = switch (typeSelectionne) {
                case "Chef de projet" -> "DashBoardChef.fxml";
                case "Administrateur" -> "DashBoardAdmin.fxml";
                default -> "DashBoardMembre.fxml";
            };
            String link = "/com/example/proprojectfx/" + fxmlFile;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(link));
            Scene scene = new Scene(loader.load());


            if (fxmlFile.equals("DashBoardMembre.fxml")) {
                DashBoardMembreController controller = loader.getController();


                String dispo = getMembreDisponibilite(id);

                Membre membre = new Membre(id, nom, prenom, email, mdp, role, typeBD, dispo, new ArrayList<>());
                controller.setCurrentMembre(membre);

            } else if (fxmlFile.equals("DashBoardAdmin.fxml")) {
                DashBoardAdminController controller = loader.getController();
                Administrateur admin = new Administrateur(id, nom, prenom, email, mdp, role, typeBD, new ArrayList<>());
                controller.setCurrentAdmin(admin);

            } else if (fxmlFile.equals("DashBoardChef.fxml")) {

                DashController controller = loader.getController();
                chefprojet chef = new chefprojet(id, nom, prenom, email, mdp, role, typeBD, new ArrayList<>());
                controller.setCurrentChef(chef);
            }

            Stage stage = new Stage();
            stage.setTitle(typeSelectionne + " Dashboard - " + prenom + " " + nom);
            stage.setScene(scene);
            stage.show();

            loginButton.getScene().getWindow().hide();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le tableau de bord.");
        }
    }

    private String getMembreDisponibilite(String membreId) {
        Connection conn = DatabaseConnection.getConnection();
        String dispo = "DISPONIBLE"; // Valeur par défaut

        try {
            String sql = "SELECT dispo FROM membre_info_tab WHERE membre_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, membreId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                dispo = rs.getString("dispo");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la disponibilité : " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return dispo;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}