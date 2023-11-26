package Autenticação;

import Conexao.Conexao;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;


public class Usuario {
    private Integer idUsuario;
    private String nome;
    private String email;
    private String senha;

    Conexao con = new Conexao();
    JdbcTemplate jdbcTemplate = con.getConexaoDoBanco();

    public Boolean emailExiste(String email) {
        try {
            String emailDoBanco = jdbcTemplate.queryForObject("SELECT email FROM usuario WHERE email = ?", String.class, email);
            return email.equals(emailDoBanco);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public Boolean senhaCorreta(String email, String senha){
        try {
            String senhaCorreta = jdbcTemplate.queryForObject("SELECT senha FROM usuario WHERE email = ?", String.class, email);
            return senha.equals(senhaCorreta);
        } catch (Exception e) {
            return false;
        }
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }
}
