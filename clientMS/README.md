# Microservice de Gestion des Utilisateurs (ms-client)

Ce projet est un microservice backend construit avec Spring Boot pour la gestion des utilisateurs, des rôles et de l'authentification. Il expose une API REST complète pour gérer le cycle de vie des utilisateurs, avec une distinction claire entre les opérations des utilisateurs standards et celles des administrateurs.

## Table des Matières
1.  [Technologies Utilisées](#technologies-utilisées)
2.  [Structure du Projet](#structure-du-projet)
3.  [Modèle de Données](#modèle-de-données)
4.  [Data Transfer Objects (DTOs)](#data-transfer-objects-dtos)
5.  [Flux d'Authentification (Bearer Token)](#flux-dauthentification-bearer-token)
6.  [API Endpoints](#api-endpoints)
    *   [Endpoints d'Authentification](#endpoints-dauthentification)
    *   [Endpoints Publics](#endpoints-publics)
    *   [Endpoints Utilisateur Protégés](#endpoints-utilisateur-protégés)
    *   [Endpoints Administrateur Protégés](#endpoints-administrateur-protégés)
7.  [Gestion des Erreurs](#gestion-des-erreurs)
8.  [Configuration et Démarrage](#configuration-et-démarrage)

---

## Technologies Utilisées

*   **Java 17**
*   **Spring Boot 3.x**
*   **Spring Data JPA**
*   **PostgreSQL**
*   **Maven**
*   **Lombok**
*   **Spring Cloud** (Eureka, Config Server)

---

## Structure du Projet

Le projet suit une architecture en couches standard :
-   `com.Client` : Package racine.
    -   `config` : Configuration des beans, de la sécurité et du Web.
    -   `controller` : Contrôleurs REST.
    -   `dto` : Data Transfer Objects (contrat de l'API).
    -   `exception` : Exceptions personnalisées et gestionnaire global.
    -   `interceptor` : Intercepteurs pour la validation des jetons.
    -   `model` : Entités JPA.
    -   `repository` : Interfaces Spring Data JPA.
    -   `Service` : Logique métier.

---

## Modèle de Données

### Entité `User`
| Champ             | Type      | Contraintes                  | Description                               |
| ----------------- | --------- | ---------------------------- | ----------------------------------------- |
| `id`              | `Long`    | Clé primaire, auto-incrémentée | Identifiant unique.                       |
| `firstName`       | `String`  |                              | Prénom.                                   |
| `lastName`        | `String`  |                              | Nom.                                      |
| `email`           | `String`  | `unique`, `not null`         | Email de connexion.                       |
| `password`        | `String`  | `not null`                   | Mot de passe haché.                       |
| `role`            | `Role`    | `not null`                   | Rôle (`CLIENT` ou `ADMIN`).               |
| `shippingAddress` | `String`  |                              | Adresse de livraison.                     |
| `phone`           | `String`  |                              | Numéro de téléphone.                      |
| `createdAt`       | `Instant` | `not null`                   | Date de création.                         |

### Enum `Role`
-   `CLIENT` : Rôle par défaut.
-   `ADMIN` : Rôle avec privilèges élevés.

---

## Data Transfer Objects (DTOs)

-   **`LoginRequestDTO`**: Corps de la requête pour la connexion (`email`, `password`).
-   **`LoginResponseDTO`**: Réponse après une connexion réussie (`token`, `UserResponseDTO`).
-   **`UserResponseDTO`**: Objet de réponse standard pour un utilisateur (exclut le mot de passe).
-   **`UserCreateDTO`**: Corps de la requête pour l'enregistrement d'un nouvel utilisateur.
-   **`UserUpdateDTO`**: Corps de la requête pour la mise à jour par un utilisateur de son propre profil.
-   **`AdminUserUpdateDTO`**: Corps de la requête pour la mise à jour par un administrateur.

---

## Flux d'Authentification (Bearer Token)

L'authentification est basée sur un jeton (token) simple.

1.  **Connexion** : Le client envoie une requête `POST` à `/api/v1/users/login` avec son `email` et `password`.
2.  **Réception du Jeton** : Si les identifiants sont valides, le serveur génère un jeton unique et le renvoie dans un objet JSON.
3.  **Stockage du Jeton** : Le client (ex: une application Angular) doit stocker ce jeton (par exemple, dans le `localStorage`).
4.  **Requêtes Authentifiées** : Pour toutes les requêtes suivantes vers des endpoints protégés, le client doit inclure le jeton dans l'en-tête `Authorization`.
    ```
    Authorization: Bearer <votre-jeton-ici>
    ```
5.  **Déconnexion** : Le client envoie une requête `POST` à `/api/v1/users/logout` (avec son jeton) pour invalider le jeton côté serveur.

---

## API Endpoints

**Préfixe global :** `/api/v1`

### Endpoints d'Authentification

#### `POST /users/login`
-   **Objectif :** Connecter un utilisateur et obtenir un jeton d'authentification.
-   **Request Body :** `LoginRequestDTO`
    ```json
    {
        "email": "user1@example.com",
        "password": "password123"
    }
    ```
-   **Success Response :** `200 OK` avec un `LoginResponseDTO`.
    ```json
    {
        "token": "c8a9d7e6-f5b4-4a3c-9e8d-1b2c3d4e5f6a",
        "user": {
            "id": 2,
            "firstName": "Alice",
            "lastName": "Smith",
            "email": "user1@example.com",
            "role": "CLIENT",
            // ... autres champs
        }
    }
    ```

### Endpoints Publics

#### `POST /users/register`
-   **Objectif :** Enregistrer un nouvel utilisateur.
-   **Request Body :** `UserCreateDTO`
-   **Success Response :** `201 Created` avec un `UserResponseDTO`.

### Endpoints Utilisateur Protégés
*(Nécessite un `Bearer Token` valide)*

#### `POST /users/logout`
-   **Objectif :** Déconnecter l'utilisateur en invalidant son jeton.
-   **Success Response :** `204 No Content`.

#### `GET /users/me`
-   **Objectif :** Obtenir les informations du compte de l'utilisateur authentifié.
-   **Success Response :** `200 OK` avec un `UserResponseDTO`.

#### `PUT /users/me`
-   **Objectif :** Mettre à jour les informations de son propre compte.
-   **Request Body :** `UserUpdateDTO`
-   **Success Response :** `200 OK` avec le `UserResponseDTO` mis à jour.

#### `DELETE /users/me`
-   **Objectif :** Supprimer son propre compte.
-   **Success Response :** `204 No Content`.

### Endpoints Administrateur Protégés
*(Nécessite un `Bearer Token` valide avec le rôle `ADMIN`)*

#### `GET /admin/users`
-   **Objectif :** Obtenir la liste de tous les utilisateurs.
-   **Success Response :** `200 OK` avec une liste de `UserResponseDTO`.

#### `GET /admin/users/{id}`
-   **Objectif :** Obtenir un utilisateur par son ID.
-   **Success Response :** `200 OK` avec un `UserResponseDTO`.

#### `PUT /admin/users/{id}`
-   **Objectif :** Mettre à jour n'importe quel utilisateur, y compris son rôle.
-   **Request Body :** `AdminUserUpdateDTO`
-   **Success Response :** `200 OK` avec le `UserResponseDTO` mis à jour.

#### `DELETE /admin/users/{id}`
-   **Objectif :** Supprimer un utilisateur par son ID.
-   **Success Response :** `204 No Content`.

---

## Gestion des Erreurs

| Exception                   | HTTP Status       | Description                                       |
| --------------------------- | ----------------- | ------------------------------------------------- |
| `AuthException`             | `401 Unauthorized`| Identifiants invalides ou jeton invalide/expiré.  |
| `ForbiddenException`        | `403 Forbidden`   | L'utilisateur n'a pas les droits (rôle `ADMIN` requis). |
| `UserNotFoundException`     | `404 Not Found`   | L'utilisateur avec l'ID spécifié n'existe pas.    |
| `EmailAlreadyExistsException` | `409 Conflict`    | L'email fourni est déjà utilisé.                  |
| `Exception` (générique)     | `500 Internal Server Error` | Erreur inattendue du serveur.             |

---

## Configuration et Démarrage

### Données de Test
Au démarrage, l'application crée automatiquement les utilisateurs suivants s'ils n'existent pas :
-   **Admin :** `admin@ecommerce.com` (mot de passe : `adminpassword`)
-   **Client 1 :** `user1@example.com` (mot de passe : `password123`)
-   **Client 2 :** `user2@example.com` (mot de passe : `password456`)

### Lancer l'Application
1.  Assurez-vous que votre base de données PostgreSQL est en cours d'exécution et accessible via l'URL spécifiée dans `application.yml`.
2.  Exécutez la commande Maven suivante :
    ```bash
    mvn spring-boot:run
    ```
L'application démarrera sur le port `8080`.
