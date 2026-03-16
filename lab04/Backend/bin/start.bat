@echo off

:: Move to the project root (one level up from bin/)
cd /d "%~dp0.."

:: Verify the virtual environment exists
if not exist ".venv\Scripts\python.exe" (
    echo [ERROR] Virtual environment not found. Run "python -m venv .venv" first.
    exit /b 1
)

:: Verify manage.py is present
if not exist "manage.py" (
    echo [ERROR] manage.py not found. Are you in the right directory?
    exit /b 1
)

echo Starting gateway on port 8079 (HTTPS)...
.venv\Scripts\python.exe manage.py runsslserver 8079
