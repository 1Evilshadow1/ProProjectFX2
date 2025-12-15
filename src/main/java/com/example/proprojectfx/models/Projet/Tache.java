package com.example.proprojectfx.models.Projet;

import java.util.List;
import com.example.proprojectfx.models.User.Membre;

public class Tache {
    protected  String Titre;
    protected String tacheInfo;
    protected Membre membreAssigne;
    protected TaskStatue statut;
    protected List<Commentaire> commentaires;
    protected Difficulté difficulté;

    public Tache(String Titre,String info, Membre membre, TaskStatue statut,
                 List<Commentaire> commentaires,  String difficulté , String statue) {
        this.Titre=Titre;
        this.tacheInfo = info;
        this.membreAssigne = membre;
        this.statut = statut;
        this.commentaires = commentaires;
        this.difficulté = Difficulté.valueOf(difficulté);
        this.statut=TaskStatue.valueOf(statue);
    }

    public void changerStatut(TaskStatue nouveauStatut) {
        this.statut = nouveauStatut;
    }

    public void ajouterCommentaire(Commentaire commentaire) {
        commentaires.add(commentaire);
    }

    public void signalerDifficulté(Difficulté nouvelleDifficulté) {
        this.difficulté = nouvelleDifficulté;
    }

    public String getInfo(){
        return tacheInfo;
    }

    public TaskStatue getStatut(){
        return statut;
    }

}
