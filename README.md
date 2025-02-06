# j4iot

## Bugs / New Features
- [x] Interne Umsetzung von Rollen überdenken, Enum?! (Projekte sind Rollen)
- [x] Admin-Rolle: UserMasterDetail wird nicht angezeigt (Seite entfernt)
- [x] Admin-Rolle: Alle Projekte etc. anzeigen, find...Auth überarbeiten
- [ ] Provisioning Confirmation
- [ ] Logout (evtl. weitere Seiten): immer auf .../ui verweisen!
- [ ] Beispieldaten: Bei Exceptions wegen Duplicate Keys nicht komplett abbrechen

### Dashboard
- [ ] Dashboard mit einzelnen Panels
- [ ] Plot im Dashboard
- [ ] Konfiguration von Dashboards, z.B. Plots hinzufügen/Kurven hinzufügen
- [ ] Live-Update von Panels
- [ ] Dashboard Projektebene: Plots aller Devices
- [ ] Dashboard Projektebene: Visulisierung lastSeen

### Epaper
- [ ] Elegante Lösung für Einbinden anderer Services wie Epaper
- [ ] Epaper: Gesamten Ansatz überdenken, von garnix lernen
- [ ] Use Case Sekretariat: Meldung anzeigen; dafür brauchen wir ein UI!
- [ ] Editor Fenster in j4iot um Json Dateien zu bearbeiten

## Konzepte
- [ ] Inwieweit können/sollten wir den Provisioning-Prozess an OAUTH2 orientieren?
      Kompakte JSONs bleiben wichtig!
- [x] Authentifizierung/User Management: Keycloak anbinden?!
- [ ] Authentifizierung: Fakultäts-Keycloak einrichten und verwenden?!
- [x] Authentifizierung User/Devices an traefik ForwardAuth auslagern (am besten in go!)?!
- [x] User-/Rollen-/Gruppenkonzept überdenken, was sollte an Keycload ausgelagert werden? (Alles an Keycloak ausgelagert)
