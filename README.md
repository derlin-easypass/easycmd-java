[![Travis](https://travis-ci.org/derlin-easypass/easycmd-java.svg?branch=master)](https://travis-ci.org/derlin-easypass/easycmd-java)
[![release](http://github-release-version.herokuapp.com/github/derlin-easypass/easycmd-java/release.svg?style=flat)](https://github.com/derlin-easypass/easycmd-java/releases/latest)
![Made in Switzerland](https://img.shields.io/badge/Made%20with%20%E2%99%A5%20in-Switzerland-red.svg)

# easycmd-java
Easypass command line interface written in Java 8 (early 2017).

## Introduction

Easycmd is a simple program to keep your password in one or more encrypted json file(s) (called session). A CLI let's you add, remove, edit, view and copy accounts information. In case anything goes wrong with the application, you can always recover your data by decrypting the session file using the openssl commandline.

### Security

the EasyPass family uses OpenSSL to encrypt and decrypt json files. The encryption phase corresponds roughly to the following openssl command:
  
    openssl enc -aes-128-cbc -salt -base64 -in file.json -out file.json.enc 

Using the `-salt` option means that the password always generates a different encryption key, which helps prevent dictionary attacks. 

> AES is what the United States government uses to encrypt information at the Top Secret level (they now use the 256bit variant, which will come soon to EasyPass) 

The decryption phase is similar to:

    openssl enc -aes-128-cbc -d -a -in file.json.enc -out file.json 

### Advantages

Advantages of EasyPass

* you only need to remember one master password.
* if anything goes wrong (i.e. the app stops working), you can always recover your data using the `openssl` commandline tool
* syncing your passwords is easy: simply store your session file(s) to your favorite cloud solution, like Dropbox or Google Drive.
* backups are easy: just make a copy of the session file.
* no install required: just download the .jar.
* cross-platform: easycmd-java is compatible with any platform having ANSI terminal support. The EasyPass family also features an Android app and a GUI app (java swing).
* this is free software: you can download the source code and make your own app if you want.


## Command line arguments

    usage: easycmd
     -d,--decrypt <arg>   decrypt the file given by -f into the file <arg> and stop 
     -e,--encrypt <arg>   encrypt the file given by -f into the file <arg> and stop
     -f,--file <arg>      the session file.
     -h,--help            print this message.
     -nocolor             turn off the coloring in prompts.
     -p,--pass <arg>      the password (unsafe: added to history).


## Interpreter commands

In the command description, the `<acc. hint>` parameter denotes an account hint. It can either a number (see "finding accounts") or a pattern matching _only one_ account.

__finding accounts__

The following commands lookup for accounts and display a list of account names. Each item of the result list is prefixed with an integer that you can use later as an account hint. Those numbers are valid until a new search command is executed.

 * `find`: display all the account names in the current session
 * `find <search(s)>`: display all the account names having at least one field containing the search string

__manipulating accounts__: 

 * `add|new [account name]` : add a a new account. The file is then automatically updated.
 * `delete <acc. hint>`: delete the matching account.
 * `modify <acc. hint>`: edit the properties of the matching account.
 * `show <acc. hint>`: display the details of the account (but not the password).
 * `showpass <acc. hint>` : display the password (in clear text !!) in the terminal (this supposes a readline _GNU_ support). Press any key to hide it again.

__copying properties__: 

 * `copy <property> <acc. hint>`: copy some property (pseudo, name, email, pass, notes) of an account to the clipboard.
 * `pass <acc. hint>`: copy the password of the account in the clipboard. This is an alias of `copy pass`.

__global commands__:

 * `help`: print the list of available commands.
 * `help <cmd>`: print the specified command usage.
 * `man`: print the list of commands and their usage.
 * `exit`: quit the program.
 
__files__:

* `load <file>`: load a json file from the filesystem and encrypt it.
* `dump <file>`: save the content of the given session into a json file (non encrypted).
* `pass <search | index>`: copy the password of the given account to the clipboard. Same as copy pass.

# Troubleshooting

__copy does not work (HeadlessException)__

In Java, accessing the system clipboard requires the AWT toolkit, which in turn needs a display. If you are in a headless environment, the clipboard won't be available. 

To make it work in a unix environment through SSH: 

 - ensure you have __xauth__ installed: `yum install xauth` or `apt install xauth`
 - ensure you have the line `X11Forwarding: yes` uncommented in `/etc/ssh/sshd_config`
 - use the -X option when connecting through ssh: `ssh -X ...`
 
 
 NOTE: a sample program to decrypt encoded files in golang is available in the playground: https://play.golang.org/p/mWEL9TdfJV
