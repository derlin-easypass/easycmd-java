package ch.derlin.easycmd;

import ch.derlin.easycmd.accounts.Account;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * date: 15.02.17
 *
 * @author Lin
 */
public class EasyCmd {

    // algo for the deserialisation of data
    private static final String CRYPTO_ALGORITHM = "aes-128-cbc";

    public static void main(String[] args) throws Exception {
        Console c = new Console();
        Account a = new Account();
        a.name = "aléksdfj";
        a.notes = "alksdjf";
        a.password = "aaa";
        a.edit(c);
        System.out.println(a);
    }//end main
    public static void main_(String[] args) throws Exception {
        SerialisationManager sm = new SerialisationManager();
        List<Account> accounts = (List<Account>) sm.deserialize( CRYPTO_ALGORITHM,
                "/tmp/essai2.data_ser", "essai", new TypeToken<List<Account>>(){}.getType());

        System.out.println(accounts);
//        Account a = new Account();
//        a.name = "daplab";
//        a.pseudo = "er45tz";
//        a.password = "aaaaa";
//        accounts.add(a);
//        sm.serialize(accounts, CRYPTO_ALGORITHM, "/tmp/essai2.data_ser", "essai");

    }//end main



}
