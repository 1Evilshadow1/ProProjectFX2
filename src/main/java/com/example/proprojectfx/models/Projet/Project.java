package com.example.proprojectfx.models.Projet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.proprojectfx.models.User.*;

public class Project  {
    protected String Titre;
    protected String info;
    protected chefprojet chefProjet;
    protected List<Membre> equipe;
    protected List<Tache> taches;
    protected Map<Tache,Integer> avancement;

    public Project( String Titre,String info, chefprojet chefProjet, List<Membre> equipe,
                   List<Tache> taches, Map<Tache, Integer> avancement) {
        this.Titre=Titre;
        this.info = info;
        this.chefProjet = chefProjet;
        this.equipe = equipe;
        this.taches = taches;
        this.avancement = avancement;
    }

    public String creerRapport() {
        GenerateurRapport gen = () -> "Projet: " + info + "\nTâches: " + taches.size();
        return gen.genererRapport();
    }

    public List<String> getNomsMembres() {
        return equipe.stream()
                .map(m -> m.getName())
                .collect(Collectors.toList());
    }

    public void ajouterMembre(Membre membre) {
        equipe.add(membre);
    }

    public void retirerMembre(Membre membre) {
        equipe.remove(membre);
    }

    public void creerTache(Tache tache) {
        taches.add(tache);
    }

    public void calculerAvancement(Tache tache, int pourcentage) {
        avancement.put(tache, pourcentage);
    }

    public String getInfo(){
        return info;
    }

    public List<Membre> getEquipe(){
        return equipe;
    }

    public List<Tache> getTaches(){
        return taches;
    }

}