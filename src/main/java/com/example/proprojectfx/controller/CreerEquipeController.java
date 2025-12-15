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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreerEquipeController {

    @FXML private TextField nomEquipeField;
    @FXML private TextField departementField;
    @FXML private Button backButton;
    @FXML private ComboBox<String> projetCombo;
    @FXML private ComboBox<String> membreCombo;
    @FXML private ListView<String> membresList;
    @FXML private TableView<Equipe> equipesTable;
    @FXML private TableColumn<Equipe, String> nomColumn;
    @FXML private TableColumn<Equipe, String> departementColumn;
    @FXML private TableColumn<Equipe, String> projetColumn;
    @FXML private TableColumn<Equipe, Integer> membresCountColumn;
    @FXML private Button viewButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // ======================== DONNÉES ========================

    private ObservableList<String> membresSelectionnes = FXCollections.observableArrayList();
    private ObservableList<Equipe> equipesList = FXCollections.observableArrayList();
    private List<String> membresIds = new ArrayList<>();
    private String editingEquipeId = null;

//initialise la page creer equipe
    public void initialize() {
        membresList.setItems(membresSelectionnes);

        loadProjetsFromDB();
        loadMembresFromDB();
        setupTableColumns();
        loadEquipesFromDB();
    }

//charge les projet de la base donneé
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

            projetCombo.setItems(projets);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Impossible de charger les projets : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//charge les membre de bd
    private void loadMembresFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        ObservableList<String> membres = FXCollections.observableArrayList();

        try {
            String sql = "SELECT id, nom, prenom FROM user_tab WHERE type = 'MEMBRE' ORDER BY nom";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String membreDisplay = rs.getString("prenom") + " " + rs.getString("nom") +
                        " (ID: " + rs.getString("id") + ")";
                membres.add(membreDisplay);
            }

            membreCombo.setItems(membres);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Impossible de charger les membres : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

// libelise les colonnes
    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        departementColumn.setCellValueFactory(new PropertyValueFactory<>("departement"));
        projetColumn.setCellValueFactory(new PropertyValueFactory<>("projet"));
        membresCountColumn.setCellValueFactory(new PropertyValueFactory<>("membresCount"));
        equipesTable.setItems(equipesList);
    }

//charge les equipes de bd
    private void loadEquipesFromDB() {
        Connection conn = DatabaseConnection.getConnection();
        equipesList.clear();

        try {
            String sql = "SELECT e.id, e.nom, e.departement, p.titre as projet_titre, " +
                    "       (SELECT COUNT(*) FROM equipe_membre_tab em WHERE em.equipe_id = e.id) as nb_membres " +
                    "FROM equipe_tab e " +
                    "LEFT JOIN project_tab p ON e.projet_id = p.id " +
                    "ORDER BY e.date_creation DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String id = rs.getString("id");
                String nom = rs.getString("nom");
                String departement = rs.getString("departement");
                String projet = rs.getString("projet_titre");
                int nbMembres = rs.getInt("nb_membres");

                Equipe equipe = new Equipe(id, nom, departement,
                        projet != null ? projet : "Aucun projet", nbMembres);
                equipesList.add(equipe);
            }

            equipesTable.refresh();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Impossible de charger les équipes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//permet l'ajout d'un membre a la liste
    @FXML
    private void ajouterMembre() {
        String membreSelection = membreCombo.getValue();

        if (membreSelection == null || membreSelection.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sélection vide",
                    "Veuillez sélectionner un membre.");
            return;
        }

        String membreId = extractIdFromSelection(membreSelection);

        if (membreId == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'extraire l'ID du membre.");
            return;
        }

        if (membresIds.contains(membreId)) {
            showAlert(Alert.AlertType.WARNING, "Doublon",
                    "Ce membre est déjà dans la liste.");
            return;
        }

        membresSelectionnes.add(membreSelection);
        membresIds.add(membreId);
        membreCombo.setValue(null);
    }

//retire un membre de la liste
    @FXML
    private void retirerMembre() {
        String selected = membresList.getSelectionModel().getSelectedItem();

        if (selected != null) {
            int index = membresSelectionnes.indexOf(selected);
            membresSelectionnes.remove(selected);
            membresIds.remove(index);
        } else {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un membre à retirer.");
        }
    }

