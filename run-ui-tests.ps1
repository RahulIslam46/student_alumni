# UI Test Runner for Alumni Network System
# This script allows you to run each UI component individually

$CLASSPATH = ".;c:\Users\RA\OneDrive\Desktop\mysql-connector-j-9.5.0.jar"
$JAVA = "C:\Program Files\Java\jdk-23\bin\java.exe"

Write-Host "===============================================" -ForegroundColor Cyan
Write-Host "   Alumni Network System - UI Test Runner" -ForegroundColor Cyan
Write-Host "===============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Available UI Components:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1. LoginFrame               - Login Screen" -ForegroundColor Green
Write-Host "  2. RegistrationFrame        - Registration Screen" -ForegroundColor Green
Write-Host "  3. MainFrame                - Main Application Window" -ForegroundColor Green
Write-Host "  4. ModernDashboardUI        - Modern Dashboard (Grid Cards)" -ForegroundColor Green
Write-Host "  5. AlumniNetworkFrame       - Classic Network View (2 Columns)" -ForegroundColor Green
Write-Host "  6. ChatConversationFrame    - Chat/Messaging Window" -ForegroundColor Green
Write-Host "  7. ProfileEditDialog        - Profile Editor Dialog" -ForegroundColor Green
Write-Host ""
Write-Host "  0. Exit" -ForegroundColor Red
Write-Host ""

$choice = Read-Host "Enter your choice (0-7)"

switch ($choice) {
    "1" {
        Write-Host "Starting LoginFrame..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH LoginFrame
    }
    "2" {
        Write-Host "Starting RegistrationFrame..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH RegistrationFrame
    }
    "3" {
        Write-Host "Starting MainFrame..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH MainFrame
    }
    "4" {
        Write-Host "Starting ModernDashboardUI..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH ModernDashboardUI
    }
    "5" {
        Write-Host "Starting AlumniNetworkFrame..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH AlumniNetworkFrame
    }
    "6" {
        Write-Host "Starting ChatConversationFrame..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH ChatConversationFrame
    }
    "7" {
        Write-Host "Starting ProfileEditDialog..." -ForegroundColor Cyan
        & $JAVA -cp $CLASSPATH ProfileEditDialog
    }
    "0" {
        Write-Host "Exiting..." -ForegroundColor Yellow
        exit
    }
    default {
        Write-Host "Invalid choice! Please run the script again." -ForegroundColor Red
    }
}
