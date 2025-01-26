# MiniSGBDR

## Description du projet

Le projet consiste en l'implémentation d'un **MiniSGBDR** (Système de Gestion de Bases de Données Relationnelles) très simplifié. Ce SGBDR sera conçu pour fonctionner dans un environnement **mono-utilisateur** et ne comportera pas certaines fonctionnalités avancées telles que la gestion de la concurrence, les transactions, les droits d'accès, ou le crash recovery.

### Objectifs

Le but principal de ce projet est de permettre la gestion de commandes de création de tables, de sélection, de jointure, et d'autres opérations sur les données. Les commandes que le SGBDR devra traiter seront formulées dans un langage simplifié, tout en étant comparables à de vraies commandes SQL telles que :

- Insertion
- Sélection
- Jointure
- Commandes de type "debug" pour obtenir des informations supplémentaires sur les données

## Structure du projet

Le projet sera développé de manière progressive, en commençant par les couches "bas-niveau" du SGBD. Les différentes fonctionnalités seront ajoutées progressivement. 

## Fonctionnalités prévues

- Gestion des commandes de création de tables
- Traitement des commandes de sélection et d'insertion
- Exécution de jointures entre tables
- Outils de debug pour le suivi des données

## Installation

Pour installer et exécuter le projet, veuillez suivre ces étapes :

1. Clonez le dépôt :
   ```bash
   git clone https://github.com/votre-utilisateur/mini-sgbdr.git
   ```
   
2. Pour exécuter le script :
```bash
./script.sh
```
