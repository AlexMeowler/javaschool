# Logiweb
Java School Public Repository

# Codeship
[![Codeship Status for AlexMeowler/javaschool](https://app.codeship.com/projects/ae9cc548-5107-43bc-a4c0-60ee813c0211/status?branch=main)](https://app.codeship.com/projects/423888)

# SonarQube report (for version 2.4.0)
![SonarQube report](sonar.png?raw=true "SonarQube Report")

# VERSION 2.4.0

Added interfaces for business logic methods. Pages with pagination now read only required amount of rows from BD (instead of all table). Added interfaces for DAO: CountableRows (for counting rows) and PartRowsReader (for reading part of the table).  Current and total pages are shown now. Split orders: now on page Cargo&Orders only active orders (not completed) are shown. Completed orders are moved to separate page Completed orders. The information for completed orders is now saved in separate table (before it was not saved at all, assigned cars and list of drivers were just set to null). Reorganized view for driver page. Grouped some stuff up in tables. 

# TODO