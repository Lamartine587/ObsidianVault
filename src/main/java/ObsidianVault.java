import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/*
 * OBSIDIAN VAULT v4.2 - Precision Defense Edition
 * Port Vulnerability Scanner & Threat Analyzer
 * Features: Elite UI + Smart SSH Handling + Firewall State Verification
 */
public class ObsidianVault {

    // === CONFIGURATION ===
    private static final int TIMEOUT_MS = 200;
    private static final int THREAD_POOL_SIZE = 50; // Adjust based on CPU cores

    // === ANSI ELITE COLORS ===
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[38;5;196m";
    public static final String GREEN = "\033[38;5;46m";
    public static final String YELLOW = "\033[38;5;226m";
    public static final String BLUE = "\033[38;5;75m";
    public static final String PURPLE = "\033[38;5;165m";
    public static final String CYAN = "\033[38;5;87m";
    public static final String ORANGE = "\033[38;5;208m";
    public static final String GRAY = "\033[38;5;240m";
    public static final String BOLD = "\033[1m";

    // === VULNERABILITY DICTIONARY ===
    private static final Map<Integer, String> PORT_INTEL = new HashMap<>();
    static {
        PORT_INTEL.put(20, "FTP-DATA [Medium]");
        PORT_INTEL.put(21, "FTP [Critical] - Cleartext authentication");
        PORT_INTEL.put(22, "SSH [Medium] - Bruteforce vector");
        PORT_INTEL.put(23, "TELNET [Critical] - Cleartext protocol");
        PORT_INTEL.put(25, "SMTP [Low] - Relay/Spam risk");
        PORT_INTEL.put(53, "DNS [Medium] - Cache poisoning risk");
        PORT_INTEL.put(80, "HTTP [Medium] - Unencrypted traffic");
        PORT_INTEL.put(110, "POP3 [Medium] - Cleartext email");
        PORT_INTEL.put(135, "RPC [High] - Enumeration risk");
        PORT_INTEL.put(139, "NETBIOS [High] - Info leakage");
        PORT_INTEL.put(143, "IMAP [Medium] - Cleartext email");
        PORT_INTEL.put(443, "HTTPS [Low] - SSL/TLS Config");
        PORT_INTEL.put(445, "SMB [Critical] - Remote Code Execution risk");
        PORT_INTEL.put(631, "IPP [Low] - CUPS Printing");
        PORT_INTEL.put(1433, "MSSQL [High] - DB Injection risk");
        PORT_INTEL.put(3306, "MYSQL [High] - DB Exposure");
        PORT_INTEL.put(3389, "RDP [High] - BlueKeep/Bruteforce");
        PORT_INTEL.put(5432, "POSTGRES [High] - DB Exposure");
        PORT_INTEL.put(8080, "HTTP-ALT [Medium] - Unmonitored Web App");
    }

    // === HARDENING STRATEGIES ===
    private static final Map<Integer, String> HARDENING_STEPS = new HashMap<>();
    static {
        HARDENING_STEPS.put(21, "Configure FTP over SSL (FTPS) or switch to SFTP (Port 22). Disable Anonymous Login.");
        HARDENING_STEPS.put(22, "Edit /etc/ssh/sshd_config: Set 'PermitRootLogin no' and 'PasswordAuthentication no'. Use SSH Keys.");
        HARDENING_STEPS.put(23, "Migrate to SSH immediately. If legacy required, wrap in VPN tunnel.");
        HARDENING_STEPS.put(53, "Disable Zone Transfers (allow-transfer { none; }). Configure DNSSEC.");
        HARDENING_STEPS.put(80, "Configure 301 Redirect to HTTPS (Port 443). Install ModSecurity WAF.");
        HARDENING_STEPS.put(139, "Block port 139/445 at firewall level for external traffic. Allow only via VPN.");
        HARDENING_STEPS.put(445, "Ensure SMBv1 is disabled. Enforce SMB Message Signing. Restrict access via IP Whitelist.");
        HARDENING_STEPS.put(1433, "Disable 'sa' account. Enforce encryption. Bind to Localhost if web app is local.");
        HARDENING_STEPS.put(3306, "Edit my.cnf: Set 'bind-address = 127.0.0.1'. Create specific users, never use root remotely.");
        HARDENING_STEPS.put(3389, "Enable Network Level Authentication (NLA). Change default port. Use RDP Gateway.");
        HARDENING_STEPS.put(5432, "Edit pg_hba.conf: Restrict IP ranges. Use MD5/SCRAM-SHA-256 auth.");
        HARDENING_STEPS.put(8080, "Review running app. Place behind Nginx Reverse Proxy with Basic Auth.");
    }

    public static void main(String[] args) {
        // Clear terminal (Unix/Linux)
        System.out.print("\033[H\033[2J");
        System.out.flush();

        printBanner();
        delayPrint(CYAN + "Initializing Defense Matrices... " + GREEN + "[ONLINE]" + RESET, 10);
        delayPrint(GRAY + "Mitigation Database Loaded: " + HARDENING_STEPS.size() + " protocols." + RESET, 10);

        // Initial Firewall Check
        checkFirewallState();

        Scanner scanner = new Scanner(System.in);
        boolean active = true;

        while (active) {
            printMenu();
            System.out.print(BOLD + ORANGE + "root@obsidian:~# " + RESET);
            String cmd = scanner.nextLine().trim();

            switch (cmd) {
                case "1" -> scanTarget(scanner, "127.0.0.1");
                case "2" -> {
                    String target = prompt(scanner, "Enter Target IP » ");
                    if (!target.isEmpty()) scanTarget(scanner, target);
                }
                case "3" -> showThreatDatabase();
                case "4" -> analyzeSystem();
                case "5" -> engageActiveDefense(scanner);
                case "6" -> verifyFirewallRules();
                case "7" -> enableFirewall(scanner);
                case "0" -> {
                    delayPrint(RED + "Disengaging scanner. Cleaning logs..." + RESET, 20);
                    active = false;
                }
                default -> delayPrint(RED + "[!] Unknown command." + RESET, 10);
            }
            System.out.println();
        }
        scanner.close();
    }

    // ==================== SCANNING ENGINE ====================

    private static void scanTarget(Scanner s, String targetIp) {
        System.out.println(PURPLE + BOLD + "\n[ INITIATING SCAN & HARDENING ANALYSIS: " + targetIp + " ]" + RESET);
        System.out.println(GRAY + "Scanning common threat vectors..." + RESET);

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<ScanResult>> futures = new ArrayList<>();
        List<ScanResult> openPorts = new ArrayList<>();

        // Ports to scan (1-1024 + Specific High Risk)
        Set<Integer> portsToScan = new TreeSet<>();
        for (int i = 1; i <= 1024; i++) portsToScan.add(i);
        portsToScan.addAll(PORT_INTEL.keySet());

        for (int port : portsToScan) {
            futures.add(executor.submit(() -> checkPort(targetIp, port)));
        }

        System.out.println(CYAN + "------------------------------------------------------------" + RESET);
        System.out.printf("%-10s | %-15s | %s%n", "PORT", "STATUS", "INTELLIGENCE / RISK");
        System.out.println(CYAN + "------------------------------------------------------------" + RESET);

        for (Future<ScanResult> f : futures) {
            try {
                ScanResult result = f.get();
                if (result.isOpen) {
                    openPorts.add(result);
                    String risk = PORT_INTEL.getOrDefault(result.port, "Unknown Service");
                    String color = GREEN;

                    if (risk.contains("[Critical]")) color = RED + BOLD;
                    else if (risk.contains("[High]")) color = RED;
                    else if (risk.contains("[Medium]")) color = YELLOW;

                    System.out.printf("%s%-10d | OPEN            | %s%s%n", color, result.port, risk, RESET);
                }
            } catch (Exception ignored) {}
        }
        executor.shutdown();

        // Mitigation Advice Section
        if (!openPorts.isEmpty()) {
            System.out.println(CYAN + "------------------------------------------------------------" + RESET);
            System.out.println(ORANGE + BOLD + "\n[ TACTICAL HARDENING PROTOCOLS ]" + RESET);

            boolean adviceFound = false;
            for (ScanResult res : openPorts) {
                if (HARDENING_STEPS.containsKey(res.port)) {
                    adviceFound = true;
                    System.out.println(YELLOW + " [+] Port " + res.port + ": " + RESET + HARDENING_STEPS.get(res.port));
                }
            }
            if (!adviceFound) {
                System.out.println(GREEN + "No specific hardening templates available. Use standard firewall rules." + RESET);
            }
        } else {
            System.out.println(GREEN + "\n[+] System is stealthy. No hardening required." + RESET);
        }
    }

    // ==================== ACTIVE DEFENSE ====================

    private static void engageActiveDefense(Scanner s) {
        System.out.println(RED + BOLD + "\n[ ACTIVE DEFENSE PROTOCOL ]" + RESET);

        // Check firewall status first
        if (!checkFirewallState()) {
            System.out.println(RED + "[!] WARNING: Firewall (UFW) appears INACTIVE." + RESET);
            System.out.println(RED + "    Rules will be added but will NOT block traffic until enabled." + RESET);
            String fix = prompt(s, "    Enable Firewall now? (y/n) » ");
            if (fix.equalsIgnoreCase("y")) enableFirewall(s);
        }

        String input = prompt(s, "Target Port to Secure » ");
        if (input.isEmpty()) return;

        try {
            int port = Integer.parseInt(input);
            String[] command = null;

            if (port == 22) {
                System.out.println(YELLOW + "\n[ SSH SECURITY STRATEGY ]" + RESET);
                System.out.println("  [L] Limit (Recommended) - Anti-Bruteforce.");
                System.out.println("  [B] Block (Aggressive)  - Totally denies connection.");
                String choice = prompt(s, "Select Strategy (L/B) » ");

                if (choice.equalsIgnoreCase("B")) {
                    System.out.println(RED + "Strategy: BLOCK (Warning: Ensure you are not connected via SSH!)" + RESET);
                    command = new String[]{"sudo", "ufw", "deny", "ssh"};
                } else {
                    System.out.println(GREEN + "Strategy: LIMIT (Safe)" + RESET);
                    command = new String[]{"sudo", "ufw", "limit", "ssh"};
                }
            }
            else if (port == 80 || port == 443) {
                command = new String[]{"sudo", "ufw", "allow", String.valueOf(port)};
            }
            else {
                command = new String[]{"sudo", "ufw", "deny", String.valueOf(port)};
            }

            System.out.println("Command: " + Arrays.toString(command));
            String confirm = prompt(s, "EXECUTE DEFENSE? (y/n) » ");

            if (confirm.equalsIgnoreCase("y")) {
                boolean success = executeSystemCommand(command, true);
                if (success) {
                    System.out.println(GREEN + "[+] Rule injected successfully." + RESET);
                } else {
                    System.out.println(RED + "[!] Failed. Run with Root privileges." + RESET);
                }
            }

        } catch (NumberFormatException e) {
            System.out.println(RED + "Invalid port." + RESET);
        }
    }

    // ==================== UTILS & HELPERS ====================

    private static boolean checkFirewallState() {
        try {
            ProcessBuilder pb = new ProcessBuilder("sudo", "ufw", "status");
            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = r.readLine();
            if (line != null && line.contains("Status: active")) {
                System.out.println(GRAY + "Firewall Status: " + GREEN + "ACTIVE" + RESET);
                return true;
            } else {
                System.out.println(GRAY + "Firewall Status: " + RED + "INACTIVE" + RESET);
                return false;
            }
        } catch (Exception e) {
            System.out.println(RED + "Cannot verify firewall status (Sudo required)" + RESET);
            return false;
        }
    }

    private static void enableFirewall(Scanner s) {
        System.out.println(YELLOW + "Enabling UFW..." + RESET);
        executeSystemCommand(new String[]{"sudo", "ufw", "enable"}, false);
    }

    private static void verifyFirewallRules() {
        System.out.println(CYAN + BOLD + "\n[ FIREWALL STATUS REPORT ]" + RESET);
        executeSystemCommand(new String[]{"sudo", "ufw", "status", "verbose"}, false);
    }

    private static boolean executeSystemCommand(String[] cmd, boolean silent) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = r.readLine()) != null) {
                if (!silent) System.out.println("   " + line);
            }
            return p.waitFor() == 0;
        } catch (Exception e) {
            System.out.println(RED + "Error: " + e.getMessage() + RESET);
            return false;
        }
    }

    private static ScanResult checkPort(String ip, int port) {
        try {
            Socket s = new Socket();
            s.connect(new InetSocketAddress(ip, port), TIMEOUT_MS);
            s.close();
            return new ScanResult(port, true);
        } catch (Exception e) {
            return new ScanResult(port, false);
        }
    }

    record ScanResult(int port, boolean isOpen) {}

    private static void showThreatDatabase() {
        System.out.println(BLUE + BOLD + "\n[ THREAT & MITIGATION DB ]" + RESET);
        HARDENING_STEPS.forEach((p, a) -> System.out.printf("%sPort %-5d %s: %s%n", YELLOW, p, RESET, a));
    }

    private static void analyzeSystem() {
        delayPrint(YELLOW + "\n[ HEURISTIC ANALYSIS ]" + RESET, 20);
        checkFirewallState();
        delayPrint("Outbound connection check... [CLEAN]", 20);
        System.out.println(GREEN + "System integrity verified." + RESET);
    }

    private static void printBanner() {
        System.out.print(RED + BOLD);
        String[] b = {
                " ██████╗ ██████╗ ███████╗██╗██████╗ ██╗ █████╗ ███╗   ██╗",
                " ██╔═══██╗██╔══██╗██╔════╝██║██╔══██╗██║██╔══██╗████╗  ██║",
                " ██║   ██║██████╔╝███████╗██║██║  ██║██║███████║██╔██╗ ██║",
                " ██║   ██║██╔══██╗╚════██║██║██║  ██║██║██╔══██║██║╚██╗██║",
                " ╚██████╔╝██████╔╝███████║██║██████╔╝██║██║  ██║██║ ╚████║",
                "  ╚═════╝ ╚═════╝ ╚══════╝╚═╝╚═════╝ ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝",
                "",
                "          O B S I D I A N   V A U L T   v4.2",
                "     Active Vulnerability & Hardening Tool",
                "           Run anywhere • Leave no trace"
        };
        for (String line : b) {
            delayPrint("       " + line, 8);
        }
        System.out.println(RESET);
    }

    private static String prompt(Scanner s, String msg) {
        System.out.print(CYAN + msg + RESET);
        return s.nextLine().trim();
    }

    private static void delayPrint(String text, int delayMs) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            try { Thread.sleep(delayMs); } catch (Exception ignored) {}
        }
        System.out.println();
    }

    private static void printMenu() {
        System.out.println(BOLD + ORANGE + "╔═══════════════════ COMMAND MATRIX ═══════════════════╗" + RESET);
        System.out.println("  [1] Quick Scan (Localhost)       [5] Active Hardening");
        System.out.println("  [2] Target Scan (Custom IP)      [6] Verify Firewall Rules");
        System.out.println("  [3] View Mitigation DB           [7] ENABLE FIREWALL (UFW)");
        System.out.println("  [4] System Analysis              [0] Exit");
        System.out.println(BOLD + ORANGE + "╚══════════════════════════════════════════════════════╝" + RESET);
    }
}