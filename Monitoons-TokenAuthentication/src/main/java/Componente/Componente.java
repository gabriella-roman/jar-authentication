package Componente;

import java.util.ArrayList;
import java.util.List;

public class Componente {
    private Integer idComponente;
    private String tipo;
    private String nome;
    private List<Especificacao> especificacoes;

    public Componente(Integer idComponente, String tipo, String nome, List<Especificacao> especificacoes) {
        this.idComponente = idComponente;
        this.tipo = tipo;
        this.nome = nome;
        this.especificacoes = especificacoes;
    }

    public Componente(Integer idComponente, String tipo, String nome) {
        this.idComponente = idComponente;
        this.tipo = tipo;
        this.nome = nome;
        this.especificacoes = new ArrayList<>();
    }

    public void addEspecificacao(Especificacao especificacao){
        this.especificacoes.add(especificacao);
    }

    public Integer getIdComponente() {
        return idComponente;
    }
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Especificacao> getEspecificacoes() {
        return especificacoes;
    }

    public void setEspecificacoes(List<Especificacao> especificacoes) {
        this.especificacoes = especificacoes;
    }

    @Override
    public String toString(){
        return """
                idComponente=%d,
                tipo=%s,
                nome=%s
                especificacoes=%s
                """.formatted(
                this.idComponente,
                this.tipo,
                this.nome,
                this.especificacoes
        );
    }

}
