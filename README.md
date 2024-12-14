# **Meeting Planner API**

## **Introduction**
Bienvenue dans le projet **Meeting Planner API**.  
Cette application est une solution de gestion de salles de réunion qui optimise l'allocation des ressources (salles, équipements, capacités) en fonction des besoins des utilisateurs. 
Elle a été développée en prenant soin de garantir la qualité du code, en appliquant des principes SOLID et des bonnes pratiques de développement. 
L'architecture de la solution suit un modèle **Hexagonal** pour favoriser la testabilité et la maintenabilité. 
Des tests unitaires et d'intégration ont été mis en place pour assurer le bon fonctionnement et la fiabilité de l'application.

---

## **Technologies utilisées**
- **Langage** : Java 17  
- **Framework Backend** : Spring Boot 3  
- **Base de données** : MySQL  
- **Build et gestion des dépendances** : Maven  
- **Conteneurisation** : Docker & Docker Compose  
- **Tests** : JUnit 5, Mockito, MockMvc  

---

## **Sommaire**
1. [**Description des Endpoints**](#description-des-endpoints)  
   Liste des endpoints exposés, avec des exemples de requêtes et réponses.
   
2. [**Entités et Schéma de Base de Données**](#entités-et-schéma-de-base-de-données)  
   Présentation des entités principales et un diagramme de classe.

3. [**Architecture de l'Application**](#architecture-de-lapplication)  
   Explication détaillée des couches et des packages.

4. [**Tests Unitaires et d'Intégration**](#tests-unitaires-et-dintégration)  
   Explication des tests unitaires et d'intégration utilisés dans le projet.

5. [**Tutoriel de Déploiement avec Docker Compose**](#tutoriel-de-déploiement-avec-docker-compose)  
   Étapes pour exécuter l'application en local avec `docker-compose`.

---

## **Description des Endpoints**

### **1. Assignation d'une réunion à la meilleure salle disponible**

**URL** : `/api/meetings/assign-to-best-room`  
**Méthode HTTP** : `POST`  
**Description** : Ce point d'entrée permet d'assigner une réunion à la meilleure salle disponible en fonction du type de réunion, du nombre de participants et de l'horaire spécifié.

#### **Requête**  
**Headers** :  
- `Content-Type: application/json`

**Body (JSON)** :  
```json
{
  "meetingType": "SPEC",
  "participantCount": 10,
  "meetingDate": "2024-12-01",
  "meetingHour": 10
}
```

- **meetingType** : Type de la réunion (par exemple, `SPEC`, `VC`, etc.).  
- **participantCount** : Nombre de participants à la réunion.  
- **meetingDate** : Date de la réunion au format `yyyy-MM-dd`.  
- **meetingHour** : Heure de la réunion (entre 8 et 20 inclus).  

#### **Réponse**  
**Code HTTP** : `200 OK`  
**Body (JSON)** :  
```json
{
  "meetingType": "SPEC",
  "participantCount": 10,
  "meetingDate": "2024-12-01",
  "meetingHour": 10,
  "assignedRoomName": "Conference Room A"
}
```

- **meetingType** : Type de la réunion.  
- **participantCount** : Nombre de participants.  
- **meetingDate** : Date assignée pour la réunion.  
- **meetingHour** : Heure assignée pour la réunion.  
- **assignedRoomName** : Nom de la salle assignée.  

#### **Codes de Réponse**  
- `200 OK` : Réunion assignée avec succès.  
- `400 Bad Request` : Requête invalide (par exemple, heure ou type de réunion incorrects).  
- `404 Not Found` : Aucune salle adaptée disponible.

---

### **2. Meilleure salle avec heures disponibles**

**URL** : `/api/rooms/best-room-with-available-hours`  
**Méthode HTTP** : `GET`  
**Description** : Ce point d'entrée retourne la meilleure salle disponible pour un type de réunion et une capacité spécifiée, ainsi que les plages horaires disponibles pour une date donnée.

#### **Requête**  

**Query Parameters** :  
- `meetingType` (String, obligatoire) : Type de la réunion (par exemple, `SPEC`, `VC`, etc.).  
- `requiredCapacity` (int, obligatoire) : Capacité requise pour la salle.  
- `meetingDate` (String, obligatoire) : Date de la réunion au format `yyyy-MM-dd`.  

**Exemple d'appel HTTP** :  
```
GET /api/rooms/best-room-with-available-hours?meetingType=SPEC&requiredCapacity=10&meetingDate=2024-12-01
```

#### **Réponse**  
**Code HTTP** : `200 OK`  
**Body (JSON)** :  
```json
{
  "name": "Conference Room A",
  "capacity": 15,
  "roomEquipments": ["Whiteboard", "Projector"],
  "availableHours": ["8h-9h", "14h-15h", "17h-18h"]
}
```

- **name** : Nom de la salle.  
- **capacity** : Capacité totale de la salle.  
- **roomEquipments** : Liste des équipements disponibles dans la salle.  
- **availableHours** : Heures disponibles pour la salle à la date spécifiée.  

#### **Codes de Réponse**  
- `200 OK` : Salle trouvée avec succès.  
- `400 Bad Request` : Requête invalide (par exemple, date au mauvais format).  
- `404 Not Found` : Aucune salle adaptée disponible.

---

## **Description de la Base de Données**

La base de données utilisée pour cette application est **MySQL**. Elle contient plusieurs tables interconnectées pour gérer les salles, les réunions, les types de réunions et les équipements.

### **Diagramme de Classes**

[![Class-Diagram-meetingplanner.png](https://i.postimg.cc/PxL4L6NZ/Class-Diagram-meetingplanner.png)](https://postimg.cc/zbrW6FCX)

Le diagramme ci-dessus représente la structure des entités de la base de données ainsi que leurs relations.  

### **Exemple de Données**

#### Table `rooms` :
| id  | name               | capacity |
| ----|--------------------|----------|
| 1   | Conference Room A  | 15       |
| 2   | Training Room B    | 20       |

#### Table `meetings` :
| id  | date       | hour | participant_count | room_id | meeting_type_id |
| ----|------------|------|-------------------|---------|-----------------|
| 1   | 2024-12-01 | 10   | 10                | 1       | 2               |

#### Table `meeting_types` :
| id  | name  | minimum_capacity |
| ----|-------|------------------|
| 1   | SPEC  | 5                |
| 2   | VC    | 10               |

#### Table `equipment` :
| id  | name       |
| ----|------------|
| 1   | Projector  |
| 2   | Whiteboard |

---

## **Architecture de l'Application**

L'application utilise une architecture hexagonale pour garantir une séparation claire des responsabilités et faciliter la maintenance et l'extensibilité du code. Chaque couche et package a un rôle bien défini.  

### **Vue d'Ensemble de l'Architecture**

- **Couche Adapters**  
  Contient les implémentations des interfaces (ports) définies dans l'application. Elle se divise en deux parties : 
  - **Inbound** : Pour les entrées de l'application, telles que les contrôleurs REST.
  - **Outbound** : Pour les sorties, comme l'accès à la base de données ou à des services externes.

- **Couche Application**  
  Contient la logique métier (use cases) et les ports pour interagir avec les couches externes.

- **Couche Domain**  
  Contient les entités et services métier. Cette couche est indépendante des frameworks et des infrastructures externes.

---

### **Description des Couches et Packages**

1. **`adapters.inbound.rest`**  
   - **Rôle** : Gère les interactions avec les clients via des API REST.
   - **Contenu** :  
     - **Contrôleurs REST** : Exposent les endpoints de l'application.  
     - **Exemple** :  
       - `RoomController` : Endpoint pour récupérer les salles disponibles.  
       - `MeetingController` : Endpoint pour planifier une réunion.  

2. **`adapters.outbound.jpa`**  
   - **Rôle** : Implémente l'accès à la base de données à l'aide de JPA (Hibernate).  
   - **Contenu** :  
     - **Repositories JPA** : Classes qui interagissent avec la base de données.  
     - **Entités JPA** : Représentation des tables de la base de données.  
     - **Exemple** :  
       - `JpaRoomRepository` : Gère les opérations CRUD pour les salles.  
       - `JpaMeetingRepository` : Gère les réunions.  

3. **`application`**  
   - **Rôle** : Contient la logique métier spécifique à l'application.  
   - **Contenu** :  
     - **Use Cases** : Représentent les fonctionnalités principales de l'application.  
     - **Ports** : Interfaces qui définissent les contrats pour les adapters.  
     - **Exemple** :  
       - `AssignMeetingToBestRoomUseCase` : Logique pour attribuer une salle à une réunion.  
       - `RoomRepositoryPort` : Interface pour interagir avec les salles.  

4. **`domain`**  
   - **Rôle** : Contient les entités et services métier. Cette couche est indépendante des frameworks.  
   - **Contenu** :  
     - **Entités Métier** : Classes représentant les concepts métier, comme `Room`, `Meeting`, ou `MeetingType`.  
     - **Exemple** :  
       - `Room` : Classe pour gérer les informations et la disponibilité des salles.  
       - `MeetingType` : Décrit les types de réunions et leurs contraintes.  

5. **`common`**  
   - **Rôle** : Contient les classes utilitaires et les exceptions globales.  
   - **Contenu** :  
     - **Utils** : Classe utilitaire, comme des comparateurs (`RoomSuitabilityComparator`).  
     - **Exceptions** : Gestion des erreurs personnalisées (par exemple, `NoSuitableRoomException`).
     - **aspects** : Gestion global des exception et du logging de l'api.  
 

---

### **Flux des Données**

1. **Requête Entrante**  
   - Une requête REST est reçue par un contrôleur dans `adapters.inbound.rest`.  
2. **Traitement dans les Use Cases**  
   - Le contrôleur appelle un use case dans la couche `application`.  
3. **Interaction avec le Domaine**  
   - Le use case utilise les entités et services métier dans la couche `domain` pour traiter la logique.  
4. **Accès à la Base de Données**  
   - Si nécessaire, le use case interagit avec la couche `adapters.outbound.jpa` via un port pour accéder aux données.  
5. **Réponse**  
   - Une réponse est générée et renvoyée au client via le contrôleur REST.  


Cette architecture modulaire facilite l'évolutivité, permet de tester chaque couche indépendamment, et offre une séparation claire entre le domaine métier et l'infrastructure.

---

## **Tests Unitaires et d'Intégration**

### **Tests Unitaires**

Les tests unitaires sont utilisés pour valider la logique métier au sein des composants individuels de l'application, en s'assurant que chaque unité fonctionne comme prévu de manière isolée.  

#### **Structure des Tests Unitaires**

- **Utilisation de Mockito** : Nous utilisons **Mockito** pour simuler les dépendances externes et ainsi tester les unités de manière isolée.
- **JUnit 5** : Les tests sont écrits avec **JUnit 5**, un framework de test moderne pour Java, afin d'assurer une exécution rapide et fiable des tests.
- **Couvrir les cas suivants** :
  - Vérification de la logique des **use cases** comme `AssignMeetingToBestRoomUseCase`.
  - Validation des méthodes des entités et services métiers.
  - Tests des comparateurs comme `RoomSuitabilityComparator`.

#### **Exemple de Test Unitaire** :
Un test pour la méthode `isAvailableAt` de la classe `Room` :

```java
@Test
void testIsAvailableAt_ShouldReturnTrue_WhenRoomIsAvailable() {
    // Arrange
    Room room = new Room("Conference Room", 10, new ArrayList<>(), new ArrayList<>());
    LocalDate meetingDate = LocalDate.of(2024, 12, 1);
    int meetingHour = 10;

    // Act
    boolean isAvailable = room.isAvailableAt(meetingDate, meetingHour);

    // Assert
    assertTrue(isAvailable);
}
```

### **Tests d'Intégration**

Les tests d'intégration valident l'interaction entre plusieurs composants de l'application. Ces tests visent à s'assurer que l'ensemble du système fonctionne correctement lorsque les différentes couches sont combinées.

#### **Structure des Tests d'Intégration**

- **Spring Boot Test** : Nous utilisons **Spring Boot Test** pour tester l'application dans son intégralité, y compris la couche Web, la couche Service, et la couche Repository.
- **Base de Données en Mémoire** : Pour éviter d'impacter la base de données de production, une base de données en mémoire comme **H2** est utilisée pendant les tests d'intégration.
- **Vérification des Endpoints REST** : Nous utilisons **MockMvc** pour effectuer des requêtes HTTP simulées sur nos endpoints REST et vérifier les réponses.

#### **Exemple de Test d'Intégration** :
Un test pour le contrôleur `RoomController` pour vérifier que l'API retourne une réponse correcte lorsque l'on demande une salle :

```java
@Test
void testGetBestRoomWithAvailableHours_ShouldReturnRoomDetails() throws Exception {
   mockMvc.perform(get("/api/rooms/best-room-with-available-hours")
                        .param("meetingType", "SPEC")
                        .param("requiredCapacity", "10")
                        .param("meetingDate", "2024-12-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Conference Room A"))
                .andExpect(jsonPath("$.capacity").value(20))
                .andExpect(jsonPath("$.roomEquipments[0]").value("Projector"))
                .andExpect(jsonPath("$.availableHours[0]").value("8h00-9h00"));
}
```

### **Outils Utilisés pour les Tests**

- **JUnit 5** : Framework de test pour écrire, exécuter et gérer les tests unitaires.
- **Mockito** : Utilisé pour simuler les dépendances externes, comme les bases de données et les services externes.
- **MockMvc** : Permet de simuler des appels HTTP dans les tests d'intégration pour tester les contrôleurs REST.
- **H2 Database** : Utilisée pour exécuter des tests avec une base de données en mémoire, simulant un environnement de production.

---

## **Tutoriel de Déploiement avec Docker Compose**

### **Prérequis**

- **Docker** et **Docker Compose** installés. [Installer Docker](https://www.docker.com/products/docker-desktop).

### **Étapes de déploiement**

1. **Cloner le projet** :

   ```bash
   git clone https://github.com/MohamedElalami100/meetingplanner.git
   cd meetingplanner
   ```

2. **Configurer le fichier `.env`** (facultatif) :

   ```env
   MYSQL_ROOT_PASSWORD=rootpassword
   MYSQL_DATABASE=meetingplanner
   MYSQL_USER=appuser
   MYSQL_PASSWORD=apppassword
   ```

3. **Vérifier le fichier `docker-compose.yml`** : Assurez-vous que tout est correctement configuré (MySQL, application, etc.).

4. **Lancer les services** :

   ```bash
   docker-compose up --build
   ```

   Cette commande construit l'application et démarre les services (MySQL et backend).

5. **Accéder à l'application** : L'API est disponible à `http://localhost:8080`.

6. **Vérifier la base de données** : Connexion avec les informations suivantes :
   
   - Hôte : `localhost`
   - Port : `3306`
   - Utilisateur : `appuser`
   - Mot de passe : `apppassword`

7. **Arrêter les services** :

   ```bash
   docker-compose down
   ```

---

Cela vous permet de déployer l'application rapidement en local avec Docker Compose. 
```
