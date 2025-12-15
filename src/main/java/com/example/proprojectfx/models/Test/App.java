package com.example.proprojectfx.models.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.example.proprojectfx.models.Exception.ParametreInvalideException;
import com.example.proprojectfx.models.Probleme.Bug;
import com.example.proprojectfx.models.Probleme.ProblemeSysteme;
import com.example.proprojectfx.models.Probleme.StatutProbleme;
import com.example.proprojectfx.models.Projet.*;
import com.example.proprojectfx.models.User.Administrateur;
import com.example.proprojectfx.models.User.Membre;
import com.example.proprojectfx.models.User.chefprojet;

public class App {
    public static void main(String[] args) throws ParametreInvalideException {

        System.out.println("ProProject");

//        List<Tache> tachesAssignes =new ArrayList<>();
//        Membre azizM = new Membre("123","aziz","benothmen","aziz.benothmen2002@gmail.com","Aziz123","developpeur","MEMBRE","dispo",tachesAssignes);
//       /* System.out.println(azizM.getNbrTache());
//        List<Project>  ProjetAgerer= new ArrayList<>();        Bug bug1 = new Bug("1", "This is a bug message",StatutProbleme.Resolu, LocalDate.of(2022,10,10), LocalDate.of(2023,12,1), azizM);
//        bug1.afficherDetailsBug();
//        bug1.updateStatutProbleme();
//        bug1.afficherDetailsBug();
//        List<String> prb = List.of("tache1","tache2");
//        Administrateur adminAssigne = new Administrateur("999","2","3","4","5","developpeur","ADMINISTRATEUR",prb );
//        ProblemeSysteme ps = new ProblemeSysteme("11", "message",  adminAssigne);
//        ps.updateStatutProbleme();
//        */
//        Bug bug2 = new Bug("2", "Another bug message", StatutProbleme.NonResolu, azizM);
//        bug2.afficherDetailsBug();
//        bug2.updateStatutProbleme();
//        bug2.afficherDetailsBug();
//        List<Commentaire> commentaires = new ArrayList<>();
//
//        Administrateur ahmed = new Administrateur("1","Ahmed","Ali","ahmed.ali@gmail.com","Ahmed123","developpeur","ADMINISTRATEUR",new ArrayList<>());
//        ProblemeSysteme pSysteme = new ProblemeSysteme("3", "System issue", ahmed);
//
//        pSysteme.updateStatutProbleme();
//        System.out.println(pSysteme.getStatut());
//        System.out.println("Date de résolution: " + pSysteme.getDateResolution());
//        System.out.println("Administrateur assigné: " + pSysteme.getAdminAssigne().getName() + " " + pSysteme.getAdminAssigne().getPrenom());
//
//        ArrayList<Membre> equipe = new ArrayList<>();
//        equipe.add(azizM);
//        chefprojet chef = new chefprojet("1","Ahmed","Ali","ahmed.ali@gmail.com","Ahmed123","developpeur","CHEF_PROJET",new ArrayList<>());
//        Tache t1 = new Tache("Tache","tache de test",azizM,TaskStatue.A_FAIRE,commentaires, "Simple","A_FAIRE");
//        List<Tache> taches = new ArrayList<>();
//        taches.add(t1);
//        Map<Tache, Integer> avancement = new java.util.HashMap<>();
//        avancement.put(t1, 50);
//        Project projet = new Project("Projet de test","PLATEFORME",chef, equipe, taches, avancement);
//        Rapport rapport = new Rapport("4", projet, new java.util.HashMap<>(), equipe, 100);
//
//        System.out.println("Nombre total de tâches: " + rapport.getNombreTotalTaches());
//        System.out.println("Projet terminé: " + rapport.isProjetTermine());
//
//        System.out.println("Test 1: Bug sans membre");
//        try {
//            Bug bug = new Bug("B1", "Erreur", StatutProbleme.NonResolu, null);
//        } catch (ParametreInvalideException e) {
//            System.out.println("✅ Exception: " + e.getMessage());
//        }
//        // TEST LAMBDA
//        System.out.println("Test 2: TEST LAMBDA");
//        GenerateurRapport gen = () -> {
//            return "=== RAPPORT ===\n" +
//                    "Projet: " + projet.getInfo() + "\n" +
//                    "Tâches: " + projet.getTaches().size() + "\n" +
//                    "Membres: " + projet.getEquipe().size();
//        };
//        System.out.println(gen.genererRapport());
//
//
//        System.out.println("Test 3: TEST STREAM");
//        projet.ajouterMembre(new Membre("M1", "Dupont", "Jean", "jean@mail.com", "pass123", "developpeur", "MEMBRE", "disponible", new ArrayList<>()));
//        projet.ajouterMembre(new Membre("M2", "Martin", "Sophie", "sophie@mail.com", "pass456", "developpeur", "MEMBRE", "disponible", new ArrayList<>()));
//        projet.ajouterMembre(new Membre("M3", "Durand", "Paul", "paul@mail.com", "pass789", "developpeur", "MEMBRE", "disponible", new ArrayList<>()));
//
//        List<String> noms = projet.getNomsMembres();
//        System.out.println("Noms des membres: " + noms);

    }
}
