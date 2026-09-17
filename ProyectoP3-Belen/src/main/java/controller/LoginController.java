package controller;

import repository.CategoriaRepository;
import repository.RecursoXmlRepository;
import repository.ReservaXmlRepository;
import view.MainView;
import model.Usuario;
import service.CategoriaService;
import repository.CategoriaXmlRepository;
import repository.FuncionarioRepository;
import service.FuncionarioService;
import repository.FuncionarioXmlRepository;
import service.AuthService;
import repository.UsuarioRepository;
import view.CambiarClaveView;
import view.LoginView;

public class LoginController {

    private final LoginView vista;
    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    public LoginController(LoginView vista, AuthService authService,
                           UsuarioRepository usuarioRepository) {
        this.vista = vista;
        this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        registrarEventos();
    }

    private void registrarEventos() {
        vista.getBotonIngresar().addActionListener(e -> intentarIngresar());
        vista.getBotonCancelar().addActionListener(e -> System.exit(0));
        vista.getBotonCambiar().addActionListener(e -> abrirCambiarClave());
    }

    private void intentarIngresar() {
        try {
            String id = vista.getIdIngresado();
            String clave = vista.getClaveIngresada();

            Usuario usuario = authService.iniciarSesion(id, clave);

            vista.dispose();
            abrirVentanaPrincipal(usuario);

        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
        }
    }

    private void abrirCambiarClave() {
        String id = vista.getIdIngresado();

        try {
            authService.verificarExiste(id);
        } catch (IllegalArgumentException e) {
            vista.mostrarError(e.getMessage());
            return;
        }

        CambiarClaveView vistaClave = new CambiarClaveView(id);
        new CambiarClaveController(vistaClave, authService, id);
        vistaClave.setVisible(true);

        vista.limpiarCampos();
    }

    private void abrirVentanaPrincipal(Usuario usuario) {
        FuncionarioRepository funcionarioRepository = new FuncionarioXmlRepository();
        FuncionarioService funcionarioService =
                new FuncionarioService(funcionarioRepository, usuarioRepository);

        CategoriaRepository categoriaRepository = new CategoriaXmlRepository();
        CategoriaService categoriaService = new CategoriaService(
                categoriaRepository, new RecursoXmlRepository(), new ReservaXmlRepository());

        MainView mainView = new MainView(usuario, funcionarioService, categoriaService);
        new MainController(mainView, authService, usuarioRepository);
        mainView.setVisible(true);
    }
}