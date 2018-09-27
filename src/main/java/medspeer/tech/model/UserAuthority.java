package medspeer.tech.model;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

/**
 * Created by vishnu on 06/12/17.
 */
@Entity
@Table(name = "user_authority")
public class UserAuthority implements GrantedAuthority {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userRoleId;
//    @ManyToOne
//    @JoinColumn(name="id", nullable=false)
//    private int userId;
    private String userRole;

    public int getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(int userRoleId) {
        this.userRoleId = userRoleId;
    }

//    public int getUserId() {
//        return userId;
//    }

    public void setUserId(int userId) {
        userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    @Override
    public String getAuthority() {
        return this.userRole;
    }
}
