package ch.derlin.easycmd.accounts;

import ch.derlin.easycmd.SerialisationManager;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * date: 16.02.17
 *
 * @author Lin
 */
public class Accounts extends ArrayList<Account> {

    // algo for the deserialisation of data
    private static final String CRYPTO_ALGORITHM = "aes-128-cbc";

    // ------------------------------------- constructors
    public Accounts() {
        super();
    }

    public Accounts(List<Account> accounts) {
        super(accounts);
    }

    // ------------------------------------- search

    public List<Account> find(String pattern) {
        Pattern p = Pattern.compile(pattern);
        return stream().filter(a -> a.matches(p)).collect(Collectors.toList());
    }

    public List<Account> findName(String pattern) {
        Pattern p = Pattern.compile(pattern);
        return stream().filter(a -> p.matcher(a.name).matches()).collect(Collectors.toList());
    }

    public List<Account> findNote(String pattern) {
        Pattern p = Pattern.compile(pattern);
        return stream().filter(a -> p.matcher(a.notes).matches()).collect(Collectors.toList());
    }


    public List<Account> findPseudo(String pattern) {
        Pattern p = Pattern.compile(pattern);
        return stream().filter(a -> p.matcher(a.pseudo).matches()).collect(Collectors.toList());
    }

    public List<Account> findPass(String pattern) {
        Pattern p = Pattern.compile(pattern);
        return stream().filter(a -> p.matcher(a.pseudo).matches()).collect(Collectors.toList());
    }

    /* *****************************************************************
     * static utils
     * ****************************************************************/

    public static Accounts fromFile(String filepath, String password) throws IOException, SerialisationManager.WrongCredentialsException {
        List<Account> deserialized = (List<Account>) SerialisationManager.deserialize(CRYPTO_ALGORITHM, filepath, password,
                new TypeToken<List<Account>>() {
                }.getType());
        return new Accounts(deserialized);

    }


    public static void toFile(String filepath, String password, Accounts accounts) throws IOException {
        SerialisationManager.serialize(accounts, CRYPTO_ALGORITHM, filepath, password);
    }
}
