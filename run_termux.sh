#!/bin/bash

# OBSIDIAN VAULT - Termux (Android) Launcher
# ------------------------------------------
# Usage:
# 1. chmod +x run_termux.sh
# 2. ./run_termux.sh

# Define Colors for Termux
RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

clear
echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}   OBSIDIAN VAULT - MOBILE TERMINAL     ${NC}"
echo -e "${CYAN}========================================${NC}"

# 1. Check/Install Java in Termux
if ! command -v javac &> /dev/null; then
    echo -e "${RED}[!] Java not found.${NC}"
    echo -e "${CYAN}[*] Attempting to install OpenJDK-17...${NC}"
    pkg update -y && pkg install openjdk-17 -y

    if [ $? -ne 0 ]; then
        echo -e "${RED}[!] Installation failed. Try: pkg install openjdk-17${NC}"
        exit 1
    fi
fi

# 2. Setup Directory
# Ensure we are in the right place relative to src/main/java
if [ ! -f "src/main/java/ObsidianVault.java" ]; then
    echo -e "${RED}[!] Error: Source file not found at src/main/java/ObsidianVault.java${NC}"
    echo "    Make sure you are in the project root folder."
    exit 1
fi

mkdir -p bin

# 3. Compile
echo -e "${CYAN}[*] Compiling Bytecode...${NC}"
javac -d bin src/main/java/ObsidianVault.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}[+] Ready.${NC}"
    sleep 0.5
    # 4. Run
    java -cp bin ObsidianVault
else
    echo -e "${RED}[!] Build Failed.${NC}"
fi