package com.example.proprojectfx.controller;

import com.example.proprojectfx.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.UUID;

public class projetController {


    @FXML private TextField projectTitleField;
    @FXML private TextArea projectInfoArea;
    @FXML private ComboBox<String> chefProjetComboBox;
    @FXML private ComboBox<String> equipeComboBox;
    @FXML private Button createButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;


    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> idColumn;
    @FXML private TableColumn<Project, String> titleColumn;
    @FXML private TableColumn<Project, String> chefColumn;
    @FXML private TableColumn<Project, String> equipeColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private Button refreshButton;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;



    private ObservableList<Project> projectsList = FXCollections.observableArrayList();
    private String currentChefId; // ID du chef connecté
    private String editingProjectId = null; // ID du projet en cours de modification



    @FXML
    public void initialize() {
        // Charger les chefs de projet depuis la BD
        loadChefsProjetsFromDB();

        // Charger les équipes
        loadEquipes();

        // Configurer les colonnes du tableau
        setupTableColumns();

        // Charger les projets existants
        loadProjectsFromDB();

        // Configurer les événements des boutons
        setupButtonHandlers();
    }


    public void setCurrentChefId(String chefId) {
        this.currentChefId = chefId;
    }


    private void loadChefsProjetsFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        ObservableList<String> chefs = FXCollections.observableArrayList();

        try {
            String sql = "SELECT id, nom, prenom FROM user_tab WHERE type = 'CHEF_PROJET' ORDER BY nom";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String chefDisplay = rs.getString("prenom") + " " + rs.getString("nom") + " (" + rs.getString("id") + ")";
                chefs.add(chefDisplay);
            }

