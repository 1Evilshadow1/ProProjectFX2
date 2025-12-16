package com.example.proprojectfx.controller;

import com.example.proprojectfx.models.User.Administrateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.example.proprojectfx.utils.DatabaseConnection;
import java.sql.*;
import java.util.UUID;

import java.io.IOException;
import java.util.ArrayList;

public class DashBoardAdminController {

    @FXML private Text fullNameText;
    @FXML private TableView<UserDisplay> usersTable;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField mdpField;
    @FXML private Button logoutButton;
    @FXML private TextField dispoField;

    private Administrateur currentAdmin;

    public static class UserDisplay {
        private String id;
        private String nom;
        private String prenom;
        private String email;
        private String type;
        private String role;

        public UserDisplay(String id, String nom, String prenom, String email, String type, String role) {
            this.id = id;
            this.nom = nom;
            this.prenom = prenom;
            this.email = email;
            this.type = type;
            this.role = role;
        }

        public String getId() { return id; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
        public String getEmail() { return email; }
        public String getType() { return type; }
        public String getRole() { return role; }
    }



    public void initialize() {
        currentAdmin = new Administrateur("1", "Admin", "Système", "admin@proproject.com", "admin123", "admin", "ADMINISTRATEUR", new ArrayList<>());
        fullNameText.setText(currentAdmin.getName() + " " + currentAdmin.getPrenom());
        refreshUsers();
    }




    @FXML
    private void refreshUsers() {
        ObservableList<UserDisplay> users = FXCollections.observableArrayList();

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            String query = "SELECT id, nom, prenom, email, type, role " +
                    "FROM user_tab " +
                    "ORDER BY type, nom, prenom";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                UserDisplay user = new UserDisplay(
                        rs.getString("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("type"),
                        rs.getString("role") != null ? rs.getString("role") : "N/A"
                );
                users.add(user);
            }

            usersTable.setItems(users);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement",
                    "Impossible de charger les utilisateurs : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    @FXML
    private void voirDetailsUser() {
        UserDisplay selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un utilisateur dans la liste.");
            return;
        }

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            //recuperation des details ds comptes
            String query = "SELECT u.* FROM user_tab u WHERE u.id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, selected.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("═══════════════════════════════════════\n");
                details.append("        DÉTAILS DE L'UTILISATEUR\n");
                details.append("═══════════════════════════════════════\n\n");
                details.append(" ID : ").append(rs.getString("id")).append("\n");
                details.append(" Nom : ").append(rs.getString("nom")).append("\n");
                details.append(" Prénom : ").append(rs.getString("prenom")).append("\n");
                details.append(" Email : ").append(rs.getString("email")).append("\n");
                details.append(" Type : ").append(rs.getString("type")).append("\n");
                details.append(" Rôle : ").append(rs.getString("role") != null ? rs.getString("role") : "N/A").append("\n\n");

                //information supp selon le role
                if ("MEMBRE".equals(rs.getString("type"))) {
                    //recuperer la disponibilité
                    String memberQuery = "SELECT mi.dispo, " +
                            "(SELECT COUNT(*) FROM membre_project_tab WHERE membre_id = ?) as nb_projets, " +
                            "(SELECT COUNT(*) FROM tache_tab WHERE membre_assigne_id = ?) as nb_taches " +
                            "FROM membre_info_tab mi WHERE mi.membre_id = ?";
                    PreparedStatement memberStmt = conn.prepareStatement(memberQuery);
                    memberStmt.setString(1, selected.getId());
                    memberStmt.setString(2, selected.getId());
                    memberStmt.setString(3, selected.getId());
                    ResultSet memberRs = memberStmt.executeQuery();

                    if (memberRs.next()) {
                        details.append(" Disponibilité : ").append(memberRs.getString("dispo") != null ? memberRs.getString("dispo") : "N/A").append("\n");
                        details.append(" Nombre de projets : ").append(memberRs.getInt("nb_projets")).append("\n");
                        details.append(" Nombre de tâches : ").append(memberRs.getInt("nb_taches")).append("\n");
                    }
                } else if ("CHEF_PROJET".equals(rs.getString("type"))) {
                    //nbr projets gérer
                    String chefQuery = "SELECT COUNT(*) as nb_projets FROM project_tab WHERE chef_projet_id = ?";
                    PreparedStatement chefStmt = conn.prepareStatement(chefQuery);
                    chefStmt.setString(1, selected.getId());
                    ResultSet chefRs = chefStmt.executeQuery();

                    if (chefRs.next()) {
                        details.append(" Projets gérés : ").append(chefRs.getInt("nb_projets")).append("\n");
                    }
                }

                details.append("═══════════════════════════════════════");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Détails de l'Utilisateur");
                alert.setHeaderText(rs.getString("prenom") + " " + rs.getString("nom"));
                alert.setContentText(details.toString());
                alert.getDialogPane().setPrefWidth(500);
                alert.showAndWait();
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de récupérer les détails : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }



