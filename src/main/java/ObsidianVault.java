import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;  // Fixed import

import java.util.logging.Level;
import java.util.logging.Logger;

public class ObsidianVault {

    private static final String DB_NAME = "obsidian_vault";
    private static final String COLL_NAME = "operations";
    private static final String CONNECTION_STRING = "mongodb://localhost:27017";

    // ANSI Colors
    public static final String RESET = "\033[0m";
    public static final String RED = "\033[38;5;196m";
    public static final String GREEN = "\033[38;5;46m";
    public static final String YELLOW = "\033[38;5;226m";
    public static final String BLUE = "\033[38;5;75m";
    public static final String CYAN = "\033[38;5;87m";
    public static final String PURPLE = "\033[38;5;165m";
    public static final String ORANGE = "\033[38;5;208m";
    public static final String GRAY = "\033[38;5;240m";
    public static final String BOLD = "\033[1m";
    public static final String DIM = "\033[2m";

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Logger.getLogger("org.mongodb.driver").setLevel(Level.SEVERE);

        printBanner();
        delayPrint(CYAN + "Establishing quantum-encrypted tunnel to local C2 node..." + RESET, 30);

        try (MongoClient client = MongoClients.create(CONNECTION_STRING)) {
            MongoDatabase db = client.getDatabase(DB_NAME);
            MongoCollection<Document> col = db.getCollection(COLL_NAME);

            if (!db.listCollectionNames().into(new ArrayList<>()).contains(COLL_NAME)) {
                db.createCollection(COLL_NAME);
                delayPrint(YELLOW + "[+] Vault collection initialized." + RESET, 20);
            }

            col.createIndex(Indexes.ascending("codename"));
            col.createIndex(Indexes.ascending("target_ip"));
            col.createIndex(Indexes.ascending("status"));
            col.createIndex(Indexes.compoundIndex(Indexes.ascending("status"), Indexes.descending("created_at")));

            delayPrint(GREEN + "[+] Connection secured | Indexes deployed | Ready for ops." + RESET, 25);

            OperationsDAO dao = new OperationsDAO(col);
            Scanner scanner = new Scanner(System.in);
            boolean active = true;

            while (active) {
                printMenu();
                System.out.print(BOLD + ORANGE + "root@obsidian:~# " + RESET);
                String cmd = scanner.nextLine().trim();

                switch (cmd) {
                    case "1" -> createOperation(scanner, dao);
                    case "2" -> listOperations(dao);
                    case "3" -> updateStatus(scanner, dao);
                    case "4" -> generateIntelBriefing(dao);
                    case "5" -> purgeOperation(scanner, dao);
                    case "6" -> searchByVector(scanner, dao);
                    case "7" -> stealthMode();
                    case "0" -> {
                        delayPrint(RED + "Terminating session. Wiping RAM traces..." + RESET, 40);
                        active = false;
                    }
                    default -> delayPrint(RED + "[!] Unknown command. Access denied." + RESET, 30);
                }
                System.out.println();
            }

        } catch (Exception e) {
            delayPrint(RED + "FATAL: " + e.getMessage() + RESET, 30);
            e.printStackTrace();
        }
    }

    private static void createOperation(Scanner s, OperationsDAO dao) {
        System.out.println(PURPLE + BOLD + "\n[ NEW OPERATION REGISTRATION ]" + RESET);

        String codename = prompt(s, "Codename » ");
        if (codename.isEmpty()) return;

        if (dao.existsByCodename(codename)) {
            delayPrint(RED + "Codename conflict. Operation already exists." + RESET, 30);
            return;
        }

        String ip;
        while (true) {
            ip = prompt(s, "Target Vector (IPv4) » ");
            if (isValidIPv4(ip)) break;
            delayPrint(RED + "Invalid vector format." + RESET, 20);
        }

        String type = prompt(s, "Type (Recon/Exploit/Persistence/C2/Mitigation) » ");
        String notes = prompt(s, "Intel Notes » ");

        Document op = new Document("codename", codename)
                .append("target_ip", ip)
                .append("type", type.isEmpty() ? "Recon" : type)
                .append("status", "PLANNED")
                .append("notes", notes)
                .append("created_at", new Date())
                .append("security_level", ThreadLocalRandom.current().nextInt(1, 6));

        dao.insert(op);
        delayPrint(GREEN + "Operation [" + BOLD + codename + RESET + GREEN + "] injected into the Grid." + RESET, 35);
    }

    private static void listOperations(OperationsDAO dao) {
        System.out.println(CYAN + BOLD + "\n[ LIVE OPERATIONS FEED ]" + RESET);
        long total = dao.countAll();
        if (total == 0) {
            System.out.println(YELLOW + "No active vectors in theater." + RESET);
            return;
        }

        dao.findActive().forEach(doc -> prettyPrintDoc(doc));
    }

    private static void generateIntelBriefing(OperationsDAO dao) {
        System.out.println(PURPLE + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                OBSIDIAN VAULT – INTEL BRIEFING               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);

        long total = dao.countAll();
        long compromised = dao.countByStatus("COMPROMISED");
        long active = dao.countByStatus("ACTIVE");

        System.out.printf(" %sTotal Vectors Tracked   :%s %,d%n", BOLD, RESET, total);
        System.out.printf(" %sSystems Compromised     :%s %,d %s(%.1f%%)%s%n", RED, RESET, compromised, RED,
                total == 0 ? 0 : (compromised * 100.0 / total), RESET);
        System.out.printf(" %sActive Campaigns        :%s %,d%n", BLUE, RESET, active);
        System.out.println();

        System.out.println(" " + YELLOW + "Status Distribution:" + RESET);
        dao.aggregateStats().forEach(doc -> {
            String status = doc.getString("_id") == null ? "UNKNOWN" : doc.getString("_id");
            int count = doc.getInteger("count");
            String bar = "█".repeat(Math.min(count, 50));
            String statusColor = switch (status) {
                case "COMPROMISED" -> RED;
                case "ACTIVE" -> BLUE;
                case "MITIGATED" -> GREEN;
                case "PLANNED" -> YELLOW;
                default -> GRAY;
            };
            System.out.printf("   %s%-12s%s | %s%s %,3d%s%n", statusColor, status, RESET, statusColor, bar, count, RESET);
        });

        System.out.println(PURPLE + "╔══════════════════════════════════════════════════════════════╗");
        System.out.println(PURPLE + "║                  END OF TRANSMISSION – STAY DARK              ║");
        System.out.println(PURPLE + "╚══════════════════════════════════════════════════════════════╝" + RESET);
    }

    private static void stealthMode() {
        delayPrint(RED + BOLD + "STEALTH MODE ACTIVATED – Console output suppressed." + RESET, 50);
        System.setOut(new java.io.PrintStream(new java.io.OutputStream() {
            public void write(int b) {}
        }));
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

    private static boolean isValidIPv4(String ip) {
        String[] parts = ip.split("\\.", -1);
        if (parts.length != 4) return false;
        try {
            for (String p : parts) {
                if (p.isEmpty() || p.length() > 3) return false;
                int n = Integer.parseInt(p);
                if (n < 0 || n > 255) return false;
                if (p.length() > 1 && p.startsWith("0")) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void printBanner() {
        System.out.println(RED + BOLD);
        String[] banner = {
                "   ██████╗ ██████╗ ███████╗██╗██████╗ ██╗ █████╗ ███╗   ██╗",
                "  ██╔═══██╗██╔══██╗██╔════╝██║██╔══██╗██║██╔══██╗████╗  ██║",
                "  ██║   ██║██████╔╝███████╗██║██║  ██║██║███████║██╔██╗ ██║",
                "  ██║   ██║██╔══██╗╚════██║██║██║  ██║██║██╔══██║██║╚██╗██║",
                "  ╚██████╔╝██████╔╝███████║██║██████╔╝██║██║  ██║██║ ╚████║",
                "   ╚═════╝ ╚═════╝ ╚══════╝╚═╝╚═════╝ ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝",
                "",
                "           V A U L T   v2.0",
                "     Elite Operations & Intelligence Grid",
                "         > Stay in the shadows."
        };
        for (String line : banner) {
            delayPrint("          " + line, 8);
        }
        System.out.println(RESET);
    }

    private static void printMenu() {
        System.out.println(BOLD + ORANGE + "╔═══════════════════ COMMAND MATRIX ═══════════════════╗" + RESET);
        System.out.println("  [1] Register New Operation       [5] Purge Vector");
        System.out.println("  [2] Live Operations Feed         [6] Search by IP");
        System.out.println("  [3] Update Status                [7] Stealth Mode");
        System.out.println("  [4] Intel Briefing               [0] Disconnect");
        System.out.println(BOLD + ORANGE + "╚══════════════════════════════════════════════════════╝" + RESET);
    }

    private static void prettyPrintDoc(Document doc) {
        String status = doc.getString("status");
        String color = switch (status) {
            case "PLANNED" -> YELLOW;
            case "ACTIVE" -> BLUE;
            case "COMPROMISED" -> RED;
            case "MITIGATED" -> GREEN;
            case "ARCHIVED" -> GRAY;
            default -> RESET;
        };

        String shortId = doc.getObjectId("_id").toHexString().substring(18);
        String time = SDF.format(doc.getDate("created_at"));

        System.out.printf("%s[%s%s%s] %s%s%s | %s | IP: %s%s%s | Lvl: %s%d%s%n",
                color, BOLD, status, RESET,
                BOLD, doc.getString("codename"), RESET,
                doc.getString("type"),
                ORANGE, doc.getString("target_ip"), RESET,
                PURPLE, doc.getInteger("security_level"), RESET
        );
        System.out.printf("   %sID: %s%s | Created: %s%s%n", DIM, shortId, RESET, GRAY, time, RESET);
        String notes = doc.getString("notes");
        if (notes != null && !notes.isEmpty()) {
            System.out.printf("   %s> %s%s%n", CYAN, notes, RESET);
        }
        System.out.println("   " + GRAY + "─".repeat(70) + RESET);
    }

    // ==================== DAO ====================
    static class OperationsDAO {
        private final MongoCollection<Document> col;

        OperationsDAO(MongoCollection<Document> col) { this.col = col; }

        void insert(Document doc) { col.insertOne(doc); }

        Iterable<Document> findActive() {
            return col.find(Filters.ne("status", "ARCHIVED"))
                    .sort(Sorts.descending("created_at"));
        }

        boolean existsByCodename(String codename) {
            return col.countDocuments(Filters.eq("codename", codename)) > 0;
        }

        void updateStatus(String codename, String status) {
            col.updateOne(Filters.eq("codename", codename), Updates.combine(
                    Updates.set("status", status),
                    Updates.set("updated_at", new Date())
            ));
        }

        boolean delete(String codename) {
            return col.deleteOne(Filters.eq("codename", codename)).getDeletedCount() > 0;
        }

        long countAll() { return col.countDocuments(); }
        long countByStatus(String status) { return col.countDocuments(Filters.eq("status", status)); }

        Iterable<Document> aggregateStats() {
            return col.aggregate(Arrays.asList(
                    Aggregates.group("$status", Accumulators.sum("count", 1)),
                    Aggregates.sort(Sorts.descending("count"))
            ));
        }

        void searchByIP(String ip, Consumer<Document> consumer) {
            col.find(Filters.eq("target_ip", ip))
                    .sort(Sorts.descending("created_at"))
                    .forEach(consumer);
        }
    }

    // Menu actions
    private static void updateStatus(Scanner s, OperationsDAO dao) {
        String codename = prompt(s, "Codename to update » ");
        Document doc = dao.col.find(Filters.eq("codename", codename)).first();
        if (doc == null) {
            delayPrint(RED + "Operation not found." + RESET, 20);
            return;
        }
        System.out.println("Current: " + doc.getString("status"));
        System.out.println("1. ACTIVE  2. COMPROMISED  3. MITIGATED  4. ARCHIVED");
        String choice = prompt(s, "New status » ");
        String newStatus = switch (choice) {
            case "1" -> "ACTIVE";
            case "2" -> "COMPROMISED";
            case "3" -> "MITIGATED";
            case "4" -> "ARCHIVED";
            default -> null;
        };
        if (newStatus != null) {
            dao.updateStatus(codename, newStatus);
            delayPrint(GREEN + "Status updated." + RESET, 20);
        }
    }

    private static void purgeOperation(Scanner s, OperationsDAO dao) {
        String codename = prompt(s, "Codename to PURGE » ");
        System.out.print(RED + "Confirm purge? (y/N): " + RESET);
        if (s.nextLine().trim().equalsIgnoreCase("y")) {
            if (dao.delete(codename)) delayPrint(RED + "Vector eliminated." + RESET, 30);
            else delayPrint(YELLOW + "Not found." + RESET, 20);
        }
    }

    private static void searchByVector(Scanner s, OperationsDAO dao) {
        String ip = prompt(s, "Target IP » ");
        delayPrint(CYAN + "Scanning..." + RESET, 20);
        final long[] count = {0};
        dao.searchByIP(ip, doc -> {
            prettyPrintDoc(doc);
            count[0]++;
        });
        delayPrint(count[0] == 0 ? YELLOW + "No matches." + RESET : GREEN + "Found " + count[0] + " records." + RESET, 20);
    }
}