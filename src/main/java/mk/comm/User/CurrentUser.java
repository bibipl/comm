package mk.comm.User;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CurrentUser extends User {
    private final mk.comm.User.User user;
    public CurrentUser(String username, String password, Collection<?
                extends GrantedAuthority> authorities, mk.comm.User.User user) {
        super(username, password, authorities); this.user = user;
    }
    public mk.comm.User.User getUser() {return user;}
}
