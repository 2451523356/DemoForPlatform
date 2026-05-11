package com.kaoyan.platform.controller;

public class TestUserDir {
    public static void main(String[] args) {
        System.out.println("User dir: " + System.getProperty("user.dir"));
        System.out.println("Upload dir: " + System.getProperty("user.dir") + "/upload/avatars/");
    }
}