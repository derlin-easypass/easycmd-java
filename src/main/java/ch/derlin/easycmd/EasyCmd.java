package ch.derlin.easycmd;

import ch.derlin.easycmd.accounts.Account;
import ch.derlin.easycmd.accounts.AccountsMap;
import ch.derlin.easycmd.console.Console;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * date: 15.02.17
 *
 * @author Lin
 */
public class EasyCmd {

    // algo for the deserialisation of data
    private static final String CRYPTO_ALGORITHM = "aes-128-cbc";

    private AccountsMap accounts;
    private List<String> results;
    private Console console;

    private String filepath;
    private String pass;

    @FunctionalInterface
    interface Command {
        void apply(String command, String[] args);
    }

    private Map<String, Command> commandMap;

    public static void main(String[] args) throws Exception {
        new EasyCmd(args);
    }//end main

    public EasyCmd(String[] args) throws Exception {


        if (args.length < 1) {
            System.out.println("missing a filepath argument.");
            System.exit(1);
        }

        Options options = new Options();

        options.addOption("f", "file", true, "the session file");
        options.addOption("p", "pass", true, "the password (unsafe: added to history)");
        options.addOption("nocolor", "turn off the coloring in prompts");
        options.addOption("e", "encrypt", true, "encrypt the file given by -f and stop.");
        options.addOption("d", "decrypt", true, "decrypt the file given by -f and stop.");

        // parse the command line arguments
        CommandLine line = new DefaultParser().parse(options, args);
        for (Option option : line.getOptions()) {
            System.out.println(option);
            System.out.printf("%s %s %s%n", option.getArgs(), option.getId(), option.getValue());
        }//end for
        // validate that block-size has been set
        if (!line.hasOption("file")) {
            // print the value of block-size
            System.out.println("missing file argument (-f <file>)");
            System.exit(0);
        }


        console = new Console(line.hasOption("nocolor"));
        filepath = line.getOptionValue("file");
        pass = line.getOptionValue("pass", "");

        boolean fileExists = new File(filepath).exists();
        if (fileExists) {
            if (line.hasOption("encrypt")) {
                // encrypt and quit
                String encryptPath = line.getOptionValue("encrypt");
                loadFromFile("encrypt", encryptPath);
                System.exit(accounts == null ? 1 : 0);
            } else {
                // decrypt file
                try {
                    while (pass.isEmpty()) pass = console.readPassword("password> ", "");
                    accounts = AccountsMap.fromEncryptedFile(filepath, pass);
                } catch (SerialisationManager.WrongCredentialsException e) {
                    System.out.println("Error: wrong credentials");
                    System.exit(0);
                }
            }

        } else {
            // ensure the user wants to create a new file
            console.warn("the file '%s' does not exist: ", filepath);
            if (!console.confirm("continue ?")) {
                System.exit(0);
            }

            // get a new password (confirm to avoid typing errors,
            // since it is not recoverable)
            if (pass.isEmpty()) {
                console.println("Choose a password. Ensure it is a strong one and don't forget it, it is not recoverable.");
                pass = getNewPass();
            }
            // creat empty
            accounts = new AccountsMap();
        }

        // decrypt and quit
        if (line.hasOption("decrypt")) {
            dumpToFile("dump", line.getOptionValues("decrypt"));
            System.exit(0);
        }

        results = accounts.keys();

        commandMap = new TreeMap<>();
        commandMap.put("find", this::findAll);
        commandMap.put("show", this::show);
        commandMap.put("copy", this::copy);
        commandMap.put("edit", this::edit);
        commandMap.put("new", this::newAccount);
        commandMap.put("add", this::newAccount);

        commandMap.put("load", this::loadFromFile);
        commandMap.put("dump", this::dumpToFile);

        commandMap.put("exit", (c, a) -> System.exit(1));

        // shortcuts
        commandMap.put("pass", (c, s) -> {
            ArrayList<String> list = new ArrayList<>(Arrays.asList(s));
            list.add(0, c);
            copy("copy", s);
        });

        interpreter();

    }//end main


    public void interpreter() throws IOException {
        while (true) {
            // history enabled only for commands
            console.setHistoryEnabled(true);
            String line = console.readLine();
            console.setHistoryEnabled(false);
            if (!line.isEmpty()) {
                doCommand(line.split(" +"));
                System.out.println();
            }
        }

    }

    public void doCommand(String[] split) {
        String cmd = split[0].toLowerCase();
        String[] args = Arrays.copyOfRange(split, 1, split.length);
        if (commandMap.containsKey(cmd)) {
            commandMap.get(cmd).apply(cmd, args);
        } else {
            console.info("unrecognized command. Assuming find.");
            commandMap.get("find").apply("find", split);
        }

    }

