/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.model;

import java.util.Comparator;
import java.util.Random;

/**
 *
 * @author rlittle
 * create table profiles (id integer primary key autoincrement, name char(128), server char(16), account int, user int)
 */
public class Profile {
    
    private int id;
    private String profileName;
    private Server server;
    private Account account;
    private User user;
    private Random rand;
    
    public Profile() {
        id = -1;
        profileName = "";
        server = null;
        account = null;
        user = null;
        rand = new Random(256);
    }
    
    public Profile(int pid, Server s, Account a, User u) {
        id = pid;
        server = s;
        account = a;
        user = u;
        profileName = "";
    }
    
    /*
    Creates a new temporary profile 
    @param  serverName      The name to call the server you'll be using
    @param  acctName        The name of the account
    @param  host            The actual network host name such as localhost, host.domain.tld, etc.
    @param  path            The path for the UV account
    @param  userName        Your user name to login with
    @param  password        Your password
    */
    public static Profile createProfile(String serverName, String acctName, String host, String path, String userName, String password) {
        Profile profile = new Profile();
        profile.setServer(new Server(serverName,host));
        profile.setAccount(new Account(profile.rand.nextInt(),serverName,acctName,path));
        profile.setUser(new User(userName,password));
        return profile;
    }
    
    public int getId() {
        return id;
    }
    
    public Server getServer() {
        return server;
    }
    
    public String getServerName() {
        return server.getName();
    }
    
    public Account getAccount() {
        return account;
    }
    
    public int getAccountId() {
        return account.getId();
    }
    
    public String getAccountName() {
        return account.getName();
    }
    
    public User getUser() {
        return user;
    }
    
    public int getUserId() {
        return user.getId();
    }
    
    public String getUserName() {
        if(user==null) {
            return "";
        }
        return user.getName();
    }
    
    public String getUserPassword() {
        if(user==null) {
            return "";
        }
        return user.getPassword();
    }

    /**
     * @return the profileName
     */
    public String getProfileName() {
        return profileName;
    }
    
    @Override
    public String toString() {
        return profileName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (profileName.isEmpty() ? profileName.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object other) {
        if(other instanceof Profile) {
            Profile p = (Profile) other;
            return (p.getId()==this.getId());
        }
        return false;
    }
    
    public static Comparator<Profile> ProfileComparator = new Comparator<Profile> () {
        @Override
        public int compare(Profile o1, Profile o2) {
            return o1.profileName.compareTo(o2.profileName);
        }
    };

    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @param profileName the profileName to set
     */
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    
}
