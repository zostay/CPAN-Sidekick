package com.qubling.sidekick.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;

public class AuthenticationHelper {
    public static final String ACCOUNT_TYPE_GITHUB = "com.github";
    public static final String ACCOUNT_TYPE_TWITTER = "com.twitter.android.auth.login";
    public static final String ACCOUNT_TYPE_FACEBOOK = "com.facebook.auth.login";
    
    AccountManager accountManager;
    
    public AuthenticationHelper(Context context) {
        accountManager = AccountManager.get(context);
    }
    
    public Map<String, AuthenticatorDescription> getAuthenticatorMap() {
        AuthenticatorDescription[] authenticators = accountManager.getAuthenticatorTypes();
        
        HashMap<String, AuthenticatorDescription> result = new HashMap<String, AuthenticatorDescription>(authenticators.length);
        for (AuthenticatorDescription authenticator : authenticators) {
            result.put(authenticator.type, authenticator);
        }
        
        return result;
    }
    
    public List<Account> getAccounts() {
        Account[] githubAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE_GITHUB);
        Account[] twitterAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE_TWITTER);
        Account[] facebookAccounts = accountManager.getAccountsByType(ACCOUNT_TYPE_FACEBOOK);
        
        ArrayList<Account> result = new ArrayList<Account>(githubAccounts.length + twitterAccounts.length + facebookAccounts.length);
        Collections.addAll(result, githubAccounts);
        Collections.addAll(result, twitterAccounts);
        Collections.addAll(result, facebookAccounts);
        
        return result;
    }
}
