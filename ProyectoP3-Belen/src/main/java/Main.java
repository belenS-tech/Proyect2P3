import javax.swing.*;
import controller.LoginController;
import view.LoginView;
import service.AuthService;
import repository.UsuarioRepository;
import repository.UsuarioXmlRepository;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UsuarioRepository usuarioRepository = new UsuarioXmlRepository();
      AuthService authService = new AuthService(usuarioRepository);
            LoginView loginView = new LoginView();
            new LoginController(loginView, authService, usuarioRepository);
            loginView.setVisible(true);
        });
    }
}