//creer equipe
    @FXML
    private void handleCreate() {
        // Si on est en mode modification on appele la methode updaet
        if (editingEquipeId != null) {
            handleUpdateEquipe();
            return;
        }

        String nom = nomEquipeField.getText().trim();
        String dep = departementField.getText().trim();
        String projetSelection = projetCombo.getValue();

//verification
        if (nom.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs Manquants",
                    "Veuillez remplir le nom de l'équipe.");
            return;
        }

        if (dep.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs Manquants",
                    "Veuillez remplir le département.");
            return;
        }

        if (membresIds.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun membre",
                    "Veuillez ajouter au moins un membre à l'équipe.");
            return;
        }

        String projetId = null;
        if (projetSelection != null) {
            projetId = extractIdFromSelection(projetSelection);
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            conn.setAutoCommit(false);

            //genere id automatiquement
            String equipeId = "E" + UUID.randomUUID().toString()
                    .replace("-", "").toUpperCase().substring(0, 8);

            // insere l'equipe dans la bd
            String sqlEquipe = "INSERT INTO equipe_tab (id, nom, departement, projet_id) " +
                    "VALUES (?, ?, ?, ?)";
            PreparedStatement pstmtEquipe = conn.prepareStatement(sqlEquipe);
            pstmtEquipe.setString(1, equipeId);
            pstmtEquipe.setString(2, nom);
            pstmtEquipe.setString(3, dep);
            pstmtEquipe.setString(4, projetId);
            pstmtEquipe.executeUpdate();

            // ajouter les membre de l'equipe
            String sqlMembre = "INSERT INTO equipe_membre_tab (equipe_id, membre_id) VALUES (?, ?)";
            PreparedStatement pstmtMembre = conn.prepareStatement(sqlMembre);

            for (String membreId : membresIds) {
                pstmtMembre.setString(1, equipeId);
                pstmtMembre.setString(2, membreId);
                pstmtMembre.executeUpdate();
            }

            // si un projet est sélectionne ajouter les membres au projet
            if (projetId != null) {
                String sqlProjet = "INSERT INTO membre_project_tab (membre_id, projet_id) " +
                        "SELECT ?, ? FROM dual " +
                        "WHERE NOT EXISTS (SELECT 1 FROM membre_project_tab " +
                        "WHERE membre_id = ? AND projet_id = ?)";
                PreparedStatement pstmtProjet = conn.prepareStatement(sqlProjet);

                for (String membreId : membresIds) {
                    pstmtProjet.setString(1, membreId);
                    pstmtProjet.setString(2, projetId);
                    pstmtProjet.setString(3, membreId);
                    pstmtProjet.setString(4, projetId);
                    pstmtProjet.executeUpdate();
                }
            }

            conn.commit();

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "L'équipe \"" + nom + "\" a été créée avec succès !\n" +
                            "ID: " + equipeId + "\n" +
                            membresIds.size() + " membre(s) ajouté(s).");

            clearForm();
            loadEquipesFromDB();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Erreur lors de la création : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

//reinitialise le forume
    private void clearForm() {
        nomEquipeField.clear();
        departementField.clear();
        projetCombo.setValue(null);
        membreCombo.setValue(null);
        membresSelectionnes.clear();
        membresIds.clear();
        resetEditMode();
    }

