$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Push-Location (Join-Path $root 'frontend')
try { python -m http.server 5500 } finally { Pop-Location }
