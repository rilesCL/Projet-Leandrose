# Plan de demo de EQ04-1: En tant qu’employeur, je m’inscris
1. J'appuie sur le bouton **"S'inscrire"** dans le menu en bas à droite.
2. J'appuie sur le bouton **"Créez un compte employeur"**.
2. Dans le champ **Prénom**, je rentre : Jean.
3. Dans le champ **Nom**, je rentre : Dupont.
4. Dans le champ **Nom de l'entreprise**, je rentre : TechCorp.
5. Dans le champ **Secteur d'activité**, je rentre : Informatique.
6. Dans le champ **Email professionnel**, je rentre : contact@techcorp.com.
7. Dans le champ **Mot de passe**, je rentre : Password123.
8. J’appuie sur le bouton **"S'inscrire"**.
9. Je vois un message de succès : *"Inscription réussie !"*.
10. Je suis redirigé vers la page de connexion.

### Cas d'erreur :
1. Je laisse le champ **Nom de l'entreprise** vide → un message d’erreur *"Nom de l'entreprise requis"* apparaît.
2. Je mets un email invalide : contact@techcorp → un message d’erreur *"Email invalide"* apparaît.
3. Je mets un mot de passe trop court : 123 → un message d’erreur *"Le mot de passe doit contenir au moins 8 caractères"* apparaît.
4. Si le serveur retourne une erreur (par exemple : email déjà utilisé), un message global en rouge s’affiche : *"Impossible de créer le compte, email déjà pris"*.



# Plan de demo de EQ04-3: En tant qu'utilisateur, je me connecte

1. Dans le champ Email, je rentre cet email valide: ghil.amr@student.com
2. Dans le champ Mot de passe, je rentre le mot de passe du compte: Password123
3. J'appuie sur le bouton "Se connecter"
4. Je vérifie que je suis redirigé vers la page dashboard de l'utilisateur

### Cas d'erreur :
1. Dans le champ Email, je rentre cet email invalide: ghil.amr
2. Dans le champ Mot de passe, je rentre un mot de passe invalide(moins de 8 caractères ou qui n'est pas le bon): Paswdr