            chefProjetComboBox.setItems(chefs);

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les chefs de projet : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void loadEquipes() {
        ObservableList<String> equipes = FXCollections.observableArrayList(
                "Équipe Développement Frontend",
                "Équipe Développement Backend",
                "Équipe DevOps",
                "Équipe QA/Testing",
                "Équipe Design UI/UX",
                "Équipe Mobile",
                "Équipe Data Science"
        );
        equipeComboBox.setItems(equipes);
    }


    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        chefColumn.setCellValueFactory(new PropertyValueFactory<>("chefProjet"));
        equipeColumn.setCellValueFactory(new PropertyValueFactory<>("equipe"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        projectsTable.setItems(projectsList);

        projectsTable.setVisible(true);
    }


    private void loadProjectsFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        projectsList.clear();

        try {
            String sql = "SELECT p.id, p.titre, p.info, u.nom, u.prenom " +
                    "FROM project_tab p " +
                    "JOIN user_tab u ON p.chef_projet_id = u.id " +
                    "ORDER BY p.date_creation DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String id = rs.getString("id");
                String titre = rs.getString("titre");
                String chefNom = rs.getString("prenom") + " " + rs.getString("nom");

                // Pour l'équipe et le statut, vous pouvez les stocker dans project_tab ou utiliser des valeurs par défaut
                Project project = new Project(id, titre, chefNom, "Équipe assignée", "En cours");
                projectsList.add(project);
            }

            //Refrachir la table
            projectsTable.refresh();

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les projets : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void setupButtonHandlers() {
        createButton.setOnAction(event -> handleCreateProject());
        clearButton.setOnAction(event -> handleClearForm());
        refreshButton.setOnAction(event -> handleRefresh());
        viewButton.setOnAction(event -> handleViewProject());
        editButton.setOnAction(event -> handleEditProject());
        deleteButton.setOnAction(event -> handleDeleteProject());
        backButton.setOnAction(event -> handleBack());
    }




    @FXML
    private void handleCreateProject() {
        // Validation des champs
        if (!validateForm()) {
            return;
        }

        if (editingProjectId != null) {
            handleUpdateProject();
            return;
        }

        String titre = projectTitleField.getText().trim();
        String info = projectInfoArea.getText().trim();
        String chefSelection = chefProjetComboBox.getValue();
        String equipe = equipeComboBox.getValue();

        String chefId = extractIdFromSelection(chefSelection);
        if (chefId == null) {
            showError("Erreur", "Impossible d'extraire l'ID du chef de projet.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            String projectId = "P" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);

            String sql = "INSERT INTO project_tab (id, titre, info, chef_projet_id) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, projectId);
            pstmt.setString(2, titre);
            pstmt.setString(3, info);
            pstmt.setString(4, chefId);
            pstmt.executeUpdate();

            String sqlRelation = "INSERT INTO chefprojet_project_tab (chef_projet_id, projet_id) VALUES (?, ?)";
            PreparedStatement pstmtRelation = conn.prepareStatement(sqlRelation);
            pstmtRelation.setString(1, chefId);
            pstmtRelation.setString(2, projectId);
            pstmtRelation.executeUpdate();

            showInfo("Succès", "Le projet \"" + titre + "\" a été créé avec succès !\nID: " + projectId);

            loadProjectsFromDB();
            handleClearForm();

        } catch (SQLException e) {
            showError("Erreur BD", "Erreur lors de la création du projet : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private String extractIdFromSelection(String selection) {
        if (selection == null || !selection.contains("(") || !selection.contains(")")) {
            return null;
        }

        int start = selection.lastIndexOf("(") + 1;
        int end = selection.lastIndexOf(")");
        return selection.substring(start, end);
    }


    private void handleUpdateProject() {
        String titre = projectTitleField.getText().trim();
        String info = projectInfoArea.getText().trim();
        String chefSelection = chefProjetComboBox.getValue();
        String equipe = equipeComboBox.getValue();

        String chefId = extractIdFromSelection(chefSelection);
        if (chefId == null) {
            showError("Erreur", "Impossible d'extraire l'ID du chef de projet.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {

            String sql = "UPDATE project_tab SET titre = ?, info = ?, chef_projet_id = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, titre);
            pstmt.setString(2, info);
            pstmt.setString(3, chefId);
            pstmt.setString(4, editingProjectId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {

                String sqlDeleteRelation = "DELETE FROM chefprojet_project_tab WHERE projet_id = ?";
                PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteRelation);
                pstmtDelete.setString(1, editingProjectId);
                pstmtDelete.executeUpdate();

                String sqlInsertRelation = "INSERT INTO chefprojet_project_tab (chef_projet_id, projet_id) VALUES (?, ?)";
                PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertRelation);
                pstmtInsert.setString(1, chefId);
                pstmtInsert.setString(2, editingProjectId);
                pstmtInsert.executeUpdate();

                showInfo("Succès", "Le projet \"" + titre + "\" a été mis à jour avec succès !");

                // Réinitialiser le mode modification
                resetEditMode();

                // Recharger la liste
                loadProjectsFromDB();


                handleClearForm();
            } else {
                showError("Erreur", "Aucun projet n'a été mis à jour. Vérifiez que le projet existe toujours.");
            }

        } catch (SQLException e) {
            showError("Erreur BD", "Erreur lors de la mise à jour du projet : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    private void selectChefInComboBox(String chefId, String chefNom) {
        ObservableList<String> items = chefProjetComboBox.getItems();
        for (String item : items) {
            if (item.contains("(" + chefId + ")")) {
                chefProjetComboBox.setValue(item);
                return;
            }
        }
        for (String item : items) {
            if (item.startsWith(chefNom)) {
                chefProjetComboBox.setValue(item);
                return;
            }
        }
    }
    private void resetEditMode() {
        editingProjectId = null;
        createButton.setText("Créer le Projet");
        createButton.setStyle("-fx-background-color: #2778e4; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }
    private boolean validateForm() {
        if (projectTitleField.getText().trim().isEmpty()) {
            showError("Erreur de validation", "Le titre du projet est obligatoire.");
            projectTitleField.requestFocus();
            return false;
        }

        if (projectInfoArea.getText().trim().isEmpty()) {
            showError("Erreur de validation", "La description du projet est obligatoire.");
            projectInfoArea.requestFocus();
            return false;
        }

        if (chefProjetComboBox.getValue() == null) {
            showError("Erreur de validation", "Veuillez sélectionner un chef de projet.");
            chefProjetComboBox.requestFocus();
            return false;
        }

        if (equipeComboBox.getValue() == null) {
            showError("Erreur de validation", "Veuillez sélectionner une équipe.");
            equipeComboBox.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void handleClearForm() {
        projectTitleField.clear();
        projectInfoArea.clear();
        chefProjetComboBox.setValue(null);
        equipeComboBox.setValue(null);
        projectTitleField.requestFocus();

        resetEditMode();
    }


    @FXML
    private void handleRefresh() {
        loadProjectsFromDB();
        showInfo("Actualisation", "La liste des projets a été actualisée.");
    }


    @FXML
    private void handleViewProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un projet à consulter.");
            return;
        }

        // Charger les détails complets depuis la BD
        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "SELECT p.*, u.nom, u.prenom FROM project_tab p " +
                    "JOIN user_tab u ON p.chef_projet_id = u.id " +
                    "WHERE p.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String details = "ID: " + rs.getString("id") + "\n" +
                        "Titre: " + rs.getString("titre") + "\n" +
                        "Description: " + rs.getString("info") + "\n" +
                        "Chef: " + rs.getString("prenom") + " " + rs.getString("nom") + "\n" +
                        "Date création: " + rs.getTimestamp("date_creation");

                showInfo("Détails du Projet", details);
            }

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les détails : " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    @FXML
    private void handleEditProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un projet à modifier.");
            return;
        }

        // Charger les données complètes du projet depuis la BD
        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "SELECT p.*, u.id as chef_id, u.nom, u.prenom " +
                    "FROM project_tab p " +
                    "JOIN user_tab u ON p.chef_projet_id = u.id " +
                    "WHERE p.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Remplir le formulaire avec les données du projet
                projectTitleField.setText(rs.getString("titre"));
                projectInfoArea.setText(rs.getString("info"));

                // Sélectionner le chef de projet dans le ComboBox
                String chefId = rs.getString("chef_id");
                String chefNom = rs.getString("prenom") + " " + rs.getString("nom");
                selectChefInComboBox(chefId, chefNom);


                if (selected.getEquipe() != null && !selected.getEquipe().equals("Équipe assignée")) {
                    equipeComboBox.setValue(selected.getEquipe());
                }


                editingProjectId = selected.getId();


                createButton.setText("Mettre à Jour");
                createButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");


                projectTitleField.requestFocus();

                showInfo("Mode Modification",
                        "Le projet \"" + rs.getString("titre") + "\" est maintenant en cours de modification.\n" +
                                "Modifiez les champs et cliquez sur 'Mettre à Jour' pour sauvegarder.");
            }
        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les données du projet : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    @FXML
    private void handleDeleteProject() {
        Project selected = projectsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un projet à supprimer.");
            return;
        }

        // Demander confirmation
        if (showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir supprimer le projet \"" + selected.getTitre() + "\" ?\n" +
                        "Cette action est irréversible et supprimera aussi tous les sprints et tâches associés.")) {

            Connection conn = DatabaseConnection.getConnection();
            try {
                String sql = "DELETE FROM project_tab WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selected.getId());
                pstmt.executeUpdate();

                showInfo("Suppression", "Le projet a été supprimé avec succès.");
                loadProjectsFromDB();

            } catch (SQLException e) {
                showError("Erreur BD", "Erreur lors de la suppression : " + e.getMessage());
                e.printStackTrace();
            } finally {
                try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }


    @FXML
    private void handleBack() {
        try {
            navigateToView("/com/example/proprojectfx/DashBoardChef.fxml", "Dashboard Chef de Projet");
        } catch (IOException e) {
            showError("Erreur", "Impossible de revenir au dashboard.");
            e.printStackTrace();
        }
    }



    private void navigateToView(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
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


    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
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
        return alert.showAndWait().get() == ButtonType.OK;
    }

    // ======================== CLASSE MODÈLE PROJECT ========================


    public static class Project {
        private String id;
        private String titre;
        private String chefProjet;
        private String equipe;
        private String statut;

        public Project(String id, String titre, String chefProjet, String equipe, String statut) {
            this.id = id;
            this.titre = titre;
            this.chefProjet = chefProjet;
            this.equipe = equipe;
            this.statut = statut;
        }

        // Getters
        public String getId() { return id; }
        public String getTitre() { return titre; }
        public String getChefProjet() { return chefProjet; }
        public String getEquipe() { return equipe; }
        public String getStatut() { return statut; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setTitre(String titre) { this.titre = titre; }
        public void setChefProjet(String chefProjet) { this.chefProjet = chefProjet; }
        public void setEquipe(String equipe) { this.equipe = equipe; }
        public void setStatut(String statut) { this.statut = statut; }
    }
}