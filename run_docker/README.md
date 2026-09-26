# Docker futtatás és image építés

A repository gyökeréből:

```powershell
docker build -f run_docker/Dockerfile -t home:local .
docker compose -f run_docker/compose.yaml up -d
```

A build context a repository gyökere. A kizárásokat a `Dockerfile.dockerignore` tartalmazza. A futtatási jelszavak a helyi `.env.docker` fájlban vannak.

Részletes útmutató: [docs/docker.md](../docs/docker.md).
