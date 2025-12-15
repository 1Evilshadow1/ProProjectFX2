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

public class CreerSprintController {

    @FXML private TextField idSprintField;
    @FXML private TextArea objectifArea;
    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<String> projetCombo;
    @FXML private ComboBox<String> tachesCombo;
    @FXML private ListView<String> tachesListView;
    @FXML private Button createButton;
    @FXML private Button clearButton;
    @FXML private Button backButton;

    @FXML private TableView<Sprint> sprintsTable;
    @FXML private TableColumn<Sprint, String> idColumn;
    @FXML private TableColumn<Sprint, String> objectifColumn;
    @FXML private TableColumn<Sprint, LocalDate> dateDebutColumn;
    @FXML private TableColumn<Sprint, LocalDate> dateFinColumn;
    @FXML private TableColumn<Sprint, String> statutColumn;
    @FXML private Button refreshButton;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private ObservableList<Sprint> sprintsList = FXCollections.observableArrayList();
    private ObservableList<String> tachesSelectionnees = FXCollections.observableArrayList();
    private String currentChefId = "U001";
    private boolean isEditMode = false;

//initialisation de tab
    @FXML
    public void initialize() {
        setupTableColumns();
        loadProjets();
        loadSprints();
        setupButtonHandlers();

        projetCombo.setOnAction(event -> {
            loadTachesParProjet();
        });
        tachesListView.setItems(tachesSelectionnees);
    }

//libeliser colonnes
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idSprint"));
        objectifColumn.setCellValueFactory(new PropertyValueFactory<>("objectif"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        sprintsTable.setItems(sprintsList);
    }

 //charger list projets
    private void loadProjets() {
        ObservableList<String> projets = FXCollections.observableArrayList();

        String query = "SELECT id, titre FROM project_tab ORDER BY date_creation DESC";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String projetDisplay = rs.getString("id") + " - " + rs.getString("titre");
                projets.add(projetDisplay);
            }

            projetCombo.setItems(projets);

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger la liste des projets.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//charger taches
    private void loadTachesParProjet() {
        tachesCombo.getItems().clear();
        tachesCombo.setValue(null);

        if (projetCombo.getValue() == null) {
            tachesCombo.setPromptText("Sélectionnez d'abord un projet");
            return;
        }

        String projetId = extractId(projetCombo.getValue());

        ObservableList<String> taches = FXCollections.observableArrayList();

        String query = "SELECT id, titre, statut FROM tache_tab " +
                "WHERE projet_id = ? " +
                "ORDER BY titre";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, projetId);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                String tacheDisplay = rs.getString("id") + " - " + rs.getString("titre") +
                        " [" + rs.getString("statut") + "]";
                taches.add(tacheDisplay);
                count++;
            }

            tachesCombo.setItems(taches);

            if (count == 0) {
                tachesCombo.setPromptText("Aucune tâche disponible pour ce projet");
            } else {
                tachesCombo.setPromptText("Sélectionnez une tâche (" + count + " disponible(s))");
            }

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger les tâches du projet.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//charger sprint de bd
    private void loadSprints() {
        sprintsList.clear();

        String query = "SELECT id_sprint, objectif, date_debut, date_fin, statut, projet_id " +
                "FROM sprint_tab ORDER BY date_debut DESC";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Sprint sprint = new Sprint(
                        rs.getString("id_sprint"),
                        rs.getString("objectif"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getString("statut"),
                        rs.getString("projet_id")
                );
                sprintsList.add(sprint);
            }

        } catch (SQLException e) {
            showError("Erreur de chargement", "Impossible de charger la liste des sprints.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void setupButtonHandlers() {
        createButton.setOnAction(event -> handleCreate());
        clearButton.setOnAction(event -> handleClearForm());
        refreshButton.setOnAction(event -> handleRefresh());
        viewButton.setOnAction(event -> handleViewSprint());
        editButton.setOnAction(event -> handleEditSprint());
        deleteButton.setOnAction(event -> handleDeleteSprint());
        backButton.setOnAction(event -> handleBack());
    }


//ajout tache
    @FXML
    private void ajouterTache() {
        String tacheSelectionnee = tachesCombo.getValue();

        if (tacheSelectionnee == null || tacheSelectionnee.isEmpty()) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche dans la liste.");
            return;
        }

        if (tachesSelectionnees.contains(tacheSelectionnee)) {
            showWarning("Tâche déjà ajoutée", "Cette tâche est déjà dans la liste.");
            return;
        }
        tachesSelectionnees.add(tacheSelectionnee);
        tachesCombo.getSelectionModel().clearSelection();

        showInfo("Tâche ajoutée", "La tâche a été ajoutée à la liste (" + tachesSelectionnees.size() + " tâche(s)).");
    }

//retire tache de liste
    @FXML
    private void retirerTache() {
        String tacheSelectionnee = tachesListView.getSelectionModel().getSelectedItem();

        if (tacheSelectionnee == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une tâche à retirer de la liste.");
            return;
        }
        tachesSelectionnees.remove(tacheSelectionnee);
        showInfo("Tâche retirée", "La tâche a été retirée de la liste.");
    }


//creation sprint
    @FXML
    private void handleCreate() {
        // Validation des champs
        if (!validateForm()) {
            return;
        }

        //verifier si creer ou editer
        if (isEditMode) {
            handleUpdate();
        } else {
            handleInsert();
        }
    }

