package model;

public class Funcionario {
    private Integer id;
    private String nome;
    private String cpf; // << ADICIONADO
    private String login;
    private String senhaHash;
    private String cargo; // "ADMIN", "GERENTE", "ATENDENTE", etc.

    public Funcionario() {}

    // Construtor atualizado com CPF
    public Funcionario(Integer id, String nome, String cpf, String login, String senhaHash, String cargo) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.login = login;
        this.senhaHash = senhaHash;
        this.cargo = cargo;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    // GETTER E SETTER DO CPF (O que estava faltando!)
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
}