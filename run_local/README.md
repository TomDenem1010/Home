# Helyi futtatás

A helyi beállítások a mappában lévő `.env` fájlban vannak. A Spring alkalmazás a repository gyökeréből ezt olvassa be.

- `spring_starter.cmd`: build és alkalmazásindítás, bármely munkakönyvtárból.
- `oracle_xe_starter.cmd`: a helyi Oracle XE újralétrehozása. A korábbi XE konténert és adatokat törli.

A közös Maven-projekt és a forráskód a gyökérben marad.
