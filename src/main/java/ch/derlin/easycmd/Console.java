package ch.derlin.easycmd;

import jline.console.ConsoleReader;

import java.io.IOException;

import static sun.misc.Version.println;


/**
 * date: 15.02.17
 *
 * @author Lin
 */
public class Console extends ConsoleReader {
    public static final String DEFAULT_PROMPT = ">";
    // use: readLine("password>", PASSWORD_MASK);
    public static final Character PASSWORD_MASK = new Character('*');
    private static final String PASSWORD_REPLACE = "********************************************************************";
    private String prompt;

    public Console() throws Exception {
        this(DEFAULT_PROMPT);
    }

    public Console(String prompt) throws Exception {
        super();
        this.prompt = prompt;
        super.setPrompt(prompt);
    }

    public String readPassword(String prompt, String preload) throws IOException {
        if (preload != null && !preload.isEmpty())
            preload = PASSWORD_REPLACE.substring(0, preload.length());
        return readLine(prompt, PASSWORD_MASK, preload);
    }

    public String readWithDefault(String prompt, String preload) throws IOException {
        //resetLine();
        //resetPromptLine(prompt, preload, preload.length());
        return readLine(prompt, null, preload);
    }


    public boolean confirm(String text) throws IOException {
        putString(text + "  [y|N]> ");
        flush();
        boolean ok = readCharacter() == 'y';
        println();
        return ok;
    }

    public boolean clearScreen() throws IOException {
        boolean ok = super.clearScreen();
        flush();
        return ok;
    }
}
