package com.example.proprojectfx.controller;

import com.example.proprojectfx.models.User.chefprojet;
import com.example.proprojectfx.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class DashController {


    @FXML private Text fullNameText;
    @FXML private Text roleText;
    @FXML private Button profileButton;
    @FXML private Button logoutButton;


    @FXML private Text activeProjectsText;
    @FXML private Text activeSprintsText;
    @FXML private Text pendingTasksText;
    @FXML private Text teamMembersText;


    @FXML private VBox createProjectBox;
    @FXML private VBox createSprintBox;
    @FXML private VBox createTaskBox;
    @FXML private VBox createTeamBox;


    private chefprojet currentChef;

    @FXML
    public void initialize() {

    }


    public void setCurrentChef(chefprojet chef) {
        this.currentChef = chef;
        loadUserData();
        loadDashboardStatistics();
        setupHoverEffects();
    }


    private void loadUserData() {
        if (currentChef != null) {
            if (fullNameText != null) {
                fullNameText.setText(currentChef.getPrenom() + " " + currentChef.getName());
            }
            if (roleText != null) {
                roleText.setText("Chef de Projet");
            }
        }
    }

    /**
     * Charge les statistiques réelles depuis la base de données
     */
    private void loadDashboardStatistics() {
        if (currentChef == null) return;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            String chefId = currentChef.getId();


            String sqlProjets = "SELECT COUNT(*) as total FROM project_tab WHERE chef_projet_id = ?";
            PreparedStatement pstmtProjets = conn.prepareStatement(sqlProjets);
            pstmtProjets.setString(1, chefId);
            ResultSet rsProjets = pstmtProjets.executeQuery();

            int nbProjets = 0;
            if (rsProjets.next()) {
                nbProjets = rsProjets.getInt("total");
            }

            if (activeProjectsText != null) {
                activeProjectsText.setText(String.valueOf(nbProjets));
            }


            String sqlSprints = "SELECT COUNT(*) as total FROM sprint_tab s " +
                    "INNER JOIN project_tab p ON s.projet_id = p.id " +
                    "WHERE p.chef_projet_id = ? AND s.statut = 'EN_COURS'";
            PreparedStatement pstmtSprints = conn.prepareStatement(sqlSprints);
            pstmtSprints.setString(1, chefId);
            ResultSet rsSprints = pstmtSprints.executeQuery();

            int nbSprints = 0;
            if (rsSprints.next()) {
                nbSprints = rsSprints.getInt("total");
            }

            if (activeSprintsText != null) {
                activeSprintsText.setText(String.valueOf(nbSprints));
            }


            String sqlTaches = "SELECT COUNT(*) as total FROM tache_tab t " +
                    "INNER JOIN project_tab p ON t.projet_id = p.id " +
                    "WHERE p.chef_projet_id = ? AND t.statut IN ('A_FAIRE', 'EN_COURS')";
            PreparedStatement pstmtTaches = conn.prepareStatement(sqlTaches);
            pstmtTaches.setString(1, chefId);
            ResultSet rsTaches = pstmtTaches.executeQuery();

            int nbTaches = 0;
            if (rsTaches.next()) {
                nbTaches = rsTaches.getInt("total");
            }

            if (pendingTasksText != null) {
                pendingTasksText.setText(String.valueOf(nbTaches));
            }


            String sqlMembres = "SELECT COUNT(DISTINCT mp.membre_id) as total " +
                    "FROM membre_project_tab mp " +
                    "INNER JOIN project_tab p ON mp.projet_id = p.id " +
                    "WHERE p.chef_projet_id = ?";
            PreparedStatement pstmtMembres = conn.prepareStatement(sqlMembres);
            pstmtMembres.setString(1, chefId);
            ResultSet rsMembres = pstmtMembres.executeQuery();

            int nbMembres = 0;
            if (rsMembres.next()) {
                nbMembres = rsMembres.getInt("total");
            }

            if (teamMembersText != null) {
                teamMembersText.setText(String.valueOf(nbMembres));
            }

        } catch (SQLException e) {
            showError("Erreur BD", "Erreur lors du chargement des statistiques : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupHoverEffects() {

    }


    @FXML
    private void handleCreateProject(MouseEvent event) {
        try {
            navigateToView("/com/example/proprojectfx/creationProjet.fxml", "Créer un Projet");
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la page de création de projet.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateSprint(MouseEvent event) {
        try {
            navigateToView("/com/example/proprojectfx/creationSprint.fxml", "Créer un Sprint");
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la page de création de sprint.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateTask(MouseEvent event) {
        try {
            navigateToView("/com/example/proprojectfx/creationTache.fxml", "Créer une Tâche");
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la page de création de tâche.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateTeam(MouseEvent event) {
        try {
            navigateToView("/com/example/proprojectfx/creationEquipe.fxml", "Créer une Équipe");
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la page de création d'équipe.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewProfile() {
        try {
            navigateToView("/com/example/proprojectfx/profile.fxml", "Mon Profil");
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir la page de profil.");
            e.printStackTrace();
        }
    }

    @FXML
    private void openCreerEquipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proprojectfx/creerEquipe.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) createSprintBox.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Créer une Équipe");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void openCreerSprint() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proprojectfx/creerSprint.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) createSprintBox.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Créer un Sprint");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void navigateToView(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = getStageFromAnyNode();

        if (stage != null) {
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } else {
            stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        }
    }

    private Stage getStageFromAnyNode() {
        if (profileButton != null && profileButton.getScene() != null) {
            return (Stage) profileButton.getScene().getWindow();
        }
        if (createProjectBox != null && createProjectBox.getScene() != null) {
            return (Stage) createProjectBox.getScene().getWindow();
        }
        if (createSprintBox != null && createSprintBox.getScene() != null) {
            return (Stage) createSprintBox.getScene().getWindow();
        }
        if (createTaskBox != null && createTaskBox.getScene() != null) {
            return (Stage) createTaskBox.getScene().getWindow();
        }
        if (createTeamBox != null && createTeamBox.getScene() != null) {
            return (Stage) createTeamBox.getScene().getWindow();
        }
        if (fullNameText != null && fullNameText.getScene() != null) {
            return (Stage) fullNameText.getScene().getWindow();
        }
        return null;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().get().getText().equals("OK");
    }

    @FXML
    private void refreshDashboard() {
        loadDashboardStatistics();
        showInfo("Actualisation", "Les statistiques ont été mises à jour.");
    }

    @FXML
    private void handleLogout() {
        if (showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            try {
                navigateToView("/com/example/proprojectfx/login.fxml", "Connexion - ProProject");
            } catch (IOException e) {
                showError("Erreur", "Impossible de revenir à l'écran de connexion.");
                e.printStackTrace();
            }
        }
    }
}