    private void handleInsert() {
        String idSprint = idSprintField.getText().trim();
        String objectif = objectifArea.getText().trim();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String statut = statutCombo.getValue();
        String projetId = extractId(projetCombo.getValue());

        String query = "INSERT INTO sprint_tab (id_sprint, objectif, date_debut, date_fin, statut, projet_id, chef_projet_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idSprint);
            stmt.setString(2, objectif);
            stmt.setDate(3, Date.valueOf(dateDebut));
            stmt.setDate(4, Date.valueOf(dateFin));
            stmt.setString(5, statut);
            stmt.setString(6, projetId);
            stmt.setString(7, currentChefId);

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                if (!tachesSelectionnees.isEmpty()) {
                    int nbTachesAssociees = 0;
                    for (String tacheDisplay : tachesSelectionnees) {
                        String tacheId = extractId(tacheDisplay);
                        if (associerTacheAuSprint(idSprint, tacheId)) {
                            nbTachesAssociees++;
                        }
                    }

                    String message = "Le sprint \"" + idSprint + "\" a été créé avec succès !";
                    if (nbTachesAssociees > 0) {
                        message += "\n" + nbTachesAssociees + " tâche(s) associée(s).";
                    }
                    showInfo("Succès", message);
                } else {
                    showInfo("Succès", "Le sprint \"" + idSprint + "\" a été créé avec succès !");
                }
                handleClearForm();
                loadSprints();
            }

        } catch (SQLException e) {
            if (e.getErrorCode() == 1) {
                showError("Erreur", "Un sprint avec cet ID existe déjà.");
            } else {
                showError("Erreur de création", "Impossible de créer le sprint: " + e.getMessage());
            }
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // NOUVELLE MÉTHODE: Mise à jour d'un sprint existant
    private void handleUpdate() {
        String idSprint = idSprintField.getText().trim();
        String objectif = objectifArea.getText().trim();
        LocalDate dateDebut = dateDebutPicker.getValue();
        LocalDate dateFin = dateFinPicker.getValue();
        String statut = statutCombo.getValue();
        String projetId = extractId(projetCombo.getValue());

        String query = "UPDATE sprint_tab SET objectif = ?, date_debut = ?, date_fin = ?, " +
                "statut = ?, projet_id = ? WHERE id_sprint = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, objectif);
            stmt.setDate(2, Date.valueOf(dateDebut));
            stmt.setDate(3, Date.valueOf(dateFin));
            stmt.setString(4, statut);
            stmt.setString(5, projetId);
            stmt.setString(6, idSprint);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                // Gérer les tâches si nécessaire
                if (!tachesSelectionnees.isEmpty()) {
                    int nbTachesAssociees = 0;
                    for (String tacheDisplay : tachesSelectionnees) {
                        String tacheId = extractId(tacheDisplay);
                        if (associerTacheAuSprint(idSprint, tacheId)) {
                            nbTachesAssociees++;
                        }
                    }

                    String message = "Le sprint \"" + idSprint + "\" a été modifié avec succès !";
                    if (nbTachesAssociees > 0) {
                        message += "\n" + nbTachesAssociees + " tâche(s) associée(s).";
                    }
                    showInfo("Succès", message);
                } else {
                    showInfo("Succès", "Le sprint \"" + idSprint + "\" a été modifié avec succès !");
                }
                handleClearForm();
                loadSprints();
            }

        } catch (SQLException e) {
            showError("Erreur de modification", "Impossible de modifier le sprint: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//associer une tache a un sprint
    private boolean associerTacheAuSprint(String sprintId, String tacheId) {
        String query = "UPDATE tache_tab SET sprint_id = ? WHERE id = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            return false;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sprintId);
            stmt.setString(2, tacheId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            showWarning("Avertissement", "Le sprint a été créé mais une tâche n'a pas pu être associée.");
            e.printStackTrace();
            return false;
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//verification
    private boolean validateForm() {
        if (idSprintField.getText().trim().isEmpty()) {
            showError("Erreur de validation", "L'ID du sprint est obligatoire.");
            idSprintField.requestFocus();
            return false;
        }
        if (objectifArea.getText().trim().isEmpty()) {
            showError("Erreur de validation", "L'objectif du sprint est obligatoire.");
            objectifArea.requestFocus();
            return false;
        }
        if (dateDebutPicker.getValue() == null) {
            showError("Erreur de validation", "La date de début est obligatoire.");
            dateDebutPicker.requestFocus();
            return false;
        }
        if (dateFinPicker.getValue() == null) {
            showError("Erreur de validation", "La date de fin est obligatoire.");
            dateFinPicker.requestFocus();
            return false;
        }
        if (dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) {
            showError("Erreur de validation", "La date de fin doit être après la date de début.");
            dateFinPicker.requestFocus();
            return false;
        }
        if (statutCombo.getValue() == null) {
            showError("Erreur de validation", "Veuillez sélectionner un statut.");
            statutCombo.requestFocus();
            return false;
        }
        if (projetCombo.getValue() == null) {
            showError("Erreur de validation", "Veuillez sélectionner un projet.");
            projetCombo.requestFocus();
            return false;
        }
        return true;
    }

//reinitialisation
    @FXML
    private void handleClearForm() {
        idSprintField.clear();
        objectifArea.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        statutCombo.setValue(null);
        projetCombo.setValue(null);
        tachesCombo.setValue(null);
        tachesCombo.getItems().clear();
        tachesCombo.setPromptText("Sélectionnez d'abord un projet");
        tachesSelectionnees.clear();

        // MODIFICATION: Réinitialiser le mode édition
        isEditMode = false;
        idSprintField.setEditable(true);
        createButton.setText("Créer Sprint");

        idSprintField.requestFocus();
    }

//actualiser liste des sprints
    @FXML
    private void handleRefresh() {
        loadProjets();
        loadSprints();
        showInfo("Actualisation", "La liste des sprints a été actualisée.");
    }

//afficher detaiks sprint
    @FXML
    private void handleViewSprint() {
        Sprint selected = sprintsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un sprint à consulter.");
            return;
        }
//chargement depuis bd
        String query = "SELECT s.*, p.titre as projet_titre, " +
                "(SELECT COUNT(*) FROM tache_tab WHERE sprint_id = s.id_sprint) as nb_taches " +
                "FROM sprint_tab s " +
                "JOIN project_tab p ON s.projet_id = p.id " +
                "WHERE s.id_sprint = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, selected.getIdSprint());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String details = "═══════════════════════════════════════\n" +
                        "           DÉTAILS DU SPRINT\n" +
                        "═══════════════════════════════════════\n\n" +
                        " ID Sprint: " + rs.getString("id_sprint") + "\n" +
                        " Objectif: " + rs.getString("objectif") + "\n\n" +
                        " Projet: " + rs.getString("projet_titre") + "\n" +
                        " Date Début: " + rs.getDate("date_debut") + "\n" +
                        " Date Fin: " + rs.getDate("date_fin") + "\n" +
                        " Statut: " + rs.getString("statut") + "\n\n" +
                        " Nombre de tâches: " + rs.getInt("nb_taches") + "\n" +
                        "═══════════════════════════════════════";

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Détails du Sprint");
                alert.setHeaderText("Sprint : " + rs.getString("id_sprint"));
                alert.setContentText(details);
                alert.getDialogPane().setPrefWidth(500);
                alert.showAndWait();
            }

        } catch (SQLException e) {
            showError("Erreur", "Impossible de charger les détails du sprint.");
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//modifier sprint
    @FXML
    private void handleEditSprint() {
        Sprint selected = sprintsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un sprint à modifier.");
            return;
        }

        // MODIFICATION: Activer le mode édition
        isEditMode = true;

        idSprintField.setText(selected.getIdSprint());
        idSprintField.setEditable(false);
        objectifArea.setText(selected.getObjectif());
        dateDebutPicker.setValue(selected.getDateDebut());
        dateFinPicker.setValue(selected.getDateFin());
        statutCombo.setValue(selected.getStatut());

        for (String projet : projetCombo.getItems()) {
            if (projet.startsWith(selected.getProjetId())) {
                projetCombo.setValue(projet);
                break;
            }
        }

        loadTachesParProjet();

        // MODIFICATION: Changer le texte du bouton
        createButton.setText("Mettre à jour");

        showInfo("Mode Édition", "Vous pouvez maintenant modifier le sprint \"" + selected.getIdSprint() + "\".\nCliquez sur 'Mettre à jour' pour enregistrer les modifications.");
    }
//suprimer sprint
    @FXML
    private void handleDeleteSprint() {
        Sprint selected = sprintsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un sprint à supprimer.");
            return;
        }

        if (!showConfirmation("Confirmation",
                "Êtes-vous sûr de vouloir supprimer le sprint \"" + selected.getIdSprint() + "\" ?")) {
            return;
        }

        String query = "DELETE FROM sprint_tab WHERE id_sprint = ?";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showError("Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, selected.getIdSprint());
            int rowsDeleted = stmt.executeUpdate();

            if (rowsDeleted > 0) {
                sprintsList.remove(selected);
                showInfo("Suppression", "Le sprint a été supprimé avec succès.");
            }

        } catch (SQLException e) {
            showError("Erreur de suppression", "Impossible de supprimer le sprint: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

 //retour dashbord
    @FXML
    private void handleBack() {
        try {
            navigateToView("/com/example/proprojectfx/dashboardChef.fxml", "Dashboard Chef de Projet");
        } catch (IOException e) {
            showError("Erreur", "Impossible de revenir au dashboard.");
            e.printStackTrace();
        }
    }

    private String extractId(String displayString) {
        if (displayString == null || !displayString.contains(" - ")) {
            return displayString;
        }
        return displayString.split(" - ")[0].trim();
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

//erreur
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

//info
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

//avertissement
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

//confirmation
    private boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().get() == ButtonType.OK;
    }

    public void setCurrentChef(String chefId) {
        this.currentChefId = chefId;
        loadProjets();
        loadSprints();
    }

    public static class Sprint {
        private String idSprint;
        private String objectif;
        private LocalDate dateDebut;
        private LocalDate dateFin;
        private String statut;
        private String projetId;

        public Sprint(String idSprint, String objectif, LocalDate dateDebut,
                      LocalDate dateFin, String statut, String projetId) {
            this.idSprint = idSprint;
            this.objectif = objectif;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
            this.statut = statut;
            this.projetId = projetId;
        }

        public String getIdSprint() { return idSprint; }
        public String getObjectif() { return objectif; }
        public LocalDate getDateDebut() { return dateDebut; }
        public LocalDate getDateFin() { return dateFin; }
        public String getStatut() { return statut; }
        public String getProjetId() { return projetId; }

        public void setIdSprint(String idSprint) { this.idSprint = idSprint; }
        public void setObjectif(String objectif) { this.objectif = objectif; }
        public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
        public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
        public void setStatut(String statut) { this.statut = statut; }
        public void setProjetId(String projetId) { this.projetId = projetId; }
    }
}