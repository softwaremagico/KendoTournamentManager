package com.softwaremagico.kt.persistence.entities;

import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.security.AvailableRole;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "authenticated_users")
public class AuthenticatedUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "password")
    //@Convert(converter = StringCryptoConverter.class)
    private String password;

    @Column(name = "username")
    @Convert(converter = StringCryptoConverter.class)
    private String username;

    @Column(name = "full_name")
    @Convert(converter = StringCryptoConverter.class)
    private String fullName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "roles", joinColumns = @JoinColumn(name = "authenticated_user"))
    @Column(name = "roles")
    private Set<String> roles;

    private transient Set<SimpleGrantedAuthority> grantedAuthorities;

    public Integer getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null) {
            password = "";
        }
        //We use Bcrypt for Spring Web Security
        this.password = new BCryptPasswordEncoder().encode(password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }


    public String getMobilePhone() {
        return null;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (grantedAuthorities == null) {
            grantedAuthorities = new HashSet<>();
            if (roles != null) {
                roles.forEach(authority -> {
                    final AvailableRole availableRole = AvailableRole.get(authority);
                    if (availableRole != null) {
                        grantedAuthorities.add(new SimpleGrantedAuthority(availableRole.name()));
                    }
                });
            }
        }
        return grantedAuthorities;
    }
}