    public void findAll(String cmd, String... args) {
        if (args.length < 1) {
            results = accounts.keys();
        } else {
            results = accounts.find(args);
        }
        printResults();
    }

    public void show(String cmd, String... args) {
        Account a = findOne(args);
        if (a != null) a.show(console);
    }

    public void copy(String cmd, String... args) {
        if (args.length < 1) {
            console.error("incomplete command.");
            return;
        }

        Account a = findOne(Arrays.copyOfRange(args, 1, args.length));
        if (a != null) {
            String fieldname = args[0];
            String field = a.get(fieldname);
            if (field == null) {
                console.error("invalid field " + args[0]);
            } else if (field.isEmpty()) {
                console.warn("nothing to copy (empty field)");
            } else {
                copy(field);
                console.info("%s for account '%s' copied to clipboard%n", fieldname, a.name);
            }
        }
    }

    public void edit(String cmd, String... args) {
        Account a = findOne(args);
        if (a == null) return;

        try {
            if (a.edit(console)) {
                save();
            }
        } catch (IOException e) {
            console.error("error editing account.");
        }
    }

    public void newAccount(String cmd, String... args) {
        Account a = new Account();

        try {
            if (a.edit(console)) {
                accounts.put(a.name, a);
                save();
            }
        } catch (IOException e) {
            console.error("error saving account.");
        }

    }


    public void loadFromFile(String cmd, String... arg) {
        if (arg.length == 0) {
            console.error("missing file. Usage: %s <filepath:string>", cmd);
            return;
        }
        String to = arg[0];
        if (!canWriteTo(to)) return;
        try {
            accounts = AccountsMap.fromFile(filepath);
            filepath = to;
            if (pass.isEmpty()) pass = getNewPass();
            save();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Error loading json file '%s'%n", filepath);
        }
    }

    public void dumpToFile(String cmd, String[] arg) {
        try {
            if (arg.length == 0) {
                console.error("missing file. Usage: %s <filepath:string> [noIndent:boolean]", cmd);
                return;
            }

            if (accounts.size() == 0) {
                console.warn(" Nothing to dump; empty accounts list.");
                return;
            }

            boolean indent = true;
            if (arg.length > 1) {
                indent = !Boolean.parseBoolean(arg[1]);
            }

            String to = arg[0];
            if (!canWriteTo(to)) return;

            AccountsMap.toFile(to, accounts, indent);
            console.info("saved to '%s'", to);

        } catch (IOException e) {
            console.error(e.getMessage());
        }
    }

    private void save() {
        try {
            accounts.save(filepath, pass);
            console.info("saved.");
        } catch (IOException e) {
            console.error("error saving file.");
        }
    }

    /* *****************************************************************
     * private utils
     * ****************************************************************/

    private boolean canWriteTo(String filepath) {
        File f = new File(filepath);
        if (f.exists()) {
            // if not a file, abort
            if (!f.isFile()) {
                console.error("'%s' is not a regular file.", filepath);
                return false;
            }

            // else, ask for overwrite confirmation
            try {
                return console.confirm(String.format("'%s' already exists, overwrite ?", f.getAbsolutePath()));
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    private String getNewPass() throws IOException {
        String pass1, pass2;
        while (true) {
            // @formatter:off
                    do{ pass1 = console.readPassword("password> ", ""); } while (pass1.isEmpty());
                    do{ pass2 = console.readPassword("confirm> ", ""); } while (pass2.isEmpty()) ;
                    // @formatter:on
            if (pass1.equals(pass2)) break;
            console.error("passwords do  not match%n");
        }
        return pass1;
    }

    private void printResults() {
        int i = 0;
        for (String name : results) {
            System.out.printf("  [%d] %s%n", i++, name);
        }//end for
        System.out.printf(" %d results.%n", i);
    }

    private void copy(String s) {
        StringSelection selection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    private Account findOne(String... args) {

        // no argument: ok only if results has only one account
        if (args.length == 0) {
            if (results.size() > 1) {
                console.error("missing index");
                return null;
            } else {
                return accounts.get(results.get(0));
            }
        }

        // check for integer/index argument
        try {
            int i = Integer.parseInt(args[0]);
            if (i >= 0 && i < results.size()) {
                return accounts.get(results.get(i));
            } else {
                console.error("argument not in range 0:" + results.size());
                return null;
            }
        } catch (NumberFormatException e) {
        }

        // finally, check if the arguments match only one account
        List<String> res = accounts.find(args);
        if (res.size() == 1) {
            results = res;
            return accounts.get(results.get(0));
        } else {
            console.error("ambiguous account.");
            return null;
        }
    }
}
