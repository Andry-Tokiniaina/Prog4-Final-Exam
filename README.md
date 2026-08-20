# Prog4 - Examen Final - STD24094 STD24051

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
- Réponse : un token (JWT) est renvoyé dans le corps.
- Utilisation du token : Ajoutez une entête HTTP `Authorization` dans toutes les requêtes protégées :
  Authorization: Bearer <token>

## Comptes et mots de passe
- Il n'y a qu'un seul compte admin avec username: admin@hei.school, password: admin.
- Tous les comptes teacher partagent le même mot de passe (Teacher123!).
  Exemples d'usernames teacher (exemples à titre indicatif) :
  - hery.rakotomalala@hei.school
  - voahangy.randrianasolo@hei.school
  - tovo.rabemananjara@hei.school
- Tous les comptes student partagent le même mot de passe (Student123!).
  Exemples d'usernames student (exemples à titre indicatif) :
  - baholy.rabemananjara@student.hei.school
  - faniry.andriantsoa@student.hei.school
  - sitraka.rakotoson@student.hei.school

## Cas de test pour l'envoi du relevé de notes (mail)
- Nous avons pré-préparé un compte student contenant des données de test et votre adresse e-mail (toky@mail.hei.school) afin que vous puissiez tester l'envoi du relevé de notes.
- L'endpoint exact à appeler pour déclencher l'envoi du mail : /students/me/transcript
- Le header complet lié à ce user : Autorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2t5QG1haWwuaGVpLnNjaG9vbCIsImlhdCI6MTc4NzI0NzAwMSwiZXhwIjoxNzg5ODM5MDAxfQ.4DRUPXjne9eH01EpIpy2u2q-j1km5h0uyb0I5TSDYBjk_ESMUrhrk-YDXPLfC0AUlvHRaHLgbHnOulVP0xabNA

N.B: nous avons mis une durée de vie de 30 jours aux token pour être sûr que ce token serait encore valide pendant la correction
