package com.example.proprojectfx.controller;

import com.example.proprojectfx.utils.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class CreationTacheController {

    @FXML private TextField taskTitleField;
    @FXML private TextArea taskInfoArea;
    @FXML private ComboBox<String> membreComboBox;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> projetComboBox;
    @FXML private ComboBox<String> difficulteComboBox;
    @FXML private Button createButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;

    @FXML private TableView<Tache> tasksTable;
    @FXML private TableColumn<Tache, String> idColumn;
    @FXML private TableColumn<Tache, String> titleColumn;
    @FXML private TableColumn<Tache, String> membreColumn;
    @FXML private TableColumn<Tache, String> statutColumn;
    @FXML private TableColumn<Tache, String> difficulteColumn;
    @FXML private Button refreshButton;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private ObservableList<Tache> tachesList = FXCollections.observableArrayList();
    private String tacheIdEnCoursEdition = null;

    @FXML
    public void initialize() {
        loadMembresFromDB();    //chargi les donnees
        loadProjetsFromDB();
        loadStatuts();
        loadDifficultes();

        setupTableColumns();

        loadTachesFromDB();

        setupButtonHandlers();
    }


    private void loadMembresFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        ObservableList<String> membres = FXCollections.observableArrayList();

        try {
            String sql = "SELECT id, nom, prenom FROM user_tab WHERE type = 'MEMBRE' ORDER BY nom";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String membreDisplay = rs.getString("prenom") + " " + rs.getString("nom") + " (" + rs.getString("id") + ")";
                membres.add(membreDisplay);
            }

            membreComboBox.setItems(membres);

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les membres : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void loadProjetsFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        ObservableList<String> projets = FXCollections.observableArrayList();

        try {
            String sql = "SELECT id, titre FROM project_tab ORDER BY date_creation DESC";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String projetDisplay = rs.getString("titre") + " (" + rs.getString("id") + ")";
                projets.add(projetDisplay);
            }

            projetComboBox.setItems(projets);

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les projets : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void loadStatuts() {
        ObservableList<String> statuts = FXCollections.observableArrayList(
                "A_FAIRE",
                "EN_COURS",
                "EN_REVUE",
                "TERMINEE",
                "BLOQUEE",
                "ANNULEE"
        );
        statutComboBox.setItems(statuts);
        statutComboBox.setValue("A_FAIRE");
    }


    private void loadDifficultes() {
        ObservableList<String> difficultes = FXCollections.observableArrayList(
                "Simple",
                "Moyenne",
                "Difficile"
        );
        difficulteComboBox.setItems(difficultes);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        membreColumn.setCellValueFactory(new PropertyValueFactory<>("membreAssigne"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        difficulteColumn.setCellValueFactory(new PropertyValueFactory<>("difficulte"));

        tasksTable.setItems(tachesList);
    }


    private void loadTachesFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        tachesList.clear();

        try {
            String sql = "SELECT t.id, t.titre, t.statut, t.difficulte, " +
                    "u.nom, u.prenom, p.titre as projet_titre " +
                    "FROM tache_tab t " +
                    "LEFT JOIN user_tab u ON t.membre_assigne_id = u.id " +
                    "LEFT JOIN project_tab p ON t.projet_id = p.id " +
                    "ORDER BY t.date_creation DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String id = rs.getString("id");
                String titre = rs.getString("titre");
                String statut = rs.getString("statut");
                String difficulte = rs.getString("difficulte");

                String membreNom = rs.getString("prenom");
                String membreDisplay = (membreNom != null) ?
                        membreNom + " " + rs.getString("nom") : "Non assigné";

                String projetTitre = rs.getString("projet_titre");

                Tache tache = new Tache(id, titre, membreDisplay, statut, difficulte, projetTitre);
                tachesList.add(tache);
            }

            tasksTable.refresh();

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les tâches : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void setupButtonHandlers() {
        createButton.setOnAction(event -> handleCreateTask());
        clearButton.setOnAction(event -> handleClearForm());
        refreshButton.setOnAction(event -> handleRefresh());
        viewButton.setOnAction(event -> handleViewTask());
        editButton.setOnAction(event -> handleEditTask());
        deleteButton.setOnAction(event -> handleDeleteTask());
    }


    @FXML
    private void handleCreateTask() {
        if (!validateForm()) {
            return;
        }

        if (tacheIdEnCoursEdition != null) {
            updateTask();
        } else {
            createNewTask();
        }
    }


    private void createNewTask() {
        String titre = taskTitleField.getText().trim();
        String info = taskInfoArea.getText().trim();
        String membreSelection = membreComboBox.getValue();
        String projetSelection = projetComboBox.getValue();
        String statut = statutComboBox.getValue();
        String difficulte = difficulteComboBox.getValue();

        String membreId = extractIdFromSelection(membreSelection);
        String projetId = extractIdFromSelection(projetSelection);

        if (membreId == null || projetId == null) {
            showError("Erreur", "Impossible d'extraire les IDs.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            String tacheId = "T" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);

            String sql = "INSERT INTO tache_tab (id, titre, tache_info, membre_assigne_id, statut, difficulte, projet_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, tacheId);
            pstmt.setString(2, titre);
            pstmt.setString(3, info);
            pstmt.setString(4, membreId);
            pstmt.setString(5, statut);
            pstmt.setString(6, difficulte);
            pstmt.setString(7, projetId);
            pstmt.executeUpdate();

            String sqlRelation = "INSERT INTO membre_tache_tab (membre_id, tache_id) VALUES (?, ?)";
            PreparedStatement pstmtRelation = conn.prepareStatement(sqlRelation);
            pstmtRelation.setString(1, membreId);
            pstmtRelation.setString(2, tacheId);
            pstmtRelation.executeUpdate();

            showInfo("Succès", "La tâche \"" + titre + "\" a été créée avec succès !\nID: " + tacheId);
            loadTachesFromDB();
            handleClearForm();

        } catch (SQLException e) {
            showError("Erreur BD", "Erreur lors de la création : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    private void updateTask() {
        String titre = taskTitleField.getText().trim();
        String info = taskInfoArea.getText().trim();
        String membreSelection = membreComboBox.getValue();
        String projetSelection = projetComboBox.getValue();
        String statut = statutComboBox.getValue();
        String difficulte = difficulteComboBox.getValue();

        String membreId = extractIdFromSelection(membreSelection);
        String projetId = extractIdFromSelection(projetSelection);

        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "UPDATE tache_tab SET titre = ?, tache_info = ?, membre_assigne_id = ?, " +
                    "statut = ?, difficulte = ?, projet_id = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, titre);
            pstmt.setString(2, info);
            pstmt.setString(3, membreId);
            pstmt.setString(4, statut);
            pstmt.setString(5, difficulte);
            pstmt.setString(6, projetId);
            pstmt.setString(7, tacheIdEnCoursEdition);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showInfo("Succès", "La tâche a été modifiée avec succès !");
                loadTachesFromDB();
                handleClearForm();
            }

        } catch (SQLException e) {
            showError("Erreur BD", "Erreur lors de la modification : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    @FXML
    private void handleViewTask() {
        Tache selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "SELECT t.*, u.nom, u.prenom, p.titre as projet_titre " +
                    "FROM tache_tab t " +
                    "LEFT JOIN user_tab u ON t.membre_assigne_id = u.id " +
                    "LEFT JOIN project_tab p ON t.projet_id = p.id " +
                    "WHERE t.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String details = "ID: " + rs.getString("id") + "\n" +
                        "Titre: " + rs.getString("titre") + "\n" +
                        "Description: " + rs.getString("tache_info") + "\n" +
                        "Membre: " + rs.getString("prenom") + " " + rs.getString("nom") + "\n" +
                        "Projet: " + rs.getString("projet_titre") + "\n" +
                        "Statut: " + rs.getString("statut") + "\n" +
                        "Difficulté: " + rs.getString("difficulte") + "\n" +
                        "Date création: " + rs.getTimestamp("date_creation");

                showInfo("Détails de la Tâche", details);
            }

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger les détails : " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    @FXML
    private void handleEditTask() {
        Tache selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "SELECT t.*, u.id as membre_id, p.id as projet_id FROM tache_tab t " +
                    "LEFT JOIN user_tab u ON t.membre_assigne_id = u.id " +
                    "LEFT JOIN project_tab p ON t.projet_id = p.id " +
                    "WHERE t.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                taskTitleField.setText(rs.getString("titre"));
                taskInfoArea.setText(rs.getString("tache_info"));
                statutComboBox.setValue(rs.getString("statut"));
                difficulteComboBox.setValue(rs.getString("difficulte"));

                // selecty membre
                String membreId = rs.getString("membre_id");
                for (String item : membreComboBox.getItems()) {
                    if (item.contains("(" + membreId + ")")) {
                        membreComboBox.setValue(item);
                        break;
                    }
                }

                // selecty projet
                String projetId = rs.getString("projet_id");
                for (String item : projetComboBox.getItems()) {
                    if (item.contains("(" + projetId + ")")) {
                        projetComboBox.setValue(item);
                        break;
                    }
                }

                tacheIdEnCoursEdition = selected.getId();
                createButton.setText("Mettre à Jour");
                createButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");

                showInfo("Mode Édition", "Vous pouvez maintenant modifier la tâche.");
            }

        } catch (SQLException e) {
            showError("Erreur BD", "Impossible de charger la tâche : " + e.getMessage());
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    @FXML
    private void handleDeleteTask() {
        Tache selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche.");
            return;
        }

        if (showConfirmation("Confirmation", "Êtes-vous sûr de vouloir supprimer cette tâche ?")) {
            Connection conn = DatabaseConnection.getConnection();
            try {
                String sql = "DELETE FROM tache_tab WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, selected.getId());
                pstmt.executeUpdate();

                showInfo("Suppression", "La tâche a été supprimée avec succès.");
                loadTachesFromDB();

            } catch (SQLException e) {
                showError("Erreur BD", "Erreur lors de la suppression : " + e.getMessage());
            } finally {
                try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }


    @FXML
    private void handleRefresh() {
        loadTachesFromDB();
        showInfo("Actualisation", "La liste des tâches a été actualisée.");
    }


    @FXML
    private void handleClearForm() {
        taskTitleField.clear();
        taskInfoArea.clear();
        membreComboBox.setValue(null);
        projetComboBox.setValue(null);
        statutComboBox.setValue("A_FAIRE");
        difficulteComboBox.setValue(null);

        tacheIdEnCoursEdition = null;
        createButton.setText("Créer la Tâche");
        createButton.setStyle("-fx-background-color: #2778e4; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
    }


    @FXML
    private void handleBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proprojectfx/DashBoardChef.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean validateForm() {
        if (taskTitleField.getText().trim().isEmpty()) {
            showError("Erreur", "Le titre est obligatoire.");
            return false;
        }
        if (taskInfoArea.getText().trim().isEmpty()) {
            showError("Erreur", "La description est obligatoire.");
            return false;
        }
        if (membreComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner un membre.");
            return false;
        }
        if (projetComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner un projet.");
            return false;
        }
        if (statutComboBox.getValue() == null) {
            showError("Erreur", "Veuillez sélectionner un statut.");
            return false;
        }
        return true;
    }

    private String extractIdFromSelection(String selection) {
        if (selection == null || !selection.contains("(") || !selection.contains(")")) {
            return null;
        }
        int start = selection.lastIndexOf("(") + 1;
        int end = selection.lastIndexOf(")");
        return selection.substring(start, end);
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


    public static class Tache {
        private String id;
        private String titre;
        private String membreAssigne;
        private String statut;
        private String difficulte;
        private String projetId;

        public Tache(String id, String titre, String membreAssigne, String statut, String difficulte, String projetId) {
            this.id = id;
            this.titre = titre;
            this.membreAssigne = membreAssigne;
            this.statut = statut;
            this.difficulte = difficulte;
            this.projetId = projetId;
        }

        public String getId() { return id; }
        public String getTitre() { return titre; }
        public String getMembreAssigne() { return membreAssigne; }
        public String getStatut() { return statut; }
        public String getDifficulte() { return difficulte; }
        public String getProjetId() { return projetId; }

        public void setId(String id) { this.id = id; }
        public void setTitre(String titre) { this.titre = titre; }
        public void setMembreAssigne(String membreAssigne) { this.membreAssigne = membreAssigne; }
        public void setStatut(String statut) { this.statut = statut; }
        public void setDifficulte(String difficulte) { this.difficulte = difficulte; }
        public void setProjetId(String projetId) { this.projetId = projetId; }
    }
}