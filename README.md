# TP03 – Backend : Gestion des relations et persistance des données

**LAB 3** – API REST avec **Spring Boot**, **Spring Data JPA**, **Hibernate** et **MySQL**.

Ce projet transforme l'application de gestion de produits en mémoire (LAB 2) en une application à persistance complète, avec les entités `Product`, `Category`, `Order` et `OrderItem`.

---

## Objectifs

- Configurer une connexion MySQL avec Spring Boot
- Modéliser des entités JPA et leurs relations (`@ManyToOne`, `@OneToMany`, cascades)
- Gérer le chargement `LAZY` / `EAGER` et la sérialisation JSON
- Utiliser Spring Data JPA (repositories, pagination, tri, requêtes `@Query`)
- Structurer l'application en couches **Controller → Service → Repository** avec DTO et validation
- Tester l'API avec le navigateur, Postman et curl

## Technologies

| Outil | Version / détail |
|---|---|
| Java | 21 (choisi dans Spring Initializr) |
| Spring Boot | 4.1.2 |
| Maven | via `mvnw` (wrapper inclus) |
| Base de données | MySQL 8.0 + MySQL Workbench |
| Dépendances | Spring Web, Spring Data JPA, MySQL Driver, Validation, Lombok |
| IDE / tests | IntelliJ IDEA, Postman, curl |

> Le lab prévoit Spring Boot 3.2.0. Ce projet a été généré avec la version 4.1.2 proposée par Spring Initializr ; le code fonctionne de la même façon.

---

## Étape 1 – Création du projet

