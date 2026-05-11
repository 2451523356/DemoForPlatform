package com.kaoyan.platform.utils;

import com.kaoyan.platform.entity.User;

public class UserContext {
    
    private static final ThreadLocal<User> userHolder = new ThreadLocal<>();
    
    public static void setUser(User user) {
        userHolder.set(user);
    }
    
    public static User getUser() {
        return userHolder.get();
    }
    
    public static Long getUserId() {
        User user = userHolder.get();
        return user != null ? user.getId() : null;
    }
    
    public static boolean isAdmin() {
        User user = userHolder.get();
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    public static void remove() {
        userHolder.remove();
    }
}
