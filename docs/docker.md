# Home + Oracle + grafikus Chrome

Egyetlen Dockerfile építi a Linux amd64 image-et. A konténer elindítja az Oracle
Free adatbázist, a virtuális képernyőt és a noVNC-t. Az alkalmazás csak az Oracle
inicializálása és az adatbázis-felhasználó létrehozása után indul, és Flyway-jal
alkalmazza a migrációkat. A Chrome a frontendből indítható, grafikus módban.

## Indítás PowerShellből

### Docker Compose (ajánlott)

A `run_docker` mappában lévő `compose.yaml` eltárolja a portokat, mountokat és
futtatási beállításokat. A Docker Desktop tartalmazza a Compose-ot.

A `run_docker/.env.docker` fájlban add meg a futtatási beállításokat,
és állítsd be a jelszavakat. Meglévő `.env.docker` fájlt tarts meg.
Alapértelmezett image: `tomdenem1010/home:latest`, adatkönyvtár: `C:/DockerData/Home`.
Ha ezeket módosítanád, hozz létre egy `.env` fájlt a `run_docker` mappában, és add meg a `HOME_IMAGE` és `HOME_DATA_ROOT` értékeket.
A `.env` a Compose beállításait, a `.env.docker` az alkalmazás jelszavait tartalmazza;
mindkettő helyi, Git által figyelmen kívül hagyott fájl.

Hozd létre az `oracle`, `chrome`, `media` és `tcg` alkönyvtárakat az adatkönyvtárban.
A deck CSV-ket másold a `tcg` könyvtárba; a Compose nem másolja be őket automatikusan.
Meglévő telepítésnél pontosan a korábbi adatkönyvtárakat és a korábbi hostname-et
használd (alapértelmezés: `home`). A hostname a `compose.yaml` fájlban állítható.
A korábbi, `docker run`-nal indított konténert az első Compose-indítás előtt
állítsd le, hogy ne használják egyszerre ugyanazt az adatbázist és Chrome-profilt,
és felszabaduljanak a portok.

Indítás a `run_docker` mappából (a repository gyökerében: `cd run_docker`):

```powershell
docker compose up -d
docker compose logs -f
```

Új Docker Hub image telepítése ugyanazokkal a beállításokkal és mountokkal:

```powershell
docker compose pull
docker compose up -d
```

A Docker Desktop Containers nézetében `home` Compose-csoportként jelenik meg.
Leállítás: `docker compose stop`. A fenti parancsok nem törlik a host adatkönyvtárait.
Helyi image-hez állítsd a `.env` fájlban a `HOME_IMAGE=home:local` értéket,
majd `docker build -f Dockerfile -t home:local ..` és `docker compose up -d` (pull nélkül).

### Közvetlen docker run

Ezeket a parancsokat is a `run_docker` mappából futtasd.

```powershell
docker build -f Dockerfile -t home:local ..
# Szerkeszd a .env.docker fájlt, cseréld le az összes jelszót.
# A VNC_PASSWORD pontosan 8 karakteres legyen.
New-Item -ItemType Directory -Force C:\DockerData\Home\oracle
New-Item -ItemType Directory -Force C:\DockerData\Home\chrome
New-Item -ItemType Directory -Force C:\DockerData\Home\media
New-Item -ItemType Directory -Force C:\DockerData\Home\tcg
docker run -d --name home --hostname home --init --restart unless-stopped --stop-timeout 120 --shm-size 1g --memory 4g -p 127.0.0.1:5050:5050 -p 127.0.0.1:6080:6080 --env-file .env.docker --mount "type=bind,source=C:\DockerData\Home\oracle,target=/opt/oracle/oradata" --mount "type=bind,source=C:\DockerData\Home\chrome,target=/opt/home/chrome-profile" --mount "type=bind,source=C:\DockerData\Home\media,target=/media,readonly" --mount "type=bind,source=C:\DockerData\Home\tcg,target=/tcg,readonly" home:local
docker logs -f home
```

