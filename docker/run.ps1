param(
    [string]$Image = 'home:local',
    [string]$DataRoot = 'C:\DockerData\Home',
    [string]$EnvFile = (Join-Path $PSScriptRoot '..\.env.docker')
)

$ErrorActionPreference = 'Stop'
$resolvedEnvFile = (Resolve-Path -LiteralPath $EnvFile).Path
foreach ($folder in @('oracle', 'chrome', 'media', 'tcg')) {
    New-Item -ItemType Directory -Force -Path (Join-Path $DataRoot $folder) | Out-Null
}
$resolvedDataRoot = (Resolve-Path -LiteralPath $DataRoot).Path
$deckDirectory = Join-Path $resolvedDataRoot 'tcg'
$bundledDeckDirectory = Join-Path $PSScriptRoot '..\src\main\resources\tcg\deck'
if ((Test-Path -LiteralPath $bundledDeckDirectory) -and
    @(Get-ChildItem -LiteralPath $deckDirectory -Filter '*.csv' -File).Count -eq 0) {
    Get-ChildItem -LiteralPath $bundledDeckDirectory -Filter '*.csv' -File |
        Copy-Item -Destination $deckDirectory
}

$dockerArguments = @(
    'run', '-d', '--name', 'home', '--hostname', 'home', '--init',
    '--restart', 'unless-stopped', '--stop-timeout', '120',
    '--shm-size', '1g', '--memory', '4g',
    '-p', '127.0.0.1:5050:5050', '-p', '127.0.0.1:6080:6080',
    '--env-file', $resolvedEnvFile,
    '--mount', "type=bind,source=$resolvedDataRoot\oracle,target=/opt/oracle/oradata",
    '--mount', "type=bind,source=$resolvedDataRoot\chrome,target=/opt/home/chrome-profile",
    '--mount', "type=bind,source=$resolvedDataRoot\media,target=/media,readonly",
    '--mount', "type=bind,source=$resolvedDataRoot\tcg,target=/tcg,readonly",
    '--env', 'TCG_DECK_RESOURCE_PATTERN=file:/tcg/*.csv',
    $Image
)
& docker @dockerArguments
if ($LASTEXITCODE -ne 0) {
    throw "Docker startup failed with exit code $LASTEXITCODE."
}
