# Prog4 - Examen (guide pour le correcteur)

Ce dépôt contient le projet de l'examen final. Ce document indique comment tester l'application, en particulier l'authentification, les comptes fournis et le cas de test pour l'envoi du relevé de notes.

## But
Ce README est destiné au correcteur. Il décrit rapidement :
- comment s'authentifier,
- le format du token à utiliser dans les requêtes,
- les comptes disponibles et la façon dont les mots de passe seront fournis,
- un cas de test prêt pour vérifier l'envoi d'e-mails de relevé de notes.

## Authentification
- Route d'authentification : POST /login
- Corps attendu (JSON) : { "username": "<votre-username>", "password": "<votre-password>" }
- Réponse : un token (JWT ou équivalent) est renvoyé dans le corps.
- Utilisation du token : Ajoutez une entête HTTP `Authorization` dans toutes les requêtes protégées :
  Authorization: Bearer <token>

Remarque : je fournirai moi-même le(s) username et password pour les comptes d'administration, ainsi que les mots de passe partagés pour teachers et students (voir section suivante).

## Comptes et mots de passe
- Il n'y a qu'un seul compte administrateur (admin). Je fournirai l'username et le password de ce compte.
- Tous les comptes teacher partagent le même mot de passe (je fournirai ce mot de passe).
  Exemples d'usernames teacher (exemples à titre indicatif) :
  - teacher1
  - professeur.dupont
- Tous les comptes student partagent le même mot de passe (je fournirai ce mot de passe).
  Exemples d'usernames student (exemples à titre indicatif) :
  - student1
  - etudiant.durand

Vous renseignerez vous-même les credentials exacts (usernames et passwords) lors de l'évaluation.

## Cas de test pour l'envoi du relevé de notes (mail)
- Nous avons pré-préparé un compte student contenant des données de test et une adresse e-mail valide afin que vous puissiez tester l'envoi du relevé de notes.
- L'endpoint exact à appeler pour déclencher l'envoi du mail (URL complète, méthode et payload) sera fourni par moi-même — je renseignerai l'endpoint à utiliser dans ce README si nécessaire.
- Utilisez le compte student de test et vérifiez la boîte mail associée pour confirmer la réception du relevé.

## Démarrage et tests
1. Démarrer l'application selon les instructions habituelles du projet (ex : `mvn spring-boot:run` ou autre script fourni).
2. Appeler POST /login avec un couple username/password valide.
3. Récupérer le token et l'utiliser dans l'entête `Authorization` lors des autres appels.
4. Tester les éléments métiers (création/lecture/édition) avec les comptes teacher/student selon les besoins.
5. Tester l'envoi du mail de relevé de notes en utilisant le student de test préparé.

## Remarques finales pour le correcteur
- Les credentials (usernames et mots de passe) vous seront fournis séparément par l'auteur du projet.
- N'hésitez pas à me dire si vous voulez que j'ajoute ici les endpoints précis (je les ajouterai si vous me les fournissez).
