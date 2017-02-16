package ch.derlin.easycmd.accounts;

import ch.derlin.easycmd.Console;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * User: lucy
 * Date: 14/09/13
 * Version: 0.1
 */
public class Account {
    public String name = "", pseudo = "", password = "", notes = "";
    @SerializedName("creation date")
    public String creationDate;
    @SerializedName("modification date")
    public String modificationDate;

    public boolean matches(Pattern pattern) {
        return pattern.matcher(name).matches() ||
                pattern.matcher(pseudo).matches() ||
                pattern.matcher(notes).matches();
    }

    public boolean contains(String... patterns) {
        for (String pattern : patterns) {
            pattern = pattern.toLowerCase();
            boolean match = name.toLowerCase().contains(pattern) ||
                    pseudo.toLowerCase().contains(pattern) ||
                    notes.toLowerCase().contains(pattern);
            if(!match) return false;
        }//end for
        return true;
    }

    public String get(String field) {
        field = field.toLowerCase();
        if(field.equals("name")) return name;
        if(field.equals("pseudo")) return pseudo;
        if(field.startsWith("pass")) return password;
        if(field.startsWith("note")) return notes;
        return null;
    }


    public void show() {
        System.out.println("   name>" + name);
        System.out.println("  pseudo>" + pseudo);
        System.out.println("  notes>" + notes);
    }

    public boolean edit(Console console) throws IOException {
        Account newAccount = new Account();
        System.out.println();
        newAccount.name = console.readWithDefault("   name>", name).trim();
        newAccount.pseudo = console.readWithDefault("   pseudo>", pseudo).trim();
        newAccount.password = console.readPassword("   password>", password).trim();
        if (newAccount.password == null || newAccount.password.isEmpty())
            newAccount.password = this.password;
        newAccount.notes = console.readWithDefault("   note>", notes).trim();

        console.println();

        if (this.equals(newAccount)) {
            console.println("nothing to save.");
            return false;
        }
        if (console.confirm("Save changes ?")) {
            this.name = newAccount.name;
            this.pseudo = newAccount.pseudo;
            this.password = newAccount.password;
            this.notes = newAccount.notes;
            String now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            if (creationDate == null || creationDate.isEmpty()) creationDate = now;
            this.modificationDate = now;
        }
        console.println();
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return name.hashCode() + pseudo.hashCode() + password.hashCode() + notes.hashCode();
    }

    @Override
    public String toString() {
        return String.format("{name=%s, pseudo=%s, pass=%s, created=%s, modified=%s}",
                name, pseudo, password, creationDate, modificationDate);
    }


}//end class