    @FXML
    private void supprimerUser() {
        UserDisplay selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Aucune sélection",
                    "Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        //vérifier qu'on ne supprime pas l'admin actuel
        if (selected.getId().equals(currentAdmin.getId())) {
            showAlert(Alert.AlertType.ERROR, "Action interdite",
                    "Vous ne pouvez pas supprimer votre propre compte administrateur !");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur : " + selected.getPrenom() + " " + selected.getNom());
        confirmation.setContentText(" ATTENTION \n\n" +
                "Cette action supprimera :\n" +
                "• L'utilisateur\n" +
                "• Toutes ses assignations\n" +
                "• Ses informations de membre (si applicable)\n\n" +
                "Cette action est IRRÉVERSIBLE !\n\n" +
                "Êtes-vous sûr de vouloir continuer ?");

        ButtonType btnSupprimer = new ButtonType("Supprimer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnuler = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmation.getButtonTypes().setAll(btnSupprimer, btnAnnuler);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == btnSupprimer) {
                executeSuppessionUser(selected.getId(), selected.getPrenom() + " " + selected.getNom(), selected.getType());
            }
        });
    }


    private void executeSuppessionUser(String userId, String userName, String userType) {
        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            conn.setAutoCommit(false);

            int totalDeleted = 0;


            if ("MEMBRE".equals(userType)) {
                //supprimer les assignations de projets
                String deleteMemberProjects = "DELETE FROM membre_project_tab WHERE membre_id = ?";
                PreparedStatement pstmt1 = conn.prepareStatement(deleteMemberProjects);
                pstmt1.setString(1, userId);
                totalDeleted += pstmt1.executeUpdate();

                //désassigner les tâches
                String unassignTasks = "UPDATE tache_tab SET membre_assigne_id = NULL WHERE membre_assigne_id = ?";
                PreparedStatement pstmt2 = conn.prepareStatement(unassignTasks);
                pstmt2.setString(1, userId);
                totalDeleted += pstmt2.executeUpdate();

                //supprimer les infos membre
                String deleteMemberInfo = "DELETE FROM membre_info_tab WHERE membre_id = ?";
                PreparedStatement pstmt3 = conn.prepareStatement(deleteMemberInfo);
                pstmt3.setString(1, userId);
                totalDeleted += pstmt3.executeUpdate();
            }


            if ("CHEF_PROJET".equals(userType)) {
                //mettre à NULL le chef des projets
                String updateProjects = "UPDATE project_tab SET chef_projet_id = NULL WHERE chef_projet_id = ?";
                PreparedStatement pstmt4 = conn.prepareStatement(updateProjects);
                pstmt4.setString(1, userId);
                totalDeleted += pstmt4.executeUpdate();
            }

            //supprimer l'utilisateur
            String deleteUser = "DELETE FROM user_tab WHERE id = ?";
            PreparedStatement pstmt5 = conn.prepareStatement(deleteUser);
            pstmt5.setString(1, userId);
            int userDeleted = pstmt5.executeUpdate();

            conn.commit();

            if (userDeleted > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Suppression réussie",
                        "L'utilisateur '" + userName + "' a été supprimé avec succès.\n\n" +
                                "Éléments affectés : " + (totalDeleted + userDeleted));
                refreshUsers();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "L'utilisateur n'a pas pu être supprimé.");
            }

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Erreur de suppression",
                    "Impossible de supprimer l'utilisateur : " + e.getMessage());
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


    @FXML
    private void creerMembre() {
        if (!validateFields()) return;

        String dispo = dispoField.getText().trim();
        if (dispo.isEmpty()) dispo = "DISPONIBLE";

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            String id = "U" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);

            String sqlUser = "INSERT INTO user_tab (id, nom, prenom, email, mdp, role, type) " +
                    "VALUES (?, ?, ?, ?, ?, 'developpeur', 'MEMBRE')";
            PreparedStatement pstmtUser = conn.prepareStatement(sqlUser);
            pstmtUser.setString(1, id);
            pstmtUser.setString(2, nomField.getText());
            pstmtUser.setString(3, prenomField.getText());
            pstmtUser.setString(4, emailField.getText());
            pstmtUser.setString(5, mdpField.getText());
            pstmtUser.executeUpdate();

            String sqlDispo = "INSERT INTO membre_info_tab (membre_id, dispo) VALUES (?, ?)";
            PreparedStatement pstmtDispo = conn.prepareStatement(sqlDispo);
            pstmtDispo.setString(1, id);
            pstmtDispo.setString(2, dispo);
            pstmtDispo.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Membre créé avec ID : " + id);
            clearFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la création : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void creerChefProjet() {
        if (!validateFields()) return;

        Connection conn = DatabaseConnection.getConnection();
        if (conn == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Impossible de se connecter à la base de données.");
            return;
        }

        try {
            String id = "U" + UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 8);

            String sql = "INSERT INTO user_tab (id, nom, prenom, email, mdp, role, type) " +
                    "VALUES (?, ?, ?, ?, ?, 'chef', 'CHEF_PROJET')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, nomField.getText());
            pstmt.setString(3, prenomField.getText());
            pstmt.setString(4, emailField.getText());
            pstmt.setString(5, mdpField.getText());
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Chef de projet créé avec ID : " + id);
            clearFields();

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur BD", "Erreur lors de la création : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }



    private boolean validateFields() {
        if (nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() || mdpField.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs manquants", "Veuillez remplir nom, prénom, email et mot de passe.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        emailField.clear();
        mdpField.clear();
        dispoField.clear();
    }

    @FXML
    private void handleLogout() {
        if (showConfirmation("Déconnexion", "Voulez-vous vraiment vous déconnecter ?")) {
            try {
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/proprojectfx/login.fxml"));
                stage.setScene(new Scene(loader.load()));
                stage.setTitle("Connexion - ProProject");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la connexion.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
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
        return alert.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    public void setCurrentAdmin(Administrateur admin) {
        this.currentAdmin = admin;
        fullNameText.setText(admin.getName() + " " + admin.getPrenom());
        refreshUsers();
    }
}