//affiche details
    @FXML
    private void handleViewEquipe() {
        Equipe selected = equipesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner une équipe à consulter.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        try {
            // Charger les détails complets de l'équipe
            String sql = "SELECT e.*, p.titre as projet_titre " +
                    "FROM equipe_tab e " +
                    "LEFT JOIN project_tab p ON e.projet_id = p.id " +
                    "WHERE e.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // charger list membre
                String sqlMembres = "SELECT u.prenom, u.nom, u.email, u.role " +
                        "FROM user_tab u " +
                        "JOIN equipe_membre_tab em ON u.id = em.membre_id " +
                        "WHERE em.equipe_id = ?";
                PreparedStatement pstmtMembres = conn.prepareStatement(sqlMembres);
                pstmtMembres.setString(1, selected.getId());
                ResultSet rsMembres = pstmtMembres.executeQuery();

                StringBuilder membresList = new StringBuilder();
                int count = 1;
                while (rsMembres.next()) {
                    membresList.append(count++).append(". ")
                            .append(rsMembres.getString("prenom")).append(" ")
                            .append(rsMembres.getString("nom")).append(" - ")
                            .append(rsMembres.getString("role")).append(" (")
                            .append(rsMembres.getString("email")).append(")\n");
                }

                String details = "═══════════════════════════════════\n" +
                        "           DÉTAILS DE L'ÉQUIPE\n" +
                        "═══════════════════════════════════\n\n" +
                        " ID: " + rs.getString("id") + "\n" +
                        " Nom: " + rs.getString("nom") + "\n" +
                        " Département: " + rs.getString("departement") + "\n" +
                        " Projet: " + (rs.getString("projet_titre") != null ?
                        rs.getString("projet_titre") : "Aucun") + "\n" +
                        " Date création: " + rs.getTimestamp("date_creation") + "\n\n" +
                        " MEMBRES DE L'ÉQUIPE (" + selected.getMembresCount() + "):\n" +
                        "───────────────────────────────────\n" +
                        (membresList.length() > 0 ? membresList.toString() : "Aucun membre");

                showAlert(Alert.AlertType.INFORMATION, "Détails de l'Équipe", details);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Impossible de charger les détails : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//modifie l'quipe
    @FXML
    private void handleEditEquipe() {
        Equipe selected = equipesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner une équipe à modifier.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        try {
            // charge les données de équipe
            String sql = "SELECT e.*, p.titre as projet_titre, p.id as projet_id " +
                    "FROM equipe_tab e " +
                    "LEFT JOIN project_tab p ON e.projet_id = p.id " +
                    "WHERE e.id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                //re rempli le formulaire
                nomEquipeField.setText(rs.getString("nom"));
                departementField.setText(rs.getString("departement"));

                String projetId = rs.getString("projet_id");
                if (projetId != null) {
                    selectProjetInComboBox(projetId);
                }

                membresSelectionnes.clear();
                membresIds.clear();

                String sqlMembres = "SELECT u.id, u.prenom, u.nom " +
                        "FROM user_tab u " +
                        "JOIN equipe_membre_tab em ON u.id = em.membre_id " +
                        "WHERE em.equipe_id = ?";
                PreparedStatement pstmtMembres = conn.prepareStatement(sqlMembres);
                pstmtMembres.setString(1, selected.getId());
                ResultSet rsMembres = pstmtMembres.executeQuery();

                while (rsMembres.next()) {
                    String membreId = rsMembres.getString("id");
                    String membreDisplay = rsMembres.getString("prenom") + " " +
                            rsMembres.getString("nom") + " (ID: " + membreId + ")";
                    membresSelectionnes.add(membreDisplay);
                    membresIds.add(membreId);
                }

                editingEquipeId = selected.getId();

                // change bouton en mode modification
                Button createBtn = (Button) nomEquipeField.getScene().lookup("#createButton");
                if (createBtn == null) {
                    nomEquipeField.getScene().getRoot().lookupAll(".button").forEach(node -> {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            if (btn.getText().contains("Créer") || btn.getText().contains("Mettre")) {
                                btn.setText("Mettre à Jour");
                                btn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; " +
                                        "-fx-font-weight: bold; -fx-cursor: hand;");
                            }
                        }
                    });
                }

                nomEquipeField.requestFocus();

                showAlert(Alert.AlertType.INFORMATION, "Mode Modification",
                        "L'équipe \"" + rs.getString("nom") + "\" est en cours de modification.\n" +
                                "Modifiez les champs et cliquez sur 'Mettre à Jour'.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Impossible de charger l'équipe : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//mise a jour equip
    private void handleUpdateEquipe() {
        String nom = nomEquipeField.getText().trim();
        String dep = departementField.getText().trim();
        String projetSelection = projetCombo.getValue();

        if (nom.isEmpty() || dep.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs Manquants",
                    "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        if (membresIds.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Aucun membre",
                    "Veuillez ajouter au moins un membre à l'équipe.");
            return;
        }

        String projetId = null;
        if (projetSelection != null) {
            projetId = extractIdFromSelection(projetSelection);
        }

        Connection conn = DatabaseConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // mettre a jour les infos
            String sqlUpdate = "UPDATE equipe_tab SET nom = ?, departement = ?, projet_id = ? WHERE id = ?";
            PreparedStatement pstmtUpdate = conn.prepareStatement(sqlUpdate);
            pstmtUpdate.setString(1, nom);
            pstmtUpdate.setString(2, dep);
            pstmtUpdate.setString(3, projetId);
            pstmtUpdate.setString(4, editingEquipeId);
            pstmtUpdate.executeUpdate();

            // suppression les anciens membres
            String sqlDeleteMembres = "DELETE FROM equipe_membre_tab WHERE equipe_id = ?";
            PreparedStatement pstmtDelete = conn.prepareStatement(sqlDeleteMembres);
            pstmtDelete.setString(1, editingEquipeId);
            pstmtDelete.executeUpdate();

            // ajoute les nouveaux membres
            String sqlInsertMembres = "INSERT INTO equipe_membre_tab (equipe_id, membre_id) VALUES (?, ?)";
            PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsertMembres);

            for (String membreId : membresIds) {
                pstmtInsert.setString(1, editingEquipeId);
                pstmtInsert.setString(2, membreId);
                pstmtInsert.executeUpdate();
            }

            // 4. Si projet assigné, synchroniser les membres
            if (projetId != null) {
                String sqlProjet = "INSERT INTO membre_project_tab (membre_id, projet_id) " +
                        "SELECT ?, ? FROM dual " +
                        "WHERE NOT EXISTS (SELECT 1 FROM membre_project_tab " +
                        "WHERE membre_id = ? AND projet_id = ?)";
                PreparedStatement pstmtProjet = conn.prepareStatement(sqlProjet);

                for (String membreId : membresIds) {
                    pstmtProjet.setString(1, membreId);
                    pstmtProjet.setString(2, projetId);
                    pstmtProjet.setString(3, membreId);
                    pstmtProjet.setString(4, projetId);
                    pstmtProjet.executeUpdate();
                }
            }

            conn.commit();

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "L'équipe \"" + nom + "\" a été mise à jour avec succès !");

            resetEditMode();
            clearForm();
            loadEquipesFromDB();

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Erreur lors de la mise à jour : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

//supprimer equipe
    @FXML
    private void handleDeleteEquipe() {
        Equipe selected = equipesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner une équipe à supprimer.");
            return;
        }

        // notif de confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'équipe \"" +
                selected.getNom() + "\" ?\n\n" +
                "Cette action est irréversible et supprimera :\n" +
                "- L'équipe elle-même\n" +
                "- Tous les liens avec les membres\n\n" +
                "Note : Les membres ne seront pas supprimés de la base.");

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        try {
            String sql = "DELETE FROM equipe_tab WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selected.getId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "L'équipe \"" + selected.getNom() + "\" a été supprimée avec succès.");
                loadEquipesFromDB();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de supprimer l'équipe. Elle n'existe peut-être plus.");
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD",
                    "Erreur lors de la suppression : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

//selection projet
    private void selectProjetInComboBox(String projetId) {
        ObservableList<String> items = projetCombo.getItems();
        for (String item : items) {
            if (item.contains("(" + projetId + ")")) {
                projetCombo.setValue(item);
                return;
            }
        }
    }

 //
    private void resetEditMode() {
        editingEquipeId = null;
        nomEquipeField.getScene().getRoot().lookupAll(".button").forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getText().contains("Mettre")) {
                    btn.setText("Créer Équipe");
                    btn.setStyle("-fx-background-color: #2778e4; -fx-text-fill: white; " +
                            "-fx-font-weight: bold; -fx-cursor: hand;");
                }
            }
        });
    }

