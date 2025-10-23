package poly.edu.util;

import jakarta.servlet.http.HttpSession;
import poly.edu.model.Account;

/**
 * Utility class for managing user sessions
 */
public class SessionHelper {
    
    /**
     * Get current logged-in account from session
     * @param session HttpSession
     * @return Account or null if not logged in
     */
    public static Account getCurrentAccount(HttpSession session) {
        if (session == null) return null;
        return (Account) session.getAttribute("account");
    }
    
    /**
     * Get current account ID
     * @param session HttpSession
     * @return accountId or null if not logged in
     */
    public static Integer getCurrentAccountId(HttpSession session) {
        Account account = getCurrentAccount(session);
        return account != null ? account.getAccountId() : null;
    }
    
    /**
     * Check if user is logged in
     * @param session HttpSession
     * @return true if logged in
     */
    public static boolean isLoggedIn(HttpSession session) {
        return getCurrentAccount(session) != null;
    }
    
    /**
     * Check if current user is admin
     * @param session HttpSession
     * @return true if admin
     */
    public static boolean isAdmin(HttpSession session) {
        Account account = getCurrentAccount(session);
        if (account == null) return false;
        
        return account.getRoles().stream()
                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getRoleName()));
    }
    
    /**
     * Set account in session (for login)
     * @param session HttpSession
     * @param account Account to store
     */
    public static void setAccount(HttpSession session, Account account) {
        if (session != null && account != null) {
            session.setAttribute("account", account);
        }
    }
    
    /**
     * Remove account from session (for logout)
     * @param session HttpSession
     */
    public static void removeAccount(HttpSession session) {
        if (session != null) {
            session.removeAttribute("account");
        }
    }
    
    /**
     * Get username from session
     * @param session HttpSession
     * @return username or null
     */
    public static String getCurrentUsername(HttpSession session) {
        Account account = getCurrentAccount(session);
        return account != null ? account.getUsername() : null;
    }
    
    /**
     * Get full name from session
     * @param session HttpSession
     * @return full name or null
     */
    public static String getCurrentFullName(HttpSession session) {
        Account account = getCurrentAccount(session);
        return account != null ? account.getFullName() : null;
    }
}