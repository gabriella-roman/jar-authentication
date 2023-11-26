package Componente;

public class Especificacao {
    private Integer fkComponente;
    private String tipo;
    private String valor;

    public Especificacao(Integer fkComponente, String tipo, String valor) {
        this.fkComponente = fkComponente;
        this.tipo = tipo;
        this.valor = valor;
    }

    public Integer getFkComponente() {
        return fkComponente;
    }

    public void setFkComponente(Integer fkComponente) {
        this.fkComponente = fkComponente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }


    @Override
    public String toString(){
        return """
             fkComponente=%d,
             tipo=%s,
             valor=%s
            """.formatted(
                this.fkComponente,
                this.tipo,
                this.valor
        );
    }
}