Ugyanez az indítás a könyvtárak automatikus létrehozásával:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1 -Image home:local -DataRoot C:\DockerData\Home
```

Az indítóscript az üres TCG-könyvtárba bemásolja a repository meglévő deck CSV-it.
Ha már van CSV a könyvtárban, nem módosítja annak tartalmát.

Docker Desktop Linux konténerekkel szükséges. Az app: http://localhost:5050.
Az első indítás több percig tarthat; a timeout `ORACLE_STARTUP_TIMEOUT_SECONDS`
(alapérték 600). Linuxon a mount source például `/srv/home/oracle` és
`/srv/home/chrome`. Az image induláskor automatikusan beállítja az adatbázis- és
profilkönyvtár tulajdonosát. Üres, írható mount és az env változók elegendők;
előre telepített Oracle vagy konfigurációs fájl nem szükséges. Az előkészítés root
felhasználóval fut, majd az Oracle és az alkalmazás az oracle felhasználóval indul.

Üres mounttal ugyanaz a konténer is újrainicializálható: az image megőrzi az ehhez
szükséges eredeti Oracle-fájlokat. Adatbázisfájlok nélküli, félbemaradt konfigurációt
átnevezve megőriz, majd inicializál. Meglévő adatbázisfájlokat nem cserél le;
hiányzó paraméterfájl esetén egyértelmű hibával leáll.

Az Oracle fájljai és a Chrome profilja a host megadott könyvtáraiban maradnak,
akkor is, ha a konténert törlöd és ugyanazokkal a mountokkal újra létrehozod.
Leállítás: `docker stop --time 120 home`. A hostkönyvtárakat ne töröld. Egy profilt
és adatbázis-könyvtárat egyszerre csak egy konténer használjon. Tartsd meg a
`--hostname home` értéket újralétrehozáskor is a Chrome profillock miatt.
Az Oracle-verzió cseréje külön adatbázis-upgrade feladat.

## Bejelentkezés Chrome-ban

1. Jelentkezz be a Home alkalmazásba a megadott admin adatokkal.
2. Helper → Start Chrome. Ez megnyitja a Chrome asztalnézetét.
3. Add meg a `VNC_PASSWORD` értékét, majd a Chrome-ban nyisd meg a kívánt oldalt
   és jelentkezz be. Az asztal külön lapon is megnyitható.
4. Indítsd a deck árfrissítést. A Playwright ugyanahhoz a Chrome-hoz és annak
   meglévő kontextusához kapcsolódik; használja a bejelentkezési cookie-kat.

Helper → Open Chrome visszavisz a meglévő asztalhoz. A Chrome bezárása után a
Start Chrome gombbal újraindítható. Az oldal lejárathatja a bejelentkezést; a
profil megtartása nem hosszabbítja meg a cookie-k érvényességét.

A noVNC a 6080-as porton érhető el, a példában csak a helyi gépről. A desktop
jelszava különbözik a Home jelszavától. A Chrome debugging portját nem kell a
hostra publikálni. Távoli elérésnél állítsd a `CHROME_DESKTOP_URL` változót a
böngészőből elérhető noVNC URL-re, HTTPS alkalmazás mellé HTTPS noVNC szükséges.
A Chrome nem headless. A profil cookie-jaihoz a konténerbeli jelszótárat használja.

Meglévő adatbázisnál az inicializáló környezeti változók nem változtatják meg a
tárolt jelszavakat. Az `APP_USER` és `APP_USER_PASSWORD` egyezzen a meglévő
adatbázis-felhasználóval. A lokális `FREEPDB1` JDBC kapcsolat automatikus.

A videókat a `C:\DockerData\Home\media` könyvtárba tedd. A frontend MEDIA → Server
folder path mezőjébe `/media` kerüljön. Az alkönyvtárak is beolvashatók.

A deck CSV-ket a `C:\DockerData\Home\tcg` könyvtár közvetlen gyökerébe tedd.
Dockerben a deck load alapértelmezett forrása `/tcg/*.csv`; a beépített classpath
CSV-k helyett ezt használja. A meglévő példafájlokat a `src/main/resources/tcg/deck`
könyvtárból másolhatod ide. UTF-8 fájlokat használj a meglévő `DeckName_v1.csv`
névformátummal. Az új CSV-khez nem kell új image-et építeni.

A média- és TCG-mount csak olvasható a konténerből; a gépeden szabadon módosíthatod
őket. A Chrome korábban kivezetett útvonala, portja és argumentumai felülírhatók.

## Docker Hub publikálás

A `.github/workflows/docker-publish.yml` main push esetén (merge után is), illetve
kézi indításra épít és publikál. Repository secrets:

- `DOCKERHUB_USERNAME`: Docker Hub felhasználónév.
- `DOCKERHUB_TOKEN`: az adott repositoryba írásra jogosult access token.

Opcionális repository variable: `DOCKERHUB_REPOSITORY`, alapérték `home`.
Image: `<DOCKERHUB_USERNAME>/<DOCKERHUB_REPOSITORY>`, tagek: `latest`, `sha-<commit>`.
A futtatási jelszavak nem kerülnek az image-be vagy a workflow-ba. A publikált image
nevét használd a `home:local` helyett a fenti indítóparancsban.

GitHub Enterprise Server esetén Dockerrel és külső hálózati eléréssel rendelkező
runner kell; a `runs-on` értéket a saját runner címkéjére állítsd, és engedélyezd
a használt külső actionöket.
