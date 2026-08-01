$ErrorActionPreference = 'Stop'

$containerName = 'oracle-xe'
$imageName = 'gvenzl/oracle-xe:21-slim'
$databaseVolume = 'oracle-xe-data'
$pdbName = 'XEPDB1'
$startupTimeoutSeconds = 600

function Get-DotEnvValue {
    param(
        [string]$Path,
        [string]$Name
    )

    $line = Get-Content -LiteralPath $Path |
        Where-Object { $_ -match "^$([regex]::Escape($Name))=(.*)$" } |
        Select-Object -First 1

    if (-not $line) {
        throw "Missing $Name value in $Path."
    }

    return ($line -replace "^$([regex]::Escape($Name))=", '').Trim()
}

$envPath = Join-Path $PSScriptRoot '.env'
if (-not (Test-Path -LiteralPath $envPath)) {
    throw ".env file not found: $envPath"
}

$dbUsername = Get-DotEnvValue -Path $envPath -Name 'DB_USERNAME'
$dbPassword = Get-DotEnvValue -Path $envPath -Name 'DB_PASSWORD'

if ($dbUsername -notmatch '^[A-Za-z][A-Za-z0-9_$#]{0,29}$') {
    throw 'DB_USERNAME must be an Oracle-compatible identifier of at most 30 characters.'
}

if ($dbPassword.Length -lt 8 -or $dbPassword.Contains('"') -or $dbPassword -match '[\r\n]') {
    throw 'DB_PASSWORD must contain at least 8 characters and must not contain a double quote or a newline.'
}

$oracleUsername = $dbUsername.ToUpperInvariant()
$escapedPassword = $dbPassword.Replace('"', '""')

docker version | Out-Null

Write-Host 'Removing the previous Oracle XE database...'
$existingContainer = docker ps -aq --filter "name=^/$containerName$"
if ($existingContainer) {
    docker rm -f $containerName | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Could not remove container $containerName."
    }
}

$existingVolume = docker volume ls -q --filter "name=^$databaseVolume$"
if ($existingVolume) {
    docker volume rm $databaseVolume | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "Could not remove volume $databaseVolume."
    }
}

Write-Host 'Creating Oracle XE container...'
# On the first startup, ORACLE_PASSWORD sets the SYS and SYSTEM passwords.
docker run -d `
    --name $containerName `
    -p 1521:1521 `
    -e "ORACLE_PASSWORD=$dbPassword" `
    -v "${databaseVolume}:/opt/oracle/oradata" `
    $imageName | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Could not create the Oracle XE container from image $imageName."
}

Write-Host 'Waiting for Oracle XE startup...'
$deadline = (Get-Date).AddSeconds($startupTimeoutSeconds)
do {
    $ready = docker logs $containerName 2>&1 | Select-String -SimpleMatch 'DATABASE IS READY TO USE!' -Quiet
    if ($ready) {
        break
    }

    Start-Sleep -Seconds 5
} while ((Get-Date) -lt $deadline)

if (-not $ready) {
    throw "Oracle XE did not become ready within $startupTimeoutSeconds seconds. Check: docker logs $containerName"
}

$sql = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE;
ALTER SESSION SET CONTAINER = $pdbName;

CREATE USER $oracleUsername IDENTIFIED BY "$escapedPassword";

GRANT DBA TO $oracleUsername;
GRANT ALL PRIVILEGES TO $oracleUsername;
GRANT UNLIMITED TABLESPACE TO $oracleUsername;
EXIT;
"@

Write-Host "Creating $oracleUsername administrator in $pdbName..."
$sql | docker exec -i --user oracle $containerName sqlplus -s / as sysdba
if ($LASTEXITCODE -ne 0) {
    throw 'Oracle user creation failed.'
}

Write-Host "Done. Connection: jdbc:oracle:thin:@//localhost:1521/$pdbName"
