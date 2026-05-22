# STRMS — Smart Task & Resource Management System

Projet Java OOP — E3e S6 Spring 2026.

## Structure

```
projet/
├── src/
│   ├── main/java/com/strms/
│   │   ├── Main.java                  ← point d'entrée (lance la GUI)
│   │   ├── enums/                     ← TaskStatus, PriorityLevel, TaskCategory, NotificationType
│   │   ├── exceptions/                ← 7 exceptions custom
│   │   ├── model/                     ← User (abstract), Admin, Manager, Engineer, Task, TaskHistoryEntry
│   │   ├── manager/                   ← TaskManager (contrôleur central)
│   │   ├── util/                      ← Reportable, ReportGenerator, Dashboard, FileManager, NotificationManager
│   │   └── gui/                       ← MainWindow, LoginDialog, TaskDialog (Swing)
│   └── test/java/com/strms/           ← Tests JUnit 5
├── diagrams/                          ← Diagrammes PlantUML (importables sur Lucidchart)
│   ├── class_diagram.puml
│   ├── sequence_create_task.puml
│   ├── sequence_assign_and_complete.puml
│   ├── sequence_circular_dependency.puml
│   └── sequence_persistence.puml
├── data/                              ← Persistance (tasks.dat, users.dat, report.txt)
└── lib/                               ← JARs JUnit (à télécharger, voir plus bas)
```

## Comment compiler & exécuter (sans Maven/Gradle)

```powershell
# Compilation
cd C:\Users\josep\VSCode\projet
mkdir build
javac -d build (Get-ChildItem -Recurse src\main\java -Filter *.java).FullName

# Exécution de la GUI
java -cp build com.strms.Main
```

## Lancer les tests JUnit 5

Télécharger `junit-platform-console-standalone-1.10.0.jar` dans `lib/`, puis :

```powershell
javac -cp "lib\*;build" -d build (Get-ChildItem -Recurse src\test\java -Filter *.java).FullName
java -jar lib\junit-platform-console-standalone-1.10.0.jar -cp build --scan-classpath
```

## Importer les diagrammes dans Lucidchart

1. Ouvrir Lucidchart → File → Import Diagram → **PlantUML**.
2. Coller le contenu du fichier `.puml` souhaité.
3. Lucidchart génère un diagramme éditable.

Alternative : utiliser [PlantText](https://www.planttext.com/) ou [PlantUML online](http://www.plantuml.com/plantuml/) pour générer une image PNG/SVG directement.

## Utilisateurs de démo

| Rôle      | Nom     | Permissions                     |
|-----------|---------|---------------------------------|
| Admin     | Alice   | create, delete, assign, report  |
| Manager   | Bob     | assign, report                  |
| Engineer  | Charlie | execute (start/complete)        |
| Engineer  | Diana   | execute (start/complete)        |

## Fonctionnalités

- Gestion d'utilisateurs (héritage + polymorphisme).
- Création/Assignation/Suppression de tâches avec contrôle des permissions.
- Dépendances entre tâches + détection de cycles par DFS.
- Historique complet de chaque tâche.
- File à priorité pour ordonnancer les tâches prêtes.
- Persistance via `ObjectOutputStream`/`ObjectInputStream`.
- Rapport texte et tableau de bord (par statut / priorité / utilisateur).
- Notifications (Console / SMS / Email — simulées).
- Interface graphique Swing complète (table, dialogs, dashboard).
- Suite de tests JUnit couvrant les scénarios obligatoires.
