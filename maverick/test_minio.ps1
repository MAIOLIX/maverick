# =========================================================================
# Script PowerShell per Test Avanzati MinIO Repository - Maverick ML
# =========================================================================

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$TestFile = $null,
    [switch]$Verbose = $false
)

$ErrorActionPreference = "Continue"
$ProgressPreference = "SilentlyContinue"

# Colori per output
function Write-Success { param($Message) Write-Host "✅ $Message" -ForegroundColor Green }
function Write-Error { param($Message) Write-Host "❌ $Message" -ForegroundColor Red }
function Write-Info { param($Message) Write-Host "ℹ️  $Message" -ForegroundColor Cyan }
function Write-Warning { param($Message) Write-Host "⚠️  $Message" -ForegroundColor Yellow }

# URLs
$MinioApi = "$BaseUrl/api/v1/models/minio"
$MinioTest = "$BaseUrl/api/v1/minio"

Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "🚀 MAVERICK MINIO TEST SUITE (PowerShell)" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""

# Contatori per statistiche
$TestsPassed = 0
$TestsFailed = 0
$TestsTotal = 0

function Invoke-Test {
    param(
        [string]$TestName,
        [string]$Method,
        [string]$Uri,
        [hashtable]$Body = @{},
        [string]$FilePath = $null,
        [bool]$ExpectSuccess = $true
    )
    
    $global:TestsTotal++
    Write-Host ""
    Write-Info "Test $global:TestsTotal`: $TestName"
    
    try {
        if ($FilePath) {
            # Upload con file
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Form $Body -Verbose:$Verbose
        } elseif ($Method -eq "POST" -and $Body.Count -gt 0) {
            # POST con form data
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Body $Body -Verbose:$Verbose
        } else {
            # GET o DELETE
            $response = Invoke-RestMethod -Uri $Uri -Method $Method -Verbose:$Verbose
        }
        
        if ($response.status -eq "SUCCESS" -or $Method -eq "GET") {
            Write-Success "$TestName - PASSED"
            if ($Verbose) {
                $response | ConvertTo-Json -Depth 3 | Write-Host -ForegroundColor Gray
            }
            $global:TestsPassed++
            return $response
        } else {
            Write-Error "$TestName - FAILED: $($response.message)"
            $global:TestsFailed++
            return $null
        }
    }
    catch {
        if ($ExpectSuccess) {
            Write-Error "$TestName - FAILED: $($_.Exception.Message)"
            $global:TestsFailed++
        } else {
            Write-Success "$TestName - PASSED (Expected Error)"
            $global:TestsPassed++
        }
        return $null
    }
}

# Test 1: Health Check
$healthResponse = Invoke-Test -TestName "Health Check MinIO" -Method "GET" -Uri "$MinioTest/health"

if (-not $healthResponse) {
    Write-Error "MinIO non raggiungibile. Verifica che sia in esecuzione su localhost:32768"
    exit 1
}

# Test 2: Lista Bucket
Invoke-Test -TestName "Lista Bucket" -Method "GET" -Uri "$MinioTest/buckets"

# Test 3: Upload con file di test
Write-Info "Creazione file di test..."
$testContent = @"
{
  "model_type": "ONNX",
  "version": "1.0",
  "description": "Test model for PowerShell automation",
  "created_by": "PowerShell Test Suite",
  "timestamp": "$(Get-Date -Format 'yyyy-MM-ddTHH:mm:ss')"
}
"@

$testFileName = "test-model-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
$testFilePath = Join-Path $env:TEMP $testFileName
$testContent | Out-File -FilePath $testFilePath -Encoding utf8

# Prepara dati per upload
$uploadForm = @{
    modelName = "powershell-test"
    version = "1.0"
    file = Get-Item $testFilePath
}

$uploadResponse = Invoke-Test -TestName "Upload Modello Test" -Method "POST" -Uri "$MinioApi/upload" -Body $uploadForm -FilePath $testFilePath

