package util;

import model.Funcionario;

public class Sessao {
    private static Funcionario usuarioLogado;

    public static void setUsuarioLogado(Funcionario f) {
        usuarioLogado = f;
    }

    public static Funcionario getUsuarioLogado() {
        return usuarioLogado;
    }
}