Le projet a été généré avec [Spring Initializr](https://start.spring.io) :

- Project : **Maven**, Language : **Java**
- Group : `com.example.lab3`, Artifact : `lab3-jpa`
- Packaging : **Jar**, Configuration : **Properties**, Java : **21**
- Dépendances : Spring Web, Spring Data JPA, MySQL Driver, Validation, Lombok

<img width="1770" height="827" alt="01-spring-initializr" src="https://github.com/user-attachments/assets/33a597e3-5c0f-4d5c-8823-64b746c79504" />

---

## Étape 2 – Création de la base de données

Le script `src/main/resources/db/schema.sql` crée la base. Il a été exécuté dans MySQL Workbench :

```sql
CREATE DATABASE IF NOT EXISTS lab3_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab3_db;
-- Les tables seront créées automatiquement par Hibernate (ddl-auto=update)
```

<img width="1353" height="582" alt="02-schema-sql" src="https://github.com/user-attachments/assets/e43cea94-8d4c-4b0b-934f-8a2aa2231b53" />

---

## Étape 3 – Configuration de la connexion MySQL

Fichier `src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/lab3_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=VOTRE_MOT_DE_PASSE
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
server.port=8081
```

- Remplacez `VOTRE_MOT_DE_PASSE` par votre mot de passe MySQL.
- `ddl-auto=update` : Hibernate crée ou met à jour les tables à chaque démarrage.
- `server.port=8081` : le port 8080 était déjà utilisé sur la machine de développement.

---

## Étape 4 – Architecture du projet

```
src/main/java/com/example/lab3
├── entity        → Product, Category, Order, OrderItem
├── repository    → ProductRepository, CategoryRepository, OrderRepository, OrderItemRepository
├── dto           → ProductDto, CategoryDto, OrderDto, OrderItemDto
├── service       → ProductService, CategoryService, OrderService
├── controller    → ProductController, CategoryController, OrderController
└── Lab3JpaApplication.java
```

### Modèle de données

- Une **Category** contient plusieurs **Product** (`@OneToMany` / `@ManyToOne`)
- Une **Order** contient plusieurs **OrderItem** (`@OneToMany` / `@ManyToOne`)
- Un **OrderItem** référence un **Product**
- Les relations bidirectionnelles `Category ↔ Product` utilisent `@JsonManagedReference` / `@JsonBackReference` pour éviter une boucle infinie en JSON.

---

## Étape 5 – Lancement de l'application

Lancer la classe `Lab3JpaApplication` dans IntelliJ, ou en ligne de commande :

```bash
./mvnw spring-boot:run
```

Au premier démarrage, Hibernate crée les tables `categories`, `products`, `orders` et `order_items`. La console doit afficher `Tomcat started on port 8081` puis `Started Lab3JpaApplication`.

<img width="1905" height="1011" alt="03-structure-et-demarrage" src="https://github.com/user-attachments/assets/3d3afdcf-a449-46e6-966c-c685e687517e" />

---

## Étape 6 – Jeu de données de test

Une fois les tables créées, exécuter **une seule fois** `src/main/resources/db/data.sql` dans MySQL Workbench :

```sql
INSERT INTO categories (name) VALUES ('Électronique'), ('Livres');
INSERT INTO products (name, price, category_id) VALUES
  ('Smartphone', 699.99, 1),
  ('Laptop', 1199.00, 1),
  ('Roman', 19.90, 2);
```

Le panneau de gauche montre les 4 tables générées par Hibernate.

<img width="1360" height="635" alt="04-tables-et-data-sql" src="https://github.com/user-attachments/assets/64c63f46-50a0-4c67-a439-1f357be7d88a" />

---

## Étape 7 – Endpoints de l'API

Base URL : `http://localhost:8081`

| Méthode | URL | Description |
|---|---|---|
| POST | `/api/categories` | Créer une catégorie |
| GET | `/api/categories` | Lister les catégories |
| GET | `/api/categories/{id}` | Récupérer une catégorie |
| POST | `/api/products` | Créer un produit |
| GET | `/api/products?page=0&size=10&sort=id` | Lister les produits (pagination et tri) |
| GET | `/api/products/{id}` | Récupérer un produit |
| PUT | `/api/products/{id}` | Modifier un produit |
| DELETE | `/api/products/{id}` | Supprimer un produit |
| POST | `/api/orders` | Créer une commande avec ses items |
| GET | `/api/orders/{id}` | Récupérer une commande |

> Les paramètres `page`, `size` et `sort` de la liste des produits sont obligatoires.

---

## Étape 8 – Tests

### Navigateur : liste des catégories

`GET http://localhost:8081/api/categories`

<img width="362" height="258" alt="05-get-categories" src="https://github.com/user-attachments/assets/526c70e8-d8ba-407f-862d-fd7be2a3cf41" />

### Postman : récupérer un produit

`GET http://localhost:8081/api/products/1` → **200 OK**

<img width="1043" height="563" alt="06-postman-get-product" src="https://github.com/user-attachments/assets/a58fbdfd-e5f9-4ec8-bf1f-e107991a38e5" />

### curl : créer un produit

```bash
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Tablette","price":299.99,"categoryId":1}'
```

Corps JSON pour une catégorie :

```json
{"name":"Électronique"}
```

### Vérification de la persistance dans MySQL Workbench

```sql
SELECT * FROM categories;
SELECT * FROM products;
```

Le produit « Tablette » créé via l'API apparaît bien dans la table `products` (id 4), ce qui confirme la persistance.

<img width="1912" height="832" alt="07-workbench-products" src="https://github.com/user-attachments/assets/c9b300e0-738e-4c57-a4fc-5d224e50f05c" />

---

## Installation et exécution (résumé)

1. Installer **JDK 21**, **MySQL Server** et **MySQL Workbench**
2. Cloner le dépôt :
   ```bash
   git clone https://github.com/VOTRE-NOM/TP03-Backend.git
   cd TP03-Backend
   ```
3. Exécuter `src/main/resources/db/schema.sql` dans MySQL Workbench
4. Renseigner le mot de passe MySQL dans `application.properties`
5. Lancer l'application : `./mvnw spring-boot:run` ou depuis IntelliJ
6. Exécuter `data.sql` une seule fois, puis tester l'API sur `http://localhost:8081`

## Pistes d'amélioration

- Sécurité JWT (LAB 4)
- Tests unitaires avec JUnit et MockMvc
- Index MySQL et cache Hibernate pour les performances
- Gestion centralisée des erreurs (`@ControllerAdvice`)

## Auteur

Projet réalisé dans le cadre du module **M10 – Développement Backend** (Master IAIDDWM).
