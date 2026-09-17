package controller;

import view.CambiarClaveView;
import view.LoginView;
import service.AuthService;
import service.SessionManager;
import repository.UsuarioRepository;
import view.MainView;

/**
 * Controlador de la ventana principal (NO es un punto de entrada, no tiene main()).
 * Maneja las acciones de sesión: cambiar clave, cerrar sesión y salir.
 */
public class MainController {

    private final MainView vista;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    public MainController(MainView vista, AuthService authService,
                          UsuarioRepository usuarioRepository) {
        this.vista = vista;
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getItemCambiarClave().addActionListener(e -> abrirCambiarClave());
        vista.getItemCerrarSesion().addActionListener(e -> cerrarSesion());
        vista.getItemSalir().addActionListener(e -> salir());
    }

    private void abrirCambiarClave() {
        CambiarClaveView vistaClave = new CambiarClaveView();
        new CambiarClaveController(vistaClave, authService);
        vistaClave.setVisible(true);
    }

    private void cerrarSesion() {
        if (!vista.confirmar("¿Desea cerrar la sesión?")) {
            return;
        }

        SessionManager.getInstancia().cerrarSesion();
        vista.dispose();

        LoginView loginView = new LoginView();
        new LoginController(loginView, authService, usuarioRepository);
        loginView.setVisible(true);
    }

    private void salir() {
        if (vista.confirmar("¿Está seguro que desea salir del sistema?")) {
            System.exit(0);
        }
    }
}