# Docker Troubleshooting Guide

## Error: "The system cannot find the file specified" for dockerDesktopLinuxEngine

This error means **Docker Desktop is not running** on Windows.

## Solution

### Step 1: Start Docker Desktop

1. **Open Docker Desktop Application**
   - Press `Win` key
   - Search for "Docker Desktop"
   - Click to open

2. **Wait for Docker to Start**
   - Look for the Docker whale icon in the system tray (bottom right)
   - Wait until it shows "Docker Desktop is running"
   - This can take 30-60 seconds

3. **Verify Docker is Running**
   ```bash
   docker info
   ```
   Should show Docker system information (not an error)

### Step 2: Alternative - Start via Command Line

**PowerShell (as Administrator):**
```powershell
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

**Command Prompt:**
```cmd
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"
```

### Step 3: Check Docker Status

```bash
# Check Docker version
docker --version

# Check if Docker daemon is running
docker info

# Test with a simple command
docker ps
```

## Common Issues

### Issue 1: Docker Desktop Not Installed

**Solution:** Download and install Docker Desktop from:
https://www.docker.com/products/docker-desktop/

### Issue 2: Docker Desktop Stuck Starting

**Solution:**
1. Close Docker Desktop completely
2. Restart your computer
3. Open Docker Desktop again
4. Wait for it to fully start (check system tray)

### Issue 3: WSL 2 Not Configured

Docker Desktop on Windows requires WSL 2.

**Check WSL:**
```powershell
wsl --list --verbose
```

**Install WSL 2 if needed:**
```powershell
wsl --install
```

Then restart Docker Desktop.

### Issue 4: Hyper-V Not Enabled

**Check Hyper-V:**
```powershell
Get-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V-All
```

**Enable Hyper-V (if disabled):**
```powershell
Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
```

Then restart your computer.

## Verify Docker is Working

After Docker Desktop starts, test with:

```bash
# 1. Check Docker version
docker --version

# 2. Check Docker info
docker info

# 3. Run a test container
docker run hello-world

# 4. Check running containers
docker ps
```

## Once Docker is Running

Then you can build and run your application:

```bash
# Build the image
docker build -t json-compare-app .

# Or use docker-compose
docker-compose up -d
```

## Quick Checklist

- [ ] Docker Desktop is installed
- [ ] Docker Desktop is running (check system tray)
- [ ] WSL 2 is installed and configured
- [ ] `docker --version` works
- [ ] `docker info` shows system information
- [ ] `docker ps` works without errors

## Still Having Issues?

1. **Restart Docker Desktop**
   - Right-click Docker icon in system tray
   - Select "Restart Docker Desktop"

2. **Restart Computer**
   - Sometimes required after Docker installation

3. **Check Windows Updates**
   - Ensure Windows is up to date

4. **Check Docker Desktop Logs**
   - Docker Desktop → Settings → Troubleshoot → View logs