//la fonction de retour vers dashbord
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                    .getResource("/com/example/proprojectfx/DashBoardChef.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Dashboard Chef de Projet");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation",
                    "Impossible de charger le Dashboard.");
        }
    }


    private String extractIdFromSelection(String selection) {
        if (selection == null) return null;

        if (selection.contains("(ID: ")) {
            int start = selection.lastIndexOf("(ID: ") + 5;
            int end = selection.lastIndexOf(")");
            return selection.substring(start, end);
        } else if (selection.contains("(") && selection.contains(")")) {
            int start = selection.lastIndexOf("(") + 1;
            int end = selection.lastIndexOf(")");
            return selection.substring(start, end);
        }

        return null;
    }
    //afficher alert
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

//on a pas pu importer les classses bcp d'erreur donc on recreer ici
    public static class Equipe {
        private String id;
        private String nom;
        private String departement;
        private String projet;
        private int membresCount;

        public Equipe(String id, String nom, String departement, String projet, int membresCount) {
            this.id = id;
            this.nom = nom;
            this.departement = departement;
            this.projet = projet;
            this.membresCount = membresCount;
        }

        public String getId() { return id; }
        public String getNom() { return nom; }
        public String getDepartement() { return departement; }
        public String getProjet() { return projet; }
        public int getMembresCount() { return membresCount; }

        public void setId(String id) { this.id = id; }
        public void setNom(String nom) { this.nom = nom; }
        public void setDepartement(String departement) { this.departement = departement; }
        public void setProjet(String projet) { this.projet = projet; }
        public void setMembresCount(int membresCount) { this.membresCount = membresCount; }
    }
}