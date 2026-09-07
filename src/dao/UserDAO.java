package dao;

import model.User;

public interface UserDAO {

    User authenticate(String email, String password);

    boolean register(User user);

    boolean emailExists(String email);
}
