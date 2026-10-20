import javax.swing.*;
import controller.LoginController;
import view.LoginView;
import service.AuthService;
import repository.UsuarioRepository;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UsuarioRepository usuarioRepository = repository.Repositories.usuarios();
      AuthService authService = new AuthService(usuarioRepository);
            LoginView loginView = new LoginView();
            new LoginController(loginView, authService, usuarioRepository);
            loginView.setVisible(true);
        });
    }
}
