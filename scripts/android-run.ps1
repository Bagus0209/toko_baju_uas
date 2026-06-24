$ErrorActionPreference = "Stop"

$sdkRoot = $env:ANDROID_SDK_ROOT
if ([string]::IsNullOrWhiteSpace($sdkRoot)) {
    $sdkRoot = "C:\Users\Pongo\AppData\Local\Android\Sdk"
}

$adb = Join-Path $sdkRoot "platform-tools\adb.exe"
$apk = Join-Path $PSScriptRoot "..\app\build\outputs\apk\debug\app-debug.apk"
$packageName = "com.MyTokoBaju"
$activityName = "com.bagus.toko_baju_uas.SplashActivity"

if (-not (Test-Path $adb)) {
    throw "adb.exe tidak ditemukan di: $adb"
}

if (-not $args -or $args.Count -eq 0) {
    Write-Host "Pakai: .\scripts\android-run.ps1 devices|install|open|all"
    exit 1
}

$action = $args[0].ToLowerInvariant()

switch ($action) {
    "devices" {
        & $adb devices
    }
    "install" {
        if (-not (Test-Path $apk)) {
            throw "APK debug tidak ditemukan di: $apk. Jalankan build dulu."
        }
        & $adb install -r $apk
    }
    "open" {
        & $adb shell am start -W -n "$packageName/$activityName"
    }
    "all" {
        if (-not (Test-Path $apk)) {
            throw "APK debug tidak ditemukan di: $apk. Jalankan build dulu."
        }
        & $adb install -r $apk
        & $adb shell am start -W -n "$packageName/$activityName"
    }
    default {
        Write-Host "Perintah tidak dikenal: $action"
        Write-Host "Pakai: .\scripts\android-run.ps1 devices|install|open|all"
        exit 1
    }
}
