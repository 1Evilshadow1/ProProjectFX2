package com.example.proprojectfx.controller;

import com.example.proprojectfx.models.Projet.Project;
import com.example.proprojectfx.models.Projet.Tache;
import com.example.proprojectfx.models.User.Membre;
import com.example.proprojectfx.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashBoardMembreController {

    // ======================== ÉLÉMENTS FXML ========================

    @FXML private Text fullNameText;
    @FXML private Text roleText;
    @FXML private Text tasksCountText;
    @FXML private Text projectsCountText;
    @FXML private TableView<TacheDisplay> tasksTable;
    @FXML private TableView<ProjectDisplay> projectsTable;
    @FXML private Button logoutButton;
    @FXML private Button refreshButton;

    private String currentMembreId;

    @FXML
    public void initialize() {
        setupTables();
    }

    private void setupTables() {
        // Configuration de la table des tâches
        if (tasksTable.getColumns().size() >= 4) {
            tasksTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("titre"));
            tasksTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("info"));
            tasksTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("statut"));
            tasksTable.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("difficulte"));
        }

        // Configuration de la table des projets
        if (projectsTable.getColumns().size() >= 3) {
            projectsTable.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("titre"));
            projectsTable.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("info"));
            projectsTable.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("chefNom"));
        }
    }

    // ======================== CHARGEMENT DES DONNÉES ========================

    private void loadData() {
        if (currentMembreId == null || currentMembreId.isEmpty()) {
            showError("Erreur", "Aucun membre connecté.");
            return;
        }

        loadMembreInfo();
        loadTaches();
        loadProjets();
    }

    /**
     * Charge les informations du membre (nom, prénom, rôle)
     */
    private void loadMembreInfo() {
        String query = "SELECT nom, prenom, role FROM user_tab WHERE id = ? AND type = 'MEMBRE'";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, currentMembreId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String role = rs.getString("role");

                fullNameText.setText(prenom + " " + nom);
                roleText.setText(role != null ? role : "Membre");
            } else {
                showError("Erreur", "Membre non trouvé dans la base de données.");
            }

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les informations du membre.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Charge les tâches assignées au membre depuis la base de données
     */
    private void loadTaches() {
        ObservableList<TacheDisplay> taches = FXCollections.observableArrayList();

        String query = "SELECT t.id, t.titre, t.tache_info, t.statut, t.difficulte, t.projet_id " +
                "FROM tache_tab t " +
                "WHERE t.membre_assigne_id = ? " +
                "ORDER BY t.date_creation DESC";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, currentMembreId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TacheDisplay tache = new TacheDisplay(
                        rs.getString("id"),
                        rs.getString("titre"),
                        rs.getString("tache_info"),
                        rs.getString("statut"),
                        rs.getString("difficulte"),
                        rs.getString("projet_id")
                );
                taches.add(tache);
            }

            tasksTable.setItems(taches);
            tasksCountText.setText(String.valueOf(taches.size()));

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les tâches.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void loadProjets() {
        ObservableList<ProjectDisplay> projets = FXCollections.observableArrayList();

        String query = "SELECT p.id, p.titre, p.info, u.nom || ' ' || u.prenom as chef_nom " +
                "FROM project_tab p " +
                "JOIN user_tab u ON p.chef_projet_id = u.id " +
                "JOIN membre_project_tab mp ON p.id = mp.projet_id " +
                "WHERE mp.membre_id = ? " +
                "ORDER BY p.date_creation DESC";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, currentMembreId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ProjectDisplay projet = new ProjectDisplay(
                        rs.getString("id"),
                        rs.getString("titre"),
                        rs.getString("info"),
                        rs.getString("chef_nom")
                );
                projets.add(projet);
            }

            projectsTable.setItems(projets);
            projectsCountText.setText(String.valueOf(projets.size()));

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les projets.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Actualise toutes les données du dashboard
     */
    @FXML
    private void refreshDashboard() {
        loadData();
        showInfo("Actualisation", "Les données ont été mises à jour.");
    }

    /**
     * Gère la déconnexion
     */
    @FXML
    private void handleLogout() {
        if (showConfirmation("Déconnexion", "Êtes-vous sûr de vouloir vous déconnecter ?")) {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proprojectfx/login.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Connexion - ProProject");
            } catch (IOException e) {
                showError("Erreur", "Impossible de revenir à la connexion.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Affiche les détails d'une tâche sélectionnée
     */
    @FXML
    private void handleViewTaskDetails() {
        TacheDisplay selected = tasksTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche.");
            return;
        }

        String details = "ID: " + selected.getId() + "\n" +
                "Titre: " + selected.getTitre() + "\n" +
                "Description: " + selected.getInfo() + "\n" +
                "Statut: " + selected.getStatut() + "\n" +
                "Difficulté: " + selected.getDifficulte();

        showInfo("Détails de la Tâche", details);
    }

    /**
     * Affiche les détails d'un projet sélectionné
     */
    @FXML
    private void handleViewProjectDetails() {
        ProjectDisplay selected = projectsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un projet.");
            return;
        }

        String details = "ID: " + selected.getId() + "\n" +
                "Titre: " + selected.getTitre() + "\n" +
                "Description: " + selected.getInfo() + "\n" +
                "Chef de Projet: " + selected.getChefNom();

        showInfo("Détails du Projet", details);
    }

    /**
     * Met à jour le statut d'une tâche
     */
    @FXML
    private void handleUpdateTaskStatus() {
        TacheDisplay selected = tasksTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche à modifier.");
            return;
        }

        // Créer une boîte de dialogue pour choisir le nouveau statut
        ChoiceDialog<String> dialog = new ChoiceDialog<>("EN_COURS",
                "A_FAIRE", "EN_COURS", "EN_REVUE", "TERMINEE", "BLOQUEE");
        dialog.setTitle("Modifier le statut");
        dialog.setHeaderText("Tâche: " + selected.getTitre());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(newStatut -> {
            updateTaskStatus(selected.getId(), newStatut);
        });
    }

    /**
     * Met à jour le statut d'une tâche dans la base de données
     */
    private void updateTaskStatus(String tacheId, String newStatut) {
        String query = "UPDATE tache_tab SET statut = ? WHERE id = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatut);
            stmt.setString(2, tacheId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                showInfo("Succès", "Le statut de la tâche a été mis à jour.");
                loadTaches(); // Recharger les tâches
            }

        } catch (SQLException e) {
            showError("Erreur", "Impossible de mettre à jour le statut: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /**
     * Définit l'ID du membre connecté et charge ses données
     */
    public void setCurrentMembreId(String membreId) {
        this.currentMembreId = membreId;
        loadData();
    }

    /**
     * Définit le membre connecté à partir d'un objet Membre
     */
    public void setCurrentMembre(Membre membre) {
        if (membre != null) {
            this.currentMembreId = membre.getId();
            loadData();
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue d'avertissement
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Affiche une boîte de dialogue de confirmation
     */
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().get() == ButtonType.OK;
    }

    // ======================== CLASSES MODÈLES POUR AFFICHAGE ========================

    /**
     * Classe pour afficher les tâches dans le TableView
     */
    public static class TacheDisplay {
        private String id;
        private String titre;
        private String info;
        private String statut;
        private String difficulte;
        private String projetId;

        public TacheDisplay(String id, String titre, String info, String statut,
                            String difficulte, String projetId) {
            this.id = id;
            this.titre = titre;
            this.info = info;
            this.statut = statut;
            this.difficulte = difficulte;
            this.projetId = projetId;
        }

        // Getters
        public String getId() { return id; }
        public String getTitre() { return titre; }
        public String getInfo() { return info; }
        public String getStatut() { return statut; }
        public String getDifficulte() { return difficulte; }
        public String getProjetId() { return projetId; }

        // Setters
        public void setStatut(String statut) { this.statut = statut; }
    }

    /**
     * Classe pour afficher les projets dans le TableView
     */
    public static class ProjectDisplay {
        private String id;
        private String titre;
        private String info;
        private String chefNom;

        public ProjectDisplay(String id, String titre, String info, String chefNom) {
            this.id = id;
            this.titre = titre;
            this.info = info;
            this.chefNom = chefNom;
        }

        // Getters
        public String getId() { return id; }
        public String getTitre() { return titre; }
        public String getInfo() { return info; }
        public String getChefNom() { return chefNom; }
    }
}