# 🎓 LeandrOSE - Système de Gestion de Stages

Application web full-stack automatisant la gestion complète des stages en entreprise : publication d'offres, candidatures, signatures électroniques et évaluations pour 4 types d'utilisateurs (étudiants, employeurs, gestionnaires, professeurs).

## 🛠️ Stack Technique

**Frontend:** React 18, Vite, React Router, Tailwind CSS, i18next  
**Backend:** Spring Boot 3, Spring Security, Spring Data JPA, Hibernate  
**Base de données:** MySQL 8.0  
**Sécurité:** JWT, CORS  
**Outils:** Maven, Git, Jira, Docker

## 🚀 Installation & Exécution

### Prérequis
Node.js 18+, Java 21+, Maven 3.6+, MySQL 8.0+

### Étapes
```bash
# 1. Cloner le projet
git clone https://github.com/votre-username/leandrose.git
cd leandrose

# 2. Configurer MySQL (créer la base "leandrose")
# 3. Éditer src/main/resources/application.properties avec vos credentials MySQL

# 4. Lancer le backend (port 8080)
mvn spring-boot:run

# 5. Lancer le frontend (port 5173)
cd reactjwt
npm install
npm run dev
```

Accédez à http://localhost:5173

## ✨ Fonctionnalités

- Authentification JWT multi-rôles, upload/validation CV PDF, signatures électroniques
- Dashboards personnalisés, notifications temps réel, multilingue (FR/EN), mode clair/sombre




Définition de DONE

1. Code & Qualité

Le code source est écrit, relu (code review) et validé par au moins un autre membre de l’équipe.

Le code respecte les conventions de codage (Java, React/JavaScript, CSS).

Le code compile

Tous les tests unitaires passent avec succès :

Backend : couverture de code ≥ 80 % pour les classes métier critiques.

Frontend : tests unitaires ou de composants (Jest/Testing Library) pour les composants principaux.

Le code est versionné correctement (Git) et intégré dans la branche principale après le code review.

2. Fonctionnalités

La fonctionnalité implémentée répond exactement aux critères d’acceptation de la user story.

Les scénarios d’erreur et cas limites ont été gérés et testés.

Les intégrations entre le frontend React et le backend Spring Boot fonctionnent.

Points SMART:

- Simulation de la présentation la veille de la démo avec un membre de l'équipe

- Assignation d'une nouvelle tâche au plus tard 48h après la complétion de la dernière

- Attribution d'au moins un backend, un frontend et deux code reviews/plans de démo par étudiant



