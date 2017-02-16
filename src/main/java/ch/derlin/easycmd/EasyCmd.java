package ch.derlin.easycmd;

import ch.derlin.easycmd.accounts.Account;
import ch.derlin.easycmd.accounts.AccountsMap;
import ch.derlin.easycmd.console.Console;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

        console = new Console();
        filepath = args[0];

        if (!new File(filepath).exists()) {
            console.warn("the file '%s' does not exist: " + filepath);
            if (!console.confirm("continue ?")) {
                System.exit(0);
            }
        }

        pass = "essai"; // console.readPassword("password>", "");
        accounts = AccountsMap.fromFile(filepath, pass);
        results = accounts.keys();

        commandMap = new TreeMap<>();
        commandMap.put("find", this::findAll);
        commandMap.put("show", this::show);
        commandMap.put("copy", this::copy);
        commandMap.put("edit", this::edit);
        commandMap.put("new", this::newAccount);
        commandMap.put("add", this::newAccount);

        commandMap.put("exit", (c, a) -> System.exit(1));

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
            commandMap.get("find").apply("find", split);
        }

    }

    public void findAll(String cmd, String[] args) {
        if (args.length < 1) {
            results = accounts.keys();
        } else {
            results = accounts.find(args);
        }
        printResults();
    }

    public void show(String cmd, String[] args) {
        Account a = findOne(args);
        if (a != null) a.show(console);
    }

    public void copy(String cmd, String[] args) {
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

    public void edit(String cmd, String[] args) {
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

    public void newAccount(String cmd, String[] args) {
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

    public void save() {
        try {
            accounts.save(accounts, filepath, pass);
            console.info("saved.");
        } catch (IOException e) {
            console.error("error saving file.");
        }
    }

    /* *****************************************************************
     * private utils
     * ****************************************************************/

    private void printResults() {
        int i = 0;
        for (String name : results) {
            System.out.printf(" [%d] %s%n", i++, name);
        }//end for
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
