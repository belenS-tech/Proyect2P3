package controller;

import service.AuthService;
import repository.UsuarioRepository;
import repository.UsuarioXmlRepository;
import view.LoginView;

import javax.swing.*;

/**
 * Clase demo para probar el módulo de login (Juan).
 * No es el punto de entrada oficial del proyecto
 */
public class MainLogin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UsuarioRepository usuarioRepository = new UsuarioXmlRepository();
            AuthService authService = new AuthService(usuarioRepository);

            LoginView loginView = new LoginView();
            LoginController loginController =
                    new LoginController(loginView, authService, usuarioRepository);

            loginView.setVisible(true);
        });
    }
}