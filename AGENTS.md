# Projektutasítások

Ezek a szabályok a Home projekt egészére vonatkoznak.

## Kommunikáció

- Magyarul kommunikálj, tömören és érthetően.
- A munka végén írd le, mi változott, és milyen ellenőrzéseket futtattál.
- A kódban és az azonosítókban kövesd a meglévő angol elnevezéseket.

## Kódstílus

- Kövesd az adott modul meglévő szerkezetét és kódstílusát.
- A Java osztályokban a mezők és konstruktorok után először a public metódusok, majd a private metódusok szerepeljenek.
- Az osztályok, DTO-k és metódusok neve egyértelműen írja le a szerepüket.
- Kövesd a meglévő nullkezelési és nulljelölési mintákat; ne rejts el típusbiztonsági figyelmeztetéseket indokolatlan elnyomással.
- Kerüld a feladathoz nem kapcsolódó átstrukturálást.

## Megvalósítás

- A controllerek a kérés és a nézet összeállításáért feleljenek; az üzleti és lekérdezési logika a service rétegbe kerüljön.
- A felületi módosítások illeszkedjenek a meglévő Thymeleaf sablonokhoz és CSS-hez.
- A felhasználó meglévő módosításait ne írd felül és ne vond vissza.

## Ellenőrzés

- Java kód módosítása után futtasd a formázást: `mvn spotless:apply`.
- Futtasd a módosításhoz kapcsolódó teszteket, például: `mvn test "-Dtest=DeckVersionQueryTest,DeckVersionPageTest"`.
- Új működéshez vagy hibajavításhoz szükség szerint egészítsd ki a meglévő teszteket érdemi esetekkel.
- Ellenőrizd a diffet: `git diff --check`.
- Ha egy ellenőrzés nem futtatható vagy hibára fut, ezt jelezd a végső válaszban.
