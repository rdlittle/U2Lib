/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.util;

import com.webfront.u2.model.Account;
import com.webfront.u2.model.Profile;
import com.webfront.u2.model.Program;
import com.webfront.u2.model.Prompt;
import com.webfront.u2.model.Server;
import com.webfront.u2.model.User;
import com.webfront.u2.model.UvFile;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 * @author rlittle
 */
public class Config {

    private static Config instance = null;
    private Point location;
    private Dimension size;
    private boolean newInstall;
    private boolean hasDb;
    private static Connection connection = null;
    private final Point defaultWindowLoc = new Point(0, 0);
    private final Dimension defaultWindowSize = new Dimension(525, 425);

    private ObservableList<Account> accounts;
    private ObservableList<Profile> profiles;
    private ObservableList<Server> servers;
    private ObservableList<User> users;
    private ObservableList<Program> programs;
    private ObservableMap<String, String> preferences;
    public static String configPath;

    protected Config() {
        location = new Point();
        size = new Dimension();
        accounts = FXCollections.<Account>observableArrayList();
        profiles = FXCollections.<Profile>observableArrayList();
        servers = FXCollections.<Server>observableArrayList();
        users = FXCollections.<User>observableArrayList();
        programs = FXCollections.<Program>observableArrayList();
        preferences = FXCollections.<String, String>observableHashMap();

        hasDb = false;
        String homeDir = System.getProperty("user.home");
        String fileSep = System.getProperty("file.separator");
        File userDir = new File(homeDir);
        File dotFile = new File(homeDir + fileSep + ".uvtool");
        File uvToolDir = new File(userDir + fileSep + "UvTool");
        if (!uvToolDir.exists()) {
            uvToolDir.mkdir();
        }
        String uvToolDb = uvToolDir.getAbsolutePath() + fileSep + "uvtool.db";
        String fileList[] = uvToolDir.list();
//        configPath = uvToolDir.getAbsolutePath() + fileSep + "config.json";
        configPath = uvToolDir.getAbsolutePath() + fileSep + "network_topography.json";
        if (fileList.length > 0) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].equals("uvtool.db")) {
                    hasDb = true;
                    break;
                }
            }
        }

        newInstall = !dotFile.exists();

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + uvToolDb);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (newInstall || !hasDb) {
            try {
                location = defaultWindowLoc;
                size = defaultWindowSize;
                if (newInstall) {
                    try (FileWriter writer = new FileWriter(dotFile)) {
                        writer.write(uvToolDir.getAbsolutePath());
                        writer.flush();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            initDb();
            setConfig();
            getConfig();
        }
    }

    public void initDb() {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.
            statement.executeUpdate("create table if not exists accounts (id integer primary key autoincrement, server char(16), name char(128), path char(256), user integer)");
            statement.executeUpdate("create table if not exists profiles (id integer primary key autoincrement, name char(128), server char(16), account int, user int)");
            statement.executeUpdate("create table if not exists servers (name char(16), host char(128), url char(256))");
            statement.executeUpdate("create table if not exists settings (key char(6) not null, x int, y int, w int, h int)");
            statement.executeUpdate("create table if not exists users (id integer primary key autoincrement, name char(16), password char(256))");
            statement.executeUpdate("create table if not exists apps (id integer primary key autoincrement, name char(128), package char(256), description text, subroutine tinyint default 0, appid integer)");
            statement.executeUpdate("create table if not exists files (id integer primary key autoincrement, name char(128), read tinyint, write tinyint)");
            statement.executeUpdate("create table if not exists preferences (key varchar(128) not null, value varchar(256))");
            statement.executeUpdate("create table if not exists prompts (id integer primary key autoincrement, appid integer, number integer, message char(256), required tinyint default 0)");
            hasDb = true;
        } catch (SQLException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isNewInstall() {
        return newInstall;
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private void getConfig() {
        Statement statement;
        String sql;

        try {
            statement = connection.createStatement();

            sql = "select * from accounts";
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String sname = rs.getString("server");
                String name = rs.getString("name");
                String path = rs.getString("path");
                accounts.add(new Account(id, sname, name, path));
            }

            // Load profiles if any
            sql = "select p.id as id, p.name as profileName, p.server as serverName, p.account as acctId, p.user as userId, ";
            sql += "u.name as userName, u.password as password, ";
            sql += "a.name as acctName, a.path as acctPath, ";
            sql += "s.host as hostName ";
            sql += "from profiles p ";
            sql += "join users u on u.id = p.user ";
            sql += "join accounts a on a.id = p.account ";
            sql += "join servers s on s.name = p.server";
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                int pid = rs.getInt("id");
                String profileName = rs.getString("profileName");
                String serverName = rs.getString("serverName");
                int acctId = rs.getInt("acctId");
                int userId = rs.getInt("userId");
                String userName = rs.getString("userName");
                String password = rs.getString("password");
                String acctName = rs.getString("acctName");
                String acctPath = rs.getString("acctPath");
                String hostName = rs.getString("hostName");

                Profile p = new Profile();
                p.setProfileName(profileName);
                Account acct = new Account();
                Server serv = new Server();
                User user = new User();

                user.setId(userId);
                user.setName(userName);
                user.setPassword(password);

                acct.setId(acctId);
                acct.setName(acctName);
                acct.setPath(acctPath);
                acct.setServerName(serverName);

                serv.setName(serverName);
                serv.setHost(hostName);

                p.setId(pid);
                p.setAccount(acct);
                p.setUser(user);
                p.setServer(serv);

                profiles.add(p);
            }

            // Load servers
            sql = "select * from servers";
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                String name = rs.getString("name");
                String host = rs.getString("host");
                String url = rs.getString("url");
                servers.add(new Server(name, host));
            }

            // Load all programs, files and prompts
            sql = "SELECT ";
            sql += "a.id as 'a.id', a.name as 'a.name', a.package as 'a.package', a.description as 'a.description', a.subroutine as 'a.subroutine',";
            sql += "f.id as 'f.id', f.name as 'f.name', f.read as 'f.read', f.write as 'f.write', ";
            sql += "p.id as 'p.id', p.number as 'p.number', p.message as 'p.message', p.required as 'p.required'";
            sql += "from apps a left join files f on f.appid = a.id ";
            sql += "left outer join prompts p on p.appid = a.id ";
            sql += "order by a.name, f.name, p.number";
            rs = statement.executeQuery(sql);
            int lastId = -1;
            Program p = null;
            while (rs.next()) {
                int appId = rs.getInt("a.id");
                String appName = rs.getString("a.name");
                String appPackage = rs.getString("a.package");
                String appDescription = rs.getString("a.description");
                boolean appSubroutine = rs.getInt("a.subroutine") == 1;
                int fileId = rs.getInt("f.id");
                String fileName = rs.getString("f.name");
                boolean fileRead = rs.getInt("f.read") == 1;
                boolean fileWrite = rs.getInt("f.write") == 1;
                int promptId = rs.getInt("p.id");
                int promptNum = rs.getInt("p.number");
                String promptMessage = rs.getString("p.message");
                boolean promptRequired = rs.getInt("p.required") == 1;

                if (appId != lastId) {
                    if (lastId != -1) {
                        p.getPromptList().setAll(p.getPrompts().values());
                        programs.add(p);
                    }
                    lastId = appId;
                    p = new Program();
                    p.setId(appId);
                    p.setName(appName);
                    p.setClassName(appPackage);
                    p.setDescription(appDescription);
                    p.setSubroutine(appSubroutine);
                }

                if (fileId > 0) {
                    UvFile uvf = new UvFile(fileId, fileName, fileRead, fileWrite);
                    if (!p.getFileList().contains(uvf)) {
                        p.getFileList().add(uvf);
                    }
                }

                if (promptId > 0) {
                    Prompt prompt = new Prompt();
                    prompt.setId(promptId);
                    prompt.setNum(promptNum);
                    prompt.setMessage(promptMessage);
                    prompt.setRequired(promptRequired);
                    if (!prompt.getMessage().isEmpty()) {
                        if (!p.getPrompts().containsKey(promptId)) {
                            p.getPrompts().put(prompt.getNum(), prompt);
                        }
                    }
                }
            }
            if (p != null) {
                p.getPromptList().setAll(p.getPrompts().values());
                programs.add(p);
            }

            // Load program settings
            sql = "select * from settings where key = \"window\"";
            rs = statement.executeQuery(sql);
            location.x = rs.getInt("x");
            location.y = rs.getInt("y");
            size.width = rs.getInt("w");
            size.height = rs.getInt("h");

            // Load users
            sql = "select * from users";
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String password = rs.getString("password");
                users.add(new User(id, name, password));
            }

            // Load preferences
            sql = "select * from preferences";
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                String key = rs.getString("key");
                String value = rs.getString("value");
                preferences.put(key, value);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public final void setConfig() {
        try {
            Statement statement = connection.createStatement();
            String sql;
            if (this.newInstall || !this.hasDb) {
                sql = "insert into settings values (";
                sql += "\"window\",";
                sql += location.x + ",";
                sql += location.y + ",";
                sql += defaultWindowSize.width + ",";
                sql += defaultWindowSize.height + ")";
                statement.executeUpdate(sql);
            } else {
                if (location.x > 0 && location.y > 0) {
                    sql = "update settings set";
                    sql += " x = " + location.x;
                    sql += ",y = " + location.y;
                    sql += ",w = " + size.width;
                    sql += ",h = " + size.height;
                    sql += " where key = \"window\"";
                    statement.executeUpdate(sql);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addAccount(Account a) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            sql = "insert into accounts values (";
            sql += "null,";
            sql += "\"" + a.getServerName() + "\",";
            sql += "\"" + a.getName() + "\",";
            sql += "\"" + a.getPath() + "\",0)";
            statement.executeUpdate(sql);
            ResultSet rs = statement.executeQuery("SELECT id from accounts order by id desc limit 0,1");
            int id = rs.getInt("id");
            a.setId(id);
            accounts.add(a);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addProfile(Profile p) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            sql = "insert into profiles (name,server,account,user) values (";
            sql += "\"" + p.getProfileName() + "\",";
            sql += "\"" + p.getServerName() + "\",";
            sql += p.getAccountId() + ",";
            sql += p.getUserId() + ")";
            statement.executeUpdate(sql);
            ResultSet rs = statement.executeQuery("SELECT id from accounts order by id desc limit 0,1");
            int id = rs.getInt("id");
            p.setId(id);
            profiles.add(p);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int addProgram(Program p) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            int isSub = p.isSubroutine() ? 1 : 0;
            sql = "insert into apps (name, package, description, subroutine) values (";
            sql += '"' + p.getName() + '"' + ',' + '"' + p.getClassName() + '"' + "," + '"' + p.getDescription() + '"' + "," + isSub + ")";
            statement.executeUpdate(sql);
            ResultSet rs = statement.executeQuery("SELECT id from apps order by id desc limit 0,1");
            int id = rs.getInt("id");
            p.setId(id);
            programs.add(p);
            return id;
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    public void addPrompts(Program p) {
        try {
            Statement statement = connection.createStatement();
            String insert = "insert into prompts (appid, number, message) ";
            insert += "values ";
            String values = "";
            Iterator<Prompt> iterator = p.getPrompts().values().iterator();
            while (iterator.hasNext()) {
                Prompt prp = iterator.next();
                values = "(" + p.getId() + "," + prp.getNum() + ",\"" + prp.getMessage() + "\")";
                String sql = insert + values;
                statement.execute(sql);
            }
            statement.close();
        } catch (SQLException ex) {

        }
    }

    public void addFiles(ArrayList<UvFile> list) {
        try {
            Statement statement = connection.createStatement();
            String insert = "insert into files (name, appid, read, write)";
            for (UvFile uvf : list) {
                String sql = insert + " values (";
                sql += "\"" + uvf.getFileName() + "\",";
                sql += uvf.getAppId() + ",";
                sql += uvf.getRead() + ",";
                sql += uvf.getWrite() + ")";
                statement.executeUpdate(sql);
            }
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addPreference(String key, String value) {
        try {
            Statement statement = connection.createStatement();
            String insert = "insert into preferences (key, value) values ";
            insert += "(\"" + key + "\", \"" + value + "\") ";
            statement.executeUpdate(insert);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addServer(Server s) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            sql = "insert into servers (name,host) values (";
            sql += "\"" + s.getName() + "\",";
            sql += "\"" + s.getHost() + "\")";
            statement.executeUpdate(sql);
            servers.add(s);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int addUser(User u) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            sql = "insert into users (name,password) values (";
            sql += "\"" + u.getName() + "\",";
            sql += "\"" + u.getPassword() + "\")";
            statement.executeUpdate(sql);
            ResultSet rs = statement.executeQuery("SELECT * from users order by id desc limit 0,1");
            int id = rs.getInt("id");
            u.setId(id);
            users.add(u);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return u.getId();
    }

    public void deleteProgram(Program p) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            sql = "delete from files where appid = " + p.getId();
            statement.execute(sql);
            statement.close();
            Statement stmt = connection.createStatement();
            sql = "delete from apps where id = " + p.getId();
            stmt.execute(sql);
            stmt.close();
            stmt = connection.createStatement();
            sql = "delete from prompts where appid = " + p.getId();
            stmt.execute(sql);
            stmt.close();
            programs.remove(p);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateAccount(Account a) {
        String sql = "update accounts set server = \"" + a.getServerName() + "\", ";
        sql += "name = \"" + a.getName() + "\", ";
        sql += "path = \"" + a.getPath() + "\" ";
        sql += "where id = " + a.getId();
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            int idx = accounts.indexOf(a);
            accounts.set(idx, a);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updatePreferences(String key, String value) throws SQLException {
        String sql = "replace into preferences (key, value) ";
        sql += "values (\"" + key + "\", \"" + value + "\")";
        Statement statement;
        statement = connection.createStatement();
        statement.executeUpdate(sql);
    }

    public void updateProfile(Profile p) throws SQLException {
        String sql = "update profiles set name = \"" + p.getProfileName() + "\",";
        sql += "server = \"" + p.getServerName() + "\",";
        sql += "user = " + p.getUserId() + ",";
        sql += "account = " + p.getAccountId() + " ";
        sql += "where id = " + p.getId();
        Statement statement;
        statement = connection.createStatement();
        statement.executeUpdate(sql);
        int idx = profiles.indexOf(p);
        profiles.set(idx, p);
    }

    public void updateProgram(Program p) {
        try {
            Statement statement = connection.createStatement();
            String sql;
            int sub = p.isSubroutine() ? 1 : 0;
            sql = "update apps set name = ";
            sql += '"' + p.getName() + "\", package = ";
            sql += '"' + p.getClassName() + "\", ";
            sql += "description = \"" + p.getDescription() + "\", ";
            sql += "subroutine = " + sub + " ";
            sql += "where id = " + p.getId();
            statement.executeUpdate(sql);
            statement.close();
            Statement stmt = connection.createStatement();
            sql = "delete from files where appid = " + p.getId();
            stmt.execute(sql);
            stmt.close();
            addFiles(p.getFileList());
            for (Program p1 : programs) {
                if (p1.getId() == p.getId()) {
                    int idx = programs.indexOf(p1);
                    programs.set(idx, p);
                    break;
                }
            }
            stmt = connection.createStatement();
            sql = "delete from prompts where appid = " + p.getId();
            stmt.execute(sql);
            stmt.close();
            addPrompts(p);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateUser(User u) {
        String sql = "update users set name = \"" + u.getName() + "\", ";
        sql += "password = \"" + u.getPassword() + "\" ";
        sql += "where id = " + u.getId();
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            int idx = users.indexOf(u);
            users.set(idx, u);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updateServer(Server s) {
        String sql = "update servers set host = \"" + s.getHost() + "\" ";
        sql += "where name = \"" + s.getName() + "\"";
        Statement statement;
        try {
            statement = connection.createStatement();
            statement.executeUpdate(sql);
            int idx = servers.indexOf(s);
            servers.set(idx, s);
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(Config.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public Point getWindowLocation() {
        return location;
    }

    public Dimension getWindowSize() {
        return size;
    }

    public void setWindowLocation(int x, int y) {
        this.location.x = x;
        this.location.y = y;
    }

    public void setWindowSize(int w, int h) {
        this.size.width = w;
        this.size.height = h;
    }

    /**
     * @return the accounts
     */
    public ObservableList<Account> getAccounts() {
        return accounts;
    }

    /**
     * @param accounts the accounts to set
     */
    public void setAccounts(ObservableList<Account> accounts) {
        this.accounts = accounts;
    }

    /**
     * @return the profiles
     */
    public ObservableList<Profile> getProfiles() {
        return profiles;
    }

    public ObservableList<Program> getPrograms() {
        return programs;
    }

    public Program getProgram(int id) {
        for (Program p1 : programs) {
            if (p1.getId() == id) {
                return p1;
            }
        }
        return null;
    }

    public ObservableMap<String, String> getPreferences() {
        return this.preferences;
    }

    /**
     * @param profiles the profiles to set
     */
    public void setProfiles(ObservableList<Profile> profiles) {
        this.profiles = profiles;
    }

    public Server getServer(String name) {
        for (Server s : servers) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    /**
     * @return the servers
     */
    public ObservableList<Server> getServers() {
        return servers;
    }

    /**
     * @param servers the servers to set
     */
    public void setServers(ObservableList<Server> servers) {
        this.servers = servers;
    }

    public Account getAccountByName(String name, String svr) {
        Account a;
        String sql = "select * from accounts ";
        sql += "where name = \"" + name + "\" ";
        sql += "and server = \"" + svr + "\"";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (!rs.next()) {
                return null;
            }
            a = new Account();
            a.setId(rs.getInt("id"));
            a.setName(rs.getString("name"));
            a.setPath(rs.getString("path"));
            return a;
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public User getUser(String n, String p) {
        User u;
        String sql = "select * from users where name = \"" + n + "\" ";
        sql += "and password = \"" + p + "\"";
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            u = new User();
            u.setId(rs.getInt("id"));
            u.setName(rs.getString("name"));
            u.setPassword(rs.getString("password"));
            return u;
        } catch (SQLException ex) {
            Logger.getLogger(Config.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * @return the users
     */
    public ObservableList<User> getUsers() {
        return users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(ObservableList<User> users) {
        this.users = users;
    }

}
