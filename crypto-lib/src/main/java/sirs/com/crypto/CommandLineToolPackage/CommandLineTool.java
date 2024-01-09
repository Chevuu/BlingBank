package main.java.sirs.com.crypto.CommandLineToolPackage;



import org.apache.commons.cli.*;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;

public class CommandLineTool {
    public static void main(String[] args) {
        Options options = buildOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("help")) {
                printHelp(options);
            } else if (cmd.hasOption("protect")) {
                handleProtect(cmd);
            } else if (cmd.hasOption("check")) {
                handleCheck(cmd);
            } else if (cmd.hasOption("unprotect")) {
                handleUnprotect(cmd);
            } else {
                throw new ParseException("Invalid command");
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp(options);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Options buildOptions() {
        Options options = new Options();

        options.addOption(Option.builder()
                .longOpt("help")
                .desc("Display help")
                .build());

        options.addOption(Option.builder()
                .longOpt("protect")
                .hasArgs()
                .numberOfArgs(2)
                .desc("Protect a document")
                .build());

        options.addOption(Option.builder()
                .longOpt("check")
                .hasArg()
                .desc("Check if the document's integrity is intact")
                .build());

        options.addOption(Option.builder()
                .longOpt("unprotect")
                .hasArgs()
                .numberOfArgs(2)
                .desc("Unprotect a document")
                .build());

        return options;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("tool-name", options);
    }

    private static void handleProtect(CommandLine cmd) throws Exception {
        String unprotectedDocumentPath = cmd.getOptionValues("protect")[0];
        String protectedDocumentPath = cmd.getOptionValues("protect")[1];
        CryptographicLibrary.encryptDocument(unprotectedDocumentPath, protectedDocumentPath);
        System.out.println("Successfully protected " + unprotectedDocumentPath);
    }

    private static void handleCheck(CommandLine cmd) throws Exception {
        String protectedDocumentPath = cmd.getOptionValue("check");
        CryptographicLibrary.checkDocumentIntegrityAndFreshness(protectedDocumentPath);
    }

    private static void handleUnprotect(CommandLine cmd) throws Exception {
        String protectedDocumentPath = cmd.getOptionValues("unprotect")[0];
        String unprotectedDocumentPath = cmd.getOptionValues("unprotect")[1];
        CryptographicLibrary.decryptDocument(protectedDocumentPath, unprotectedDocumentPath);
        System.out.println("Unprotecting " + protectedDocumentPath + " to " + unprotectedDocumentPath);
    }
}
