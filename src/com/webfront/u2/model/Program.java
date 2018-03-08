/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webfront.u2.model;

import java.util.ArrayList;
import java.util.HashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author rlittle
 */
public class Program {
    private int id;
    private String name;
    private String className;
    private ArrayList<UvFile> fileList;
    private HashMap<Integer,Prompt> prompts;
    private String listName;
    private String[] selectCriteria;
    private String description;
    private boolean subroutine;
    
    private ObservableList<Prompt> promptList;
    
    public Program() {
        this.fileList = new ArrayList<>();
        this.listName = "";
        this.prompts = new HashMap<>();
        this.selectCriteria = new String[25];
        this.promptList = FXCollections.<Prompt>observableArrayList();
    }
    
    public Program(int id, String name, String pkg) {
        this();
        this.id=id;
        this.name=name;
        this.className=pkg;
        this.promptList.setAll(this.prompts.values());
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
        return className;
    }

    /**
     * @param className the className to set
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * @return the fileList
     */
    public ArrayList<UvFile> getFileList() {
        return fileList;
    }

    /**
     * @param fileList the fileList to set
     */
    public void setFileList(ArrayList<UvFile> fileList) {
        this.fileList = fileList;
    }

    /**
     * @return the listName
     */
    public String getListName() {
        return listName;
    }

    /**
     * @param listName the listName to set
     */
    public void setListName(String listName) {
        this.listName = listName;
    }

    /**
     * @return the selectCriteria
     */
    public String[] getSelectCriteria() {
        return selectCriteria;
    }

    /**
     * @param selectCriteria the selectCriteria to set
     */
    public void setSelectCriteria(String[] selectCriteria) {
        this.selectCriteria = selectCriteria;
    }

    /**
     * @return the promptList
     */
    public HashMap<Integer,Prompt> getPrompts() {
        return prompts;
    }

    /**
     * @param promptList the promptList to set
     */
    public void setPrompts(HashMap<Integer,Prompt> p) {
        this.prompts = p;
    }

    /**
     * @return the promptList
     */
    public ObservableList<Prompt> getPromptList() {
        return promptList;
    }

    /**
     * @param promptList the promptList to set
     */
    public void setPromptList(ObservableList<Prompt> promptList) {
        this.promptList = promptList;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the subroutine
     */
    public boolean isSubroutine() {
        return subroutine;
    }

    /**
     * @param subroutine the subroutine to set
     */
    public void setSubroutine(boolean subroutine) {
        this.subroutine = subroutine;
    }

}