if ($uploadResponse) {
    $modelName = $uploadResponse.modelName
    $version = $uploadResponse.version
    $fileName = $uploadResponse.fileName
    
    # Test 4: Info Modello
    Invoke-Test -TestName "Info Modello" -Method "GET" -Uri "$MinioApi/info/$modelName/$version/$fileName"
    
    # Test 5: Download Modello
    Invoke-Test -TestName "Download Modello" -Method "GET" -Uri "$MinioApi/download/$modelName/$version/$fileName"
    
    # Test 6: Copia Modello
    $copyBody = @{
        modelName = $modelName
        sourceVersion = $version
        targetVersion = "1.1"
        fileName = $fileName
    }
    Invoke-Test -TestName "Copia Modello (1.0 -> 1.1)" -Method "POST" -Uri "$MinioApi/copy" -Body $copyBody
    
    # Test 7: Lista Versioni
    Invoke-Test -TestName "Lista Versioni Modello" -Method "GET" -Uri "$MinioApi/versions/$modelName"
    
    # Test 8: Dimensione Modello
    Invoke-Test -TestName "Dimensione Modello v1.0" -Method "GET" -Uri "$MinioApi/size/$modelName/$version"
    Invoke-Test -TestName "Dimensione Modello v1.1" -Method "GET" -Uri "$MinioApi/size/$modelName/1.1"
    
    # Test 9: Cancellazione Modello v1.1
    Invoke-Test -TestName "Cancella Modello v1.1" -Method "DELETE" -Uri "$MinioApi/$modelName/1.1/$fileName"
    
    # Test 10: Verifica Cancellazione (dovrebbe fallire)
    Invoke-Test -TestName "Verifica Cancellazione (Expected Error)" -Method "GET" -Uri "$MinioApi/info/$modelName/1.1/$fileName" -ExpectSuccess $false
    
    # Test 11: Upload File Personalizzato (se fornito)
    if ($TestFile -and (Test-Path $TestFile)) {
        Write-Info "Upload file personalizzato: $TestFile"
        $customUploadForm = @{
            modelName = "custom-model"
            version = "1.0"
            file = Get-Item $TestFile
        }
        $customResponse = Invoke-Test -TestName "Upload File Personalizzato" -Method "POST" -Uri "$MinioApi/upload" -Body $customUploadForm -FilePath $TestFile
        
        if ($customResponse) {
            Invoke-Test -TestName "Info File Personalizzato" -Method "GET" -Uri "$MinioApi/info/$($customResponse.modelName)/$($customResponse.version)/$($customResponse.fileName)"
        }
    }
    
    # Cleanup
    Write-Info "Cleanup: Rimozione modelli di test..."
    try {
        Invoke-RestMethod -Uri "$MinioApi/$modelName/$version/$fileName" -Method DELETE -ErrorAction SilentlyContinue
        if ($uploadResponse) {
            Invoke-RestMethod -Uri "$MinioApi/custom-model/1.0/*" -Method DELETE -ErrorAction SilentlyContinue
        }
    }
    catch {
        Write-Warning "Cleanup parzialmente fallito (normale se alcuni file erano già stati cancellati)"
    }
}

# Rimuovi file temporaneo
Remove-Item $testFilePath -ErrorAction SilentlyContinue

# Statistiche finali
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "📊 RISULTATI TEST SUITE" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host ""
Write-Host "Test Totali:    $TestsTotal" -ForegroundColor White
Write-Host "Test Passati:   $TestsPassed" -ForegroundColor Green
Write-Host "Test Falliti:   $TestsFailed" -ForegroundColor Red

$successRate = if ($TestsTotal -gt 0) { [math]::Round(($TestsPassed / $TestsTotal) * 100, 2) } else { 0 }
Write-Host "Tasso Successo: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { "Green" } elseif ($successRate -ge 70) { "Yellow" } else { "Red" })

Write-Host ""
if ($TestsFailed -eq 0) {
    Write-Success "🎉 TUTTI I TEST SONO PASSATI! Repository MinIO funziona perfettamente!"
} else {
    Write-Warning "⚠️  Alcuni test sono falliti. Controlla la configurazione MinIO."
}

Write-Host ""
Write-Host "Uso: .\test_minio.ps1 [-BaseUrl 'http://localhost:8080'] [-TestFile 'path\to\model.onnx'] [-Verbose]" -ForegroundColor Gray
Write-Host ""

# Exit code
exit $TestsFailed
